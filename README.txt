SomnoIRC is a small custom instant-messaging program that I built after my first year at RIT. I wouldn't recommend using it in 
production as it has several issues, but it is useful as a reference for a simple socket-based infrastructure. I might expand on it
in the future for other projects, or rewrite it in something like Python. 

With that said, here are the instructions for this program.

When you start either program, Windows should ask you if you want to allow it through the firewall. Let it. If you have
another firewall installed, let it through that as well. On Linux you will need to add it to your own firewall manually.

The server should be fairly easy to run. Start it from the command prompt with this command:

java -jar SomnoServer port

If it fails to recognize java, add it to the path. You can specify a port to run it on, but if you don't, it
will launch on 27034. That port shouldn't interfere with any other program, and it is easy to customize if it does.

You will need to use port-forwarding to make the server and client work properly outside your home net. 

Once the server has launched, it will start trying to accept connections. You will also be able to give it commands.
Type "say" or "s" to say something to all connected users. Type "kick" and then a connected user's nickname to kick
them from the server. Finally, when you want to shut down the server, type "shutdown".

The client is similarly easy to run. When you run it, give it an ip and a port to try and connect to, along with a
nickname. Navigate to the correct folder in your shell, then use this command:

java -jar SomnoClient ip port nickname

Type anything into the console to send it as a message. If the server is up, your message should echo back with your
nickname and timestamp. When you want to shut down, type /logout and you should disconnect.