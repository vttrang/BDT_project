package SparkSQL;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog;

import HBase.HBaseCovidTable;
import SparkStreaming.StreamingJob;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SparkSQL {

    public static void main(String[] args) throws IOException {
    	InputStream input = StreamingJob.class.getClassLoader().getResourceAsStream("config.properties");
    	  
    	if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            return;
        }
    	
    	Properties prop = new Properties();
    	//load a properties file from class path, inside static method
        prop.load(input);
        
    	 String catalog = "{\n" +
    	            "\t \"table\":{\"namespace\":\"default\", \"name\":\"" + HBaseCovidTable.TABLE_NAME + "\", \"tableCoder\":\"PrimitiveType\"},\n" +
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

        dataset.createOrReplaceTempView(HBaseCovidTable.TABLE_NAME);
       
        spark.sql("SELECT * FROM " + HBaseCovidTable.TABLE_NAME).show();
        System.out.println("Schema: " + catalog);
    }
}
