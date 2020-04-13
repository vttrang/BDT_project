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

public class HBaseCovidTable {

	public static final String TABLE_NAME = "covid19";
	public static final String CF_1_CONFIRMED = "Confirmed";
	public static final String CF_2_DEATH = "Death";
	public static final String CF_3_RECOVERED = "Recovered";
	public static final String CF_4_ACTIVE = "Active";

	public HBaseCovidTable(String dataLine) throws IOException {
		createTable();
		insertData(dataLine);
	}
	
	public HBaseCovidTable() throws IOException{
		createTable();
	}
	
	public void createTable() throws IOException {
		Configuration config = HBaseConfiguration.create();

		try (Connection connection = ConnectionFactory.createConnection(config);
				Admin admin = connection.getAdmin()) {
			HTableDescriptor table = new HTableDescriptor(
					TableName.valueOf(TABLE_NAME));
			
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
			String column = cells[4].substring(0, 10); //date time format: yyyy-mm-dd
			String rowKey = cells[3] + "." + cells[2] + "." + cells[1];
			rowKey = rowKey.replace(' ', '_');
			System.out.println(line);
			Configuration config = HBaseConfiguration.create();

			try (Connection connection = ConnectionFactory.createConnection(config);
					Table table = connection.getTable(TableName.valueOf(TABLE_NAME))) {

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

	private String[] parseData(String line) {
		String newFormat = "(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),\"(.*)\"";
		String oldFormat = "(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*)";

		String[] cells = new String[12];
		if (line.matches(newFormat)) {
			cells = line.split(",");
		}
		else if (line.matches(oldFormat)){
			String[] oldCells = new String[8];
			oldCells = line.split(",");
			
			cells[2] = oldCells[0]; //state
			cells[3] = oldCells[1]; //country
			cells[4] = oldCells[2]; //date
			cells[7] = oldCells[3]; //confirmed
			cells[8] = oldCells[4]; //death
			cells[9] = oldCells[5]; //recovered
		}

		return cells;
	}
}