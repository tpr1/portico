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

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.StringUtils;
import org.portico2.lrc.LRC;
import org.portico2.rti.RTI;
import org.portico2.shared.configuration.RID;

/**
 * A {@link Container} represents ... JVM Execution
 */
public class Container
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Container INSTANCE = new Container();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti;
	private Set<LRC> lrcset;
	
	// private HandlerRegistry handlerRegistry -- can this just be a data class genreated when needed? Yes

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Container()
	{
		this.rti = null; // the first one in is the one that gets the bacon!
		this.lrcset = new HashSet<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods   ///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is called when a new {@link RTI} instance is being created and is attempting
	 * to register itself as the central RTI that all local federates should talk to. If there
	 * is no RTI already registered, the given one is put in place and used from here on in.
	 * If there is an RTI already in place, and it is not the given argument, an exception is
	 * thrown as we can only have a single RTI registered in any given execution environment.
	 * 
	 * @param rti The RTI we wish to register as the controller for the clearing house
	 * @throws JConfigurationException If there is already an RTI registered
	 */
	public void registerRti2( RTI rti ) throws JRTIinternalError
	{
		// Make sure we don't already have an RTI, or if we do, make sure it is the same
		if( this.rti != null && this.rti != rti )
			throw new JRTIinternalError( "An RTI already exists in the runtime. Cannot create a second." );
		else
			this.rti = rti;
	}

	public void unregisterRti2( RTI rti ) throws JRTIinternalError
	{
		if( this.rti == null || this.rti != rti )
			throw new JRTIinternalError( "The given RTI was not registered with the container, can't unregister it" );
		
		// send a disconnection notice to each of the LRCs and remove them
		
		// remove the reference for this RTI
		this.rti = null;
	}
	
	/**
	 * This method is called when an LRC is first coming on-line. It registers the LRC and links
	 * it to the centrally registered RTI. If there is no RTI active an exception will be thrown.
	 *  
	 * @throws JRTIinternalError If there is no RTI to link the LRC to
	 */
	public void registerLrc2( LRC lrc ) throws JRTIinternalError
	{
		// Do we have an RTI yet? If we don't, start one
		if( this.rti == null )
			//createAndStartRti();
			throw new JRTIinternalError( "No RTI is running to connect with" ); 

		// store the LRC
		this.lrcset.add( lrc );
	}
	
	public void unregisterLrc2( LRC lrc ) throws JRTIinternalError
	{
		this.lrcset.remove( lrc );
	}
	
	private void createAndStartRti() throws JRTIinternalError
	{
		// Get the command line from the system properties
		String property = System.getProperty( RID.KEY_RTI_COMMAND_LINE, "" );
		String[] commandline = StringUtils.splitAndTrim( property, " " );
		
		// Make sure it has a JVM connection configured
		// ...
		
		// Create the RTI
		RTI rti = new RTI( RID.loadRid(commandline) );
		registerRti2( rti );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static Container instance()
	{
		return INSTANCE;
	}
}
