/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.rti.federation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JFederateNameAlreadyInUse;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.rti.RTI;
import org.portico2.shared.PorticoConstants;
import org.portico2.shared.messaging.MessageSink;

public class Federation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final AtomicInteger HANDLE_COUNTER = new AtomicInteger( 0 );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLAVersion hlaVersion;
	private RTI rti;
	private Logger logger;

	private String name;
	private ObjectModel fom;	
	private int federationHandle;
	private Map<String,Federate> federates;
	
	// Message Processing
	private Queue<PorticoMessage> controlQueue;
	private MessageSink incomingSink;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federation( String name, ObjectModel fom, HLAVersion hlaVersion, Logger logger )
	{
		this.name = name;
		this.fom = fom;
		this.hlaVersion = hlaVersion;
		this.logger = logger;
		this.federationHandle = HANDLE_COUNTER.incrementAndGet();
		this.federates = new HashMap<>();
		
		// Message Processing
		this.controlQueue = new LinkedList<>();
		this.incomingSink = new MessageSink( name+"-incoming", logger );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public HLAVersion getHlaVersion()
	{
		return this.hlaVersion;
	}
	
	public Logger getLogger()
	{
		return this.logger;
	}

	public void addFederate( Federate federate )
	{
		if( federates.containsKey(federate.getFederateName()) )
			throw new JFederateNameAlreadyInUse( federate.getFederateName() );
		
		this.federates.put( federate.getFederateName(), federate );
	}
	
	public void removeFederate( Federate federate )
	{
		this.federates.remove( federate.getFederateName() );
	}
	
	public Federate getFederate( String name )
	{
		for( Federate federate: federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase(name) )
				return federate;
		}
		
		return null;
	}
	
	public Federate getFederate( int federateHandle )
	{
		for( Federate federate : federates.values() )
		{
			if( federate.getFederateHandle() == federateHandle )
				return federate;
		}
		
		return null;
	}
	
	public int getFederateHandle( String name )
	{
		int value = PorticoConstants.NULL_HANDLE;
		for( Federate federate : federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase("") )
				return federate.getFederateHandle();
		}
		
		return value;		
	}
	
	public boolean containsFederate( int federateHandle )
	{
		for( Federate f : federates.values() )
		{
			if( f.getFederateHandle() == federateHandle )
				return true;
		}
		
		return false;
	}
	
	public boolean containsFederate( String name )
	{
		for( Federate federate : federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase(name) )
				return true;
		}
		
		return false;
	}
	
	public boolean containsFederates()
	{
		return federates.isEmpty() == false;
	}
	
	public String getFederationName()
	{
		return this.name;
	}

	public int getFederationHandle()
	{
		return this.federationHandle;
	}
	
	public MessageSink getIncomingSink()
	{
		return this.incomingSink;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
