package server;

import java.util.HashSet;
import java.net.*;
import java.io.*;

/**
 * Defines a server that accepts SomnoClient connections
 */
public class SomnoServer {
    private HashSet<SomnoServerThread> connectedUsers = new HashSet();
    private static final int DEFAULT_PORT = 27034;
    Thread thisThread = Thread.currentThread();
    /*
     * On startup, check whether a port number was provided. If none, use 27034
     * Start listening for connections. When someone tries to connect, put them on a new server thread and connect them
     * to the main chatroom.
     * Accomplish this by just taking whatever messages they send and sending them out to every connected user
     */

    /**
     * Main method. Accepts a port to launch on. If no port is specified, it runs on port 27034
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                //launch on default port 27034
                SomnoServer s = new SomnoServer(DEFAULT_PORT);
            } else {
                //launch on specified port
                SomnoServer s = new SomnoServer(Integer.parseInt(args[0]));
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Port is not a number!");
        }
        System.out.println("SomnoServer shutting down...");
    }

    /**
     * Interrupts the main thread when it's time to shut down
     */
    public void shutdown() {
        thisThread.interrupt();
    }

    /**
     * Defines a SomnoServer which runs on a port at its current ip address
     * @param port the port it runs on
     */
    public SomnoServer(int port) {
        System.out.println("Attempting to launch on port " + port);
        CommandInterpreter c = new CommandInterpreter(connectedUsers, this);
        c.start();
        try (ServerSocket socket = new ServerSocket(port)) {
            while (!Thread.interrupted()) {
                SomnoServerThread t = new SomnoServerThread(socket.accept(), connectedUsers);
                connectedUsers.add(t);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

/**
 * Interpreter that runs alongside the server itself, sort of like a pseudo-client.
 * Allows the server operator to input commands
 */
class CommandInterpreter extends Thread {
    /*
    Needs to do a few things:

    1: send messages ("say" command)
    2: force remove users ("kick" command)
    3: shut down the server ("shutdown" command)

    commands server-side will match client side commands, starting with a /

     */
    private HashSet<SomnoServerThread> connectedUsers;
    private SomnoServer hostServer;
    private boolean isShutDown = false;

    /**
     * CommandInterpreter constructor
     * @param connectedUsers the list of connected users
     * @param server the server that this console is connected to
     */
    public CommandInterpreter(HashSet<SomnoServerThread> connectedUsers, SomnoServer server) {
        this.connectedUsers = connectedUsers;
        hostServer = server;
    }

    /**
     * Starts the thread
     */
    public void run() {
        BufferedReader cmdReader = new BufferedReader(new InputStreamReader(System.in));
        //endless loop of grabbing commands from the server console
        String cmd;
        while (!isShutDown) {
            try {
                cmd = cmdReader.readLine();
                executeCommand(cmd);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Executes a command server-side.
     * @param cmd the command to try and execute
     * @throws IOException any ioexception
     */
    private void executeCommand(String cmd) throws IOException {
        String[] cmdSplit = cmd.split(" ", 2);
        boolean successful = false;
        switch (cmdSplit[0]) {
            case "say":
            case "s":
                //send a message to every user and log it in the console
                for (SomnoServerThread thread : connectedUsers) {
                    thread.sendMessage("Server says: " + cmdSplit[1]);
                }
                break;
            case "kick":
                //kick a user and log it in the console
                //should eventually change HashSet to a HashMap with nicknames as keys
                for (SomnoServerThread thread: connectedUsers) {
                    if (thread.getNickname().equals(cmdSplit[1])) {
                        thread.kick();
                        successful = true;
                    }
                }

                if (!successful) {
                    System.out.println("Error: no such user " + cmdSplit[1]);
                }

                break;
            case "shutdown":
                //disconnect every client and shut down the server
                isShutDown = true;
                for (SomnoServerThread thread : connectedUsers) {
                    thread.kick();
                }
                //interrupt the SomnoServer main thread, causing it to shut down
                hostServer.shutdown();
                break;
        }
    }

}
