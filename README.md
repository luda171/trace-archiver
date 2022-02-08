# Crawler for portals  based on StormCrawler. 
Produces WARC files to be stored in ./warcs/warcstore directory.

## Prerequisites:
* Install Mysql ~ 5.7.23
* Install Apache Storm >= 1.2.2
    * https://storm.apache.org/downloads.html
    * https://vincenzogulisano.com/2015/07/30/5-minutes-storm-installation-guide-single-node-setup/
* Install Docker    
* Install Warcprox
    * https://github.com/internetarchive/warcprox
  

To setup the crawler, please follow the steps below in order:

## Create user/ table  in mysql using 

```sh
./mysql/tablesetup.sql
```
## Selenium-Docker Browsers

The Dockerfile to create the browser containers is in the `docker-selenium` directory of the source. To build and start the browser container,

```sh
$ cd ./docker-selenium

# building the container
$ sudo docker build -t lanlproto/selenium-chrome .

# starting the browser
$ sudo docker run -d -p 4444:4444  --shm-size 8G lanlproto/selenium-chrome
```
## Check that  warcprox can be started
For docker instances, the hostname will have to be the IP address of the host as seen by the container.
```sh
$ cd path/to/warc/storage/location
$ warcprox -b $(sudo docker inspect --format "{{ .NetworkSettings.Gateway }}" $(sudo docker ps -ql)) -p 8080 --certs-dir certs -d warcs -g md5 -v --trace  -s 2000000000 
$ ps aux |grep warcprox
$ kill -9  <pid>
```

## Configure portals-crawler
Open the file ./crawler-conf.yaml in an editor and fill in the values:

* Add the warcprox port and domain name in storm-crawler. The host name of the proxy should be the same as the host name provided to `warcprox` above. i.e, the output of the command:
```sh
$ sudo docker inspect --format "{{ .NetworkSettings.Gateway }}" $(sudo docker ps -ql)
```
    Eg:
      - `http.proxy.host: 172.17.0.1`
      - `http.proxy.port: 8080`
```
The program will use 5 ports from http.proxy.port to http.proxy.port +5, make sure they are free.
* The browser running as a docker container will be listening in an IP address and port for requests from selenium. So, this information will have to be entered in the property `selenium.addresses`. The URL will be of the form `http://<container-ip>:<container-port>/wd/hub`.
The container IP can be obtained by executing the command:
```sh
$ sudo docker inspect --format "{{ .NetworkSettings.IPAddress }}" $(sudo docker ps -ql)
```
The container port is the port number that was used to start the container in the command above (4444).
Eg: `selenium.addresses: "http://172.17.0.2:4444/wd/hub"`

* Change mysql parameters
``` 
 mysql.url: "jdbc:mysql://localhost:3306/crawl?autoReconnect=true"
 mysql.table: "urls"
 mysql.user: "cache"
 mysql.password: "plentyPl3nty"
```
 
* This metadata.persist parameters will be stored in mysqldb in metadata column
```
  metadata.persist:
   - warcs
   - event
   - trace
   - discoveryDate
   - filter
  metadata.transfer:
   - event
   - trace
   
```
* point out to traces config file
``` 
navigationfilters.config.file: "./traces/boundary-filters.json"
  
```


## Run the crawler

With Storm installed, you must first generate an uberjar:

``` sh
$ mvn clean package
```

Inject seeds to the mysql table.


before submitting the topology using the storm command:

``` sh
storm jar target/stormcrawler-0.1.jar gov.lanl.crawler.CrawlTopology -conf crawler-conf.yaml -local 
```

or

run crawler with flux to use flexible topology (described in flux.conf) : 
```sh
 nohup storm jar target/stormcrawler-0.1.jar  -Djava.io.tmpdir=/data/tmp  org.apache.storm.flux.Flux    crawler.flux -s 10000000000 > storm.txt &
```
## To stop  crawler 

```sh
 ps aux|grep flux
 kill -9  <pid>
```
check also that no hanging sessions of warcprox, if crawler killed 
```sh
ps aux |grep warcprox
ludab     65245 38.0  0.0 5506560 40728 pts/5   Sl   19:53   0:00 /usr/bin/python2.7 /usr/local/bin/warcprox -b 172.17.0.1 -p 8072 --certs-dir certs -d /data/web/warcs/warcstore8072 -g md5 -v --trace -s 3000000000 --dedup-db-file=/dev/null --stats-db-file=/dev/null
ludab     65322  0.0  0.0 110512  2264 pts/5    S+   19:53   0:00 grep --color=auto warcprox
```

to be on the save side also clean tmp directories - 
to ensure no half cooked files left (sometimes permissions are broke if you delete directories and crawler restarted by different user).
 
```sh 
  check that no files in  ./warcs/warcstore80*
  
```



