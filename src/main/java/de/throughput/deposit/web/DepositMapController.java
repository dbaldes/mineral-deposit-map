package de.throughput.deposit.web;

import de.throughput.deposit.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
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
        double radius = Math.max(Math.abs(latMax - latMin), Math.abs(lonMax - lonMin)) * 111;

        // Fetch points from Redis
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> rawPoints = redisTemplate.opsForGeo()
                .radius(Constants.REDIS_KEY_COORDS, new Circle(new Point(midLon, midLat), new Distance(radius, Metrics.KILOMETERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates())
                .getContent();

        List<Map<String, Object>> points = new ArrayList<>();
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

        return points;
    }
}
