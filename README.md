# BIG DATA TECHNOLOGY #

#### Project Name: CORONA Analysis
#### Professor: Mrudula Mukadam
##### Team: Covid-333 
##### Member: 
- Chi Cuong Nguyen - 61111
- Phan Anh Nguyen - 611034
- The Vinh Trang - 611035

## Project Technology
| Name | version  | type  |
| ------- | --- | --- |
| Kafka | 2.0.1 | new install |
| Spark Core | 2.2.0 | upgrade from 1.6.0 |
| Elasticsearch | 6.8.8 | new install |
| Kibana | 6.8.8 | new install|

## Project overview
<p align="center">
  <img width="1444" height="596" src="https://i.imgur.com/llJiv54.png">
</p>

## HBase schema
<p align="center">
  <img src="https://i.imgur.com/qPlSwnn.png">
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
		```ssh 
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
	$ ./kafka-console-consumer.sh --zookeeper localhost:2181 -topic ${topic_name} --from-beginnin
	```
### Import data
1. Prepares data (*.csv) // Reads per file by date
	
2. Prepares dataRun.sh
	```sh 
	#!/bin/ksh
		file="/home/cloudera/Downloads/covid_dataset.csv"
		count=1
		delay=5
		while IFS= read line
		do
			# display $line or do something with $line
			if [ $count -ne 1 ]; then
				echo "$line"
				modulo=$(( $count  % 11 ))
				if [ $modulo -eq 0 ]; then
					sleep $delay
				fi
					#count=$[$count +1]		
			fi
			count=$[$count+1]
		done <"$file"
	```
3. Run
	```sh 
	sudo sh /home/cloudera/Downloads/dataRun.sh | /usr/lib/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic-name}
	```

### How to install Spark 2 in Cloudera Manager (CM)
1. Startup Cloudera Manager (CM)
    - Once the VM starts up, navigate to the Desktop and Execute the “Launch Cloudera Express” script.
    - Once complete, you should now be able to view the Cloudera Manager by opening up your web browser (within the VM) and navigating to:
    ```
    http://quickstart.cloudera:7180
    ```
    Default Credentials: cloudera/cloudera

2. Configure CM to use Parcels
    - Navigate to the Desktop and Execute the “Migrate to Parcels” script.
    - You can validate that CM is now using parcels by logging into the Cloudera Manager Web UI. Right next to the cluster name, it should say: (CDH x.x.x, Parcels)
    - You will need to restart all the services on the cluster after step-2. You can do this by: Going to the Cloudera Manager Web UI, click on the button next to the Cluster Name and click Start.

3. Select the version of Spark 2 you want to install
    - Navigate here to get a full list of the Spark versions that are available:
        ```
        https://docs.cloudera.com/documentation/spark2/latest/topics/spark2_packaging.html
        ```
    - Copy the Custom Service Descriptor (CSD) URL (To be referred to as CSD_URL in next sections)

4. Install Spark 2 CSD
    - Open a Command Line Terminal
    - Login as Root
    ```sh
    $ sudo su
    ```
    - Navigate to the CSD Directory
    ```sh
    $ cd /opt/cloudera/csd
    ```
    - Download the CSD (Replace CSD_URL with the URL you copied in previous sections)
    ```sh
    $ wget CSD_URL
    ```
    - Set Permissions and Ownership
    ```sh
    $ chown cloudera-scm:cloudera-scm SPARK2_ON_YARN-x.x.x.clouderax.jar
    $ chmod 644 SPARK2_ON_YARN-x.x.x.clouderax.jar
    ```
    - Restart CM Services
    ```sh
    $ service cloudera-scm-server restart
    ```
    - Login to the Cloudera Manager Web UI
    - Restart the Cloudera Management Service
        - Select Clusters > Cloudera Management Service
        - Select Actions > Restart
    - Restart the Cluster Services
        - Select Clusters > Cloudera QuickStart
        - Select Actions > Restart

5. Install Spark 2 Parcel
    - Login to the Cloudera Manager Web UI
    - Navigate to Hosts > Parcels
    - Locate the SPARK2 parcel from the list
    - Under Actions, click Download and wait for it to download
    - Under Actions, click Distribute and wait for it to be distributed
    - Under Actions, click Activate and wait for it to be activated

6. Install Spark 2 Service
    - Login to the Cloudera Manager Web UI
    - Click on the button next to the Cluster Name and select “Add Service”
    - Select “Spark 2” and click “Continue”
    - Select whichever set of dependencies you would like and click “Continue”
    - Select the one instance available as the **History Server** and the **Gateway** and click “Continue”
    - Leave the default configurations as is and click “Continue”
    - The service will now be added and then you will be taken back to the CM home
    - Click on the blue button next to the Spark 2 service and click “Restart Stale Services”
    - Ensure the “Re-deploy client configuration” is checked and click “Restart Now”

    7. Testing
    ```sh
    MASTER=yarn /opt/cloudera/parcels/SPARK2/lib/spark2/bin/run-example SparkPi 100
    ```

    Ref: https://blog.clairvoyantsoft.com/installing-spark2-on-clouderas-quickstart-vm-bbf0db5fb3a9
	
