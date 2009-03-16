//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.time;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TemporalSupport object is intended to provide rudimentary support for
 * choosing the right Temporal object for a given time. A list of TemporalRecord
 * objects are managed by this support object and given a time, it will return
 * the applicable TemporalRecord for that time. The updateForTemporalRecord
 * method allows this object to be extended to keep track of what happens to an
 * object from the start of time, so that an object's status can be observed and
 * calculated based on everything that might have changed in the TemporalRecord
 * list before a given time.
 */
public class TemporalSupport {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.omGraphics.time.TemporalSupport");

    protected TreeSet<TemporalRecord> temporals;

    /**
     * 
     * @param time in milliseconds, offset from beginning time of all other
     *        temporal objects.
     * @param interpolate flag to signal that the returned Temporal object
     *        should be interpolated (in whatever way needed) if the time falls
     *        between Temporal objects.
     */
    public TemporalRecord getPosition(long time, boolean interpolate) {
        TemporalRecord previous = null;
        TemporalRecord next = null;

        // Find out where the timestamp is in relation to the reported
        // positions
        synchronized (temporals) {

            Iterator<TemporalRecord> it = iterator();
            while (it.hasNext()) {
                TemporalRecord temporal = it.next();
                long recTimeStamp = temporal.getTime();

                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("evaluating: " + temporal + " vs " + time);
                }

                if (recTimeStamp < time) {
                    previous = temporal;
                    updateForTemporal(time, temporal);
                } else if (recTimeStamp > time) {
                    next = temporal;
                    break;
                } else {
                    // Hit a time right at a position.
                    updateForTemporal(time, temporal);
                    return temporal;
                }
            }
        }

        TemporalRecord pos = null;

        // OK, now's the opportunity to leave if
        // interpolation is not wanted.

        if (previous != null && !interpolate) {
            return previous;
        } else if (previous == null) {
            // time is before MissionFeature is placed.
            // Don't want to set pos here, should be null to set
            // visibility properly.
            if (next != null) {
                // Just for fun, put location where it will be. We
                // still want to get here if interpolation is not
                // wanted if the current time is before the first
                // position report.
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("premature time, invisible: "
                            + next.toString());
                }
                // We're displaying this position before we're
                // supposed to know it's there.
                // pos = createPosition(time, next);

            } else {
                // no timestamps
                // Returning null Position
            }

        } else if (next == null) {

            // time is after last temporal

            // We're displaying this position after the last position
            // report, assuming that the object is just going to sit
            // there.
            pos = previous;

        } else {

            // Need to interpolate between the two, previous and next.
            // This may not be exact, but it's close.

            pos = interpolate(time, previous, next);
        }

        return pos;
    }

    /**
     * Override this method to use the TemporalRecord's contents to affect the
     * status of whatever you like.
     * 
     * @param time millisecond offset, the current time.
     * @param temporal record that reflects something that has happened.
     */
    protected void updateForTemporal(long time, TemporalRecord temporal) {

    }

    /**
     * Just returns the TemporalRecord that is closes to the current time.
     * Assumes neither previous or next are null.
     * 
     * @param time the current time.
     * @param previous TemporalRecord that occurred before current time.
     * @param next TemporalRecord that occurred after current time.
     * @return closest one.
     */
    protected TemporalRecord interpolate(long time, TemporalRecord previous,
                                         TemporalRecord next) {
        long midTime = previous.getTime()
                + (next.getTime() - previous.getTime()) / 2l;

        if (time < midTime) {
            return previous;
        }

        return next;
    }

    public TreeSet<TemporalRecord> getTemporals() {
        synchronized (temporals) {
            if (temporals == null) {
                temporals = new TreeSet<TemporalRecord>(new TemporalRecordComparator());
            }
            return temporals;
        }
    }

    public void setTemporals(TreeSet<TemporalRecord> temporals) {
        this.temporals = temporals;
    }

    public Iterator<TemporalRecord> iterator() {
        return temporals.iterator();
    }

    public void add(TemporalRecord tr) {
        getTemporals().add(tr);
    }

    /**
     * Return true if the TemporalRecord was contained in the list.
     * 
     * @param tr
     * @return
     */
    public boolean remove(TemporalRecord tr) {
        return getTemporals().remove(tr);
    }

    public void clear() {
        temporals.clear();
    }
}
