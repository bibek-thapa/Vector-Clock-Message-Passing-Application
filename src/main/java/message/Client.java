package message;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import clock.VectorClock;
import message.MessageComparator;
import queue.PriorityQueue;






public class Client {

	private static DatagramSocket socket;
	private static InetAddress address;
	private static VectorClock vectClock;
	private static int port;
	private static Message message;
	private static Message topMessage;
	private static Scanner scanner;
	private static String userName;
	private static int initial_value = 0;
	
	private static int current_pid;

	Client(String name) {
		userName = name;
		vectClock = new VectorClock();
		scanner = new Scanner(System.in);
		

		
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName("localhost");
		} catch (Exception e) {
			e.printStackTrace();
		}
		port = 8000;

		message = new Message(MessageTypes.REGISTER, name, 0, null, MessageTypes.SUCCESFULLY_REGISTERED);
		Message.sendMessage(message, socket, address, port);

		Thread t1 = new Thread(new MessageRecieve());
		t1.start();

	}

	class MessageRecieve implements Runnable {

		public void run() {

			boolean done = false;
			MessageComparator messageComparator = new MessageComparator();
			PriorityQueue<Message> queue = new PriorityQueue<Message>(messageComparator);
			while (!done) {

				message = Message.receiveMessage(socket);
			

				if (message.type == MessageTypes.ERROR) {
					done = true;
					System.out.println(message.message);
					System.exit(0);
				} else if (message.type == MessageTypes.ACK) {
					
					current_pid = message.pid;
					vectClock.addProcess(current_pid, initial_value);
					System.out.println(message.sender + " with processor id "+ message.pid+ " is succesfully registered");

				} else if (message.type == MessageTypes.CHAT_MSG) {

					if (message.message != "exit") {
						
						queue.add(message);
						topMessage=queue.peek();
						
						while(topMessage!=null) {
							boolean seenAll=true;
							int topMsgTime = 0;
		        			
							for (String key: topMessage.ts.clock.keySet()) {
								
		        				if (  Integer.parseInt(key) != topMessage.pid && Integer.parseInt(key) != current_pid) {
		        					if (vectClock.clock.containsKey(key)) {
		        						
		        						if (topMessage.ts.getTime(Integer.parseInt(key)) > vectClock.getTime(Integer.parseInt(key)) ) { seenAll = false;}
		        					
		        					}else {
		        						
		        						seenAll = false;
		        					}
		        				}
		        			}	
							
						if(vectClock.clock.containsKey(Integer.toString(topMessage.pid)))
		        		{
		        				topMsgTime = vectClock.getTime(topMessage.pid);
		        				
		        		}
					
						int topmessage_value = topMessage.ts.getTime(topMessage.pid);
						
					
						if(topmessage_value==topMsgTime+1 && seenAll) 
						{
							System.out.println(topMessage.sender+":"+topMessage.message);
							queue.poll();
							vectClock.update(topMessage.ts);
							topMessage=queue.peek();
							
						}
						else {
						
						topMessage=null;}
					}	
						
						}

					}

					

				}

			}
		
		
		
		
		

		}
	

	void sendMessageMethod() {
		boolean done = false;
		while (!done) {

			
			String cmd = scanner.nextLine();
			

			if (cmd.equals("exit")) {
				done = true;
				System.exit(0);
			}
			vectClock.tick(current_pid);
			message = new Message(MessageTypes.CHAT_MSG, userName,current_pid, vectClock, cmd);
			Message.sendMessage(message, socket, address, port);


		}

	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your name");
		String name = scanner.next();
		Client client = new Client(name);
		client.sendMessageMethod();
		scanner.close();

	}

}
