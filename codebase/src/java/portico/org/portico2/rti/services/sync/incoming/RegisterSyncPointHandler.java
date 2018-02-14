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
package org.portico2.rti.services.sync.incoming;

import java.util.HashSet;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.sync.msg.SyncPointRegister;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.shared.data.sync.SyncPoint;
import org.portico2.shared.messaging.MessageContext;

public class RegisterSyncPointHandler extends RTIMessageHandler
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
		SyncPointRegister request = context.getRequest( SyncPointRegister.class, this );
		int source = request.getSourceFederate();
		String label = request.getLabel();
		HashSet<Integer> syncset = request.getFederateSet();
		
		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		logger.debug( "ATTEMPT Register sync point ["+label+"] by ["+moniker(source)+"]" );
		
		// check for a null label
		if( label == null || label.trim().equals("") )
			throw new JRTIinternalError( "Can't register sync point with null or empty label" );
		
		// if the sync set exists, but is empty, set it to null so as to indicate that this point
		// is a federation-wide point (empty set == every federate, every federate denoted by null)
		if( syncset != null && syncset.isEmpty() )
		{
			syncset = null;
			request.makeFederationWide();
		}

		// validate that we know about each of the handles that have been requested
		String resultMessage = validateGroupHandles( label, syncset );
		if( resultMessage != null )
		{
			// Some handle in the set isn't valid, don't process the rest of the request.
			// Send back the failure reason so that the federate knows what is happening
			context.error( resultMessage );
			return;
		}

		// try and create the sync point, if it already exists, queue up an error message
		synchronized( this )
		{
			if( syncPoints.containsPoint(label) )
				throw new JRTIinternalError( "label already exists" );
			
			SyncPoint point = syncPoints.registerPoint( label, request.getTag(), syncset, source );
		}
		
		// Queue an announcement message for processing
		// FIXME
		
		// Set the respone to successful and return
		context.success();
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Registered sync point ["+label+"] by ["+moniker(source)+"]" );
	}

	/**
	 * Check the set of handles that were given for the synchronization group and validate them.
	 * If the validation is successful, we return null (NOTE).
	 * 
	 * If the handle set is null, there was no group, so just return. If the handle set
	 * exists, but contains the handle of a federate we don't know about, return an appropriate
	 * error message containing a sync point failure reason.
	 */
	private String validateGroupHandles( String label, HashSet<Integer> groupHandles )
	{
		// a null group means this is a federation wide syncpoint, no need to validate handles
		if( groupHandles == null )
			return null;

		for( Integer federateHandle : groupHandles )
		{
			if( federation.containsFederate(federateHandle) == false )
				return "invalid fedeate handle in sync-set, handle="+federateHandle;
		}
		
		// everything looks good!
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
