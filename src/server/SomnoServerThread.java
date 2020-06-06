package server;

import java.util.HashSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Iterator;

/**
 * Defines a SomnoServerThread, which is started by the SomnoServer
 */
public class SomnoServerThread extends Thread {
    HashSet<SomnoServerThread> connectedUsers;
    private Socket socket;
    private BufferedReader receive;
    private PrintWriter send;
    private String nickname;
    private boolean kicked = false;

    /**
     * Constructor for a SomnoServerThread. Stores the socket and connectedusers
     * @param socket the socket it's connecting on
     * @param connectedUsers the HashSet of connected users
     */
    public SomnoServerThread(Socket socket, HashSet<SomnoServerThread> connectedUsers) {
        this.socket = socket;
        this.connectedUsers = connectedUsers;
    }

    /**
     * Logs a message to the console
     * @param msg the message
     */
    public synchronized void log(String msg) {
        System.out.println(msg);
    }

    /**
     * Loop through all the other connections and make them send out the received message.
     * The opcode specifies how the message should be formatted
     *
     * Key:
     *      0: standard message (nickname, time, msg)
     *      1: new connection message (msg, "connected at ", time)
     *      2: disconnect message (msg, " disconnected at ", time)
     *
     * @param msg the message to send out
     * @param opcode the opcode to use to format the message
     */
    public void update(String msg, int opcode) {
        switch (opcode) { //format the message based on the opcode
            case 0:
                msg = "nickname [time] > " + msg;
                break;
            case 1:
                msg = msg + " connected at " + "[time]";
                break;
            case 2:
                msg = msg + " disconnected at " + "[time]";
                break;
        }

        //execute on each connection
        for (SomnoServerThread thread : connectedUsers) {
            //print the message on each thread's outstream
            thread.sendMessage(msg);
        }

    }

    /**
     * Sends a message to the client
     * @param msg the preformatted message
     */
    public void sendMessage(String msg) {
        if (send != null) {
            send.println(msg);
        }
    }

    public void kick() throws IOException {
        kicked = true;
        closeConnections(this.receive, this.send);

    }

    /**
     * Closes the connections and shuts down the thread
     * @param receive the receiver, a BufferedReader
     * @param send the sender, a PrintWriter
     * @throws IOException thrown when receive, send, or socket are already closed
     */
    private void closeConnections(BufferedReader receive, PrintWriter send) throws IOException {
        receive.close();
        send.close();
        socket.close();
    }

    public String getNickname() {
        return nickname;
    }



    /**
     * Runs the send/receive logic for a single user
     */
    public void run() {

        try {
            send = new PrintWriter(socket.getOutputStream(), true);
            receive = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            nickname = receive.readLine(); //gets the nickname
            update(nickname, 1);

            String msgIn;
            while (!kicked) {
                msgIn = receive.readLine();
                if (msgIn.equals("/logout")) {
                    update(nickname, 2); //disconnect logic is done in the finally block
                    break;
                } else { //send message to all connected clients
                    update(msgIn, 0);
                    log(msgIn);
                }


            }
        } catch (IOException e) {
            System.out.println("IOException caught. Client may have closed unexpectedly");
            //call signoff message stuff
            update(nickname, 2);
        } finally {
            try {
                closeConnections(receive, send);
                connectedUsers.remove(this);
            } catch (IOException e) {
                System.out.println("Error closing socket");
                e.printStackTrace();
            }
        }
    }
}