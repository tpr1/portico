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
package org.portico.lrc.services.pubsub.msg;

import java.util.HashSet;

import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.shared.messaging.MessageType;

public class SubscribeObjectClass extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private HashSet<Integer> attributes;
	private boolean passive;
	private int regionToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public SubscribeObjectClass( int classHandle, HashSet<Integer> attributes )
	{
		this.classHandle = classHandle;
		this.attributes = attributes;
		this.passive = false;
		this.regionToken = PorticoConstants.NULL_HANDLE;
	}
	
	public SubscribeObjectClass( int classHandle, HashSet<Integer> attributes, boolean passive )
	{
		this( classHandle, attributes );
		this.passive = passive;
	}

	public SubscribeObjectClass( int classHandle,
	                             HashSet<Integer> attributes,
	                             boolean passive,
	                             int regionToken )
	{
		this( classHandle, attributes, passive );
		this.regionToken = regionToken;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override 
	public MessageType getType()
	{
		return MessageType.SubscribeObjectClass;
	}

	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public void setClassHandle( int classHandle )
	{
		this.classHandle = classHandle;
	}
	
	public HashSet<Integer> getAttributes()
	{
		return this.attributes;
	}
	
	public void setAttributes( HashSet<Integer> attributes )
	{
		this.attributes = attributes;
	}
	
	public boolean isPassive()
	{
		return this.passive;
	}
	
	public void setPassive( boolean passive )
	{
		this.passive = passive;
	}
	
	public int getRegionToken()
	{
		return this.regionToken;
	}
	
	public void setRegionToken( int regionToken )
	{
		this.regionToken = regionToken;
	}
	
	public boolean usesDdm()
	{
		return this.regionToken != PorticoConstants.NULL_HANDLE;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
