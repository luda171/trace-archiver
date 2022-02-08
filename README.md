# Trace-Crawler  based on StormCrawler see https://github.com/DigitalPebble/storm-crawler/wiki. 
Produces WARC files to be stored in ./warcs/warcstore directory.


## Build the Project:
* go to portals-crawler directory and follow instructions 
* or see docker instructions below
   



## Configure docker-crawler
Open the file ./storm-crawler/crawler-conf-docker.yaml 
```

   
```
* point out to traces config file
``` 
navigationfilters.config.file: "./traces/boundary-filters.json"
  
```


## Compile the trace-crawler

 you must first generate an uberjar:

``` sh
cd ./portals-crawler
$ mvn clean package
```
## Run Topology on Docker
A configuration to run the topologies via docker-compose is provided. 
The file docker-compose.yaml puts every component (Mysql,Apache storm,Chrome Selenium hub) into own container




``` sh
docker-compose build
docker-compose run

```





