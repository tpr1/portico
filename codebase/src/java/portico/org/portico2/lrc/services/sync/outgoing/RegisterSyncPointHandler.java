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
package org.portico2.lrc.services.sync.outgoing;

import java.util.HashSet;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.sync.msg.SyncPointRegister;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.shared.messaging.MessageContext;

public class RegisterSyncPointHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		SyncPointRegister announcement = context.getRequest( SyncPointRegister.class, this );
		String label = announcement.getLabel();
		HashSet<Integer> syncset = announcement.getFederateSet();

		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		logger.debug( "ATTEMPT Register sync point ["+label+"] by ["+moniker()+"]" );
		
		// check for a null label
		if( label == null || label.trim().equals("") )
			throw new JRTIinternalError( "Can't register sync point with null or empty label" );
		
		// if the sync set exists, but is empty, set it to null so as to indicate that this point
		// is a federation-wide point (empty set == every federate, every federate denoted by null)
		if( syncset != null && syncset.isEmpty() )
		{
			syncset = null;
			announcement.makeFederationWide();
		}

		/////////////////////////////////
		// Send the request to the RTI //
		/////////////////////////////////
		connection.sendControlRequest( context );
		
		if( context.isErrorResponse() )
		{
			// FIXME Queue a failure callback
			logger.fatal( "SYNC REGISTRATION WAS A FAILURE: "+context.getErrorResponse().getResult() );
		}
		else
		{
			// FIXME Queue a success callback
			logger.fatal( "SYNC REGISTRATION WAS A SUCCESS" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
