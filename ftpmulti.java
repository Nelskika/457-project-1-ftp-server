import java.net.*;
import java.io.*;

public class ftpmulti {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;
	ftpserver w;

        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        } catch (IOException e) {
            System.err.println("Could not listen on port: 1200.");
            System.exit(-1);
        }

	while (listening)
	{
	    
	    w = new ftpserver(serverSocket.accept());
	    Thread t = new Thread(w);
	    t.start();
	   
	   
	}

       
    }
}

