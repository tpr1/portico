/*
 *   Copyright 2016 The Portico Project
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.management.Federate;
import org.portico.lrc.management.Federation;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.object.msg.UpdateAttributes;

public class MomFederation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	private enum Attribute
	{
		FederationName("FederationName","HLAfederationName"),
		FederatesInFederation("FederatesInFederation","HLAfederatesInFederation"),
		RtiVersion("RTIversion","HLARTIversion"),
		MimDesignator(null,"HLAMIMdesignator"),                        // 1516e
		FomModuleDesignatorList(null,"HLAFOMmoduleDesignatorList"),    // 1516e
		CurrentFdd(null,"HLAcurrentFDD"),                              // 1516e
		FedID("FEDid",null),
		TimeImplementationName(null,"HLAtimeImplementationName"),      // 1516e
		LastSaveName("LastSaveName","HLAlastSaveName"),
		LastSaveTime("LastSaveTime","HLAlastSaveTime"),
		NextSaveName("NextSaveName","HLAnextSaveName"),
		NextSaveTime("NextSaveTime","HLAnextSaveTime"),
		AutoProvide(null,"HLAautoProvide");                            // 1516e

		private final String hla13;
		private final String ieee1516e; 
		private Attribute( String hla13, String ieee1516e )
		{
			this.hla13 = hla13;
			this.ieee1516e = ieee1516e;
		}
	}
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private int managerClassHandle;
	private Map<Attribute,Integer> handleMap;
	private HLAVersion targetVersion; // HLA version of the local requesting federate
	
	private int instanceHandle;                    // handle of the instance of HLAfederation
	private Map<Federate,Integer> federateHandles; // handles for all known HLAfederate instances

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederation( HLAVersion federateVersion )
	{
		this.handleMap = new HashMap<>();
		this.targetVersion = federateVersion;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Fetch all the handles for this type from the MOM information in the given object model.
	 */
	public void initialize( ObjectModel objectModel ) throws JRTIinternalError
	{
		this.managerClassHandle = -1;
		
		// find the manager class
		OCMetadata managerClass = objectModel.getObjectClass( "Manager" );
		if( managerClass == null )
			managerClass = objectModel.getObjectClass( "HLAmanager" );
		
		this.managerClassHandle = managerClass.getHandle();
		
		// find the HLAfederate class
		OCMetadata federationClass = objectModel.getObjectClass( "Manager.Federation" );
		if( federationClass == null )
			federationClass = objectModel.getObjectClass( "HLAmanager.HLAfederation" );
		
		if( federationClass == null )
			throw new JRTIinternalError( "Could not bootstrap MOM: Missing HLAfederation class" );
		
		// pull the class and attribute handles out
		this.classHandle = federationClass.getHandle();
		for( Attribute attribute : Attribute.values() )
		{
			ACMetadata temp = federationClass.getDeclaredAttribute( attribute.hla13 );
			if( temp == null )
				temp = federationClass.getDeclaredAttribute( attribute.ieee1516e );
			
			// if we found this attribute in the FOM, store the handle
			if( temp != null )
				this.handleMap.put( attribute, temp.getHandle() );
		}
	}

	public UpdateAttributes generateUpdate( Federation federation, Set<Integer> requested )
		throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> map = new HashMap<Integer,byte[]>();

		for( Attribute attribute : handleMap.keySet() )
		{
			int handle = handleMap.get( attribute );
			if( requested.contains(handle) == false )
				continue;
		
			switch( attribute )
			{
				case FederationName:
					map.put( handle, getFederationName(federation) );
					break;
				case FederatesInFederation:
					map.put( handle, getFederatesInFederation(federation) );
					break;
				case RtiVersion:
					map.put( handle, getRtiVersion() );
					break;
				case MimDesignator:
					map.put( handle, getMimDesignator() );
					break;
				case FomModuleDesignatorList:
					map.put( handle, getFomModuleDesignatorList() );
					break;
				case CurrentFdd:
					map.put( handle, getCurrentFdd() );
					break;
				case FedID:
					map.put( handle, getFedID() );
					break;
				case TimeImplementationName:
					map.put( handle, getTimeImplementationName() );
					break;
				case LastSaveName:
					map.put( handle, getLastSaveName() );
					break;
				case LastSaveTime:
					map.put( handle, getLastSaveTime() );
					break;
				case NextSaveName:
					map.put( handle, getNextSaveName() );
					break;
				case NextSaveTime:
					map.put( handle, getNextSaveTime() );
					break;
				case AutoProvide:
					map.put( handle, notYetSupported("AutoProvide") );
					break;
				default:
					break;
			}
		}
		
		UpdateAttributes update = new UpdateAttributes( classHandle, new byte[0], map );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}

	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public int getManagerClassHandle()
	{
		return this.managerClassHandle;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// Attribute Encoding Methods    ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	private byte[] getFederationName( Federation federation )
	{
		return encodeString( federation.getFederationName() );
	}
	
	private byte[] getFederatesInFederation( Federation federation )
	{
		//FIXME yeah, clearly in need of fixing :P
		return notYetSupported( "FederatesInFederation" );
	}
	
	private byte[] getRtiVersion()
	{
		return encodeString( PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getMimDesignator()
	{
		return notYetSupported( "MimDesignator" );
	}

	private byte[] getFomModuleDesignatorList()
	{
		return notYetSupported( "HLAversion" );
	}

	private byte[] getCurrentFdd()
	{
		return notYetSupported( "CurrentFDD" );
	}

	private byte[] getTimeImplementationName()
	{
		return notYetSupported( "TimeImplementation" );
	}

	private byte[] getFedID()
	{
		//FIXME Obviously
		return notYetSupported( "FedID" );
	}
	
	private byte[] getLastSaveName()
	{
		return notYetSupported( "LastSaveName" );
	}
	
	private byte[] getLastSaveTime()
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "LastSaveTime" );
	}
	
	private byte[] getNextSaveName()
	{
		return notYetSupported( "NextSaveName" );
	}
	
	private byte[] getNextSaveTime()
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "NextSaveTime" );
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Update Generating Methods ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////

	private byte[] notYetSupported( String property )
	{
		//momLogger.trace( "Requeted MOM property that isn't supported yet: Federation." + property );
		return encodeString( "property ["+property+"] not yet supported" );
	}
	
	private byte[] encodeString( String string )
	{
		switch( targetVersion )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( string );
			case IEEE1516e:
				return new HLA1516eUnicodeString(string).toByteArray();
			case IEEE1516:
				return new HLA1516eUnicodeString(string).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+targetVersion );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
