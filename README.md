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

## Project overview
<p align="center">
  <img width="1444" height="596" src="https://i.imgur.com/llJiv54.png">
</p>

## HBase schema
| Row Key | Confirmed | Death | Recovered | Active |
| ------- | --- | --- | --- | --- | --- |
| <Country>.<State>.<County> || Date-1 | Date-2 | ... | Current Date ||| Date-1 | Date-2 | ... | Current Date ||| Date-1 | Date-2 | ... | Current Date ||| Date-1 | Date-2 | ... | Current Date ||

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
