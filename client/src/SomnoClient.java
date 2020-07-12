import java.io.*;
import java.net.*;

/**
 * Defines a SomnoClient which connects to a SomnoServer
 *
 * KNOWN BUGS:
 * program can be run
 * successfully logging out triggers the "kicked" message
 *
 */
public class SomnoClient {
    private Socket socket;

    public static void main(String[] args) {
        //starts the client
        if (args.length != 3) {
            System.out.println("Usage: java SomnoClient ip port nickname");
        } else {
            SomnoClient c = new SomnoClient(
                    args[0],
                    Integer.parseInt(args[1]),
                    args[2]);
        }
    }

    /**
     * Constructor for a SomnoClient
     * @param ip the hostname to connect to
     * @param port the port which it tries to connect to the host on
     * @param nickname the nickname the client will be using
     */
    public SomnoClient(String ip, int port, String nickname) {
        //first try to connect to the server
        //if successful, send the nickname and then start listening and sending information
        //sending will be handled on this thread, while listening will be handled on another

        try (BufferedReader userin = new BufferedReader(new InputStreamReader(System.in))){
            socket = new Socket(ip, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(nickname); //sends the nickname to the server

            //start the receiving thread
            Listener l = new Listener(socket, this);
            l.start();

            String msg;

            //start the sending loop
            do {
                //get chats from the user
                msg = userin.readLine();
                out.println(msg);
            } while (!msg.equals("/logout"));

            Thread.sleep(1000); //makes a socket-handling race condition exception less likely
            socket.close();
        } catch (UnknownHostException e) {
            System.out.println("Invalid host address. Cannot continue.");
        } catch (IOException e) {
            System.out.println("IOException caught");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Forces the program to close. May leave memory leaks hanging
     */
    void forceClose(String reason) {
        System.out.println("The server connection has been lost. Reason: " + reason);
        System.exit(1);
    }

}

/**
 * Defines a Listener which listens on a socket for any new lines
 */
class Listener extends Thread {
    private Socket socket;
    private SomnoClient hostclient;

    /**
     * Creates a new Listener object
     * @param socket the socket that the thread listens on
     */
    public Listener(Socket socket, SomnoClient hostclient) {
        this.socket = socket;
        this.hostclient = hostclient;
    }

    public void run() {
        String received;
        boolean wasKicked = true;
        try (BufferedReader receive = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))){
            while (!((received = receive.readLine()).equals("/logout"))) {
                System.out.println(received);
            }
        } catch (IOException e) {
            System.out.println("An error occurred!");
            wasKicked = false;
        } finally {
            if (wasKicked) {
                hostclient.forceClose("kicked");
            } else {
                hostclient.forceClose("connection closed");
            }
        }
    }

}