### Install Elasticsearch 
1. Install GPG key for the elasticsearch rpm packages.
	```sh
	sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
	```
2. Create yum repository file for the elasticsearch
	```sh
	sudo vi /etc/yum.repos.d/elasticsearch.repo
	```
	Add below content
	```
	[elasticsearch-6.x]
	name=Elasticsearch repository for 6.x packages
	baseurl=https://artifacts.elastic.co/packages/6.x/yum
	gpgcheck=1
	gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
	enabled=1
	autorefresh=1
	type=rpm-md
	```
3. Install Elasticsearch
	```sh
	sudo yum install elasticsearch
	```
4. Config Elasticsearch is localhost:9200
	```sh
	sudo vi /etc/elasticsearch/elasticsearch.yml
	```
	Add config `network.host`
	```sh
	network.host: localhost
	```
5. Activate Elasticsearch as a service
	```sh
	sudo chkconfig --add elasticsearch
	sudo chkconfig elasticsearch on
	```
6. Start Elasticsearch
	```sh
	sudo service elasticsearch start
	```
7. Test Elasticsearch
	```sh
	curl localhost:9200
	```
	Result
	```json
	{
		"name" : "o45_amy",
		"cluster_name" : "elasticsearch",
		"cluster_uuid" : "7HaWcw8wRvWXXPCkoIzXsw",
		"version" : {
			"number" : "6.8.8",
			"build_flavor" : "default",
			"build_type" : "rpm",
			"build_hash" : "2f4c224",
			"build_date" : "2020-03-18T23:22:18.622755Z",
			"build_snapshot" : false,
			"lucene_version" : "7.7.2",
			"minimum_wire_compatibility_version" : "5.6.0",
			"minimum_index_compatibility_version" : "5.0.0"
 		},
		"tagline" : "You Know, for Search"
	}
	```
	
### Install kibana
1. Install GPG key for the elasticsearch rpm packages.
	```sh
	sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
	```
2. Create yum repository file for the kibana
	```sh
	sudo vi /etc/yum.repos.d/kibana.repo
	```
	Add below content
	```
	[kibana-6.x]
	name=Kibana repository for 6.x packages
	baseurl=https://artifacts.elastic.co/packages/6.x/yum
	gpgcheck=1
	gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
	enabled=1
	autorefresh=1
	type=rpm-md
	```
3. Install Kibana
	```sh
	sudo yum install kibana
	```
4. Activate Kibana as a service
	```sh
	sudo chkconfig --add kibana
	sudo chkconfig kibana on
	```
5. Start kiabana
	```sh
	sudo service kibana start
	```
6. Test: default kibana will connect to x-pack elasticsearch by localhost:9200, so don't need to config host for local kibana
	```url
	localhost:5601  - admin/admin
	```
## Example Run Project

JAR FILE: https://drive.google.com/file/d/1zbOl35SwqzLyUaDEV0DlrPP_AVx4sG4b/view?usp=sharing
ENV requirement: spark 2.2.0 - http://archive.cloudera.com/spark2/parcels/2.2.0.cloudera4/

1. Create topic : **topic_covid**
	```sh
	cd /usr/lib/kafka/bin
	./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic topic_covid
	```
2. Start Producer use topic: **topic_covid**
	```sh
	cd /usr/lib/kafka/bin 
	./kafka-console-producer.sh --broker-list localhost:9092 --topic topic_covid
	```
3. Start consumer use topic: **topic_covid**
	```sh
	cd /usr/lib/kafka/bin 
	./kafka-console-consumer.sh --zookeeper localhost:2181 -topic ${topic_name} --from-beginnin
	```
4. Run Jar Script to STREAMMING data to HBASE 
	```sh
	export SPARK_KAFKA_VERSION=0.10
	spark2-submit --class "SparkStreaming.StreamingJob" --master yarn ${path_to_jar_file}/SparkStream.jar "hbase_table_name" "topic_covid"
	```
5. Run script to put data to producer 
	```sh
	./import_data.sh | /usr/lib/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic topic_covid
	```
> please check how to write the import_data.sh at import data section	

6. Import data to elasticsearch 
park2-submit --class "SparkSQL.SparkSQL" --master yarn /home/cloudera/Desktop/SparkStream.jar "hbase_table_name" "date"
> date format: yyyy-MM-dd
