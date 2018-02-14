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
package org.portico2.rti;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.lrc.services.federation.msg.DestroyFederation;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;
import org.portico2.rti.services.RTIHandlerRegistry;
import org.portico2.shared.messaging.MessageContext;

public class RtiInbox
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private RTI rti;
	private FederationManager federationManager;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected RtiInbox( RTI rti )
	{
		this.rti = rti;
		this.federationManager = rti.getFederationManager();
		this.logger = rti.getLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// message receiving methods //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void receiveControlMessage( MessageContext context ) throws JRTIinternalError
	{
		PorticoMessage request = context.getRequest();
		
		// if the message is a "federation internal" one, find the federation and route it there
		if( request.getType().isFederationMessage() )
		{
			//
			// Federation Message - Find the federation and forward to it
			//
//			int fedId = request.getFederationId();
//			Federation targetFederation = federationManager.getFederation( fedId );
//			if( targetFederation != null )
//				targetFederation.getIncomingSink().process( context );
//			else
//				throw new JRTIinternalError( "No known federation with ID "+fedId );
		}
		else
		{
			//
			// Non-Federation Message - Process at RTI level
			//
			switch( request.getType() )
			{
				case Connect:
//					throw new JRTIinternalError( "Message not supported yet: "+request.getType() );
					return;
				case Disconnect:
//					throw new JRTIinternalError( "Message not supported yet: "+request.getType() );
				case CreateFederation:
					createFederation( context );
					return;
				case DestroyFederation:
					destroyFederation( context );
					return;
				case JoinFederation:
					joinFederation( context ); // pass through to federation
					return;
				case ResignFederation:
					resignFederation( context ); // pass through to federation
					return;
				case ListFederations:
					throw new JRTIinternalError( "Message not supported yet: "+request.getType() );
				case Ping:
					context.success( System.currentTimeMillis() );
					return;
				default:
					throw new JRTIinternalError( "Unknown control message type: "+request.getType() );
			}
		}
	}

	public void receiveBroadcast( PorticoMessage message ) throws JRTIinternalError
	{
		// find the federation this message is for
		
		// reflect the message to the other participants (how do we stop reflect back?)
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Federation Management Methods   //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private synchronized void createFederation( MessageContext context )
	{
		CreateFederation request = context.getRequest( CreateFederation.class );
		String name = request.getFederationName();
		ObjectModel fom = request.getModel();
		HLAVersion hlaVersion = request.getHlaVersion();
		
		// Check to see if the name is taken
		if( federationManager.containsFederation(name) )
		{
			context.error( new JFederationExecutionAlreadyExists("name="+name) );
			return;
		}

		// Create the federation object and store it
		logger.info( "ATTEMPT Creating federation name="+name );
		
		Logger logger = LogManager.getFormatterLogger( rti.getLogger().getName()+"."+name );
		Federation federation = new Federation( name, fom, hlaVersion, logger );
		RTIHandlerRegistry.loadHandlers( federation );
		federationManager.addFederation( federation );
		
		logger.info( "SUCCESS Created federation name="+name );
		context.success( federation.getFederationHandle() );
	}

	private synchronized void joinFederation( MessageContext context )
	{
		try
		{
			JoinFederation request = context.getRequest( JoinFederation.class );
			String federationName = request.getFederationName();
			Federation federation = federationManager.getFederation( federationName );

			// Hand the message off to the federation's incoming sink
			federation.getIncomingSink().process( context );
		}
		catch( Exception e )
		{
			// Set an error on the context
			context.error( e );
		}
	}

	private synchronized void resignFederation( MessageContext context )
	{
		try
		{
			ResignFederation request = context.getRequest( ResignFederation.class );
			String federationName = request.getFederationName();
			Federation federation = federationManager.getFederation( federationName );
    		
    		// Hand the message off to the federation's incoming sink
    		federation.getIncomingSink().process( context );
		}
		catch( Exception e )
		{
			// Set an error on the context
			context.error( e );
		}
	}

	private synchronized void destroyFederation( MessageContext context )
	{
		DestroyFederation request = context.getRequest( DestroyFederation.class );
		String name = request.getFederationName();
		
		// Create the federation object and store it
		logger.info( "ATTEMPT Destroy federation name="+name );

		// Check to see if the name is taken
		Federation federation = federationManager.getFederation( name );
		if( federation == null )
		{
			logger.error( "FAILURE Federation does not exist: name="+name );
			context.error( new JFederationExecutionDoesNotExist("name="+name) );
			return;
		}

		// Check to see if it has federates
		if( federation.containsFederates() )
		{
			logger.error( "FAILURE Can't destory fedearation, it has federates joined: name="+name );
			context.error( new JFederatesCurrentlyJoined("federates joined to fedeation ["+name+"]") );
			return;
		}
		
		// Remove the federation
		federationManager.removeFederation( federation );
		logger.info( "SUCCESS Destroyed federation name="+name );
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
