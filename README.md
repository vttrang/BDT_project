# BIG DATA TECHNOLOGY #

#### Project Name: CORONA Analysis
#### Professor: Mrudula Mukadam
#####Team: Covid-333 
##### Member: 
- Chi Cuong Nguyen - 61111
- Phan Anh Nguyen - 611034
- The Vinh Trang - 611035

## Project Technology
| Name | version  | type  |
| ------- | --- | --- |
| Kafka | 2.0.1 | new install |
| Spark Core | 2.2.0 | upgrade from 1.6.0 |

## Project overview
<p align="center">
  <img width="1444" height="596" src="https://i.imgur.com/llJiv54.png">
</p>

# How to installation project
### Install kafka

1. Download package

	```sh
	$ cd /etc/yum.repos.d 
	$ sudo wget http://archive.cloudera.com/kafka/redhat/6/x86_64/kafka/
	```

	- install wget if your machine did not have wget 
	```sh
	$ sudo yum install wget
	```

2. Install kafka
	```sh
	$ sudo yum clean all
	$ sudo yum install kafka
	$ sudo yum install kafka-server
	```
3. Start Kafka service

	```sh
	$ sudo service kafka-server start
	```
	- Verify kafka installation
		```sh
		$ sudo jps
		```
		```sh
		[cloudera@quickstart bin]$ sudo jps
...
2189 QuorumPeerMain
2574 Kafka
...
		```
		
4. Create TOPIC
	```sh
	cd /usr/lib/kafka/bin
	./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic ${topic_name}
	```

5. Start Producer for ${topic_name}
	```sh
	cd /usr/lib/kafka/bin 
	./kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic_name}
	```
6. Start consumer for ${topic_name}
	```sh
	$ cd /usr/lib/kafka/bin 
	$ ./kafka-console-consumer.sh --zookeeper localhost:2181 -topic test_topic --from-beginnin
	```
