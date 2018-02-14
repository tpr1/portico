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
package org.portico2.container;

import java.util.HashSet;
import java.util.Set;

import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.lrc.LRC;
import org.portico2.rti.RTI;
import org.portico2.shared.messaging.MessageContext;
import org.portico2.shared.messaging.ResponseMessage;

/**
 * The JvmExchange acts as the aggregation point with an execution. As {@link RTI} and {@link LRC}
 * components come online they can attach to the exchange and start sharing messages. The exchange
 * is a Singleton.
 * <p/>
 * At any given time there can only be a single RTI active within the exchange. When an LRC tries
 * to connect to an exchange inside which there is no RTI, an exception will be thrown. It is best
 * to think of this exchange point as being akin to a private network. Without an active RTI, each
 * LRC has nobody to talk to, and thus should fail on connection. If the RTI is removed while the
 * LRCs are still running, they should be disconnected, much as they would if the RTI was killed
 * and they were communicating over a network connection.
 * <p/>
 * TODO Notes about message exchange
 */
public class JvmExchange
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final JvmExchange INSTANCE = new JvmExchange();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti; // Can only have one active RTI at once. First one in gets the bacon!
	private Set<LRC> lrcs;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private JvmExchange()
	{
		this.rti = null;
		this.lrcs = new HashSet<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Methods   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The Exchange can only have a single RTI active at once. This method will register that
	 * RTI instance so that we know where to route control messages to.
	 * 
	 * @param rti The RTI to route all control messages to
	 * @throws JRTIinternalError If there is already an RTI registered
	 */
	public void attachRti( RTI rti ) throws JRTIinternalError
	{
		if( rti == null )
			return;
		
		if( this.rti != null )
			throw new JRTIinternalError( "An RTI is already registered witht the JvmExchange" );
		
		this.rti = rti;
	}

	/**
	 * Remove the RTI from the exchange. This may simulate an RTI crash or other type of exit.
	 * If there are LRCs attached to the exchange, they will each be sent a disconnection notice
	 * since they can no longer communicate with an RTI that isn't meant to exist.
	 * 
	 * @param rti The RTI we with to detach
	 * @throws JRTIinternalError If the given RTI does not match the one we have
	 */
	public void detachRti( RTI rti ) throws JRTIinternalError
	{
		// ignore if we don't have a registered RTI
		if( this.rti == null )
			return;
		
		if( this.rti != rti )
			throw new JRTIinternalError( "The RTI given to detach from the JvmExchange is not the one that is registered" );
		
		this.rti = null;
		
		// TODO Send disconnection notice to all connected LRCs
	}

	/**
	 * Attach the given LRC to the exchange so that it can start sending control and broadcast
	 * messages. If there is no RTI registered, this call will fail, as we are not yet in a state
	 * where we are ready for connections from federates.
	 * 
	 * @param lrc The LRC we wish to attach to the exchange
	 * @throws JRTIinternalError If there is no active RTI yet
	 */
	public void attachLrc( LRC lrc ) throws JRTIinternalError
	{
		if( lrc == null )
			return;
		
		if( this.rti == null )
			throw new JRTIinternalError( "There is no running RTI in the JvmExchange" );
		
		this.lrcs.add( lrc );
	}

	/**
	 * Detach the given LRC from the exchange. This will remove it from the set of active 
	 * LRCs that are running within the exchange and stop it receiving messages from those
	 * who are.
	 * 
	 * @param lrc The LRC to detach
	 * @throws JRTIinternalError TBA
	 */
	public void detachLrc( LRC lrc ) throws JRTIinternalError
	{
		this.lrcs.remove( lrc );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Exchange Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void sendControlMessage( MessageContext context ) throws JRTIinternalError
	{
		if( this.rti == null )
			throw new JRTIinternalError( "RTI is not present in JvmExchange. Cannot send message" );

		rti.getInbox().receiveControlMessage( context );
	}
	
	public void sendControlResponse( ResponseMessage response ) throws JRTIinternalError
	{
		throw new JRTIinternalError( "NOT FILLED OUT" );
	}

	public void sendBroadcast( PorticoMessage message ) throws JRTIinternalError
	{
		if( this.rti == null )
			throw new JRTIinternalError( "RTI is not present in JvmExchange. Cannot send message" );

		this.rti.getInbox().receiveBroadcast( message );		
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static final JvmExchange instance()
	{
		return JvmExchange.INSTANCE;
	}
}
