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
package org.portico2.rti.services;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.federation.incoming.JoinFederationHandler;
import org.portico2.rti.services.federation.incoming.ResignFederationHandler;
import org.portico2.shared.messaging.IMessageHandler;
import org.portico2.shared.messaging.MessageType;

public class RTIHandlerRegistry
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
	private static void loadHla13( Federation federation )
	{
		
	}
	
	private static void loadIeee1516( Federation federation )
	{
		
	}
	
	private static void loadIeee1516e( Federation federation )
	{
		// object map for configuration of handlers - stuffed with a reference to the LRC
		Map<String,Object> settings = new HashMap<>();
		settings.put( IMessageHandler.KEY_RTI_FEDERATION, federation );
		
		///////////////////////////////////////////////////////////////////////////
		///  Outgoing Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		HashMap<MessageType,RTIMessageHandler> outgoing = new HashMap<>();
		
		///////////////////////////////////////////////////////////////////////////
		///  Incoming Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		HashMap<MessageType,RTIMessageHandler> incoming = new HashMap<>();
		incoming.put( MessageType.JoinFederation,    new JoinFederationHandler() );
		incoming.put( MessageType.ResignFederation,  new ResignFederationHandler() );

		
		///////////////////////////////////////////////////////////////////////////
		///  Handler Registration   ///////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		// configure and register outgoing handlers
		for( MessageType type : outgoing.keySet() )
		{
			RTIMessageHandler handler = outgoing.get( type );
			handler.configure( settings );
			//federation.getOutgoingSink().registerHandler( type, handler );
		}
		
		// configure and register incoming handlers
		for( MessageType type : incoming.keySet() )
		{
			RTIMessageHandler handler = incoming.get( type );
			handler.configure( settings );
			federation.getIncomingSink().registerHandler( type, handler );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will load a {@link Federation} within an RTI with the appropriate set of
	 * incoming/outgoing message handlers it should use based on the version of the HLA
	 * interface used.
	 * 
	 * @param federation The Federation to load the appropriate handler set into
	 * @throws JConfigurationException If the version is unknown or any of the handlers experience
	 *                                 an error as they are starting up
	 */
	public static void loadHandlers( Federation federation ) throws JConfigurationException
	{
		switch( federation.getHlaVersion() )
		{
			case HLA13:
				loadHla13( federation );
				break;
			case IEEE1516:
				loadIeee1516( federation );
				break;
			case IEEE1516e:
				loadIeee1516e( federation );
				break;
			default:
				throw new JConfigurationException( "Cannot load RTI handlers for unknown version: %s",
				                                   federation.getHlaVersion() );
		}		
	}
}
