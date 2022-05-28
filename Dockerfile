FROM storm:1.2.4
 
RUN uname -r
RUN apt list --installed 

RUN apt-get update -qq && \
    apt-get install -yq --no-install-recommends \
    	    curl \
	    	 jq \
		    less \
		    	 emacs  maven



RUN mkdir /tracer-crawler && \
    chmod -R a+rx /tracer-crawler                
WORKDIR /tracer-crawler



RUN env
 


COPY portals-crawler/target/stormcapture-0.2.jar /tracer-crawler/tracer-crawler.jar
COPY storm-docker-conf/crawler-conf-docker.yaml /tracer-crawler/crawler-conf-docker.yaml
COPY storm-docker-conf/crawler-docker.flux /tracer-crawler/crawler-docker.flux



#inject seeds and traces to mysql
#CMD storm jar tracer-crawler.jar  gov.lanl.crawler.CrawlTopology -conf crawler-conf-docker.yaml 
#CMD storm jar tracer-crawler.jar   gov.lanl.crawler.SeedInjector /seeds  seedswithtraces.txt   -conf crawler-conf-docker.yaml


RUN chown -R "storm:storm" /tracer-crawler/

#USER storm
