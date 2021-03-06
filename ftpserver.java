import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

public class ftpserver extends Thread {
    private Socket connectionSocket;
    int port;
    int count = 1;

    public ftpserver(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }


    public void run() {
        if (count == 1)
            System.out.println("User connected" + connectionSocket.getInetAddress());
        count++;

        try {
            processRequest();

        } catch (Exception e) {
            System.out.println(e);
        }

    }


    private void processRequest() throws Exception {
        String fromClient;
        String clientCommand;
        byte[] data;
        String frstln;
        boolean terminate = false;
        while (!terminate) {
            if (count == 1)
                System.out.println("User connected" + connectionSocket.getInetAddress());
            count++;

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            fromClient = inFromClient.readLine();

            //System.out.println(fromClient);
            StringTokenizer tokens = new StringTokenizer(fromClient);

            frstln = tokens.nextToken();
            port = Integer.parseInt(frstln);
            clientCommand = tokens.nextToken();
            //System.out.println(clientCommand);


            if (clientCommand.equals("list:")) {
                String curDir = System.getProperty("user.dir");

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient =
                        new DataOutputStream(dataSocket.getOutputStream());
                File dir = new File(curDir);

                String[] children = dir.list();
                System.out.println(children);
                if (children == null) {
                    // Either dir does not exist or is not a directory
                } else {
                    for (int i = 0; i < children.length; i++) {
                        // Get filename of file or directory
                        String filename = children[i];

                        if (filename.endsWith(".txt"))
                            dataOutToClient.writeUTF(children[i]);
                        //System.out.println(filename);
                        if (i - 1 == children.length - 2) {
                            dataOutToClient.writeUTF("eof");
                            // System.out.println("eof");
                        }//if(i-1)


                    }//for

                    dataSocket.close();
                    //System.out.println("Data Socket closed");
                }//else


            }//if list:


            if (clientCommand.equals("retr:")) {
                String fileName = tokens.nextToken();
                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                File file = new File(fileName);
                if(file.exists()) {
                    dataOutToClient.writeBoolean(true);
                    Scanner fromFile = new Scanner(file);
                    while(fromFile.hasNext()) {
                        dataOutToClient.writeUTF(fromFile.nextLine() + "\n");
                    }
                    dataOutToClient.writeUTF("eof");
                    dataSocket.close();
                }
                else {
                    dataOutToClient.writeBoolean(false);
                }
            }

            if (clientCommand.equals("stor:")) {
                Socket connection = new Socket(connectionSocket.getInetAddress(), port);
                String fileName = tokens.nextToken();
                DataInputStream uploadingFile = new DataInputStream(new
                        BufferedInputStream(connection.getInputStream()));

                boolean fileFound = uploadingFile.readBoolean();

                if (fileFound) {
                    File testFile = new File(fileName);
                    if (!testFile.exists()) {
                        PrintWriter file = new PrintWriter(new FileWriter(fileName, true));
                        String line = uploadingFile.readUTF();
                        while (!line.equals("eof")) {
                            file.write(line);
                            line = uploadingFile.readUTF();
                        }
                        file.close();
                        connection.close();

                    }
                }

                uploadingFile.close();
            }//main
        if(clientCommand.equals("close")){
            System.out.println("User " + connectionSocket.getInetAddress() + " has disconnected.");
            this.connectionSocket.close();
            terminate = true;
        }
        }
    }
}
