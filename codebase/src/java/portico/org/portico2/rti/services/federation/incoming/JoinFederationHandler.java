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
package org.portico2.rti.services.federation.incoming;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateNameAlreadyInUse;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.shared.messaging.MessageContext;

public class JoinFederationHandler extends RTIMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		JoinFederation request = context.getRequest( JoinFederation.class );
		String federateName = request.getFederateName();
		String federationName = request.getFederationName();
		
		logger.info( "ATTEMPT Join federate [%s] to federation [%s]", federateName, federationName );
		
		// See if the name is taken
		if( federation.containsFederate(federateName) )
		{
			logger.error( "FAILURE Federate name [%s] is taken in federation [%s]", federateName, federationName );
			throw new JFederateNameAlreadyInUse( "Name %s already taken in federation %s",
			                                     federateName, federationName );
		}

		// Create a new federate object to join the federation
		Federate federate = new Federate( federateName );
		federation.addFederate( federate );
		
		HashMap<String,Object> results = new HashMap<>();
		results.put( "federateHandle", federate.getFederateHandle() );
		results.put( "federationHandle", federation.getFederationHandle() );
		context.success( results );
		
		logger.info( "SUCCESS Federate [%s] joined to federation [%s] with handle [%d]",
		             federateName, federationName, federate.getFederateHandle() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
