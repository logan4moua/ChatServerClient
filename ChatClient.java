/**
 * @author Surin Assawajaroenkoon
 * ChatClient class
 */
//package com.me.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
	
	int port = 4444; // port which the host is listening
	String host = "127.0.0.1"; // host computer or server
	Socket socket; // socket to read and write stream
	Thread cThread; // thread to start a chat session
	Thread msgUpdateThread; // thread to update messaging from other users in the same server
	
	BufferedReader reader; // to read incoming streams from chat server
	BufferedReader userInput; // to read user typing input from user console
	PrintWriter writer; // to write outgoing streams to server
	
	private boolean isClose = true;
	
	public ChatClient(){
		try {
			socket = new Socket(host, port);
		} catch (UnknownHostException e) {
			System.out.println("Host unknown exception"+ e.getMessage());
			
		} catch (IOException e) {
			System.out.println("I/O Exception"+ e.getMessage());
		}
		
		Runnable sessionRunnable = new Runnable(){
			public void run(){
				startChat();
			}
		};
		
		cThread = new Thread(sessionRunnable);
		cThread.start();
		
		Runnable msgUpdateRunnable = new Runnable(){
			public void run(){
				msgUpdate();
			}
		};
		
		msgUpdateThread = new Thread(msgUpdateRunnable);	
	}
	
	/**
	 * chat session start
	 */
	private void startChat(){
		if(isClose){
			isClose = openChannel();
			msgUpdateThread.start();
		}

		String line = new String();
		
		// welcoming session
		System.out.println("Welcome!");
		System.out.println("Enter your name:");
		
		try {			
			writer.println(userInput.readLine());
		} catch (IOException e1) {
			System.out.println("Failed to sent or read message: "+ e1.getMessage());
		}
		
		// chat starts
		try {
			// if user enter input and cThread still running
			while((line = userInput.readLine()) != null){ 																						
				
				if(line.compareTo("EXIT")==0){					
					writer.println(line);
					break;
				}
				writer.println(line); // sending message to server
			}
		} catch (IOException e) {
			System.out.println("Failed to sent or read message: "+ e.getMessage());
		}
		
		close();
	}
	/**
	 * reading stream messages from server
	 */
	private void msgUpdate(){
		String line = new String();

		try {
			while((line = reader.readLine()) != null){
				
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println("Message reading failed: "+ e.getMessage());
		}
	}
	
	/**
	 * open reader, writer, and user input read
	 */
	private boolean openChannel(){
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			userInput = new BufferedReader(new InputStreamReader(System.in));
			writer = new PrintWriter(socket.getOutputStream(), true); 
			
			return false;
		} catch (IOException e) {
			System.out.println("Failed to open reader or writer");
			System.out.println(e.getMessage());
			
			return true;
		}
	}
	
	/**
	 * close reader, writer and interrupt current running threads.
	 */
	@SuppressWarnings("deprecation")
	private void close(){
		try {
			reader.close();
			writer.close();
			
			if(cThread != null){
				cThread.stop();
				cThread = null;
			}
			if(msgUpdateThread != null){
				msgUpdateThread.stop();
				msgUpdateThread = null;
			}

		} catch (IOException e) {
			System.out.println("Failed to close reader: "+ e.getMessage());
		}
	}
	
	public static void main(String [] args){
		new ChatClient();
	}
	
}
