package de.throughput.deposit.importer;

import de.throughput.deposit.Constants;
import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.FileReader;

/**
 * Imports mineral resources data into redis.
 * <br />
 * Download https://tin.er.usgs.gov/mrds/rdbms-tab-all.zip and extract it to a directory.
 * Call this program with the path to that directory as the first argument.
 * Wait about a minute.
 */
@SpringBootApplication
public class DepositImporterApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final String FIELD_DEP_ID = "dep_id";
    public static final String FIELD_LATITUDE = "wgs84_lat";
    public static final String FIELD_LONGITUDE = "wgs84_lon";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_LINE = "line";
    public static final String FIELD_COMMOD = "commod";

    public static void main(String[] args) {
        new SpringApplicationBuilder(DepositImporterApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        var gisDataDir = new File(args[0]);
        if (!gisDataDir.isDirectory()) {
            throw new IllegalStateException("specify GIS data dir");
        }

        var fileCoords = new File(gisDataDir, "Coords.txt");
        var fileNames = new File(gisDataDir, "Names.txt");
        var fileCommodity = new File(gisDataDir, "Commodity.txt");

        var csvFormat = CSVFormat.TDF.builder()
                .setHeader()
                .build();

        System.out.println("Importing coordinates...");
        try (var in = new FileReader(fileCoords)) {
            int count = 0;
            for (var csvRecord : csvFormat.parse(in)) {
                String depositId = csvRecord.get(FIELD_DEP_ID);
                double latitude = Double.parseDouble(csvRecord.get(FIELD_LATITUDE));
                double longitude = Double.parseDouble(csvRecord.get(FIELD_LONGITUDE));

                redisTemplate.opsForGeo().add(Constants.REDIS_KEY_COORDS, new Point(longitude, latitude), depositId);
                if (++count % 10000 == 0) {
                    System.out.printf("Imported %d coordinates...%n", count);
                }
            }
            System.out.printf("Imported %d coordinates (done)%n", count);
        }

        // note: there may be multiple Name records per deposit ID.
        // each of the entries has a different "line" number. we only store line 1 which should be the latest one.
        System.out.println("Importing names...");
        try (var in = new FileReader(fileNames)) {
            int count = 0;
            for (var csvRecord : csvFormat.parse(in)) {
                String depositId = csvRecord.get(FIELD_DEP_ID);
                String name = csvRecord.get(FIELD_NAME);
                int line = Integer.parseInt(csvRecord.get(FIELD_LINE));
                if (1 == line) {
                    redisTemplate.opsForHash().put(Constants.REDIS_KEY_NAMES, depositId, name);
                    if (++count % 10000 == 0) {
                        System.out.printf("Imported %d names...%n", count);
                    }
                }
            }
            System.out.printf("Imported %d names (done)%n", count);
        }

        // note: there are often multiple commodities per deposit ID.
        System.out.println("Importing commodities...");
        try (var in = new FileReader(fileCommodity)) {
            int count = 0;
            for (var csvRecord : csvFormat.parse(in)) {
                String depositId = csvRecord.get(FIELD_DEP_ID);
                String commodity = csvRecord.get(FIELD_COMMOD);

                redisTemplate.opsForList().rightPush(Constants.REDIS_KEY_PREFIX_COMMODITIES + depositId, commodity);
                if (++count % 10000 == 0) {
                    System.out.printf("Imported %d commodities...%n", count);
                }
            }
            System.out.printf("Imported %d commodities%n", count);
        }
    }
}

