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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.StringUtils;

public class RtiConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_RTI_CONNECTIONS = "rti.network.connections";
	public static final String PFX_RTI_CONNECTION  = "rti.network"; // rti.network.$name

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,ConnectionConfiguration> rtiConnections;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected RtiConfiguration()
	{
		this.rtiConnections = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Network Configuration Options    ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getConnection( String name )
	{
		return rtiConnections.get( name );
	}
	
	public void addConnection( ConnectionConfiguration configuration )
	{
		String name = configuration.getName();
		if( rtiConnections.containsKey(name) )
			throw new IllegalArgumentException( "RTI already contains connection with name "+name );
		else
			rtiConnections.put( name, configuration );
	}

	/**
	 * @param name The name of the connection to remove
	 * @return The connection that was removed, or null if none could be found with that name
	 */
	public ConnectionConfiguration removeConnection( String name )
	{
		return rtiConnections.remove( name );
	}
	
	public Map<String,ConnectionConfiguration> getConnections()
	{
		return Collections.unmodifiableMap( this.rtiConnections );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected void parseProperties( Properties properties ) throws JConfigurationException
	{
		String property = properties.getProperty( KEY_RTI_CONNECTIONS, "" ).trim();
		if( property.equals("") )
		{
			// no configurations have been specified - fall back on just having a JVM connection
			this.rtiConnections.put( "jvm", new JvmConnectionConfiguration("jvm") );
		}
		else
		{
    		String[] names = StringUtils.splitAndTrim( property, "," );
    		
    		// build a configuration object for each type
    		for( String name : names )
    		{
    			String prefix = PFX_RTI_CONNECTION+"."+name;
    			
    			// get the connection type
    			String typeString = properties.getProperty( prefix+".type" );
    			if( typeString == null )
    				throw new JConfigurationException( "RTI Connection [%s] does not specify a type", name );
    			
    			ConnectionType type = ConnectionType.fromConfigString( typeString );
    
    			// create the configuration and store it
    			ConnectionConfiguration configuration = type.newConfig( name );
    			configuration.parseConfiguration( prefix, properties );
    			this.rtiConnections.put( name, configuration );
    		}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
