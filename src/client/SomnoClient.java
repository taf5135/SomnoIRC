package client;

import java.io.*;
import java.net.*;

/**
 * Defines a SomnoClient which connects to a SomnoServer
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

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))){
            socket = new Socket(ip, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //start the receiving thread
            Listener l = new Listener(socket);
            l.start();

            String msg;

            //start the sending loop
            while(true) {
                //get chats from the user
                msg = in.readLine();

                out.println(msg);
                if (msg.equals("/logout")) {
                    l.interrupt();
                }


            }

        } catch (UnknownHostException e) {
            System.out.println("Invalid host address. Cannot continue.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

/**
 * Defines a Listener which listens on a socket for any new lines
 */
class Listener extends Thread {
    private Socket socket;

    /**
     * Creates a new Listener object
     * @param socket the socket that the thread listens on
     */
    public Listener(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader receive = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))){
            while (!Thread.interrupted()) {
                System.out.println(receive.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
