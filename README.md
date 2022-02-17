# Trace-Crawler  based on StormCrawler see https://github.com/DigitalPebble/storm-crawler/wiki. 
Produces WARC files to be stored in ./warcs/warcstore directory.


## Build the Project:
* if you do not want to use docker go to the portals-crawler directory and follow instructions 
* or see docker instructions below
* 
## Compile the trace-crawler

 you must first generate an uberjar:

``` sh
cd ./portals-crawler
$ mvn clean package
```   



## Run Topology on Docker
A configuration to run the topologies via docker-compose is provided. 
The file docker-compose.yaml puts every component (Mysql,Apache storm,Warcproxy,Chrome Selenium hub) into  containers.
First we launch all components:



``` sh
docker-compose -f docker-compose.yaml up --build  --remove-orphans
docker-compose run

```
Now we can launch the container tracer-archiver
``` sh
docker-compose run --rm tracer-archiver
```
and in the running container first  topology to load seeds to mysql:
``` sh
tracer-crawler> storm jar tracer-crawler.jar   gov.lanl.crawler.SeedInjector /seeds seedswithtraces.txt   -conf crawler-conf-docker.yaml
```
and in the running container second  topology to run crawler:
``` sh
tracer-crawler> storm jar tracer-crawler.jar  gov.lanl.crawler.CrawlTopology -conf crawler-conf-docker.yaml
```

Let's check whether topology is running:
``` sh
tracer-crawler> storm list
```
To kill the topology
``` sh
tracer-crawler> storm kill SeedInjector -w 10
```
Let's check logs - logs are mapped to host machine at /data
``` sh
ls -la data/supervisor/logs/workers-artifacts/*/*/worker.log
more data/supervisor/logs/workers-artifacts/CrawlTopology-1-1644877391/6700/worker.log
```
to leave the container (exit) and shut down all running containers:
``` sh
docker-compose down
```

to look at the mysql db
``` sh
docker exec -it tracer-db /bin/bash
```
in the contaner
``` sh
mysql -u cache -p 
```
at  the mysql prompt:
``` sh
use portals;
show tables;
select * from urls;
if you want to clean the db:  delete from urls;
```
if you want to bring back url to crawl again  for testing 
``` sh
update urls set status='DISCOVERED',nextfetchdate='2018-01-21 15:41:22' where url='https://wormbase.org/species/c_elegans/gene/WBGene00006604#0-9g-3';
```
## Also the Storm UI on localhost is available and will provide metrics about the running topology.
## Configuration docker-crawler
the  configuration file at  the file ./storm-docker-conf/crawler-conf-docker.yaml 
```

   
```
* default points out to traces config file
``` 
navigationfilters.config.file: "boundary-filters3.json"
  
```
