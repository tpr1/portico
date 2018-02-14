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
package org.portico2.shared.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JRTIinternalError;

public class MessageSink
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private Logger logger;

	private Map<MessageType,IMessageHandler> messageHandlers;
	private IMessageHandler defaultHandler;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MessageSink( String name, Logger logger )
	{
		this.name = name;
		this.logger = logger;
		
		this.messageHandlers = new HashMap<>();
		this.defaultHandler = new DefaultHandler();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void process( MessageContext context )
	{
		IMessageHandler handler = messageHandlers.get( context.getRequest().getType() );
		if( handler == null )
			defaultHandler.process( context );
		else
			handler.process( context );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Handler Management Methods   /////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void registerHandler( MessageType type, IMessageHandler handler )
	{
		this.messageHandlers.put( type, handler );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: DefaultHandler   ////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private class DefaultHandler implements IMessageHandler
	{
		@Override public String getName() { return "DefaultHandler"; }
		@Override public void configure( Map<String,Object> properties ){}
		@Override public void process( MessageContext context ) throws JRTIinternalError
		{
			logger.warn( "(sink: %s) IGNORE MESSSAGE. No handler for type: %s",
			             name, context.getRequest().getType() );
			
			// TODO Do we need to throw an exception?
			//throw new JRTIinternalError( "(sink: %s) No handler for type: %s",
			//                             name, context.getRequest().getType() );
		}
	}
}
