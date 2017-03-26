import java.sql.*;

/*
 * I pledge my honor that I have abided by the Stevens Honor System.
 * Justin Barish
 * 
 */

public class Create_Database {
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost";

   //  Database credentials
   static final String USER = "root";   //the user name;
   static final String PASS = "root";   //the password;
 
   public static void main(String[] args) {
       Connection conn = null;
       Statement stmt = null;
       
       try{
	   // Register JDBC driver
	   Class.forName("com.mysql.jdbc.Driver");

	   //Open a connection to database
	   System.out.println("Connecting to database...");
	   conn = DriverManager.getConnection(DB_URL, USER, PASS);
	   
	   //Create the database
	   System.out.println("Creating database...");
	   stmt = conn.createStatement();
	   
	   //If the boatrental database already exists, drop it before recreating it.
	   String sql;
	   sql = "DROP DATABASE IF EXISTS manhunt;";
	   stmt.executeUpdate(sql);
	   
	   //Create the BoatRental Database
	   sql = "CREATE DATABASE manhunt;";
	   stmt.executeUpdate(sql);
	   System.out.println("Database created successfully...");
	   
	   //Select the database
	   sql = "use manhunt;";
	   stmt.executeUpdate(sql);
	   
	   /*******SQL to create tables ********/
	   

	   sql = "create table users(uid VARCHAR(20) NOT NULL,"+
	       "port INTEGER," +
	       "addr VARCHAR(15),"+
	       "PRIMARY KEY (uid));";
	   stmt.executeUpdate(sql);
	   

	    sql = "create table lobby(lcode VARCHAR(5) NOT NULL," +
		"name VARCHAR(20)," +
		"TLLAT VARCHAR(128)," +
		"TLLONG VARCHAR(128)," +
		"TRLAT VARCHAR(128)," +
		"TRLONG VARCHAR(128)," +
		"BLLAT VARCHAR(128)," +
		"BLLONG VARCHAR(128)," +
		"BRLAT VARCHAR(128)," +
		"BRLONG VARCHAR(128)," +
		"active INTEGER," +
		"time INTEGER," + 
	       "PRIMARY KEY (lcode));";
	   stmt.executeUpdate(sql);
	   System.out.println("creating team");
	   sql = "create table team(tname VARCHAR(20),lcode VARCHAR(5),uid VARCHAR(20),PRIMARY KEY (tname, lcode, uid),FOREIGN KEY (uid) REFERENCES users(uid) ON DELETE CASCADE,FOREIGN KEY (lcode) REFERENCES lobby(lcode)ON DELETE CASCADE);";
	   stmt.executeUpdate(sql);

	   System.out.println("creating jailbounds");
	    sql = "create table jailbounds(lcode VARCHAR(5) NOT NULL," +
		"TLLAT VARCHAR(128)," +
		"TLLONG VARCHAR(128)," +
		"TRLAT VARCHAR(128)," +
		"TRLONG VARCHAR(128)," +
		"BLLAT VARCHAR(128)," +
		"BLLONG VARCHAR(128)," +
		"BRLAT VARCHAR(128)," +
		"BRLONG VARCHAR(128)," +
	       "PRIMARY KEY (lcode), FOREIGN KEY (lcode) REFERENCES lobby(lcode )ON DELETE CASCADE) ;";
	   stmt.executeUpdate(sql);

	    System.out.println("creating jailmembers");
	   sql = "create table jailmembers(lcode VARCHAR(5) NOT NULL," +
		"uid VARCHAR(20)," +
	       "PRIMARY KEY (lcode, uid), FOREIGN KEY (lcode) REFERENCES lobby(lcode),FOREIGN KEY (uid) REFERENCES users(uid) ON DELETE cascade) ;";
	   stmt.executeUpdate(sql);
	
	   System.out.println("creating memberof");
	   sql = "create table memberof(lcode VARCHAR(5) NOT NULL," +
		"uid VARCHAR(20)," +
	       "PRIMARY KEY (lcode, uid), FOREIGN KEY (lcode) REFERENCES lobby(lcode),FOREIGN KEY (uid) REFERENCES users(uid) ON DELETE CASCADE) ;";
	   stmt.executeUpdate(sql);
	   
	 
	  
       }catch(SQLException se){
	   //Handle errors for JDBC
	   se.printStackTrace();
       }catch(Exception e){
	   //Handle errors for Class.forName
	   e.printStackTrace();
       }finally{
	   //finally block used to close resources
	   try{
	       if(stmt!=null)
		   stmt.close();
	   }catch(SQLException se2){
	   }// nothing we can do
	   try{
	       if(conn!=null)
		   conn.close();
	   }catch(SQLException se){
	       se.printStackTrace();
	   }//end finally try
       }//end try
       System.out.println("Exiting...");
   }//end main
}//end JDBCHW3
