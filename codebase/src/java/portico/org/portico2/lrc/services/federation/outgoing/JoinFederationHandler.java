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
package org.portico2.lrc.services.federation.outgoing;

import java.net.URL;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.utils.fom.FomParser;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.shared.PorticoConstants;
import org.portico2.shared.messaging.MessageContext;

public class JoinFederationHandler extends LRCMessageHandler
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
		JoinFederation request = context.getRequest( JoinFederation.class, this );
		
		String federate = request.getFederateName();
		String federation = request.getFederationName();
		
		/////////////////////////////
		// perform validity checks //
		/////////////////////////////
		// check for null properties
		if( federate == null || federate.trim().equals("") )
			throw new JRTIinternalError( "Can't join a federation using a null or empty federate name" );
		
		if( federation == null || federation.trim().equals("") )
		{
			throw new JFederationExecutionDoesNotExist(
			    "Can't join a federation using a null or empty federation name" );
		}
		
		// check to make sure the federate isn't already joined
		if( lrcState.isJoined() )
		{
			throw new JFederateAlreadyExecutionMember( "Already connected to federation [" +
			                                            lrcState.getFederationName() +
			                                            "] as federate ["+moniker()+"]" );
		}

		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Join federate ["+federate+"] to federation ["+federation+"]" );
		
		// parse any additional FOM modules and store back in the request for processing
		if( request.getFomModules().size() > 0 )
		{
			for( URL fedLocation : request.getFomModules() )
				request.addJoinModule( FomParser.parse(fedLocation) );
			
			// let people know what happened
			logger.debug( "Parsed ["+request.getJoinModules().size()+"] additional FOM modules" );
		}
		
		/////////////////////////////////
		// Send the request to the RTI //
		/////////////////////////////////
		connection.sendControlRequest( context );
		
		// check the result
		if( context.isSuccessResponse() == false )
			throw context.getErrorResponseException();

		// get the assigned handle
		int federateHandle = context.getSuccessResultAsInt( "federateHandle" );
		int federationHandle = context.getSuccessResultAsInt( "federationHandle" );
		this.lrcState.localFederateJoinedFederation( federateHandle,
		                                             federationHandle,
		                                             federate,
		                                             federation,
		                                             request.getFOM() );
		
		// replace the results with only the federate handle
		context.success( federateHandle );
		
		logger.info( "SUCCESS Federate [%s] joined to federation [%s] with handle [%s]",
		             federate, federation, federateHandle );

		// if FOM printing is enabled, do so
		if( PorticoConstants.isPrintFom() )
			logger.info( "FOM in use for federation ["+federation+"]:\n"+fom() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
