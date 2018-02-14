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

import org.portico.lrc.compat.JConfigurationException;

public enum ConnectionType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	MULTICAST
	{
		public ConnectionConfiguration newConfig( String name ) { return new MulticastConnectionConfiguration(name); }
	},
	TCP
	{
		public ConnectionConfiguration newConfig( String name ) { return new TcpConnectionConfiguration(name); }
	},
	UDP
	{
		public ConnectionConfiguration newConfig( String name ) { throw new IllegalArgumentException("Not yet supported"); }
	},
	JVM
	{
		public ConnectionConfiguration newConfig( String name ) { return new JvmConnectionConfiguration(name); }
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public abstract ConnectionConfiguration newConfig( String name );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static ConnectionType fromString( String string )
	{
		for( ConnectionType type : ConnectionType.values() )
			if( type.name().equalsIgnoreCase(string) )
				return type;
		
		throw new IllegalArgumentException( "No such type: "+string );
	}

	/**
	 * The same as {@link #fromString(String)} except that it throws a {@link JConfigurationException}
	 * if the value cannot be found.
	 * 
	 * @param string The string to load a connection type from
	 * @return The type of connection represented by the string
	 */
	public static ConnectionType fromConfigString( String string )
	{
		try
		{
			return fromString(string);
		}
		catch( IllegalArgumentException ia )
		{
			throw new JConfigurationException( "No such type: "+string );
		}
	}
	
}
