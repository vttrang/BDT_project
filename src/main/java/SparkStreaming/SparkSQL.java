package SparkStreaming;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog;

public class SparkSQL {
	private static String sparkMaster = "local[2]";

	private static String sparkHbaseHost;

	private static String sparkHbasePort;
	
	public static void main(String[] args){
		SparkConf conf = new SparkConf().setAppName("SparkHbaseConnectorPOC")
                .setMaster(sparkMaster);

		JavaSparkContext javaSparkContext = new JavaSparkContext(conf);
//        javaSparkContext.hadoopConfiguration().set("spark.hbase.host", sparkHbaseHost);
//        javaSparkContext.hadoopConfiguration().set("spark.hbase.port", sparkHbasePort);

        SQLContext sqlContext = new SQLContext(javaSparkContext);
        
        String catalog = "{\n" +
        		 "\t\"table\":{\"namespace\":\"default\", \"name\":\"lines\", \"tableCoder\":\"PrimitiveType\"},\n" +
        		 "    \"rowkey\":\"key\",\n" +
        		 "    \"columns\":{\n" +
        		 "\t    \"rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"},\n" +
        		 "\t    \"words\":{\"cf\":\"words\", \"col\":\"word\", \"type\":\"string\"}\n" +
        		 "    }\n" +
        		 "}";
        
        Map<String, String> optionsMap = new HashMap<>();

        String htc = HBaseTableCatalog.tableCatalog();

        optionsMap.put(htc, catalog);
        // optionsMap.put(HBaseRelation.MIN_STAMP(), "123");
        // optionsMap.put(HBaseRelation.MAX_STAMP(), "456");

        Dataset dataset = sqlContext.read().options(optionsMap).format("org.apache.spark.sql.execution.datasources.hbase").load();
        
//        Dataset filteredDataset = dataset.filter(
//        	    dataset.col("rowkey").$greater$eq("1")
//        	        .and(dataset.col("rowkey").$less$eq("10")))
//        	    .filter(
//        	        dataset.col("rowKey").$greater$eq("3")
//        	            .and(dataset.col("rowkey").$less$eq("30"))
//        	    )
//        	    .select("rowkey");
        
        dataset.createOrReplaceTempView("words");
        sqlContext.sql("select * from words");
        
	}
}
