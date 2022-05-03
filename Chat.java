import java.io.*;
import java.util.*;
import java.net.*;

public class Chat {

    
    static volatile ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    static volatile ArrayList<Boolean> booleanList = new ArrayList<Boolean>();
    static volatile ServerSocket ss;

    public static void main(String[] args) throws IOException {

        int serverPort = Integer.parseInt(args[0]);
        
        Socket s;

        String destination;
        int clientPort;
        String messageToSend;
        int userSelection;
        
        String inputCommand;
        Scanner inputScanner = new Scanner(System.in);
        Scanner scn = new Scanner(System.in);

        booleanList.add(true);

        new Thread(() -> {
            
            try {
                ss = new ServerSocket(serverPort);
                System.out.println();
                System.out.print("Server listening on port: ");
                System.out.println(serverPort);
                System.out.println();
                
                boolean isOn = true;
                Socket r;

                while(isOn) {
                    if (booleanList.get(0) == true) {
                        
                        r = ss.accept();

                        System.out.println();
                        System.out.println("New connection request received : " + r);
                        

                        //Obtain input and output streams
                        DataInputStream dis = new DataInputStream(r.getInputStream());
                        DataOutputStream dos = new DataOutputStream(r.getOutputStream());

                        System.out.println("Creating a new handler for this connection... ");
                        ClientHandler mtch = new ClientHandler(r, dis, dos);

                        //Create a new Thread with this new client
                        Thread t = new Thread(mtch);

                        System.out.println("Adding this connection to active connection list.");

                        //add this client to active clients list
                        clientHandlers.add(mtch);

                        //start the thread
                        t.start();
                        
                    } else {
                        isOn = false;
                        break;
                    }
                }
                ss.close();
            } catch (Exception e) {
                // System.out.println(e);
            }
            

            
        }).start();

        //Thread checks to see if a socket was closed by an external process
        new Thread(() -> {
            boolean isOn = true;
            while(isOn) {
                if (booleanList.get(0) == true) {
                    if (clientHandlers.isEmpty()){

                    } else {
                    
                        try {
                            for (int i = 0; i < clientHandlers.size(); i++) {
                                if (clientHandlers.get(i).s.isClosed()) {
                                    System.out.println();
                                    System.out.println(clientHandlers.get(i).s.getInetAddress() + " has been disconnected!");
                                    System.out.println();
                                    clientHandlers.remove(i);
                                }   
                            }
                        } catch (Exception e) {
                        }
                    }
                } else {
                    isOn = false;
                }
            }
        }).start();


        

        Scanner sc = new Scanner(System.in);

		int input;
		do {
            help();
			input = sc.nextInt();
			if (input == 1) {
                System.out.println();
				System.out.println("\nHELP MENU");
                System.out.println("=========\n");
                System.out.println("Available Commands:");
                System.out.println("-------------------");
                System.out.println("help                            -- This menu. Displays information about the available options.");
                System.out.println("myip                            -- Displays the IP address of this machine.");
                System.out.println("myport                          -- Displays the port on which this process is listening for incoming connections.");
                System.out.println("connect <destination> <port no> -- Establish a new TCP connection to the IP address at <destination> at the specified <port no>.");
                System.out.println("list                            -- Displays a numbered list of all the connections this process is part of.");
                System.out.println("terminate <connection id>       -- Terminates the connection listed under the specified number from the \'list\' command.");
                System.out.println("send <connection id> <message>  -- Send <message> to <connection id>");
                System.out.println("exit                            -- Closes all connections and terminates this process.");
                System.out.println();
                System.out.println();
			}
			if (input == 2) {
                System.out.println();
				System.out.println("Your IP is: " + InetAddress.getLocalHost().getHostAddress());
				System.out.println();
			}
			if (input == 3) {
				System.out.println();
                System.out.println("Your port is: " + serverPort);
				System.out.println();
			}
			if (input == 4) {
                System.out.println();
                System.out.println("Please enter a destination (IP Address): ");
                destination = inputScanner.nextLine();
                System.out.println("Please enter a port number: ");
                clientPort = sc.nextInt();

                //Attempts to connect to ServerSocket
                s = new Socket(destination, clientPort);

                //Obtain input and output streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println();
                System.out.println("New connection request received : " + s);

                System.out.println("Creating a new handler for this connection... ");
                ClientHandler mtch = new ClientHandler(s, dis, dos);

                //Create a new Thread with this new client
                Thread t = new Thread(mtch);

                System.out.println("Adding this connection to the active connection list.");

                //add this client to active clients list
                clientHandlers.add(mtch);

                //start the thread
                t.start();
            }
            if (input == 5) {

                System.out.println();
                
                if (clientHandlers.isEmpty()) {
                    System.out.println("No current Users");
                } else {
                    System.out.println("id:\t IP address \t \tPort No.");
                    for (int i = 0; i < clientHandlers.size(); i++) {
                        System.out.print(i + 1);
                        
                        System.out.println("\t" + clientHandlers.get(i).s.getInetAddress() + "\t\t" + clientHandlers.get(i).s.getLocalPort());
                    }
                }

                System.out.println();

                for (int i = 0; i < clientHandlers.size(); i++) {
                    System.out.println(clientHandlers.get(i).dis);
                }
                
            }
            if (input == 6) {

                System.out.println(); 

                if (clientHandlers.isEmpty()) {
                    System.out.println("No current users to disconnect.");
                } else {
                    System.out.println("Which user would you like to disconnect?");
                    System.out.print("Disconnect: ");
                    userSelection = sc.nextInt();
                    System.out.println();

                    try {
                        clientHandlers.get(userSelection - 1).dos.writeUTF("logout");
                        clientHandlers.get(userSelection - 1).dis.close();
                        clientHandlers.get(userSelection - 1).dos.close();
                        clientHandlers.get(userSelection - 1).s.close();
                    } catch (EOFException e) {
                        e.printStackTrace();
                    }

                }

                System.out.println();
            }
            if (input == 7) {

                System.out.println();
                
                if (clientHandlers.isEmpty()) {
                    System.out.println("No current connections to send a message to.");
                } else {

                    //prompt user to choose who they want to send a message to from the clientHandlers list
                    System.out.print("Who from the list would you like to send to? ");
                    userSelection = sc.nextInt();
                    
                    //prompt user for the message they would like to send
                    System.out.println("What message would you like to send");
                    messageToSend = inputScanner.nextLine();
                    
                    
                    //send message to specified user in clientHandlers list
                    clientHandlers.get(userSelection - 1).dos.writeUTF(messageToSend);

                }

                System.out.println();

            } 
		} while (input != 8);
        System.out.println("Exiting...");

        for (int i = 0; i < clientHandlers.size(); i++) {
            clientHandlers.get(i).dos.writeUTF("logout");
            clientHandlers.get(i).dis.close();
            clientHandlers.get(i).dos.close();
            clientHandlers.get(i).s.close();
            clientHandlers.remove(i);
        }

        booleanList.set(0, false);
        
        ss.close();
        System.out.println("Goodbye!");
    }

    public static void help() {
        System.out.println("1. Help");
        System.out.println("2. My IP");
        System.out.println("3. My port");
        System.out.println("4. Connect");
        System.out.println("5. List");
        System.out.println("6. Terminate");
        System.out.println("7. Send Messages");
        System.out.println("8. Exit");
        System.out.println("");
        System.out.print("Enter input: ");

    }

}

class ClientHandler implements Runnable {

    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    // boolean isLoggedIn;

    //constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    @Override
    public void run() {
        String received;
        boolean isOn = true;

        if (this.s.isClosed() == true){
            isOn = false;
        }
        while (isOn) {
            if (this.s.isClosed()) {
                break;
            } else {

            
                try {
                    if(this.s.isClosed()) {
                        break;
                    }
                    received = dis.readUTF();

                    System.out.println(received);

                    if (received.equals("logout")){
                        this.s.close();
                        this.dis.close();
                        this.dos.close();
                        break;
                    }
                    
                    System.out.println();
                    System.out.println("Message received from: " + this.s.getInetAddress());
                    System.out.println("Senders port: " + this.s.getPort());
                    System.out.println("Message " + received);
                    System.out.println();
                    

                } catch (IOException e) {
                    
                    // e.printStackTrace();
                }
            }
        } 
    }
}

