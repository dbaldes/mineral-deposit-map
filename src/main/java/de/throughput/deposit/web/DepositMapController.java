package de.throughput.deposit.web;

import de.throughput.deposit.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads points from redis to show on the map.
 */
@RestController
public class DepositMapController {

    private static final double EARTH_RADIUS_KM = 6371;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/api/points")
    public List<Map<String, Object>> getPoints(@RequestParam String bounds) {
        String[] bbox = bounds.split(",");
        double lonMin = Double.parseDouble(bbox[0]);
        double latMin = Double.parseDouble(bbox[1]);
        double lonMax = Double.parseDouble(bbox[2]);
        double latMax = Double.parseDouble(bbox[3]);

        double midLat = (latMin + latMax) / 2;
        double midLon = (lonMin + lonMax) / 2;
        double radius = haversine(midLat, midLon, latMax, lonMax);

        // Fetch points from Redis
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(Constants.REDIS_KEY_COORDS, new Circle(new Point(midLon, midLat), new Distance(radius, Metrics.KILOMETERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates());

        List<Map<String, Object>> points = new ArrayList<>();
        if (results != null) {
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> rawPoints = results.getContent();
            for (var rawPoint : rawPoints) {
                String key = rawPoint.getContent().getName();
                String name = (String) redisTemplate.opsForHash().get(Constants.REDIS_KEY_NAMES, key);
                if (name == null) {
                    name = key;
                }
                List<?> commodities = redisTemplate.opsForList().range(Constants.REDIS_KEY_PREFIX_COMMODITIES + key, 0, -1);
                Map<String, Object> pointData = new HashMap<>();
                pointData.put("name", name);
                pointData.put("coordinates", new double[] {rawPoint.getContent().getPoint().getX(), rawPoint.getContent().getPoint().getY()});
                pointData.put("commodities", commodities);

                points.add(pointData);
            }
        }
        return points;
    }

    /**
     * Calculates the approximate distance between two coordinates in kilometers.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // calculate the distance
        return EARTH_RADIUS_KM * c;
    }
}
