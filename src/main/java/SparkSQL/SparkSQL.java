package SparkSQL;

import org.apache.hadoop.hbase.util.HBaseFsck;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog;

import HBase.HBaseCovidTable;
import SparkStreaming.StreamingJob;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import org.spark_project.guava.collect.ImmutableList;
import org.spark_project.guava.collect.ImmutableMap;

public class SparkSQL {
    /**
     *
     * @param args [0] is table name, [1] is day which is used to query
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
    	InputStream input = StreamingJob.class.getClassLoader().getResourceAsStream("config.properties");
    	
    	if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            return;
        }
    	
    	Properties prop = new Properties();
    	//load a properties file from class path, inside static method
        prop.load(input);

        String tableName = HBaseCovidTable.DEFAULT_TABLE_NAME;
        if("" != args[0]) {
            tableName = args[0];
        }
        String catalog = "{\n" +
                "\t \"table\":{\"namespace\":\"default\", \"name\":\"" + tableName + "\", \"tableCoder\":\"PrimitiveType\"},\n" +
                "\t \"rowkey\":\"key\",\n" +
                "\t \"columns\":{\n" +
                "\t\t \"Rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"},\n" +
                "\t\t \""+ HBaseCovidTable.CF_1_CONFIRMED + "\":{\"cf\":\""+ HBaseCovidTable.CF_1_CONFIRMED + "\", \"col\":\"" + args[0] + "\", \"type\":\"string\"},\n" +
                "\t\t \""+ HBaseCovidTable.CF_2_DEATH + "\":{\"cf\":\""+ HBaseCovidTable.CF_2_DEATH + "\", \"col\":\"" + args[0] + "\", \"type\":\"string\"},\n" +
                "\t\t \""+ HBaseCovidTable.CF_3_RECOVERED + "\":{\"cf\":\""+ HBaseCovidTable.CF_3_RECOVERED + "\", \"col\":\"" + args[0] + "\", \"type\":\"string\"},\n" +
                "\t\t \""+ HBaseCovidTable.CF_4_ACTIVE + "\":{\"cf\":\""+ HBaseCovidTable.CF_4_ACTIVE + "\", \"col\":\"" + args[0] + "\", \"type\":\"string\"}\n" +
                "\t}\n" +
                "}";
    	
        Map<String, String> optionsMap = new HashMap<>();

        String htc = HBaseTableCatalog.tableCatalog();

        optionsMap.put(htc, catalog);

        SparkSession spark = SparkSession
                .builder()
                .master(prop.getProperty("spark.sql.master"))
                .appName(prop.getProperty("spark.sql.application.name"))
                .getOrCreate();

        Dataset<Row> dataset = spark.read().options(optionsMap).format("org.apache.spark.sql.execution.datasources.hbase").load();

        dataset.createOrReplaceTempView(tableName);
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(args[1]);
       
        List<Map<String, Object>> covids = new ArrayList<Map<String, Object>>();
        
        //Process some map-reduce job to prepare data for report on elastic search
        
        //send data to ES 
        for(Row rs : spark.sql("SELECT * FROM " + tableName).collectAsList()) {
        	Map<String, Object> map = new HashMap<>(); 
            map.put("DATE", dateFormat.format(date)); 
            
            String location = (String) rs.get(0);
            String[] locations = location.split(Pattern.quote("."), -1);
            
            map.put("COUNTRY", locations[0]);
            map.put("STATE", locations[1]);
            map.put("CITY", locations[2]);
            map.put("CONFIRMED", Integer.valueOf((String) rs.get(1)));
            map.put("DEATH", Integer.valueOf((String) rs.get(2)));
            map.put("RECOVERED", Integer.valueOf((String) rs.get(3)));
            map.put("ACTIVE", Integer.valueOf((String) rs.get(4)));
        	
            covids.add(map);
        }
        
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        ImmutableList<Map<String, Object>> iList = ImmutableList.<Map<String, Object>>builder().addAll(covids).build(); 
       
        JavaRDD<Map<String, Object>> javaRDD = jsc.parallelize(iList); 
        JavaEsSpark.saveToEs(javaRDD, "covid19/data");    
        
        System.out.println("Schema: " + catalog);
    }
}
