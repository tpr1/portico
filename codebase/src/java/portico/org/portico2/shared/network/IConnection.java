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
package org.portico2.shared.network;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.shared.configuration.ConnectionConfiguration;
import org.portico2.shared.configuration.ConnectionType;
import org.portico2.shared.messaging.MessageContext;
import org.portico2.shared.messaging.ResponseMessage;

/**
 * In the Portico networking framework there are different connection interfaces depending on
 * whether you are talking about a connection that lives inside the RTI and services federates,
 * or a connection that lives inside an LRC and connects to the RTI, or even connections that
 * live inside a Forwarder. All these types of connections, regardless of their home, have some
 * universal methods. Those are defined here.
 */
public interface IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Get a name for this type of connection.
	 */
	public String getName();

	/**
	 * @return The type of this connection
	 */
	public ConnectionType getType();

	///////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Methods   //////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is called when a connection object is being created. It passes configuration
	 * information extracted from the RID object. It also passes an instance to the "owner" of
	 * the connection (be that an RTI, LRC or Forwarder). The connection type, depending on the
	 * purpose it serves, will need to cast that object down to the appropriate type. 
	 *
	 * This method will be called prior to any call to {@link #connect()}.
	 * 
	 * @param owner The RTI, LRC or Forwarder instance that this connection is being dpeloyed into
	 * @param configuration The configuration data extracted from the RID
	 * @throws JConfigurationException If there is a problem during connection
	 */
	public void configure( Object owner, ConnectionConfiguration configuration )
		throws JConfigurationException;
	
	/**
	 * This method is called when the connection should establish itself and commence processing.
	 */
	public void connect() throws JRTIinternalError;
	
	/**
	 * Called when the owner is shutting down. All active connections should be closed.
	 */
	public void disconnect() throws JRTIinternalError;

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sends a control message to the RTI and BLOCK until we get a response.
	 * Control messages are exchanges between a single federate and the RTI.
	 * This includes all HLA messages except data exchanges (reflection, interfaction).
	 * On return, the response portion of the message context will be filled out with
	 * an appropriate response message.
	 * 
	 * @param context The message context object containing the request
	 * @throws JRTIinternalError If there is a problem of any kind
	 */
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError;

	/**
	 * Sends a response to a previously received control request. This is typically only
	 * used by the RTI to route back results. Control messages each have a message ID that
	 * can be used to link a request to a response. This call WILL NOT BLOCK.   
	 * 
	 * @param response The response message containing result information.
	 * @throws JException If there is a problem sending the message
	 */
	public void sendControlResponse( ResponseMessage response ) throws JException;
	
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
	public void sendBroadcast( PorticoMessage message ) throws JException;
}
