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
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.object.msg.UpdateAttributes;

/**
 * This class caches the handles for the MOM class <code>HLAmanager.HLAfederate</code>.
 * <br/>
 * It's primary job is to take a {@link Federate} instance and generate a Map<Integer,byte[]>
 * that can be used in an attribute reflection containing MOM information about the federate.
 */
public class MomFederate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	private enum Attribute
	{
		FederateHandle("FederateHandle","HLAfederateHandle"),
		FederateName(null,"HLAfederateName"),                        // 1516e
		FederateType("FederateType","HLAfederateType"),
		FederateHost("FederateHost","HLAfederateHost"),
		FomModuleDesignatorList(null,"HLAFOMmoduleDesignatorList"),  // 1516e
		RtiVersion("RTIversion",null),
		FedID("FEDid",null),
		TimeConstrained("TimeConstrained","HLAtimeConstrained"),
		TimeRegulating("TimeRegulating","HLAtimeRegulating"),
		AsynchronousDelivery("AsynchronousDelivery","HLAasynchronousDelivery"),
		FederateState("FederateState","HLAfederateState"),
		TimeManagerState("TimeManagerState","HLAtimeManagerState"),
		LogicalTime("FederateTime","HLAlogicalTime"),
		Lookahead("Lookahead","HLAlookahead"),
		LBTS("LBTS",null), // synonym for LITS
		GALT(null,"HLAGALT"),
		LITS("NextMinEventTime","HLALITS"), // NextMinEventTime in 1.3,
		ROlength("ROlength","HLAROlength"),
		TSOlength("TSOlength","HLATSOlength"),
		ReflectionsReceived("ReflectionsReceived","HLAreflectionsReceived"),
		UpdatesSent("UpdatesSent","HLAupdatesSent"),
		InteractionsReceived("InteractionsReceived","HLAinteractionsReceived"),
		InteractionsSent("InteractionsSent","HLAinteractionsSent"),
		ObjectInstancesThatCanBeDeleted("ObjectsOwned","HLAobjectInstancesThatCanBeDeleted"), // ObjectsOwned in 1.3
		ObjectInstancesUpdated("ObjectsUpdated","HLAobjectInstancesUpdated"),                 // ObjectsUpdated in 1.3
		ObjectInstancesReflected("ObjectsReflected","HLAobjectInstancesReflected"),           // ObjectsReflected in 1.3
		ObjectInstancesDeleted(null,"HLAobjectInstancesDeleted"),
		ObjectInstancesRemoved(null,"HLAobjectInstancesRemoved"),
		ObjectInstancesRegistered(null,"HLAobjectInstancesRegistered"),
		ObjectInstancesDiscovered(null,"HLAobjectInstancesDiscovered"),
		TimeGrantedTime(null,"HLAtimeGrantedTime"),
		TimeAdvancingTime(null,"HLAtimeAdvancingTime"),
		ConveyProducingFederate(null,"HLAconveyRegionDesignatorSets"),        // 1516e 
		ConveyRegionDesignatorSets(null,"HLAconveyProducingFederate");        // 1516e
		
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
	private Map<Attribute,Integer> handleMap;
	private HLAVersion targetVersion; // HLA version of the local requesting federate

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederate( HLAVersion federateVersion )
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
		// find the HLAfederate class
		OCMetadata federateClass = objectModel.getObjectClass( "Manager.Federate" );
		if( federateClass == null )
			federateClass = objectModel.getObjectClass( "HLAmanager.HLAfederate" );
		
		if( federateClass == null )
			throw new JRTIinternalError( "Could not bootstrap MOM: Missing HLAfederate class" );
		
		// pull the class and attribute handles out
		this.classHandle = federateClass.getHandle();
		for( Attribute attribute : Attribute.values() )
		{
			ACMetadata temp = federateClass.getDeclaredAttribute( attribute.hla13 );
			if( temp == null )
				temp = federateClass.getDeclaredAttribute( attribute.ieee1516e );
			
			// if we found this attribute in the FOM, store the handle
			if( temp != null )
				this.handleMap.put( attribute, temp.getHandle() );
		}
	}

	/**
	 * Generate an udpate for the requested handles using the given federate as the source.
	 */
	public UpdateAttributes generateUpdate( Federate federate, Set<Integer> requested )
	{
		HashMap<Integer,byte[]> map = new HashMap<>();
		
		for( Attribute attribute : handleMap.keySet() )
		{
			int handle = handleMap.get( attribute );
			if( requested.contains(handle) == false )
				continue;
			
			switch( attribute )
			{
				case FederateName:
					map.put( handle, getFederateName(federate) );
					break;
				case FederateHandle:
					map.put( handle, getFederateHandle(federate) );
					break;
				case FederateType:
					map.put( handle, getFederateType(federate) );
					break;
				case FederateHost:
					map.put( handle, getFederateHost(federate) );
					break;
				case FomModuleDesignatorList:
					map.put( handle, getFomModuleDesignatorList(federate) );
					break;
				case RtiVersion:
					map.put( handle, getRTIversion(federate) );
					break;
				case FedID:
					map.put( handle, getFEDid(federate) );
					break;
				case TimeConstrained:
					map.put( handle, getTimeConstrained(federate) );
					break;
				case TimeRegulating:
					map.put( handle, getTimeRegulating(federate) );
					break;
				case AsynchronousDelivery:
					map.put( handle, getAsynchronousDelivery(federate) );
					break;
				case FederateState:
					map.put( handle, getFederateState(federate) );
					break;
				case TimeManagerState:
					map.put( handle, getTimeManagerState(federate) );
					break;
				case LogicalTime:
					map.put( handle, getFederateTime(federate) );
					break;
				case Lookahead:
					map.put( handle, getLookahead(federate) );
					break;
				case LBTS:
					map.put( handle, getLBTS(federate) );
					break;
				case GALT:
					map.put( handle, getGALT(federate) );
					break;
				case LITS:
					map.put( handle, getLITS(federate) );
					break;
				case ROlength:
					map.put( handle, getROlength(federate) );
					break;
				case TSOlength:
					map.put( handle, getTSOlength(federate) );
					break;
				case ReflectionsReceived:
					map.put( handle, getReflectionsReceived(federate) );
					break;
				case UpdatesSent:
					map.put( handle, getUpdatesSent(federate) );
					break;
				case InteractionsReceived:
					map.put( handle, getInteractionsReceived(federate) );
					break;
				case InteractionsSent:
					map.put( handle, getInteractionsSent(federate) );
					break;
				case ObjectInstancesThatCanBeDeleted:
					map.put( handle, getObjectsOwned(federate) );
					break;
				case ObjectInstancesUpdated:
					map.put( handle, getObjectsUpdated(federate) );
					break;
				case ObjectInstancesReflected:
					map.put( handle, getObjectsReflected(federate) );
					break;
				case ObjectInstancesDeleted:
					map.put( handle, getObjectInstancesDeleted(federate) );
					break;
				case ObjectInstancesRemoved:
					map.put( handle, getObjectInstancesRemoved(federate) );
					break;
				case ObjectInstancesRegistered:
					map.put( handle, getObjectInstancesRegistered(federate) );
					break;
				case ObjectInstancesDiscovered:
					map.put( handle, getObjectInstancesDiscovered(federate) );
					break;
				case TimeGrantedTime:
					map.put( handle, getTimeGrantedTime(federate) );
					break;
				case TimeAdvancingTime:
					map.put( handle, getTimeAdvancingTime(federate) );
					break;
				case ConveyRegionDesignatorSets:
					map.put( handle, getConveyRegionDesignatorSets(federate) );
					break;
				case ConveyProducingFederate:
					map.put( handle, getConveyProducingFederate(federate) );
					break;
				default:
					break; // ignore
			} 
		}

		UpdateAttributes update = new UpdateAttributes( classHandle, new byte[0], map );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}

	protected int getClassHandle()
	{
		return this.classHandle;
	}

	//////////////////////////////////////////////////////////////////////////////
	/// Attribute Encoding Methods    ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	private byte[] getFederateHandle( Federate federate )
	{
		return encodeHandle( federate.getFederateHandle() );
	}

	private byte[] getFederateType( Federate federate )
	{
		return encodeString( federate.getFederateName() ); // wrong in 1516e
	}
	
	private byte[] getFederateName( Federate federate )
	{
		return encodeString( federate.getFederateName() );
	}

	private byte[] getFederateHost( Federate federate )
	{
		return notYetSupported( "FederateHost" );
	}
	
	private byte[] getFomModuleDesignatorList( Federate federate )
	{
		return notYetSupported( "FomModuleDesignatorList" );
	}

	private byte[] getRTIversion( Federate federate )
	{
		return encodeString( PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getFEDid( Federate federate )
	{
		return notYetSupported( "FDDID" );
	}

	private byte[] getTimeConstrained( Federate federate )
	{
		return encodeBoolean( federate.getTimeStatus().isConstrained() );
	}

	private byte[] getTimeRegulating( Federate federate )
	{
		return encodeBoolean( federate.getTimeStatus().isRegulating() );
	}

	private byte[] getAsynchronousDelivery( Federate federate )
	{
		return encodeBoolean( federate.getTimeStatus().isAsynchronous() );
	}

	private byte[] getFederateState( Federate federate )
	{
		return notYetSupported( "FederateState" );
	}

	private byte[] getTimeManagerState( Federate federate )
	{
		return notYetSupported( "TimeManagerState" );
	}

	private byte[] getFederateTime( Federate federate )
	{
		return encodeTime( federate.getTimeStatus().getCurrentTime() );
	}

	private byte[] getLookahead( Federate federate )
	{
		return encodeTime( federate.getTimeStatus().getLookahead() );
	}

	private byte[] getLBTS( Federate federate )
	{
		return encodeTime( federate.getTimeStatus().getLbts() );
	}

	private byte[] getMinNextEventTime( Federate federate )
	{
		return getLBTS( federate );
	}
	
	private byte[] getGALT( Federate federate )
	{
		return notYetSupported("GALT");
	}

	private byte[] getLITS( Federate federate )
	{
		return notYetSupported("LITS");
	}

	private byte[] getROlength( Federate federate )
	{
		return notYetSupported("ROlength");
	}

	private byte[] getTSOlength( Federate federate )
	{
		return notYetSupported("TSOlength");
	}

	private byte[] getReflectionsReceived( Federate federate )
	{
		return notYetSupported("ReflectionsReceived");
	}

	private byte[] getUpdatesSent( Federate federate )
	{
		return notYetSupported("UpdatesSent");
	}

	private byte[] getInteractionsReceived( Federate federate )
	{
		return notYetSupported("InteractionsReceived");
	}

	private byte[] getInteractionsSent( Federate federate )
	{
		return notYetSupported("InteractionsSent");
	}

	private byte[] getObjectsOwned( Federate federate )
	{
		return notYetSupported("ObjectOwned");
	}

	private byte[] getObjectsUpdated( Federate federate )
	{
		return notYetSupported("ObjectsUpdated");
	}

	private byte[] getObjectsReflected( Federate federate )
	{
		return notYetSupported("ObjectsReflected");
	}

	private byte[] getObjectInstancesDeleted( Federate federate )
	{
		return notYetSupported( "ObjectInstancedDeleted" );
	}

	private byte[] getObjectInstancesRemoved( Federate federate )
	{
		return notYetSupported( "ObjectInstancedRemoved" );
	}

	private byte[] getObjectInstancesRegistered( Federate federate )
	{
		return notYetSupported( "ObjectInstancedRegistered" );
	}

	private byte[] getObjectInstancesDiscovered( Federate federate )
	{
		return notYetSupported( "ObjectInstancedDiscovered" );
	}

	private byte[] getTimeGrantedTime( Federate federate )
	{
		return encodeTime( federate.getTimeStatus().getCurrentTime() );
	}

	private byte[] getTimeAdvancingTime( Federate federate )
	{
		return encodeTime( federate.getTimeStatus().getRequestedTime() );
	}
	
	private byte[] getConveyRegionDesignatorSets( Federate federate )
	{
		return encodeBoolean( true );
	}
	
	private byte[] getConveyProducingFederate( Federate federate )
	{
		return encodeBoolean( true );
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// MOM Encoding Methods    //////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	private byte[] notYetSupported( String property )
	{
		//momLogger.trace( "Requeted MOM property that isn't supported yet: Federate." + property );
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
	
	private byte[] encodeHandle( int handle )
	{
		switch( targetVersion )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+handle );
			case IEEE1516e:
				return new HLA1516eHandle(handle).getBytes();
			case IEEE1516:
				return new HLA1516eHandle(handle).getBytes();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+targetVersion );
		}
	}

	private byte[] encodeBoolean( boolean value )
	{
		switch( targetVersion )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+value );
			case IEEE1516e:
				return new HLA1516eBoolean(value).toByteArray();
			case IEEE1516:
				return new HLA1516eBoolean(value).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+targetVersion );
		}
	}
	
	private byte[] encodeTime( double time )
	{
		switch( targetVersion )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+time );
			case IEEE1516e:
				return new org.portico.impl.hla1516e.types.time.DoubleTime(time).toByteArray();
			case IEEE1516:
				return new org.portico.impl.hla1516.types.DoubleTime(time).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+targetVersion );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
