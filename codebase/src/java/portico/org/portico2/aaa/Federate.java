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
package org.portico2.aaa;

import java.io.File;
import java.net.URL;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;

public class Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIambassador rtiamb;
	private String federation;
	private String federate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federate()
	{
		this.rtiamb = null;
		this.federation = "unknown";
		this.federate = "unknown";
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////
	///  HLA Methods   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	public void createAndJoin( String federate, String federation ) throws Exception
	{
		// Create the RTIambassador
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		
		// Connect to the RTI
		this.rtiamb.connect( new FedAmb(), CallbackModel.HLA_IMMEDIATE );
		
		this.federation = federation;
		this.federate = federate;
		
		// Try to create the federation
		try
		{
			URL fom = new File( "codebase/resources/test-data/fom/ieee1516e/testfom.xml" ).toURI().toURL();
			this.rtiamb.createFederationExecution( federation, fom );
		}
		catch( FederationExecutionAlreadyExists feae )
		{
			// No-op
			System.out.println( "Federation already exists: "+federation );
		}
		catch( Exception e )
		{
			throw e;
		}
		
		this.rtiamb.joinFederationExecution( federate, federation );
	}
	
	public void synchronize( String point ) throws Exception
	{
		
	}
	
	public void publishAndSubscribe() throws Exception
	{
		
	}
	
	public void registerObject() throws Exception
	{
		
	}
	
	public void updateObject() throws Exception
	{
		
	}
	
	public void sendInteraction() throws Exception
	{
		
	}
	
	public void enableTimePolicy() throws Exception
	{
		
	}
	
	public void advanceTime( double next ) throws Exception
	{
		
	}

	public void resignAndDestroy() throws Exception
	{
		this.rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
		
		try
		{
			this.rtiamb.destroyFederationExecution( federation );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			System.out.println( "Canont destroy federation ["+federation+"] - federates are still joined" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////
	///  Federate Ambassador   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	private class FedAmb extends NullFederateAmbassador
	{
		
	}


}
