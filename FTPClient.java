import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
class FTPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        int port1 = 6790;
        int port = 3734;
        String statusCode;
        boolean clientgo = true;

        System.out.println("Welcome to the simple FTP App   \n     Commands  \nconnect servername port# connects to a specified server \nlist: lists files on server \nretr: fileName.txt downloads that text file to your current directory \nstor: fileName.txt Stores the file on the server \nclose terminates the connection to the server");
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);


        if (sentence.startsWith("connect")) {
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            System.out.println("You are connected to " + serverName);
            Socket ControlSocket = new Socket(serverName, port);
            while (isOpen && clientgo) {

                sentence = inFromUser.readLine();
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                if (sentence.equals("list:")) {

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);


                    System.out.println("\n \n \nThe files on this server are:");
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = inData.readUTF();
                        if (modifiedSentence.equals("eof"))
                            break;
                        System.out.println("	" + modifiedSentence);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    System.out.println("\nWhat would you like to do next: \nretr: file.txt ||  stor: file.txt  || close");

                } else if (sentence.startsWith("retr: ")) {
                    port = port + 2;
                    StringTokenizer tokenizer = new StringTokenizer(sentence);
                    tokenizer.nextToken();
                    if(!tokenizer.hasMoreTokens()) {
                        System.out.println("No file name included");
                    }
                    else {
                        String fileName = tokenizer.nextToken();
                        if(!fileName.endsWith(".txt")) {
                            System.out.println("File must be a .txt file");
                        }
                        else {
                            ServerSocket welcomeData = new ServerSocket(port);
                            outToServer.writeBytes(port + " " + sentence + "\n");
                            Socket dataSocket = welcomeData.accept();
                            DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                            boolean fileFound = inData.readBoolean();
                            if(fileFound) {
                                File temp;
                                boolean exists = false;
                                try{
                                    temp = File.createTempFile(fileName, ".txt");
                                    exists = temp.exists();
                                }catch (Exception e){

                                }
                                if(!exists){
                                PrintWriter file = new PrintWriter(new FileWriter(fileName, true));
                                String dataLine = inData.readUTF();
                                while (!dataLine.equals("eof")) {
                                    file.write(dataLine);
                                    dataLine = inData.readUTF();
                                }
                                file.close();
                                inData.close();
                                welcomeData.close();
                                System.out.println("File successfully retrieved");
                                }else {
                                    System.out.println("File Already Exists");
                                }
                            }
                            else {
                                System.out.println("File not found on server");
                            }
                        }
                    }
                } else if (sentence.startsWith("stor: ")){
                    port = port + 2;
                    StringTokenizer tokenizer = new StringTokenizer(sentence);
                    tokenizer.nextToken();
                    String fileName = tokenizer.nextToken();
                    FileInputStream fileInput = null;
                    boolean fileExists = true;

                    try {
                        fileInput = new FileInputStream(fileName);
                    } catch(FileNotFoundException e) {
                        fileExists = false;
                    }

                    try{
                    if (fileExists){
                        ServerSocket socket = new ServerSocket(port);
                        outToServer.writeBytes(port + " " + sentence + "\n");
                        Socket dataSocket = socket.accept();
                        DataOutputStream upload = new DataOutputStream
                                (dataSocket.getOutputStream());

                        System.out.println("File uploading to server...Please wait...\n");
                        upload.writeBoolean(true);

                        Scanner fromFile = new Scanner(fileInput);
                        while(fromFile.hasNext()){
                            upload.writeUTF(fromFile.nextLine() + "\n");
                        }
                        upload.writeUTF("eof");
                        fileInput.close();
                        fromFile.close();
                        upload.close();
                        System.out.print("Uploading file done...");
                        dataSocket.close();
                    } else {
                        System.out.println("File not found in client directory.");
                    }}catch (java.net.SocketException e){
                        System.out.println("File Already On Server");

                    }

                    System.out.println("\nWhat would you like to do next: \n" +
                            "list: || retr: file.txt ||  stor: file.txt  || close");

                } else {
                    if (sentence.equals("close")) {
                        outToServer.writeBytes(port + " close " + "\n");
                        ControlSocket.close();
                        System.out.println("You have disconnected.");
                    }else {
                        System.out.print("No server exists with that name or server not listening on that port try again");
                    }
                    }
            }
        }
    }
}