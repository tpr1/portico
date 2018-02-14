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
package org.portico2.shared.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class TcpConnectionConfiguration extends ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_ADDRESS     = "address";
	public static final String KEY_PORT        = "port";

	public static final String DEFAULT_ADDRESS = "127.0.0.1";
	public static final int    DEFAULT_PORT    = 20913;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String address;
	private int port;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpConnectionConfiguration( String name )
	{
		super( name );
		this.address = DEFAULT_ADDRESS;
		this.port    = DEFAULT_PORT;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Read Only
	 */
	public ConnectionType getType()
	{
		return ConnectionType.TCP;
	}
	
	public String getAddressString()
	{
		return this.address;
	}
	
	public InetAddress getAddress()
	{
		try
		{
			return InetAddress.getByName( this.address );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Set the address to connect to or listen on. This can be an IP address or a hostname.
	 * 
	 * @param address The address or hostname to use for connection or listening.
	 */
	public void setAddress( String address )
	{
		try
		{
			InetAddress temp = InetAddress.getByName( address );
			if( temp.isMulticastAddress() )
				throw new IllegalArgumentException( address+" is a multicast address" );
			else
				this.address = address;
		}
		catch( UnknownHostException e )
		{
			throw new IllegalArgumentException( address+" is not a valid address" );
		}
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	public void setPort( int port )
	{
		if( port > 65536 )
			throw new IllegalArgumentException( "Port must be in range 0-65536" );
		else
			this.port = port;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void parseConfiguration( String prefix, Properties properties )
	{
		prefix += ".";
		String temp = properties.getProperty( prefix+KEY_ADDRESS );
		if( temp != null )
			setAddress( temp );
		
		temp = properties.getProperty( prefix+KEY_PORT );
		if( temp != null )
			setPort( Integer.parseInt(temp) );
	}

}
