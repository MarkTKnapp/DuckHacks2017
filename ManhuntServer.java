import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
//import java.sql.*;
/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */



public class ManhuntServer {

    private static ArrayList<Socket> sockets = new ArrayList<Socket>();

   /*// JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost";

    //Database credentials
    static final String USER = "root";   //the user name;
    static final String PASS = "root";   //the password;
    Connection conn = null;
    Statement stmt = null;
    String sql; */

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
            log(Integer.toString(sockets.size()));
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
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line starting with #exit to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if ( input.startsWith("#exit") ) {
                        out.println("");
                        break;
                    } else if ( input.startsWith("#init") ) {
                        out.println("Creating a Lobby");
                    } else if ( input.startsWith("#join") ) {
                        out.println("Joining a Lobby");
                    } else if ( input.startsWith("#new") ) {
                        out.println("Creating Team");
                    } else if ( input.startsWith("#refresh") ) {
                        out.println("refreshing team list");
                    } else if ( input.startsWith("#display") ) {
                        out.println("Displaying teamnames");
                    } else if ( input.startsWith("#start") ) {
                        out.println("Starting a match");
                    } else if ( input.startsWith("#loc") ) {
                        out.println("You have a location");
                    } else if ( input.startsWith("Ay") ) {
                        sendToAll("Ayyyy Lmaooooo");
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

        public void sendToAll ( String message ) {
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

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }

}