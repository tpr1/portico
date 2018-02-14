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
package org.portico2.rti.network.multicast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.rti.RTI;
import org.portico2.shared.configuration.ConnectionConfiguration;
import org.portico2.shared.configuration.ConnectionType;
import org.portico2.shared.configuration.RID;
import org.portico2.shared.messaging.MessageContext;
import org.portico2.shared.messaging.ResponseMessage;
import org.portico2.shared.network.IConnection;

public class RtiMulticastConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RtiMulticastConnection()
	{
		
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Get a name for this type of connection.
	 */
	@Override
	public String getName()
	{
		return "RTI Multicast Connection";
	}

	@Override
	public ConnectionType getType()
	{
		return ConnectionType.MULTICAST;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Methods   //////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is called by the RTI during setup.
	 * 
	 * It will be called when the RTI first creates the connection, prior to any call to
	 * {@link #connect()}. This gives the connection a chance to configure itself.
	 */
	@Override
	public void configure( Object owner, ConnectionConfiguration configuration )
		throws JConfigurationException
	{
		if( owner instanceof RTI == false )
			throw new JConfigurationException( "Trying to deploy RtiMulticastConnection into %s", owner.getClass().getSimpleName() );
		
		RTI rti = (RTI)owner;
		this.logger = LogManager.getFormatterLogger( rti.getLogger().getName()+".multicast" );
		
		RID rid = rti.getRid();
		this.logger.fatal( "configure(): Multicast Connection" );
	}
	
	/**
	 * This method is called with the RTI would like the connection to commence processing.
	 * 
	 * At this stage, connection instances should commence whatever network management is
	 * necessary to start taking requests from external parties.
	 */
	@Override
	public void connect() throws JRTIinternalError
	{
		logger.fatal( "connect(): Multicast Connection" );
	}
	
	/**
	 * This method is called when the RTI is shutting down.
	 * 
	 * All open connections should be closed and any clean-up work completed.
	 */
	@Override
	public void disconnect() throws JRTIinternalError
	{
		logger.fatal( "disconnect(): Multicast Connection" );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * TBA
	 * 
	 * @param context The request message to send
	 * @return A response message with the result
	 * @throws JRTIinternalError If there is a problem of any kind
	 */
	@Override
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// No-op in RTI
		throw new JException( "Sending control-message requests from the RTI is not supported" );
	}

	/**
	 * Sends a response to a previously received control request. This is typically only
	 * used by the RTI to route back results. Control messages each have a message ID that
	 * can be used to link a request to a response. This call WILL NOT BLOCK.   
	 * 
	 * @param response The response message containing result information.
	 * @throws JException If there is a problem sending the message
	 */
	@Override
	public void sendControlResponse( ResponseMessage response ) throws JException
	{
		
	}
	
	/**
	 * Broadcast messages are intended to be sent to all federates within a federation.
	 * Although they may be routed through the RTI, they are not a control message. 
	 * 
	 * Their use is currently limited to attribute reflections and interactions. Although
	 * these messages are only a small subset of all those available, in any given federation
	 * they will represent the _vast_ majority of the volume of messages exchanged. 
	 * 
	 * @param message The message to send to all other federates
	 * @throws JException If there is a problem sending the message
	 */
	@Override
	public void sendBroadcast( PorticoMessage message ) throws JException
	{
		// Send this to all other connections
		// Send to all directly connected federates
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}