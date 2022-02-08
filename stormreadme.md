 * currently storm library installed at /usr/local 
  
```sh
# nice blog https://vincenzogulisano.com/2015/07/30/5-minutes-storm-installation-guide-single-node-setup/
# minimal steps below to get it working
# download storm and unzip it
$ cd /usr/local
$ wget http://apache.claz.org/storm/apache-storm-1.2.2/apache-storm-1.2.2.tar.gz
$ gzip -d  apache-storm-1.2.2.tar.gz
$ tar -xf apache-storm-1.2.2.tar
# download zookeeper and unzip it
$ sudo wget http://apache.claz.org/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz
$ gzip -d zookeeper-3.4.13.tar.gz
$ sudo tar   -xf zookeeper-3.4.13.tar
$ sudo chown -R  ludab:users zookeeper-3.4.13
$ cd zookeeper-3.4.13/conf
$ cp zoo_sample.cfg zoo.cfg
# Configure ZooKeeper
# Add the following to zookeeper-3.4.13/conf/zoo.cfg
tickTime=2000
dataDir=/data/web/tmp/zookeeper
clientPort=2181
# Start ZooKeeper
zookeeper-3.4.6/bin/zkServer.sh start
# mkdir /data/web/tmp/zookeeper
# mkdir /data/tmp
```
 * For the standalone storm execution instalation of the library is enough, no need to configure  cluster.
