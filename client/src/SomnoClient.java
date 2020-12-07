import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

/**
 * Defines a SomnoClient which connects to a SomnoServer
 *
 * KNOWN BUGS:
 * successfully logging out triggers the "kicked" message
 *
 * TODO Add sound effects, protocol, encryption
 *
 */
public class SomnoClient implements SomnoProtocol {
    private Socket socket;

    public static void main(String[] args) {
        //starts the client

        int argcount = args.length; //need to pull this code block out into a launcher later
        int port = 27034;
        boolean soundOn = true;
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
                    soundOn = false;
                    break;
                default:
                    System.err.println("Error: unrecognized option " + args[i]);
                    System.err.println("Usage: java SomnoClient -ip [ip] -prt [port] -nick [nickname] -pwd [password]");
                    System.exit(1);
            }
            i++;
        }
        SomnoClient c = new SomnoClient(ip, port, nick, pwd, soundOn);

    }

    /**
     * Constructor for a SomnoClient
     * @param ip the hostname to connect to
     * @param port the port which it tries to connect to the host on
     * @param nickname the nickname the client will be using
     * @param pwd the password of the server
     */
    public SomnoClient(String ip, int port, String nickname, String pwd, boolean soundOn) {
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
            Listener l = new Listener(socket, this, soundOn);
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
 * When a line is received, it prints it to the console and, if the user chose,
 * plays a sound
 */
class Listener extends Thread implements SomnoProtocol{
    private Socket socket;
    private SomnoClient hostclient;
    private boolean soundOn;


    /**
     * Creates a new Listener object
     * @param socket the socket that the thread listens on
     */
    public Listener(Socket socket, SomnoClient hostclient, boolean soundOn) {
        this.socket = socket;
        this.hostclient = hostclient;
        this.soundOn = soundOn;
    }

    /**
     * Plays a sound file
     * @param filename the name of the file to play
     */
    public void playSound(String filename) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File("../../resources/audio/" + filename).getAbsoluteFile());
        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        clip.start();
    }

    public void run() {
        String received;
        String soundFileName = "msg_received.wav";
        int logoutCode = 0;
        try (BufferedReader receive = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))){
            label:
            while (true) {
                received = receive.readLine();

                String[] split = received.split(" ", 2);
                String header = split[0];
                String body = split[1];

                switch (header) {
                    case stdMsgHeader:
                        System.out.println(body);
                        soundFileName = "msg_received.wav";
                        break;
                    case logoutHeader:
                        logoutCode = Integer.parseInt(body);
                        break label;
                    case userJoinHeader:
                        System.out.println(body);
                        soundFileName = "user_join.wav";
                        break;
                    case userLeaveHeader:
                        System.out.println(body);
                        soundFileName = "user_leave.wav";
                        break;
                }

                if (soundOn) {
                    //play the appropriate sound
                    try {
                        playSound(soundFileName);
                    } catch (Exception e) {
                        System.err.println("Error playing file: " + e);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred!");
            logoutCode = 0;
            if(soundOn) {
                try {
                    playSound("err.wav");
                } catch (Exception f) {
                    System.err.println("Error playing file: " + f);
                }
            }
        } finally {
            if (logoutCode == 1) {
                hostclient.forceClose("kicked");
                if(soundOn) {
                    try {
                        playSound("err.wav");
                    } catch (Exception f) {
                        System.err.println("Error playing file: " + f);
                    }
                }
            } else {
                hostclient.forceClose("connection closed");
            }
        }
    }

}
