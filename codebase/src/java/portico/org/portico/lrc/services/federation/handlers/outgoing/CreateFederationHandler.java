/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.federation.handlers.outgoing;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.utils.fom.FomParser;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=CreateFederation.class)
public class CreateFederationHandler extends LRCMessageHandler
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

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		CreateFederation request = context.getRequest( CreateFederation.class, this );
		
		// check that we don't have a null name
		if( request.getFederationName() == null )
			throw new JRTIinternalError( "Can't create a federation with null name" );

		//
		// 1. Parse all the FOM fragments
		//
		// try and parse each of the fragments we have been given
		List<ObjectModel> foms = new ArrayList<ObjectModel>();
		for( URL module : request.getFomModules() )
			foms.add( FomParser.parse(module) );
		
		//
		// 2. Check for the MIM
		//
		// if it isn't already loaded, graft in the MIM
		// Check all modules to see if any are the MIM. If they aren't, load it, otherwise skip
		loadStandardMim( foms );
		
		//
		// 3. Merge the modules
		//
		ObjectModel combined = ModelMerger.merge( foms );
		request.setModel( combined );

		//
		// 4. Create the Federation
		//
		logger.debug( "ATTEMPT Create federation execution [" + request.getFederationName() + "]" );
		connection.createFederation( request );
		context.success();
		logger.info( "SUCCESS Created federation execution [" + request.getFederationName() + "]" );
	}

	/**
	 * Check the given set of models to see if any represent the standard MIM.
	 * If the MIM is present, return. If the MIM isn't present, load it from our system
	 * resources and add it to the set of models.
	 */
	private void loadStandardMim( List<ObjectModel> foms ) throws Exception
	{
		for( ObjectModel fragment : foms )
		{
			if( fragment.getPrivilegeToDelete() != ObjectModel.INVALID_HANDLE )
			{
				if( fragment.getObjectClass("HLAmanager") != null ||
					fragment.getInteractionClass("HLAmanager") != null )
				{
					return; // found it! nothing more to do
				}
			}
		}

		// if we get here, we didn't find the MIM in the given set of FOM fragments
		logger.debug( "Standard MIM not present - adding it" );
		URL mim = ClassLoader.getSystemResource( "etc/ieee1516e/HLAstandardMIM.xml" );
		foms.add( 0, FomParser.parse(mim) );
	}

	//private void validateStandardMimPresent( List<ObjectModel> foms ) throws Exception
	//{
	//	boolean found = false;
	//	for( ObjectModel model : foms )
	//	{
	//		if( model.getPrivilegeToDelete() != -1 )
	//		{
	//			found = true;
	//			break;
	//		}
	//	}
	//	
	//	if( found == false )
	//	{
	//		logger.debug( "Standard MIM not present - adding it" );
	//		URL mim = ClassLoader.getSystemResource( "etc/ieee1516e/HLAstandardMIM.xml" );
	//		foms.add( 0, FomParser.parse(mim) );
	//	}
	//}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
