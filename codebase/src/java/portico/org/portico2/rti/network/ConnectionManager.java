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
package org.portico2.rti.network;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.rti.RTI;
import org.portico2.rti.network.jvm.JvmConnection;
import org.portico2.rti.network.multicast.RtiMulticastConnection;
import org.portico2.shared.configuration.ConnectionConfiguration;
import org.portico2.shared.configuration.ConnectionType;
import org.portico2.shared.configuration.RID;
import org.portico2.shared.network.IConnection;

/**
 * This class manages the various connections that an RTI can have running at any given point.
 * It is responsible for extracting configuration information, creating and starting the instances
 * of {@link IRtiConnection} and linking them with the core {@link RTI}.
 */
public class ConnectionManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private RTI rti;
	private Map<String,IConnection> connections;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ConnectionManager()
	{
		this.logger = null;   // set in configure()
		this.rti = null;      // set in configure()
		this.connections = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Methods   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * For each of the RTI connection configurations contained in the RID for the given {@link RTI},
	 * create a new {@link IRtiConnection} and store it. The lifecycle of these connections is
	 * managed by the {@link #startup()} and {@link #shutdown()} methods of the manager. If there
	 * is a problem creating any of the connections, a {@link ConfigurationException} will be
	 * thrown.
	 * 
	 * @param rti The {@link RTI} to link the connections in to
	 * @throws ConfigurationException If there if a problem creating or configuring any connections
	 */
	public void configure( RTI rti ) throws JConfigurationException
	{
		this.logger = rti.getLogger();
		this.rti = rti;
		
		// 1. extract the names of the different connections we need to create
		RID rid = rti.getRid();
		Map<String,ConnectionConfiguration> connections = rid.getRtiConfiguration().getConnections();
		if( connections.isEmpty() )
			throw new JConfigurationException( "No RTI connections specified in RID" );
		
		// 2. Instantiate each connection and call its configure() method
		for( ConnectionConfiguration configuration : connections.values() )
		{
			// create the connection
			String connectionName = configuration.getName();
			IConnection connection = createConnection( connectionName, configuration.getType() );
			logger.debug( "Created connection [%s], type=%s", connectionName, connection.getType() );
			
			// configure the connection
			connection.configure( rti, configuration );
			this.connections.put( connectionName, connection );
		}
	}

	private IConnection createConnection( String connectionName, ConnectionType type )
		throws JConfigurationException
	{
		switch( type )
		{
			case MULTICAST:
				return new RtiMulticastConnection();
			case TCP:
				throw new JConfigurationException( "Cannot create RTIConnection (name: %s); unsupported type: TCP",
				                                   connectionName );
			case UDP:
				throw new JConfigurationException( "Cannot create RTIConnection (name: %s); unsupported type: UDP",
				                                   connectionName );
			case JVM:
				return new JvmConnection();
		}
		
		// if we get here, we were never able to create the connection
		throw new JConfigurationException( "Cannot create RTIConnection; unknown type: %s", type );
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods   ////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void startup() throws JRTIinternalError
	{
		for( IConnection connection : this.connections.values() )
		{
			logger.info( "Starting connection %s", connection.getName() );
			connection.connect();
		}
	}
	
	public void shutdown() throws JRTIinternalError
	{
		for( IConnection connection : this.connections.values() )
		{
			logger.info( "Shutting down connection %s", connection.getName() );
			connection.disconnect();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
