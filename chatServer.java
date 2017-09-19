import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class chatServer {
	static ArrayList<clientHandler> clientList = new ArrayList(); //this will hold the list of all of the clients
	chatServer(){
		ServerSocket serverSocket = null; //the socket the server runs on
		int clientNumber = 0; //to keep track of the number of clients.
		
		try {
			serverSocket = new ServerSocket(4444); // provide a socket at port 4444
			System.out.println(serverSocket);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
			System.exit(-1);
		}
		
		while(true) { //loops forever so that the server is always listening   
			Socket clientSocket = null;
			try{
				clientSocket = serverSocket.accept();
				clientNumber++;
				clientHandler handleClient = new clientHandler(this, clientSocket, clientNumber);
				clientList.add(handleClient);
				Thread serverThread = new Thread(handleClient);
				serverThread.start();
			} catch (IOException e) {
				System.err.println("Accept failed: 4444");
				System.exit(-1);
			} catch (NoSuchElementException e){
				e.printStackTrace();
			}
		}
	}
	
	public void sendAllClientsMessage(String currentUser, String message){
		if(!clientList.isEmpty()){
			try{
				for(int i = 0; i < clientList.size(); i++){
					if(!clientList.get(i).userName.equals(currentUser)){
						clientList.get(i).sendMessage(message);
					}
				}
			}catch (NullPointerException e){
				
			}
		}
	}
	
	public void sendAllClientsImage(String currentUser, String fileName) throws IOException{
		if(!clientList.isEmpty()){
			this.sendAllClientsMessage(currentUser, "file incoming");
			this.sendAllClientsMessage(currentUser, fileName);
			try{
				for(int i = 0; i < clientList.size(); i++){
					if(!clientList.get(i).getUserName().equals(currentUser)){
						clientList.get(i).sendImage(fileName);
					}
				}
			}catch (NullPointerException e){
				
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		chatServer cs = new chatServer();
	}
}

class clientHandler implements Runnable {
	Socket chatSocket = null; // this is socket on the server side that connects to the client
	int userNumber = 0; // keeps track of the socket's number for identifying purposes
	String userName = null; // The name of the user on the current client
	File userImage = null;
	Scanner in;
	PrintWriter out;
	chatServer cs;
	
	public String getUserName(){
		return userName;
	}
	
	clientHandler(chatServer cs, Socket chatSocket, int userNum) {
		try{
			this.chatSocket = chatSocket;
			this.userNumber = userNum;
			this.cs = cs;
			in = new Scanner(new BufferedInputStream(chatSocket.getInputStream()));
			out = new PrintWriter(new BufferedOutputStream(chatSocket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO 
		// 1. USE THE SOCKET TO READ WHAT THE CLIENT IS SENDING
		while(true){
			if(userName == null){
				userName = in.nextLine();
			}
			int userChoice = Integer.parseInt(in.nextLine());
			if(userChoice == 1){
				String userMessage = (userName + ": " + in.nextLine());
				System.out.println(userMessage);
				writeMessageToFile(userMessage);
				cs.sendAllClientsMessage(userName, userMessage);
			} else if(userChoice == 2){
				String fileName = in.nextLine();
				System.out.println(userName + ": " + fileName);
				try {
					writeMessageToFile(userName + ": " + fileName);
					receiveImage(fileName);
					cs.sendAllClientsImage(userName, fileName);
				} catch (ClassNotFoundException | IOException e) {
					
				}
			}	
		}
		
	}
	
	public void sendMessage(String message){
		out.println(message);
		out.flush();
	}
	
	public void sendImage(String fileName) throws IOException{
		try{
			FileInputStream fis = new FileInputStream(fileName);
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			ObjectOutputStream oos = new ObjectOutputStream(chatSocket.getOutputStream()) ;
			oos.writeObject(buffer);
			fis.close();
		}catch (FileNotFoundException e){
			System.err.println("File not found");
		}
	}
	
	public void receiveImage(String fileName) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(chatSocket.getInputStream());
		byte[] buffer = (byte[])ois.readObject();
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(buffer);
		fos.close();
	}
	
	public void writeMessageToFile(String message) throws IllegalStateException 
	{
		 try{
//			 Scanner reader = new Scanner(new FileReader("chat.txt"));
//			 int lineCount = 1;
//			 while(reader.hasNextLine()){
//				 lineCount++;
//			 }
			 
			 BufferedWriter output = null;
			 output = new BufferedWriter(new FileWriter("chat.txt", true));
			 output.newLine();
			 output.write(message); // lineCount + "; " +  
			 output.close();
			 in = new Scanner(new BufferedInputStream(chatSocket.getInputStream()));
			 out = new PrintWriter(new BufferedOutputStream(chatSocket.getOutputStream()));
		 }catch(FileNotFoundException e1){
			 System.out.println("Output File Not Found");
		 }catch(IOException e2){
			 
		 }
	}
}