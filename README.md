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
7. Import data
	
	a. Prepares data (*.csv) // Reads per file by date
	
	b. Prepares dataRun.sh
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
	c. Run
	```sh 
	sudo sh /home/cloudera/Downloads/dataRun.sh | /usr/lib/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic-name}
	```

# How to install Spark 2 in Cloudera Manager (CM)
### Startup Cloudera Manager (CM)
1. Once the VM starts up, navigate to the Desktop and Execute the “Launch Cloudera Express” script.
2. Once complete, you should now be able to view the Cloudera Manager by opening up your web browser (within the VM) and navigating to:
``` http://quickstart.cloudera:7180 ```
Default Credentials: cloudera/cloudera

### Configure CM to use Parcels
1. Navigate to the Desktop and Execute the “Migrate to Parcels” script.
2. You can validate that CM is now using parcels by logging into the Cloudera Manager Web UI. Right next to the cluster name, it should say: (CDH x.x.x, Parcels)
3. You will need to restart all the services on the cluster after step-2. You can do this by: Going to the Cloudera Manager Web UI, click on the button next to the Cluster Name and click Start.

### Select the version of Spark 2 you want to install
1. Navigate here to get a full list of the Spark versions that are available:
``` https://docs.cloudera.com/documentation/spark2/latest/topics/spark2_packaging.html ```
2. Copy the Custom Service Descriptor (CSD) URL (To be referred to as CSD_URL in next sections)

### Install Spark 2 CSD
1. Open a Command Line Terminal
2. Login as Root
``` $ sudo su ```
3. Navigate to the CSD Directory
``` $ cd /opt/cloudera/csd ```
4. Download the CSD (Replace CSD_URL with the URL you copied in previous sections)
``` $ wget CSD_URL ```
5. Set Permissions and Ownership
``` 
$ chown cloudera-scm:cloudera-scm SPARK2_ON_YARN-x.x.x.clouderax.jar
$ chmod 644 SPARK2_ON_YARN-x.x.x.clouderax.jar
```
6. Restart CM Services
``` $ service cloudera-scm-server restart ```
7. Login to the Cloudera Manager Web UI
8. Restart the Cloudera Management Service
- Select Clusters > Cloudera Management Service
- Select Actions > Restart
9. Restart the Cluster Services
- Select Clusters > Cloudera QuickStart
- Select Actions > Restart

### Install Spark 2 Parcel
1. Login to the Cloudera Manager Web UI
2. Navigate to Hosts > Parcels
3. Locate the SPARK2 parcel from the list
4. Under Actions, click Download and wait for it to download
5. Under Actions, click Distribute and wait for it to be distributed
6. Under Actions, click Activate and wait for it to be activated

### Install Spark 2 Service
1. Login to the Cloudera Manager Web UI
2. Click on the button next to the Cluster Name and select “Add Service”
3. Select “Spark 2” and click “Continue”
4. Select whichever set of dependencies you would like and click “Continue”
5. Select the one instance available as the **History Server** and the **Gateway** and click “Continue”
6. Leave the default configurations as is and click “Continue”
7. The service will now be added and then you will be taken back to the CM home
8. Click on the blue button next to the Spark 2 service and click “Restart Stale Services”
9. Ensure the “Re-deploy client configuration” is checked and click “Restart Now”

## Testing
### Smoke Test
```
MASTER=yarn /opt/cloudera/parcels/SPARK2/lib/spark2/bin/run-example SparkPi 100
```

Ref: https://blog.clairvoyantsoft.com/installing-spark2-on-clouderas-quickstart-vm-bbf0db5fb3a9
