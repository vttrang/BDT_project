package SparkStreaming;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import HBase.HBaseCovidTable;

public final class StreamingJob {
	
	private static HBaseCovidTable covidTable;
	private static List<String> list = new ArrayList<String>();
	
    public static void main(String[] args) throws Exception {
    	InputStream input = StreamingJob.class.getClassLoader().getResourceAsStream("config.properties");
  
    	if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            return;
        }
    	
    	Properties prop = new Properties();
    	//load a properties file from class path, inside static method
        prop.load(input);
        
        SparkConf sparkConf = new SparkConf()
        				.setAppName(prop.getProperty("spark.streaming.application.name"))
        				.setMaster(prop.getProperty("spark.streaming.master"));;
        
        // Create the context with 2 seconds batch size
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));
        
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(prop.getProperty("bootstrap.servers.key"), prop.getProperty("bootstrap.servers.value"));
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put(prop.getProperty("group.id.key"), prop.getProperty("group.id.value"));
        kafkaParams.put(prop.getProperty("auto.offset.reset.key"), prop.getProperty("auto.offset.reset.value"));
        kafkaParams.put(prop.getProperty("enable.auto.commit.key"), prop.getProperty("enable.auto.commit.value"));

        Collection<String> topics = Arrays.asList(args[0]);

        final JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );
        
		
        covidTable = new HBaseCovidTable();
        
		//Insert single row
		/*messages.foreachRDD(rdd -> rdd.foreach(message ->  {
        	covidTable.insertData(message.value());	
        }));*/
		
		//Insert multiple rows
		messages.foreachRDD(rdd -> {
        	list = new ArrayList<String>();
        	rdd.foreach(message -> {
        		list.add(message.value());
        	});
        	covidTable.insertMulti(list);
        });

        jssc.start();
        jssc.awaitTermination();
    }
}
