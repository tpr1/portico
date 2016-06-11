/*
 *   Copyright 2009 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL) 
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *   
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package org.portico.lrc.services.mom.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.LRCState;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.services.object.msg.DeleteObject;
import org.portico.lrc.services.object.msg.DiscoverObject;
import org.portico.lrc.services.object.msg.UpdateAttributes;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * The MOM manager takes care of all the MOM related tasks for the local federate. When the federate
 * joins a federation, it will create a Manager.Federation object to represent the federation and
 * when any other federate joins the federation, it will register a new object for that federate.
 * <p/>
 * <b>NOTE:</b> The methods in this class will only run if {@link PorticoConstants#isMomEnabled()}
 * returns <code>true</code> <i>at the time the LRC was initially created</i>. This generally means
 * that the MOM has to be enabled or disabled through the RID.
 */
public class MomManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean enabled;
	private LRCState lrcState;
	private HLAVersion hlaVersion;
	private Logger logger;

	// The Serializers. These classes extract the handles for the MOM bits and pieces
	// and then take care of servicing update requests.
	private MomFederation momFederation;
	private MomFederate momFederate;
	
	// handles and instances of MOM objects created
	private OCInstance momFederationObject;               // HLAfederation object
	private Map<Federate,OCInstance> momFederateObjects;  // HLAfederate objects
	
	// this flag is used to stop discovery notifications being sent during a federation restore
	// we re-populate the momFederation from the lrcState and use the federateJoinedFederation
	// to create the MomFedeate objects, but we don't want to send discovery notices.
	private boolean isRestore;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomManager( LRCState lrcState, HLAVersion hlaVersion, Logger logger )
	{
		this.enabled = PorticoConstants.isMomEnabled();
		this.lrcState = lrcState;
		this.hlaVersion = hlaVersion;
		this.logger = logger;
		this.isRestore = false;

		this.reinitialize();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	private void reinitialize()
	{
		this.momFederation = new MomFederation( hlaVersion );
		this.momFederate = new MomFederate( hlaVersion );
		
		this.momFederationObject = null;
		this.momFederateObjects = new HashMap<>();
	}
	
	public final int getFederationClassHandle()
	{
		return momFederation.getClassHandle();
	}
	
	public final int getFederateClassHandle()
	{
		return momFederate.getClassHandle();
	}
	
	public final int getManagerClassHandle()
	{
		return momFederation.getManagerClassHandle();
	}

	//////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Lifecycle Handlers ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is called by the LRC when the <b>local</b> federate joins the federation. It is
	 * used by the MomManager to perform any setup logic that needs to take place now that MOM
	 * information needs to begin flowing.
	 * <p/>
	 * This method won't queue a registration for the MOM federtion object as the local federate
	 * will still just be joining and will not have had a chance to register any subscriptions yet.
	 * Thus, this method will just put the created {@link OCInstance} directly into the repository
	 * as an undiscovered object.
	 */
	public void connectedToFederation()
	{
		if( enabled == false )
			return;
		
		// Initialize our handle collecting methods
		try
		{
			this.momFederation.initialize( lrcState.getFOM() );
			this.momFederate.initialize( lrcState.getFOM() );
		}
		catch( Exception e )
		{
			logger.error( "Could not initialize the MOM: "+e.getMessage(), e );
		}
		
		// Create an object instance for the federation and store it as an undiscovered object
		momFederationObject = getFederationClass().newInstance( 0 );
		momFederationObject.setHandle( 0 ); // handle for MOM federation object, same as RTI fed handle
		momFederationObject.setName( "MOM.Federation("+lrcState.getFederationName()+")" );
		lrcState.getRepository().addUndiscoveredInstance( momFederationObject );
		
		if( logger.isTraceEnabled() )
			logger.trace( "Created Mom.Federation object, added to Repository (undiscovered)" );
	}

	/**
	 * This method is called by the LRC when the <b>local</b> federate resigns from the federation.
	 * It is used by the MomManager to complete any necessary cleanup.
	 */
	public void localFederateResignedFederation()
	{
		if( enabled == false )
			return;
		
		// reinitialize
		this.reinitialize();
	}
	
	/**
	 * This method should be called when a federate joins the federation. It will create the local
	 * {@link OCInstance} to represent that federate in the MOM and then will queue up a discovery
	 * notification for the federate to process. As this could occur at any time, we have to queue
	 * a notification because the federate might be subscribed to the appropriate MOM class by now
	 */
	public void federateJoinedFederation( Federate federate )
	{
		if( enabled == false )
			return;
		
		// create the object for the Federate and queue up a discovery
		OCInstance ocInstance = getFederateClass().newInstance( 0 );
		ocInstance.setHandle( lrcState.getMomFederateObjectHandle(federate.getFederateHandle()) );
		ocInstance.setName( "MOM.Federate("+federate.getFederateName()+")" );
		this.momFederateObjects.put( federate, ocInstance );

		// queue up the discovery
		// skip if this is a federation restore because discoveries will have already been sent
		if( this.isRestore )
			return;

		DiscoverObject discover = new DiscoverObject( ocInstance );
		discover.setSourceFederate( PorticoConstants.RTI_HANDLE );
		lrcState.getQueue().offer( discover );
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "Created Mom.Federate("+federate.getFederateName()+
			              "), queued discovery notification" );
		}
	}

	/**
	 * This method should be called when a federate resigns from the federation. It will cause a
	 * delete object to be created locally and queued locally for the MOM object representing the
	 * federate. There is no need to broadcast this out, as each federate will do this itself when
	 * it hears about the resignation.
	 */
	public void federateResignedFederation( Federate federate )
	{
		if( enabled == false )
			return;
		
		// queue up the delete notice in case we were listening
		OCInstance ocInstance = momFederateObjects.remove( federate );
		if( ocInstance != null )
		{
			DeleteObject delete = new DeleteObject( ocInstance.getHandle(), new byte[0] );
			delete.setSourceFederate( PorticoConstants.RTI_HANDLE );
			lrcState.getQueue().offer( delete );
		}
		else
		{
			logger.warn( "Federate resigned, but no MOM object for it: "+federate.getFederateName() );
		}
	}

	private OCMetadata getFederationClass()
	{
		switch( hlaVersion )
		{
			case HLA13:
				return lrcState.getFOM().getObjectClass( "Manager.Federation" );
			case IEEE1516:
				return lrcState.getFOM().getObjectClass( "HLAmanager.HLAfederation" );
			case IEEE1516e:
				return lrcState.getFOM().getObjectClass( "HLAmanager.HLAfederation" );
			default:
				return null;
		}
	}
	
	private OCMetadata getFederateClass()
	{
		switch( hlaVersion )
		{
			case HLA13:
				return lrcState.getFOM().getObjectClass( "Manager.Federate" );
			case IEEE1516:
				return lrcState.getFOM().getObjectClass( "HLAmanager.HLAfederate" );
			case IEEE1516e:
				return lrcState.getFOM().getObjectClass( "HLAmanager.HLAfederate" );
			default:
				System.out.println( "CRAP" );
				return null;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Update Helper Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Generates an {@link UpdateAttributes} message for the MOM object representing the specific
	 * federate. It only includes the attributes provided in the given set.
	 */
	public UpdateAttributes updateFederateMomObject( int federateHandle, Set<Integer> attributes )
		throws JAttributeNotDefined, JRTIinternalError
	{
		// find the federate info in the LRC
		Federate federate = lrcState.getKnownFederate( federateHandle );
		if( federate == null )
			throw new JRTIinternalError( "Rquested MOM update for unknown federate: handle="+federateHandle );
		
		return momFederate.generateUpdate( federate, attributes );		
	}
	
	/**
	 * Generates an {@link UpdateAttributes} message for the MOM object representing the federation.
	 * It only includes the attributes provided in the given set.
	 */
	public UpdateAttributes updateFederationMomObject( Set<Integer> attributes )
		throws JAttributeNotDefined
	{
		return momFederation.generateUpdate( lrcState.getFederation(), attributes );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		// do nothing
	}

	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		// clear the local status and for each federate in the federation (which should have
		// already been save/restored), create a new MomFederate
		try
		{
			this.isRestore = true;
    		reinitialize();
    		for( Federate federate : lrcState.getFederation() )
    			federateJoinedFederation( federate );
		}
		finally
		{
			this.isRestore = false;
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
