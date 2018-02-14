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
package org.portico2.lrc;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.lrc.services.federation.outgoing.CreateFederationHandler;
import org.portico2.lrc.services.federation.outgoing.DestroyFederationHandler;
import org.portico2.lrc.services.federation.outgoing.JoinFederationHandler;
import org.portico2.lrc.services.federation.outgoing.ListFederationsHandler;
import org.portico2.lrc.services.federation.outgoing.PingHandler;
import org.portico2.lrc.services.federation.outgoing.ResignFederationHandler;
import org.portico2.lrc.services.sync.outgoing.RegisterSyncPointHandler;
import org.portico2.shared.messaging.IMessageHandler;
import org.portico2.shared.messaging.MessageType;

/**
 * The {@link HandlerRegistry} is the central point through which all handlers for the various
 * interface versions (HLA 1.3, 1516 or 1516e) are loaded.
 */
public class LRCHandlerRegistry
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
	private static void loadHla13( LRC lrc )
	{
		
	}
	
	private static void loadIeee1516( LRC lrc )
	{
		
	}
	
	private static void loadIeee1516e( LRC lrc )
	{
		// object map for configuration of handlers - stuffed with a reference to the LRC
		Map<String,Object> settings = new HashMap<>();
		settings.put( IMessageHandler.KEY_LRC, lrc );
		
		///////////////////////////////////////////////////////////////////////////
		///  Outgoing Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		HashMap<MessageType,LRCMessageHandler> outgoing = new HashMap<>();
//		outgoing.put( MessageType.Connect, null );
//		outgoing.put( MessageType.Disconnect, null );
		outgoing.put( MessageType.CreateFederation,  new CreateFederationHandler() );
		outgoing.put( MessageType.JoinFederation,    new JoinFederationHandler() );
		outgoing.put( MessageType.ResignFederation,  new ResignFederationHandler() );
		outgoing.put( MessageType.DestroyFederation, new DestroyFederationHandler() );
		outgoing.put( MessageType.ListFederations,   new ListFederationsHandler() );
		outgoing.put( MessageType.Ping,              new PingHandler() );
		
		// Synchronization Point Management
		outgoing.put( MessageType.RegisterSyncPoint, new RegisterSyncPointHandler() );
		
		///////////////////////////////////////////////////////////////////////////
		///  Incoming Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		HashMap<MessageType,LRCMessageHandler> incoming = new HashMap<>();

		
		///////////////////////////////////////////////////////////////////////////
		///  Handler Registration   ///////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		// configure and register outgoing handlers
		for( MessageType type : outgoing.keySet() )
		{
			LRCMessageHandler handler = outgoing.get( type );
			handler.configure( settings );
			lrc.getOutgoingSink().registerHandler( type, handler );
		}
		
		// configure and register incoming handlers
		for( MessageType type : incoming.keySet() )
		{
			LRCMessageHandler handler = incoming.get( type );
			handler.configure( settings );
			lrc.getIncomingSink().registerHandler( type, handler );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will load the LRC with the appropriate set of incoming/outgoing message
	 * handlers it should use based on the version of the HLA interface used.
	 * 
	 * @param lrc The LRC to load the appropriate handler set into
	 * @throws JConfigurationException If the version is unknown or any of the handlers experience
	 *                                 an error as they are starting up
	 */
	public static void loadHandlers( LRC lrc ) throws JConfigurationException
	{
		switch( lrc.getHlaVersion() )
		{
			case HLA13:
				loadHla13( lrc );
				break;
			case IEEE1516:
				loadIeee1516( lrc );
				break;
			case IEEE1516e:
				loadIeee1516e( lrc );
				break;
			default:
				throw new JConfigurationException( "Cannot load LRC handlers for unknown version: %s", lrc.getHlaVersion() );
		}		
	}
}
