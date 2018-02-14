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

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.rti.federation.Federation;
import org.portico2.shared.messaging.IMessageHandler;
import org.portico2.shared.messaging.MessageContext;

public abstract class RTIMessageHandler implements IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Federation federation;
	protected Logger logger;
//	private IConnection connection; FIXME - Shouldn't this be an outgoing queue? Yes.
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return The class name of this handler (final part, not qualified).
	 */
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Extracts some key information from the given properties, such as a referce to the
	 * {@link Federation} that it is contained in. Caches a number of important objects in
	 * protected member variable so that the handlers can just refer to them without needing
	 * any special setup. 
	 */
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		if( properties == null )
			throw new JConfigurationException( "Cannot initialize LRC from null property set" );

		this.federation = (Federation)properties.get( IMessageHandler.KEY_RTI_FEDERATION );
		this.logger = federation.getLogger();
//		this.connection = lrc.getConnection();
	}
	
	/**
	 * A message has been received for processing. The request and/or response is contained within
	 * the given {@link MessageContext} object. Take appropriate action and throw any exception
	 * from the compatibility library you need to.
	 * 
	 * @param context The request and/or response holder
	 * @throws JException If there is a problem processing the message
	 */
	@Override
	public abstract void process( MessageContext context ) throws JException;

	/////////////////////////////////////////////////////////////////////////////////////////
	///  Helper Methods  ////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	protected final String federationName()
	{
		return federation.getFederationName();
	}
	
	protected final int federationHandle()
	{
		return federation.getFederationHandle();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
