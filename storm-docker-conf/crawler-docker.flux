name: "tracer-crawler"

includes:
    - resource: true
      file: "/crawler-default.yaml"
      override: false

    - resource: false
      file: "crawler-conf-docker.yaml"
      override: true

spouts:
   # - id: "ispout"
   #   className: "gov.lanl.crawler.SQLSpoutInput"
     # parallelism: 1
   #  constructorArgs:
   #   - [ "https://github.com/mementoweb/node-solid-server/"]

  - id: "spout"
    className: "gov.lanl.crawler.core.SQLSpoutInput"
    parallelism: 1
 #   constructorArgs:
 #     - [ "https://www.slideshare.net/hvdsomp/paul-evan-peters-lecture","https://github.com/mementoweb/node-solid-server/"]

bolts:
  - id: "partitioner"
    className: "com.digitalpebble.stormcrawler.bolt.URLPartitionerBolt"
    parallelism: 1
  - id: "fetcher"
    className: "com.digitalpebble.stormcrawler.bolt.FetcherBolt"
    parallelism: 1
  - id: "sitemap"
    className: "com.digitalpebble.stormcrawler.bolt.SiteMapParserBolt"
    parallelism: 1
  - id: "parse"
    className: "com.digitalpebble.stormcrawler.bolt.JSoupParserBolt"
    parallelism: 1
  - id: "index"
    className: "com.digitalpebble.stormcrawler.indexing.DummyIndexer"
    parallelism: 1
  - id: "status"
    #className: "com.digitalpebble.stormcrawler.persistence.StdOutStatusUpdater"
    #className: "com.digitalpebble.stormcrawler.persistence.MemoryStatusUpdater"
    className:  "gov.lanl.crawler.core.StatusUpdaterBolt"
    #className:  "com.digitalpebble.stormcrawler.sql.StatusUpdaterBolt"              
    parallelism: 1

streams:
  - from: "spout"
    to: "partitioner"
    grouping:
      type: SHUFFLE

  - from: "partitioner"
    to: "fetcher"
    grouping:
      type: FIELDS
      args: ["key"]

  - from: "fetcher"
    to: "sitemap"
    grouping:
      type: LOCAL_OR_SHUFFLE

  - from: "sitemap"
    to: "parse"
    grouping:
      type: LOCAL_OR_SHUFFLE

  - from: "parse"
    to: "index"
    grouping:
      type: LOCAL_OR_SHUFFLE

  - from: "fetcher"
    to: "status"
    grouping:
      type: FIELDS
      args: ["url"]
      streamId: "status"

  - from: "sitemap"
    to: "status"
    grouping:
      type: FIELDS
      args: ["url"]
      streamId: "status"

  - from: "parse"
    to: "status"
    grouping:
      type: FIELDS
      args: ["url"]
      streamId: "status"

  - from: "index"
    to: "status"
    grouping:
      type: FIELDS
      args: ["url"]
      streamId: "status"
