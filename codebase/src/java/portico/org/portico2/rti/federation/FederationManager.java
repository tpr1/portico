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
package org.portico2.rti.federation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JRTIinternalError;

/**
 * The purpose of the {@link FederationManager} is to keep track and state about the various
 * active federations that are being supported by the RTI instance in which they are contained.
 */
public class FederationManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,Federation> federations;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationManager()
	{
		this.federations = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public Collection<Federation> getActiveFederations()
	{
		return this.federations.values();
	}

	public Federation getFederation( String name )
	{
		return federations.get( name );
	}
	
	public Federation getFederation( int federationHandle )
	{
		for( Federation federation : federations.values() )
		{
			if( federation.getFederationHandle() == federationHandle )
				return federation;
		}
			
		throw new JRTIinternalError( "Could not find federation with handle %d", federationHandle );
	}
	
	public boolean containsFederation( String name )
	{
		return federations.containsKey( name );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federation Management   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void addFederation( Federation federation )
	{
		this.federations.put( federation.getFederationName(), federation );
	}
	
	public void removeFederation( Federation federation )
	{
		this.federations.remove( federation.getFederationName() );
	}

// Should this be handled in a handler? Yes, probably
//	public void createFederation()
//	{
//		
//	}
//	
//	public void destroyFederation()
//	{
//		
//	}

	
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
