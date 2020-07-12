SomnoIRC is not protected under any license. This is because it sucks. Any reasonable implementation of it would
have to change the source code so much that it'd functionally be a different program. Even I, a gaming youtuber who
wrote this after his first year at uni, can see the obvious problems in this. It isn't encrypted, it's full of
memory leaks waiting to go off at any moment, and it barely works even with two threads for every client or server.
If you want a secure IRC, there's thousands of better programs out there. If this ever stops working, please delete
the program and then burn your hard drive to keep it from spreading.

With that said, here are the instructions for this cursed program.

When you start either program, Windows should ask you if you want to allow it through the firewall. Let it. If you have
another firewall installed, let it through that as well. If you're not running windows and are instead on Linux, then
you should know better than to run this garbage.

The server should be fairly easy to run. Start it from the command prompt with this command:

java -jar SomnoServer port

If it fails to recognize java as a thing, add it to the path. You can specify a port to run it on, but if you don't, it
will launch on 27034. That port shouldn't interfere with any other program, so long as Wikipedia didn't lie to me.

Once the server has launched, it will start trying to accept connections. You will also be able to give it commands.
Type "say" or "s" to say something to all connected users. Type "kick" and then a connected user's nickname to kick
them from the server. Finally, when you want to shut down the server, type "shutdown".

The client is similarly easy to run. When you run it, give it an ip and a port to try and connect to, along with a
nickname. Navigate to the correct folder in your shell, then use this command:

java -jar SomnoClient ip port nickname

Type anything into the console to send it as a message. If the server is up, your message should echo back with your
nickname and timestamp. When you want to shut down, type /logout and you should disconnect.

If you want to talk to me about making this not a pile of trash, email me at taf5135@rit.edu.