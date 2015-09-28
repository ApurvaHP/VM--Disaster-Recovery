
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mysql.jdbc.PreparedStatement;

public class MongotoMysql {
	private static DB db;

	private static Connection conn;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost:3306/project2";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";

	private static DB connectToMongoDb() throws UnknownHostException {
		if (db == null) {
			MongoClient client = new MongoClient("130.65.133.242",27017);
			db = client.getDB("logToMongo");
			System.out.println("connected to mongo");
		}
		return db;
	}

	public static Connection connectToMySql() {
		if (conn == null) {
			try {
				Class.forName(DRIVER);
				conn = DriverManager
						.getConnection(URL, USERNAME, PASSWORD);
				System.out.println("connected to sql");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}
	
	private static void archiveDataOfMongoDb() throws UnknownHostException {
	DBCollection tbl = connectToMongoDb().getCollection("testData");
	Date today = new Date();
	String atblname = "archive"+today.getYear()+today.getMonth()+today.getDate();
	DBCollection atbl = connectToMongoDb().getCollection(atblname);
	DBCursor cur = tbl.find();
	while (cur.hasNext()) {
		atbl.insert(cur.next());
	}
	tbl.drop();
}


	public static String getAggregateData() throws UnknownHostException {
		
		DBCollection tbl = connectToMongoDb().getCollection("collect");
		//tbl.rename("temp_logs4");
		
		//DBCollection tbl = connectToMongoDb().getCollection("collect");
		String grp = "{$group:{_id:'$vmname',avgcpu:{$avg:'$disk'},avgmemory:{$avg:'$mem'},avgdisk:{$avg:'$cpu'},avgnetwork:{$avg:'$net'},avgsystem:{$avg:'$sys'}}}";
		DBObject group = (DBObject) JSON.parse(grp);
		AggregationOutput output = tbl.aggregate(group);
		System.out.println(tbl);
		System.out.println("aggregated");
		ArrayList<DBObject> list = (ArrayList<DBObject>) output.results();
		System.out.println(list.isEmpty());
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date dt = new Date();
		String currentDate =  df.format(dt);
		
		for (DBObject dbObject : list) {
			System.out.println(dbObject);
			insertIntoMySql(currentDate, dbObject);
		}
		archiveDataOfMongoDb();
		System.out.println("archived");
		return "";
	}

	public static void insertIntoMySql(String currentDate, DBObject obj) {
		try {
			PreparedStatement st = (PreparedStatement) connectToMySql().prepareStatement("insert into vmstatsdb(date,vmname,disk,memory,cpu,network,sys) values(?,?,?,?,?,?,?)");
			
			st.setString(1, currentDate);
			
			st.setString(2, obj.get("_id").toString());
			st.setDouble(3, Double.parseDouble( obj.get("avgcpu").toString()));
			st.setDouble(4, Double.parseDouble( obj.get("avgmemory").toString()));
			st.setDouble(5, Double.parseDouble( obj.get("avgdisk").toString()));
			st.setDouble(6, Double.parseDouble( obj.get("avgnetwork").toString()));
			st.setDouble(7, Double.parseDouble( obj.get("avgsystem").toString()));
			st.executeUpdate();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static Thread t1 = new Thread(){
		public void run(){
			while(true){
			try{
			getAggregateData();
			Thread.sleep(5000 * 60);
			}catch(UnknownHostException e){
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
	};

	public static void main(String[] args) throws UnknownHostException {
		t1.start();
	}
}


