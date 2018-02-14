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

import org.portico2.rti.RTI;
import org.portico2.shared.configuration.RID;

public class Tester
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti;
	private Federate federateOne;
	private Federate federateTwo;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Tester()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void run( String[] args ) throws Exception
	{
		// Set up the RTI and federates
		initialize();
		System.out.println( "Components initialized and ready" );
		
	}

	private void initialize() throws Exception
	{
		// Create the RTI
		RID rid = RID.loadDefaultRid();
		this.rti = new RTI( rid );
		this.rti.startup();
		
		// Create each of the federates
		this.federateOne = new Federate();
		this.federateTwo = new Federate();
		
		// Connect, Create the federation and join it
		this.federateOne.createAndJoin( "federateOne", "testFederation" );
		this.federateTwo.createAndJoin( "federateTwo", "testFederation" );
		
		// Initial Sync
		this.federateOne.registerSyncPoint( "START" );
		this.federateOne.registerSyncPoint( "START" ); // will fail
		this.federateTwo.waitForAnnounce( "START" );
		this.federateTwo.achieve( "START" );
		this.federateOne.achieve( "START" );
		this.federateOne.waitForSynchronized( "START" );
		this.federateTwo.waitForSynchronized( "START" );
		
		// Tear things down
		this.federateOne.resignAndDestroy();
		this.federateTwo.resignAndDestroy();
		
		
		// Or; create individual LRCs
		//LRC lrc = new LRC( HLAVersion.IEEE1516e, rid );
		//lrc.connect();
		//System.out.println( "LRC is Connected" );
		
		// Try and send a Ping to the RTI
		//MessageContext context = new MessageContext( new Ping() );
		//lrc.getOutgoingSink().process( context );
		//System.out.println( "Sent a control message" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args ) throws Exception
	{
		new Tester().run( args );
		
//		for( int i = 0; i < 10; i++ )
//		{
//			java.util.Random random = new java.util.Random();
//			long t0 = System.nanoTime();
//			for( int j = 0; j < 1000000; j++ )
//			{
////				int l = random.nextInt();
//				java.util.UUID uuid = java.util.UUID.randomUUID();
//				if( System.currentTimeMillis() < 0 )
////					System.out.println( l );
//					System.out.println( uuid.toString() );
//			}
//			System.out.println( "1M generated in "+(java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0))+"ms" );
//		}
	}
}
