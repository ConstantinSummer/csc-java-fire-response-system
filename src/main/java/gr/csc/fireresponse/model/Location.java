package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/**
 * An immutable place in Greece: a named area inside a {@link Region}, with coordinates.
 * Immutability means a Location can be shared safely between an incident and a station.
 */
public class Location {

    public static final double MIN_LATITUDE = 34.5;
    public static final double MAX_LATITUDE = 42.0;
    public static final double MIN_LONGITUDE = 19.0;
    public static final double MAX_LONGITUDE = 29.9;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final Region region;
    private final String areaName;
    private final double latitude;
    private final double longitude;

    public Location(Region region, String areaName, double latitude, double longitude) {
        Validate.notNull(region, "region");
        this.region = region;
        this.areaName = Validate.notBlank(areaName, "areaName");
        this.latitude = Validate.inRange(latitude, MIN_LATITUDE, MAX_LATITUDE, "latitude");
        this.longitude = Validate.inRange(longitude, MIN_LONGITUDE, MAX_LONGITUDE, "longitude");
    }

    public Region getRegion() {
        return region;
    }

    public String getAreaName() {
        return areaName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /** Great-circle distance to another Location, in kilometres (haversine formula). */
    public double distanceKmTo(Location other) {
        return distanceKmTo(other.latitude, other.longitude);
    }

    /** Overloaded variant: the same distance calculation for raw coordinates. */
    public double distanceKmTo(double otherLatitude, double otherLongitude) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(otherLatitude);
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(otherLongitude - longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return areaName + ", " + region.getDisplayName();
    }
}
