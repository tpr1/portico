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
package org.portico2.rti.services.sync.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico2.shared.data.sync.SyncPoint;

/**
 * This class manages all the record keeping about synchronization points. It takes care of
 * transitioning them from one state to the next and keeping track of which points have been
 * achieved by which federates. The point also keeps a set of the handles of all federates that
 * have attempted to register it.
 */
public class SyncPointManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HashMap<String,SyncPoint> syncPoints;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SyncPointManager()
	{
		this.syncPoints = new HashMap<String,SyncPoint>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Tries to create and register a restricted synchronization point. If a point with the given
	 * label alrady exists, an exception will be thrown. If one with the same name doesn't exist,
	 * a new one will be created, stored and returned.
	 * 
	 * @return The newly created point if everything is successful
	 * @throws RuntimeException If a point with the same label already exists
	 */
	public synchronized SyncPoint registerPoint( String label,
	                                             byte[] tag,
	                                             Set<Integer> handles,
	                                             int registrant )
		throws RuntimeException
	{
		if( syncPoints.containsKey(label) )
			throw new RuntimeException( "Synchronziation Point already exists: label="+label );

		SyncPoint point = new SyncPoint( label,
		                                 tag,
		                                 handles,
		                                 SyncPoint.Status.ANNOUNCED,
		                                 registrant );
		syncPoints.put( label, point );
		return point;
	}
	
	/**
	 * A federate has received notification of a synchronization point announcement. This method
	 * will create the point locally if it doesn't already exist, or it will update the point with
	 * the given tag and handle information, setting the points status to
	 * {@link SyncPoint.Status#ANNOUNCED}.
	 * 
	 * @param label The label of the sync point that has been announced
	 * @param tag The tag that was provided with the announcement notification
	 * @param handles The set of federate handles involved with the point. A null or empty set
	 *                indicates that this is a federation-wide synchronization point.
	 */
	public synchronized void pointAnnounced( String label,
	                                         byte[] tag,
	                                         Set<Integer> handles,
	                                         int registrant )
	{
		SyncPoint point = syncPoints.get( label );
		if( point == null )
		{
			point = new SyncPoint( label, tag, handles, SyncPoint.Status.ANNOUNCED, registrant );

			syncPoints.put( label, point );
		}
		else
		{
			point.setTag( tag );
			point.setFederates( handles );
			point.setStatus( SyncPoint.Status.ANNOUNCED );
		}
	}

	public synchronized boolean containsPoint( String label )
	{
		return syncPoints.containsKey( label );
	}
	
	public synchronized SyncPoint getPoint( String label )
	{
		return syncPoints.get( label );
	}
	
	public synchronized SyncPoint removePoint( String label )
	{
		return syncPoints.remove( label );
	}
	
	public synchronized Collection<SyncPoint> getAllPoints()
	{
		return syncPoints.values();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Convenience Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return a map of all the sync points that have been achieved by the given federate handle.
	 * The keys for the map are the labels, the values are the tags that were used when the sync
	 * points were announced. This is generally only used for role call information so the tag is
	 * needed in order to allow a point to be announced properly to a local federate if it hasn't
	 * already been so. Note that only federation-wide sync points will be considered. Restricted
	 * points are ignored by this method because a newly joined federate couldn't be part of a
	 * restricted point that had already been registered (as its handle wouldn't have existed yet,
	 * and so it couldn't be in the set of federate handles the point is restricted to).
	 */
	public HashMap<String,byte[]> getAchieved( int federateHandle )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( SyncPoint point : syncPoints.values() )
		{
			if( point.isFederationWide() && point.hasFederateAchieved(federateHandle) )
				map.put( point.getLabel(), point.getTag() );
		}
		
		return map;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( this.syncPoints );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.syncPoints = (HashMap<String,SyncPoint>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
