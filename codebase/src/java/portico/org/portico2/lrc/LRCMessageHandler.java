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

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.Space;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.shared.PorticoConstants;
import org.portico2.shared.messaging.IMessageHandler;
import org.portico2.shared.messaging.MessageContext;
import org.portico2.shared.network.IConnection;

public abstract class LRCMessageHandler implements IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected LRC lrc;
	protected LRCState lrcState;
	protected Logger logger;
	protected IConnection connection;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return The class name of this handler (final part, not qualified).
	 */
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Extracts some key information from the given properties, such as a referce to the
	 * {@link LRC} that it is contained in. Caches a number of important objects in protected
	 * member variable so that the handlers can just refer to them without needing any
	 * special setup. 
	 */
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		if( properties == null )
			throw new JConfigurationException( "Cannot initialize LRC from null property set" );

		this.lrc = (LRC)properties.get( IMessageHandler.KEY_LRC );
		this.lrcState = lrc.getState();
		this.logger = lrc.getLogger();
		this.connection = lrc.getConnection();
	}
	
	/**
	 * A message has been received for processing. The request and/or response is contained within
	 * the given {@link MessageContext} object. Take appropriate action and throw any exception
	 * from the compatibility library you need to.
	 * 
	 * @param context The request and/or response holder
	 * @throws JException If there is a problem processing the message
	 */
	@Override
	public abstract void process( MessageContext context ) throws JException;
	

	//////////////////////////////////////////////////////////////////////////////////////////
	///  FOM Helper Methods   ////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search the FOM for the class with the given class handle and return it. Throw an exception
	 * if the class cannot be found.
	 */
	protected OCMetadata getObjectClass( int classHandle ) throws JObjectClassNotDefined
	{
		OCMetadata theClass = lrcState.getFOM().getObjectClass( classHandle );
		if( theClass == null )
			throw new JObjectClassNotDefined( "Class [" + classHandle + "] not found in FOM" );
		else
			return theClass;
	}
	
	/**
	 * Quick way to write {@link LRCState#getFOM()}
	 */
	protected ObjectModel fom()
	{
		return lrcState.getFOM();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	///  Handle and Name Methods  ///////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Convenience method that just calls <code>lrcState.getFederateHandle()</code>.
	 */
	protected int federateHandle()
	{
		return lrcState.getFederateHandle();
	}

	/**
	 * Convenience method that calls {@link LRCState#getKnownFederate(int)} passing the given
	 * federate handle. If there is no known federate with the given handle, null is returned.
	 * 
	 * @param federateHandle The federate handle to fetch the name for.
	 * @return The name of the federate with the given handle if it is known, or null if it isn't
	 */
	protected String federateName( int federateHandle )
	{
//		Federate federate = lrcState.getKnownFederate( federateHandle );
//		if( federate == null )
//		{
//			if( federateHandle == PorticoConstants.RTI_HANDLE )
//				return "RTI";
//			else
//				return null;
//		}
//		else
//		{
//			return federate.getFederateName();
//		}
		return "unknown";
	}
	
	/**
	 * Shortcut to get the name of the local federate.
	 */
	protected String federateName()
	{
		return lrcState.getFederateName();
	}
	
	/**
	 * THis method returns a string that contains either the name or the handle of the identified
	 * federate. If logging with names is enabled for federates (see the documentation for
	 * {@link PorticoConstants#isPrintHandlesForFederates()}), the string will contain the federate
	 * name, otherwise it will contain the federate handle.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker( int federateHandle )
	{
		if( PorticoConstants.isPrintHandlesForFederates() )
			return ""+federateHandle;
			//return ""+PorticoConstants.isPrintHandlesForFederates();
		else
			return federateName(federateHandle);
	}

	/**
	 * This is the same as {@link #moniker(int)} except that it assumed the local federate.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker()
	{
		return moniker( federateHandle() );
	}

	/**
	 * Returns the result of calling {@link #moniker(int)} and passing the source federate of the
	 * provided message.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker( PorticoMessage message )
	{
		return moniker( message.getSourceFederate() );
	}

	/**
	 * Returns a string that represents the object instance. If logging with names is enabled
	 * for object instances, the name of the instance will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjects()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String objectMoniker( int objectHandle )
	{
		if( PorticoConstants.isPrintHandlesForObjects() )
			return ""+objectHandle;
		else
			return "unknown"; // TODO FIXME
//			return repository.findObjectName( objectHandle );
	}
	
	/**
	 * Returns result of calling {@link #objectMoniker(int)} passing the handle of given instance.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String objectMoniker( OCInstance instance )
	{
		return objectMoniker( instance.getHandle() );
	}
	
	/**
	 * Returns a string that represents the object class. If logging with names is enabled
	 * for object classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String ocMoniker( int objectClassHandle )
	{
		if( PorticoConstants.isPrintHandlesForObjectClass() )
		{
			return ""+objectClassHandle;
		}
		else
		{
			String name = fom().getObjectClassName( objectClassHandle );
			if( name == null )
				name = objectClassHandle+" <unknown>";
			return name;
		}
	}
	
	/**
	 * Returns a string that represents the object class. If logging with names is enabled
	 * for object classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String ocMoniker( OCMetadata objectClass )
	{
		return ocMoniker( objectClass.getHandle() );
	}

	/**
	 * Returns a string that represents all the attribute handles. If logging with names is
	 * enabled for attributes, the sting will contain the attribute names rather than the handles
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String acMoniker( Set<Integer> attributeHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForAttributeClass() )
		{
			for( int handle : attributeHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : attributeHandles )
				attributes.add( fom().findAttributeName(handle) );
		}

		return attributes.toString();
	}
	
	/**
	 * Returns a string that represents all the attribute handles. If logging with names is
	 * enabled for attributes, the sting will contain the attribute names rather than the handles
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String acMoniker( int... attributeHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForAttributeClass() )
		{
			for( int handle : attributeHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : attributeHandles )
				attributes.add( fom().findAttributeName(handle) );
		}

		return attributes.toString();
	}

	/**
	 * Returns a printable string for the given set of attributes that also includes the
	 * size of the values provided in brackets after the name. For example [name(14b),other(4b)].
	 * <p/>
	 * The names are only substituted for handles if set to do so in the FOM.
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 */
	protected String acMonikerWithSizes( Map<Integer,byte[]> attributes )
	{
		ArrayList<String> printable = new ArrayList<>();
		final boolean printHandles = PorticoConstants.isPrintHandlesForAttributeClass();
		for( Integer handle : attributes.keySet() )
		{
			int size = attributes.get(handle).length;
			if( printHandles )
				printable.add( handle+"("+size+"b)" );
			else
				printable.add( fom().findAttributeName(handle)+"("+size+"b)" );
		}
		
		return printable.toString();
	}
	
	/**
	 * Returns a printable string for the given set of parameters that also includes the
	 * size of the values provided in brackets after the name. For example [name(14b),other(4b)].
	 * <p/>
	 * The names are only substituted for handles if set to do so in the FOM.
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 */
	protected String pcMonikerWithSizes( Map<Integer,byte[]> parameters )
	{
		ArrayList<String> printable = new ArrayList<>();
		final boolean printHandles = PorticoConstants.isPrintHandlesForParameterClass();
		for( Integer handle : parameters.keySet() )
		{
			int size = parameters.get(handle).length;
			if( printHandles )
				printable.add( handle+"("+size+"b)" );
			else
				printable.add( fom().findParameterName(handle)+"("+size+"b)" );
		}

		return printable.toString();
	}
	
	/**
	 * Returns a string that represents the interaction class. If logging with names is enabled
	 * for interaction classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForInteractionClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String icMoniker( int interactionClassHandle )
	{
		if( PorticoConstants.isPrintHandlesForInteractionClass() )
		{
			return ""+interactionClassHandle;
		}
		else
		{
			String name = fom().getInteractionClassName( interactionClassHandle );
			if( name == null )
				name = interactionClassHandle+" <unknown>";
			return name;
		}
	}

	/**
	 * Returns a string that represents all the parameters. If logging with names is enabled
	 * for parameter classes, the names of the parameters will be in the returned string, if not,
	 * the handles will be in the returned string. See
	 * {@link PorticoConstants#isPrintHandlesForParameterClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String pcMoniker( Set<Integer> parameterHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForParameterClass() )
		{
			for( int handle : parameterHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : parameterHandles )
				attributes.add( fom().findParameterName(handle) );
		}

		return attributes.toString();
	}

	/**
	 * Returns a string that represents the space. If logging with names is enabled
	 * for spaces, the name of the space will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String spaceMoniker( int spaceHandle )
	{
		if( PorticoConstants.isPrintHandlesForSpaces() )
		{
			return ""+spaceHandle;
		}
		else
		{
			Space space = fom().getSpace( spaceHandle );
			if( space == null )
				return spaceHandle+" <unknown>";
			else
				return space.getName();
		}
	}
	
	/**
	 * Returns a string that represents the dimension. If logging with names is enabled
	 * for dimensions, the name of the dimsneion will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForDimensions()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String dimensionMoniker( int dimensionHandle )
	{
		if( PorticoConstants.isPrintHandlesForDimensions() )
			return ""+dimensionHandle;
		
		for( Space space : fom().getAllSpaces() )
		{
			if( space.hasDimension(dimensionHandle) )
				return space.getDimension(dimensionHandle).getName();
		}
		
		return dimensionHandle+" <unknown>";
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	///  General Utility Methods   ///////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	protected void checkForErrorResponse( MessageContext context ) throws JException
	{
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
