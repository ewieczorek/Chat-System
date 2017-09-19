import java.io.*;
import java.net.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Scanner;

import javax.imageio.ImageIO;
	
public class chatClient {
	chatClient() throws UnknownHostException, IOException{
		Socket localSocket = new Socket("localhost", 4444);
		ServerListener sl;
		sl = new ServerListener(this, localSocket);
		new Thread(sl).start();
		
		String userName = "";
		Boolean exit = false;
		
		System.out.println(localSocket);
		PrintWriter out = new PrintWriter(new BufferedOutputStream(localSocket.getOutputStream()));
		
		System.out.println("Please enter your name: ");
		System.out.println("Only alphabet characters are allowed, one word only.");
		
		while(!exit){
			//the following code block asks for the users name and verifies that it is a valid name
			BufferedReader r = new BufferedReader (new InputStreamReader (System.in));
			String userInput = null;
			while(userInput == null && userName == ""){
				userInput = r.readLine();
				if(userInput.matches("\\w+")) {
					userName = userInput;
					out.println(userName);
					System.out.println("accepted user name choice " + userName);
				}else{
					System.err.println("Please enter a valid name");
					userInput = null;
				}
			}
			userInput = null;
			
			//The following code block is for deciding whether the user wants to send a text or an image message
			System.out.println("Enter 1 to send a text message");
			System.out.println("Enter 2 to send an image message");
			int userChoice = 0;
			while(userInput == null){
				userInput = r.readLine();
				if(userInput.matches("\\d")) {
					int userin = Integer.parseInt(userInput);
					if(userin == 1 || userin == 2){
						userChoice = userin;
						out.println(userChoice);
					}else{
						System.err.println("please enter a 1 or a 2");
					}
					
				}else{
					System.err.println("Please enter a a valid number");
					userInput = null;
				}
			}
			userInput = null;
			if(userChoice == 1){
				System.out.println("Please enter your message: ");
				while(userInput == null){
					userInput = r.readLine();
					if(userInput.matches("(?>\\w+ *)+")) {
						out.println(userInput);
						out.flush();
					}else{
						System.err.println("Please enter a valid message");
						System.out.println("Only alphanumeric characters are allowed");
						userInput = null;
					}
				}
			}else if(userChoice == 2){
				while(userInput == null){
					System.out.println("Please enter a valid file name: ");
					try{
						userInput = r.readLine();
						File tmpDir = new File(userInput); 
						if(tmpDir.exists()){
							String fileName = userInput + "_" + userName + "_" + System.currentTimeMillis() + ".jpg";
							out.println(fileName);
							out.flush();
							FileInputStream fis = new FileInputStream(userInput);
							byte[] buffer = new byte[fis.available()];
							fis.read(buffer);
							ObjectOutputStream oos = new ObjectOutputStream(localSocket.getOutputStream()) ;
							oos.writeObject(buffer);
							fis.close();
						}else{
							userInput = null;
						}
					}catch (FileNotFoundException e){
						System.err.println("File not found");
						userInput = null;
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		chatClient lc = new chatClient();
	}
}

class ServerListener implements Runnable {
	chatClient client;
	Scanner in;
	Socket localSocket;
	
	ServerListener(chatClient client, Socket s) {
		try {
			this.client = client;
			this.localSocket = s;
			in = new Scanner(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) { // run forever
			String message = in.nextLine();
			String fileName = "";
			if(message.equals("file incoming")){
				try {
					fileName = in.nextLine();
					receiveImage(fileName);
				} catch (ClassNotFoundException | IOException e) {
					
				}
			}
			System.out.println(message + ": " + fileName);
		}
	}
	
	public void receiveImage(String fileName) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(localSocket.getInputStream());
		byte[] buffer = (byte[])ois.readObject();
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(buffer);
		fos.close();
		in = new Scanner(new BufferedInputStream(localSocket.getInputStream()));
	}
}