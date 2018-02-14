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
package org.portico2.lrc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.federation.msg.Connect;
import org.portico2.lrc.network.jvm.LrcJvmConnection;
import org.portico2.shared.PorticoConstants;
import org.portico2.shared.configuration.ConnectionConfiguration;
import org.portico2.shared.configuration.ConnectionType;
import org.portico2.shared.configuration.LrcConfiguration;
import org.portico2.shared.configuration.RID;
import org.portico2.shared.logging.Log4jConfigurator;
import org.portico2.shared.messaging.MessageContext;
import org.portico2.shared.messaging.MessageSink;
import org.portico2.shared.network.IConnection;

public class LRC
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private boolean isConnected;
	
	// Configuration Data //
	private RID rid;
	private LrcConfiguration configuration;
	
	// Messaging Infrastructure //
	private MessageSink incoming;
	private MessageSink outgoing;
	
	// Network Infrastructure //
	private IConnection connection;
	
	
	// State Information //
	private ISpecHelper specHelper; // contains hlaVersion
	private LRCState state;

	// Callback Processing //
	private Thread immediateCallbackDispatcher;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LRC( ISpecHelper helper )
	{
		this( helper, RID.loadDefaultRid() );
	}
	
	/**
	 * Create a new LRC using the given specification helper. This method will initialize the
	 * contained message sinks and create/initialize the contained IConnection.
	 * 
	 * @param rid        The configuration settings to use for this LRC
	 * @param specHelper Instance of the {@link ISpecHelper} that HLA-version specific code can
	 *                   use to cast down and access arbitrary facilities associated with the
	 *                   implementation of that HLA interface version.
	 */
	public LRC( ISpecHelper helper, RID rid )
	{
		if( rid == null )
			rid = RID.loadDefaultRid();

		// set up whatever else we need to before the handlers are created and initialized
		this.rid = rid;
		this.isConnected = false;
		this.configuration = rid.getLrcConfiguration();
		this.specHelper = helper;
		Log4jConfigurator.activate( this.rid.getLog4jConfiguration() );
		this.logger = LogManager.getFormatterLogger( "portico.lrc" );
		this.logger.info( "Creating new LRC" );
		this.logger.info( "Portico version: "+PorticoConstants.RTI_VERSION );
		this.logger.info( "Interface: "+helper.getHlaVersion() );

		// create the notification manager
//		this.notificationManager = NotificationManager.newNotificationManager();
		
		// the immediate callback processing remains null until turned on explictly
		this.immediateCallbackDispatcher = null;

		// create the LRCState component that has most of the state-holding components inside it
		this.state = new LRCState( this );
		
		// register the state as a Notification handler so that it can take the appropriate
		// actions when the location federate joins and resigned from a federation. We only
		// want to do this once, so we don't put it in initializeLrc() as that will get called
		// when we re-join a new federation. Also, we have to register it this way so the 
		// NotificationManager doesn't try to instantiate it!
//		this.notificationManager.addListener( Priority.LOW, this.state );
		
		// initialize the parts of the LRC that should be re-initialized whenever the
		// federate attached to it resigns and rejoins
		initializeLrc();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Configuration Methods   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * So that the messaging components can be properly reinitialized when a federate resigns from
	 * a federation and then joins another (the same LRC) this method contains all the logic to
	 * reinitialize the appropriate internal LRC components. This will wipe out the existing
	 * message sinks and reconfigure them. The connection will remain untouched.
	 */
	private void initializeLrc() throws JConfigurationException
	{
		this.incoming = new MessageSink( "incoming", logger );
		this.outgoing = new MessageSink( "outgoing", logger );

		// initialize the connection - but only if we haven't done so before
		if( connection == null )
			connection = initializeConnection();
		
		// initialize the messaging framework
		LRCHandlerRegistry.loadHandlers( this );
		
		logger.info( "LRC initialized (HLA version: %s)", specHelper.getHlaVersion() );
	}

	private IConnection initializeConnection()
	{
		ConnectionConfiguration connectionConfiguration = this.configuration.getConnectionConfiguration();
		ConnectionType type = connectionConfiguration.getType();
		IConnection newConnection = null;
		switch( type )
		{
			case JVM:       newConnection = new LrcJvmConnection(); break;
			case MULTICAST: // fall through
			case TCP:       // fall through
			case UDP:       // fall through
			default: 
				throw new JConfigurationException( "Cannot create LRC connection; unsupported type: %s", type );
		}

		newConnection.configure( this, connectionConfiguration );
		return newConnection;
	}
	
	private void initializeMessaging()
	{
		
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isConnected()
	{
		return this.isConnected;
	}

	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;

		// Ask the connection to start
		this.connection.connect();
		
		// Send a connection request message
		Connect message = new Connect();
		this.connection.sendControlRequest( new MessageContext(message) );
		
		//context.awaitResponse( timeout );
	}
	
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;
		
		// Tell the connection to break
		this.connection.disconnect();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Tick and Callback Processing   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The IEEE-1516 and 1516e standards provide facilities to allow the immediate delivery
	 * of callback messages rather than the usual asynchronous/tick delivery mechanism. To
	 * provide support for this, when the mode is enabled the LVCQueue itself will have an
	 * additional thread that will be used to deliver all callbacks immediately, rather than
	 * waiting for tick to be called (although we'll extract callbacks via the same poll()
	 * call to ensure we only release TSO messages at the appropriate time).
	 * <p/>
	 * This call will enable that mode and kick off a separate processing thread.
	 */
	public void enableImmediateCallbackProcessing()
	{
		if( state.isImmediateCallbackDeliveryEnabled() )
			return;
		
		// create the immediate callback delivery processing thread and start it
		this.immediateCallbackDispatcher = new ImmediateCallbackDispatcher();
		this.immediateCallbackDispatcher.start();
		
		// give the dispatch thread just a moment to start
		try{ Thread.sleep( 5 ); } catch( InterruptedException ie ) { /*ignore*/ }
		
		// set the flag on the LRCState to say that we're in this mode now
		state.setImmediateCallbackDelivery( true );
	}
	
	public void disableImmediateCallbackProcessing()
	{
		if( state.isImmediateCallbackDeliveryEnabled() == false )
			return;
		
		// interrupt the callback processing thread and wait for it to stop
		try
		{
			if( this.immediateCallbackDispatcher != null &&
				this.immediateCallbackDispatcher.isAlive() )
			{
				this.immediateCallbackDispatcher.interrupt();
				this.immediateCallbackDispatcher.join();
			}
		}
		catch( InterruptedException ie )
		{
			logger.error( "Received exception while disabling immediate callbacks", ie );
		}
		finally
		{
			// update the state to set the immediate processing flag to off
			this.immediateCallbackDispatcher = null;
			state.setImmediateCallbackDelivery( false );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public Logger getLogger()
	{
		return this.logger;
	}

	public LRCState getState()
	{
		return this.state;
	}
	
	public HLAVersion getHlaVersion()
	{
		return this.specHelper.getHlaVersion();
	}
	
	public IConnection getConnection()
	{
		return this.connection;
	}

	public MessageSink getOutgoingSink()
	{
		return this.outgoing;
	}
	
	public MessageSink getIncomingSink()
	{
		return this.incoming;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	
	///////////////////////////////////////////////////////////////
	///////// Private Class: ImmediateCallbackDispatcher //////////
	///////////////////////////////////////////////////////////////
	/**
	 * This class provides the logic for the immediate callback processing thread. When immediate
	 * callbacks are enabled, this thread will be started and will continually poll the message
	 * queue for available messages, processing them as they are received until the Thread is
	 * interrupted. Immediate callback processing is turned on via the LRC and not enabled at
	 * startup.
	 */
	private class ImmediateCallbackDispatcher extends Thread
	{
		public ImmediateCallbackDispatcher()
		{
			super( "ImmediateCallbackDispatcher" );
			super.setDaemon( true );
		}
		
		public void run()
		{
			logger.debug( "Starting immediate callback delivery processor" );
			
			// Loop continuously until we are interrupted, polling for messages.
			// When we receive one, proces it and move on to the next.
//			while( Thread.interrupted() == false )
//			{
//				try
//				{
//					// If callbacks are currently not enabled, sleep for a bit and come back
//					// You mean just block!? Yes. I do. It's fine. Really. The stated use case
//					// for enabled/disable callbacks it to allow the federate to initiate a
//					// block on callbacks temporarily, so if unblocking takes a brief moment,
//					// it's really not an issue. Relax, tiger.
//					if( state.areCallbacksEnabled() == false )
//					{
//						Thread.sleep( 500 ); // sleep for half a second
//						continue;
//					}
//
//					// if callbacks are enabled, get bizzay processing them
//					PorticoMessage message = state.messageQueue.pollUntilNextMessage();
//					
//					if( message != null )
//					{
//						try
//						{
//							tickProcess( message );
//						}
//						catch( Exception e )
//						{
//							// something went wrong in the callback, log it
//							logger.error( "Problem processing callback message: "+e.getMessage(), e );
//						}
//					}
//				}
//				catch( InterruptedException ie )
//				{
//					break;
//				}
//			}
			
			logger.debug( "Immediate callback delivery processor disabled" );
		}
	}
}
