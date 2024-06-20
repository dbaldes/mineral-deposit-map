# Mineral Deposit Map

This is a toy project that displays mineral deposits on a browsable map.

The [Spring Boot](https://spring.io/projects/spring-boot) application uses [Redis](https://redis.io) to
store and retrieve data about mineral deposits. The Web UI shows deposits on a browsable map by 
[OpenStreetMap](https://www.openstreetmap.org/) using [Leaflet](https://leafletjs.com/).

The data can be imported into Redis from the files provided by the
[Mineral Resources Data System](http://tin.er.usgs.gov/mrds/), using the DepositImportApplication provided
in the project. I'm not entirely sure about the licensing status of the data, so use it at your own discretion.

I found this data set in this great collection of [Free GIS Data](https://freegisdata.rtwilson.com/) by a guy
named Robin Wilson.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

