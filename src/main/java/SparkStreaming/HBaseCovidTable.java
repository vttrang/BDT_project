package SparkStreaming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;

import com.clearspring.analytics.util.Pair;

public class HBaseCovidTable {
	private String TABLE_NAME = "";
	private String CF_DEFAULT = "";
	private List<String> CFs;
	
	
	public HBaseCovidTable(String tableName, String CFDefault, List<String> CFs) throws IOException {
		this.TABLE_NAME = tableName;
		this.CF_DEFAULT = CFDefault;
		this.CFs = CFs; 
		this.createTable();	
	}
	
	private void createTable() throws IOException{

		Configuration config = HBaseConfiguration.create();

		try (Connection connection = ConnectionFactory.createConnection(config);
				Admin admin = connection.getAdmin())
		{
			HTableDescriptor table = new HTableDescriptor(TableName.valueOf(this.TABLE_NAME));
			table.addFamily(new HColumnDescriptor(this.CF_DEFAULT).setCompressionType(Algorithm.NONE));
			
			for(String CF : CFs) {
				table.addFamily(new HColumnDescriptor(CF));
			}

			if (admin.tableExists(table.getTableName()))
			{
				admin.disableTable(table.getTableName());
				admin.deleteTable(table.getTableName());
			}
			admin.createTable(table);
		}
	}

	public String getTABLE_NAME() {
		return TABLE_NAME;
	}

	public void setTABLE_NAME(String tABLE_NAME) {
		TABLE_NAME = tABLE_NAME;
	}

	public String getCF_DEFAULT() {
		return CF_DEFAULT;
	}

	public void setCF_DEFAULT(String cF_DEFAULT) {
		CF_DEFAULT = cF_DEFAULT;
	}

	public List<String> getCFs() {
		return CFs;
	}

	public void setCFs(List<String> cFs) {
		CFs = cFs;
	}
	
	
	public void insertData(String string, String cf, String cfc, String value) throws IOException {
		Configuration config = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(config);
		Table table = connection.getTable(TableName.valueOf(this.TABLE_NAME));
		try {
			
			//Initially the row for table
			Put put = new Put(Bytes.toBytes(string));
			
			//create data row 1
			put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cfc), Bytes.toBytes(value));
			
			//Put data to table
			table.put(put);
		} finally {
			table.close();
			connection.close();
		}
	}
	
	
}
