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

import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;

public class LrcConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_LRC_CONNECTION = "lrc.network.connection";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ConnectionConfiguration lrcConnection;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LrcConfiguration()
	{
		this.lrcConnection = null; // set in parseProperties()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Pull the configuration information we need from the given property set.
	 */
	protected void parseProperties( Properties properties ) throws JConfigurationException
	{
		//
		// Connection Configuration
		//
		String property = properties.getProperty( KEY_LRC_CONNECTION, "" ).trim();
		if( property.equals("") )
		{
			// no configurations have been specified - fall back on just having a JVM connection
			this.lrcConnection = new JvmConnectionConfiguration("jvm");
		}
		else
		{
			ConnectionType type = ConnectionType.fromConfigString( property );
			this.lrcConnection = type.newConfig( "lrc" );
			this.lrcConnection.parseConfiguration( "lrc.network", properties );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessors and Mutators   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getConnectionConfiguration()
	{
		return this.lrcConnection;
	}

	public void setConnectionConfiguration( ConnectionConfiguration lrcConnection )
	{
		this.lrcConnection = lrcConnection;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
