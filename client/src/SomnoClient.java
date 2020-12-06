import java.io.*;
import java.net.*;

/**
 * Defines a SomnoClient which connects to a SomnoServer
 *
 * KNOWN BUGS:
 * successfully logging out triggers the "kicked" message
 *
 * TODO Add sound effects, protocol, encryption
 * TODO add command that toggles sound
 *
 */
public class SomnoClient implements SomnoProtocol {
    private Socket socket;

    public static void main(String[] args) {
        //starts the client

        int argcount = args.length; //need to pull this code block out into a launcher later
        int port = 27034;
        boolean sound = true;
        String pwd = "";
        String nick = "user";
        String ip = "localhost";

        //this is a bad way to do arg parsing but until i can install getopt it'll do
        for (int i = 0; i < argcount; i++) {
            switch (args[i]) {
                case "-prt":
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "-pwd":
                    pwd = args[i + 1];
                    break;
                case "-nick":
                    nick = args[i + 1];
                    break;
                case "-ip":
                    ip = args[i + 1];
                    break;
                case "-nosound":
                    sound = false;
                    break;
                default:
                    System.err.println("Error: unrecognized option " + args[i]);
                    System.err.println("Usage: java SomnoClient -ip [ip] -prt [port] -nick [nickname] -pwd [password]");
                    System.exit(1);
            }
            i++;
        }
        SomnoClient c = new SomnoClient(ip, port, nick, pwd, sound);

    }

    /**
     * Constructor for a SomnoClient
     * @param ip the hostname to connect to
     * @param port the port which it tries to connect to the host on
     * @param nickname the nickname the client will be using
     * @param pwd the password of the server
     */
    public SomnoClient(String ip, int port, String nickname, String pwd, boolean sound) {
        //first try to connect to the server
        //if successful, send the nickname and then start listening and sending information
        //sending will be handled on this thread, while listening will be handled on another

        try (BufferedReader userin = new BufferedReader(new InputStreamReader(System.in))){
            socket = new Socket(ip, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //if the user has input a password, send it with the attached "/pwd" format
            //otherwise, just send the nickname
            if(!pwd.equals("")) {
                out.println("/pwd " + pwd);
            }

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
            System.err.println("Invalid host address. Cannot continue.");
        } catch (IOException e) {
            System.err.println("IOException caught");
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
 * When a line is received, it prints it to the console
 * TODO add sound here
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
