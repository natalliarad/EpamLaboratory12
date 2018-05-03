package com.natallia.radaman.epamlabtrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A class for processing the coordinates which received from the service.
 */
public class RouteList {
    private static RouteList workInstance = null;

    private List<RouteLocation> places;

    private RouteList() {
        initPlaceList();
    }

    public static RouteList getWorkInstance() {
        if (workInstance == null) {
            workInstance = new RouteList();
            return workInstance;
        } else
            return workInstance;
    }

    public List<RouteLocation> getPlaces() {
        return places;
    }

    private void initPlaceList() {
        places = new ArrayList<>();
    }

    public static String distanceOfPath(List<RouteLocation> places) {
        Float distance = 0f;
        for (RouteLocation location : places) {
            distance = +Float.parseFloat(location.getDistance());
        }
        return "Your route distance is " + distance;
    }

    public static String timeOfTheStartPath(List<RouteLocation> places) {
        return "Your route started at " + places.get(0).getTime() + ".";
    }

    public static String timeOfTheEndPath(List<RouteLocation> places) {
        return "Your route ends at " + places.get(places.size() - 1).getTime() + ".";
    }

    public static String timeOfThePath(List<RouteLocation> places) {

        return "The duration of the route is " + places.get(places.size() - 1).getTime() + ".";
    }

    public static long getDateDiff(String date1, String date2, TimeUnit timeUnit) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateEnd = dateFormat.parse(date2);
            Date dateStart = dateFormat.parse(date1);
            long millis = dateEnd.getTime() - dateStart.getTime();
            return timeUnit.convert(millis, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
