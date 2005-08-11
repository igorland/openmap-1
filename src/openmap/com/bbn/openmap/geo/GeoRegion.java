/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2001, 2002 All Rights Reserved
 *
 */

package com.bbn.openmap.geo;

/**
 * An arbitrary space described in terms of Geo objects. GeoRegions
 * are assumed to be closed paths representing areas.
 */
public interface GeoRegion extends GeoPath {

    /**
     * Is the Geo inside the region?
     * 
     * @param point
     * @return true if point is inside region.
     */
    boolean isPointInside(Geo point);

    Object getRegionId();

    // ------------------------------
    // Basic Implementation
    // ------------------------------

    public static class Impl extends GeoPath.Impl implements GeoRegion {
        protected Object id = GeoRegion.Impl.this;

        public Impl(Geo[] coords) {
            super(coords);
        }

        /**
         * Create a region of LatLon pairs.
         * 
         * @param lls alternating lat/lon in decimal degrees.
         */
        public Impl(float[] lls) {
            this(lls, true);
        }

        /**
         * Create a region of LatLon pairs.
         * 
         * @param lls alternating lat/lon values.
         * @param isDegrees true if lat/lon are in degrees, false if
         *        in radians.
         */
        public Impl(float[] lls, boolean isDegrees) {
            super(lls, isDegrees);
        }

        public void setRegionId(Object rid) {
            id = rid;
        }

        public Object getRegionId() {
            return id;
        }

        public boolean isSegmentNear(GeoSegment s, double epsilon) {
            return Intersection.isSegmentNearPolyRegion(s,
                    toPointArray(),
                    epsilon);
        }

        public boolean isPointInside(Geo p) {
            return Intersection.isPointInPolygon(p, toPointArray());
        }

        public BoundingCircle getBoundingCircle() {
            if (bc == null) {
                return new BoundingCircle.Impl(this);
            }
            return bc;
        }
    }

}
