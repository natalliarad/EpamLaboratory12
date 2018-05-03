package com.natallia.radaman.epamlabtrack;

/**
 * Class for the data model, including coordinates, time stamp and distance of the path.
 */
public class RouteLocation {
    private String time;
    private String lat;
    private String lan;
    private String distance;

    public RouteLocation() {
    }

    public RouteLocation(String time, String lat, String lan, String distance) {
        this.time = time;
        this.lat = lat;
        this.lan = lan;
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
