package HBase;

import java.io.IOException;
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
import org.apache.hadoop.hbase.util.Bytes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class HBaseCovidTable {

	public static final String DEFAULT_TABLE_NAME = "covid19";
	public static final String CF_1_CONFIRMED = "Confirmed";
	public static final String CF_2_DEATH = "Death";
	public static final String CF_3_RECOVERED = "Recovered";
	public static final String CF_4_ACTIVE = "Active";

	private String tableName;

	public HBaseCovidTable(String tableName) throws IOException {
		this.tableName = tableName;
		if(this.tableName == "") {
			this.tableName = DEFAULT_TABLE_NAME;
		}
		createTable();
	}
	
	public HBaseCovidTable() throws IOException{
		createTable();
	}
	
	public void createTable() throws IOException {
		Configuration config = HBaseConfiguration.create();

		try (Connection connection = ConnectionFactory.createConnection(config);
				Admin admin = connection.getAdmin()) {
			HTableDescriptor table = new HTableDescriptor(
					TableName.valueOf(this.tableName));
			
			if (admin.tableExists(table.getTableName())) {
				System.out.println("Table has already existed");
			} else {
				
				table.addFamily(new HColumnDescriptor(CF_1_CONFIRMED));
				table.addFamily(new HColumnDescriptor(CF_2_DEATH));
				table.addFamily(new HColumnDescriptor(CF_3_RECOVERED));
				table.addFamily(new HColumnDescriptor(CF_4_ACTIVE));
				
				System.out.print("Creating table");
				admin.createTable(table);
				System.out.println("Done!");
			}
		}
	}

	public void insertData(String line) throws IOException {
		System.out.print("Insert data");
		String[] cells = parseData(line);
		try {
			String column = cells[4]; //date time format: yyyy-MM-dd
			String rowKey = cells[3] + "." + cells[2] + "." + cells[1];
			rowKey = rowKey.replace(' ', '_');
			System.out.println(line);
			Configuration config = HBaseConfiguration.create();

			try (Connection connection = ConnectionFactory.createConnection(config);
					Table table = connection.getTable(TableName.valueOf(this.tableName))) {

				Put p1 = new Put(Bytes.toBytes(rowKey));
				p1.addColumn(Bytes.toBytes(CF_1_CONFIRMED), Bytes.toBytes(column), Bytes.toBytes(cells[7]));
				p1.addColumn(Bytes.toBytes(CF_2_DEATH), Bytes.toBytes(column), Bytes.toBytes(cells[8]));
				p1.addColumn(Bytes.toBytes(CF_3_RECOVERED), Bytes.toBytes(column), Bytes.toBytes(cells[9]));
				p1.addColumn(Bytes.toBytes(CF_4_ACTIVE), Bytes.toBytes(column), Bytes.toBytes(cells[10]));
				table.put(p1);

				System.out.println("Done!");

			}
		} catch (StringIndexOutOfBoundsException ex) {
			System.out.println("Error at:" + cells[4]);
		}
	}
	
	public void insertMulti(List<String> list) throws IOException {
		System.out.print("Insert data");
		
		Configuration config = HBaseConfiguration.create();
		try (Connection connection = ConnectionFactory.createConnection(config);
				Table table = connection.getTable(TableName.valueOf(this.tableName))) {

			List<Put> putList = new ArrayList<Put>();
			list.forEach(line -> {
				String[] cells = parseData(line);
				
				String column = cells[4]; //date time format: yyyy-MM-dd
				String rowKey = cells[3] + "." + cells[2] + "." + cells[1];
				rowKey = rowKey.replace(' ', '_');
				System.out.println(line);
				
				Put p = new Put(Bytes.toBytes(rowKey));
				p.addColumn(Bytes.toBytes(CF_1_CONFIRMED), Bytes.toBytes(column), Bytes.toBytes(cells[7]));
				p.addColumn(Bytes.toBytes(CF_2_DEATH), Bytes.toBytes(column), Bytes.toBytes(cells[8]));
				p.addColumn(Bytes.toBytes(CF_3_RECOVERED), Bytes.toBytes(column), Bytes.toBytes(cells[9]));
				p.addColumn(Bytes.toBytes(CF_4_ACTIVE), Bytes.toBytes(column), Bytes.toBytes(cells[10]));
				
				putList.add(p);
			});
			
			table.put(putList);
		}
		
		System.out.println("Done!");
	}

	private String[] parseData(String line) {
		String newFormat = "(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),\"(.*)\""; //12 fields
		String oldFormat = "(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*)"; //8 fields
		String oldestFormat = "(.*),(.*),(.*),(.*),(.*),(.*)"; //6 fields
		String dirtyFormat = "(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*)"; //12 fields

		String[] cells = new String[12];
		if (line.matches(newFormat)){
			cells = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			if(cells[3].indexOf(',') > 0){
				cells[3] = cells[3].substring(1,cells[3].indexOf(','));
			}
			cells[4] = convertDate(cells[4]);
		}
		else if (line.matches(dirtyFormat)){
			cells = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			if(cells[3].indexOf(',') > 0){
				cells[3] = cells[3].substring(1,cells[3].indexOf(','));
			}
			cells[4] = convertDate(cells[4]);
		}
		else if (line.matches(oldFormat) && line.split(",").length == 8){
			String[] oldCells = new String[8];
			oldCells = line.split(",");
			
			cells[2] = oldCells[0]; //state
			cells[3] = oldCells[1]; //country
			cells[4] = convertDate(oldCells[2]); //date
			cells[7] = oldCells[3]; //confirmed
			cells[8] = oldCells[4]; //death
			cells[9] = oldCells[5]; //recovered
		}
		else if (line.matches(oldestFormat) && line.split(",").length == 6){
			String[] oldCells = new String[6];
			oldCells = line.split(",");
			
			cells[2] = oldCells[0]; //state
			cells[3] = oldCells[1]; //country
			cells[4] = convertDate(oldCells[2]); //date
			cells[7] = oldCells[3]; //confirmed
			cells[8] = oldCells[4]; //death
			cells[9] = oldCells[5]; //recovered
		}
		else{
			//Dirty data
			cells[4] = "yyyy-MM-dd";
			cells[7] = "";
			cells[8] = "";
			cells[9] = "";
			cells[10] = "";
		}

		return cells;
	}
	
	private String convertDate(String date) {
		String datePattern1 = "(.*)-(.*)-(.*) (.*)"; // 2020-04-12 23:18:00
		String datePattern2 = "(.*)\\/(.*)\\/(.*) (.*)"; // 1/22/2020 17:00

		if (date.matches(datePattern1)) {
			return date.substring(0, 10);
		} else if (date.matches(datePattern2)) {
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.format(new Date(date));
		}
		return "";
	}
}