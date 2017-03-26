import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.sql.*;
import java.net.InetAddress;


public class ManhuntServer {

   static ArrayList<Socket> sockets = new ArrayList<Socket>();
    static int code = 0;
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost";

    //Database credentials
    static final String USER = "root";   //the user name;
    static final String PASS = "root";   //the password;
    static Connection conn = null;
    static Statement stmt = null;
    static String sql; 

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The manhunt server is running.");
	try{
	    Class.forName("com.mysql.jdbc.Driver");
	    conn = DriverManager.getConnection(DB_URL, USER, PASS);
	    stmt = conn.createStatement();
	    sql = "use manhunt;";
	     stmt.executeUpdate(sql);
	}catch (Exception e){
	    
	}
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new Capitalizer(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Capitalizer extends Thread {
        private Socket socket;
        private int clientNumber;
        private String user;
        private String lKey;

        public Capitalizer(Socket socket, int clientNumber) {
            this.socket = socket;
            sockets.add(socket);
            //log(Integer.toString(sockets.size()));
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
		    String content[] = input.split("#");
		    
		    System.out.println(Arrays.toString(content));
                    if ( input.startsWith("#exit") ) {
                      	/*#exit#uid*/
			try{
			    sql ="delete from users where uid = '"+content[2]+ "';";
			    stmt.executeUpdate(sql);
			}catch(Exception e){
			    e.printStackTrace();
			}
			    out.println("");
                        break;
			
                    } else if ( input.startsWith("#init") ) {
			System.out.println(content[4]);
			/*format #init#lcode#name#uid#TLLAT#TLLONG#TRLAT#TRLONG#BLLAT#BLLONG#BRLAT#BRLONG#tname#
			 #TLLAT#TLLONG#TRLAT#TRLONG#BLLAT#BLLONG#BRLAT#BRLONG#time*/
			try{
			     int namecount = 0;
			  Statement s = conn.createStatement (); 
			   s.executeQuery ( "SELECT * from users WHERE uid = '" + content[4] +"';");
			   ResultSet rs = s.getResultSet ();
			   while (rs.next ())
			       {
				   ++namecount;
			       }
			   if(namecount==0){
			    
			       sql = "insert into lobby values(" + "'" + content[2] + "','" + content[3] + "','" +
				   content[5] + "','" + content[6] + "','" + content[7] + "','" +
				   content[8] + "','" + content[9] + "','" + content[10] + "','" + content[11] + "','" +
				   content[12] + "', 0, '" + content[22] + "');";
			       stmt.executeUpdate(sql);

			       sql = "insert into users values ('" + content[4] + "','" + socket.getPort() +
				   "','" + socket.getInetAddress() + "');";
			    
			       stmt.executeUpdate(sql);
			       sql = "insert into memberof values ('" + content[2] + "','" + content[4] + "');";
			       stmt.executeUpdate(sql);

			       sql = "insert into team values ('" + content[13] + "','" + content[2] + "','" +
				   content[4] + "');";
			       stmt.executeUpdate(sql);

			       sql = "insert into jailbounds values(" + "'" + content[2] + "','" + content[14] +
				   "','" +content[15] + "','" + content[16] + "','" + content[17] + "','" +
				   content[18] + "','" + content[19] + "','" + content[20] + "','" + content[21] +
				   "');";
			       stmt.executeUpdate(sql);
			       System.out.println("succ");
			         out.println("#succ");
			   }else{
			       System.out.println("fail");
			        out.println("#fail#badname");
			   }
			    
			}catch (Exception e){
			    e.printStackTrace();
			    out.println("#fail");
			}
			   
			 
                      
                    } else if ( input.startsWith("#join") ) {
			/*#join#lcode#lname#uid#tname*/
			try {
			   Statement s = conn.createStatement (); 
			   //System.out.println("SELECT * from lobby WHERE lcode = '" + content[2] +
			   //   " 'AND name = '" + content[3] + "';");
			   s.executeQuery ( "SELECT * from lobby WHERE lcode = '" + content[2] +
					    "' AND name = '" + content[3] + "' AND active = 0;");
			   int count = 0;
			   ResultSet rs = s.getResultSet ();
			   while (rs.next ())
			       {
				   ++count;
			       }
			   int namecount = 0;
			   conn.createStatement (); 
			   s.executeQuery ( "SELECT * from users WHERE uid = '" + content[4] +"';");
			    rs = s.getResultSet ();
			   while (rs.next ())
			       {
				   ++namecount;
			       }
			  
			   if(count != 0 && namecount ==0){
			       sql = "insert into users values ('" + content[4] + "','" + socket.getPort() +
				   "','" + socket.getInetAddress() + "');";
			       stmt.executeUpdate(sql);
			       sql = "insert into memberof values('" + content[2]+ "','" + content[4] + "');";
			       stmt.executeUpdate(sql);
			       sql = "insert into team values ('" + content[5] + "','" + content[2] + "','" +
				   content[4] + "');";
			    stmt.executeUpdate(sql);
			       out.println("#succ");
			   }else if(namecount !=0){
			       out.println("#fail#badname");
			   }else{
			       out.println("#fail");
			   }
			}catch (Exception e){
			    e.printStackTrace();
			}
                        
                    } else if ( input.startsWith("#new") ) {
                        out.println("Creating Team");
                    } else if ( input.startsWith("#refresh") ) {
			/*#refresh#lcode#t1name#t2name*/
			String res = "";
			try{
			    Statement s = conn.createStatement ();
			   
			    s.executeQuery("SELECT uid FROM team WHERE lcode = '" + content[2] +
					   "' AND tname = '" + content[3] + "' ;");
			    res = "#" + content[3];
			    ResultSet rs = s.getResultSet();
			  
			    while(rs.next()){
				res += "," + rs.getString("uid");
				System.out.println(res);
			    }
			    res += "#" + content[4];
			    s = conn.createStatement ();
			    s.executeQuery("SELECT uid FROM team WHERE lcode = '" + content[2] +
					   "' AND tname = '" + content[4] + "' ;");
			    
			    rs = s.getResultSet();
			    while(rs.next()){
				res += "," + rs.getString("uid");
			    }
			}catch (Exception e){
			    e.printStackTrace();
			}
			System.out.println(res);
                        out.println(res);
                    } else if ( input.startsWith("#display") ) {
                        out.println("Displaying teamnames");
                    } else if ( input.startsWith("#start") ) {
			 ArrayList<Socket> members = new ArrayList<Socket>();
			  String time = "";
			   int i = 0;
			try{
			   
			    /*#start#lcode#tname*/
			    Statement s = conn.createStatement ();
			   
			    s.executeQuery("SELECT port, addr FROM users u, memberof m  WHERE m.uid= u.uid AND lcode = '" + content[2] + "';");
			    ResultSet rs = s.getResultSet();
			    
			    while(rs.next()){
				members.add(getSocket(InetAddress.getByName(rs.getString("addr").substring(1)), rs.getInt("port")));
			    }
			    
			     s.executeQuery("SELECT time from lobby where lcode = '" + content[2] + "';");
			   rs = s.getResultSet();
			  
			    while(rs.next()){
				time = rs.getString("time");
			    }
			    
			    sql = "UPDATE lobby SET active= 1 WHERE lcode = '" + content[2] + "';";
			    stmt.executeUpdate(sql);

			     s = conn.createStatement ();
			   
			    s.executeQuery("SELECT COUNT(*) AS numMem FROM team WHERE lcode = '" + content[2] + "' AND tname = '" + content[3] + "';");
			    rs = s.getResultSet();
			   
			    while(rs.next()){
				i = rs.getInt("numMem");
			    }
			    
			    // System.out.println(Arrays.toString(members.toArray()));
			}catch(Exception e){
			     e.printStackTrace();
			}
			sendToAll("#start#"+time + "#" + Integer.toString(i),members);
			
			// out.println("Starting a match");
                    } else if ( input.startsWith("#loc") ) {
                        out.println("You have a location");
                    } else if ( input.startsWith("#tag") ) {
			String res = "bounds";
			ArrayList<Socket> members = new ArrayList<Socket>();
			try{
			    Statement s = conn.createStatement ();
			    
			    s.executeQuery("SELECT port, addr FROM users u, memberof m  WHERE m.uid= u.uid AND lcode = '" + content[2] + "';");
			    ResultSet rs = s.getResultSet();
			    
			    while(rs.next()){
				members.add(getSocket(InetAddress.getByName(rs.getString("addr").substring(1)), rs.getInt("port")));
			    }
			}catch(Exception e){
			    e.printStackTrace();
			}
			
			System.out.println("members: "+ Arrays.toString(members.toArray()));
			sendToAll("#tag", members);
		    } else if ( input.startsWith("#jailbreak") ) {
			String res = "bounds";
			ArrayList<Socket> members = new ArrayList<Socket>();
			try{
			    Statement s = conn.createStatement ();
			    
			    s.executeQuery("SELECT port, addr FROM users u, memberof m  WHERE m.uid= u.uid AND lcode = '" + content[2] + "';");
			    ResultSet rs = s.getResultSet();
			    
			    while(rs.next()){
				members.add(getSocket(InetAddress.getByName(rs.getString("addr").substring(1)), rs.getInt("port")));
			    }
			}catch(Exception e){
			    e.printStackTrace();
			}
			System.out.println("members: "+ Arrays.toString(members.toArray()));
			sendToAll("#jailbreak", members);
		    } else if ( input.startsWith("#getlcode") ) {
			String lcode = "";
			lcode = genCode(code++,0);
			out.println("#" + lcode);
		    } else if ( input.startsWith("#getbounds") ) {
			/*#getbounds#lcode*/
			String res = "bounds";
			ArrayList<Socket> members = new ArrayList<Socket>();
			    try{
				Statement s = conn.createStatement ();
				
				s.executeQuery("SELECT port, addr FROM users u, memberof m  WHERE m.uid= u.uid AND lcode = '" + content[2] + "';");
				ResultSet rs = s.getResultSet();
				
				while(rs.next()){
				    members.add(getSocket(InetAddress.getByName(rs.getString("addr").substring(1)), rs.getInt("port")));
				}
				
				s = conn.createStatement ();
				
				s.executeQuery("SELECT * FROM jailbounds  WHERE lcode = '" + content[2] + "';");
				 rs = s.getResultSet();
			    
				while(rs.next()){
				    res += "#" + rs.getString("TLLAT")+"#"+ rs.getString("TLLONG")+ "#" +
					rs.getString("TRLAT")+"#"+ rs.getString("TRLONG")+ "#" +
					rs.getString("BLLAT")+"#"+ rs.getString("BLLONG")+ "#"+
					rs.getString("BRLAT")+"#"+ rs.getString("BRLONG");
				}
				s.executeQuery("SELECT * FROM lobby  WHERE lcode = '" + content[2] + "';");
				rs = s.getResultSet();
				
				while(rs.next()){
				res += "#" + rs.getString("TLLAT")+"#"+ rs.getString("TLLONG")+ "#" +
				    rs.getString("TRLAT")+"#"+ rs.getString("TRLONG")+ "#" +
				    rs.getString("BLLAT")+"#"+ rs.getString("BLLONG")+ "#"+
				    rs.getString("BRLAT")+"#"+ rs.getString("BRLONG") + "#" + rs.getInt("time");
				}
				// System.out.println(Arrays.toString(members.toArray()));
			    }catch(Exception e){
				e.printStackTrace();
			    }
			    //sendToAll(res,members);
			    out.println(res);
		    }else if ( input.startsWith("#end") ) {
			/*#end#lcode*/
			try{
			    sql ="delete from lobby where lcode = '"+content[2]+ "';";
			    stmt.executeUpdate(sql);
			}catch(Exception e){
			    e.printStackTrace();
			}
                        out.println("end");
                    } else {
                        out.println("#error#");
                    }
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
		            for ( int i = 0; i < sockets.size(); i++ ) {
                        if ( socket.equals(sockets.get(i)) ) {
                            sockets.remove(i);
                        }
			        }
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

	public Socket getSocket ( InetAddress addr, int port ) {
	    for ( Socket s : sockets ) {
		if ( s.getInetAddress().equals(addr) && s.getPort() == port ) {
		    return s;
		}
	    }
	    return null;
	}
	
        public void sendToAll ( String message, ArrayList<Socket> sockets ) {
            try {
                PrintWriter out;
                for ( Socket s : sockets ) {
                    out = new PrintWriter(s.getOutputStream(), true);
                    out.println(message);
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

	String genCode ( int a, int b ) {
	    if ( b == 4 ) { return String.valueOf((char)((a%26)+97)); }
	    return genCode(a/26,b+1) + String.valueOf((char)((a%26)+97));
	}

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }

}
