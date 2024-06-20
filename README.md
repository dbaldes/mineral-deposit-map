# Mineral Deposit Map

This is a toy project that displays mineral deposits on a browsable map.

The [Spring Boot](https://spring.io/projects/spring-boot) application uses [Redis](https://redis.io) to
store and retrieve data about mineral deposits. The Web UI shows deposits on a browsable map by 
[OpenStreetMap](https://www.openstreetmap.org/) using [Leaflet](https://leafletjs.com/).

The data can be imported into Redis from the files provided by the
[Mineral Resources Data System](https://tin.er.usgs.gov/mrds/), using the DepositImportApplication provided
in the project. I'm not entirely sure about the licensing status of the data, so use it at your own discretion.

I found this data set in this great collection of [Free GIS Data](https://freegisdata.rtwilson.com/) by a guy
named Robin Wilson.

## Setup

Start redis, e.g. by running:
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

Download https://tin.er.usgs.gov/mrds/rdbms-tab-all.zip and extract it to a folder, e.g.:
```bash
mkdir mrdsdata && cd mrdsdata
wget https://tin.er.usgs.gov/mrds/rdbms-tab-all.zip
unzip rdbms-tab-all.zip
```

Run the importer tool from the project, e.g.:
```bash
mvn spring-boot:run -Dstart-class=de.throughput.deposit.importer.DepositImporterApplication -Dspring-boot.run.arguments=/path/to/mrdsdata
```

Finally, start the web application:
```bash
mvn spring-boot:run -Dstart-class=de.throughput.deposit.web.DepositWebApplication
```

And navigate to http://localhost:8080/ to view the map.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

