package message;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import clock.VectorClock;
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
	private MessageComparator messageComparator;
	private static int current_pid;

	Client(String name) {
		userName = name;
		vectClock = new VectorClock();
		scanner = new Scanner(System.in);
		

		messageComparator = new MessageComparator();
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
							boolean hasSeenAll=true;
							int myClockTopMsgTime = 0;
		        			if(vectClock.clock.containsKey(Integer.toString(topMessage.pid))) { myClockTopMsgTime = vectClock.getTime(topMessage.pid); }
							for (String key: topMessage.ts.clock.keySet()) {
		        				if (Integer.parseInt(key) != current_pid && Integer.parseInt(key) != topMessage.pid) {
		        					if (vectClock.clock.containsKey(key)) {
		        						if (topMessage.ts.getTime(Integer.parseInt(key)) > vectClock.getTime(Integer.parseInt(key)) ) { hasSeenAll = false;}
		        					}else {
		        						hasSeenAll = false;
		        					}
		        				}
		        			}	
					
						int topmessage_value = topMessage.ts.getTime(topMessage.pid);
						int currentmessage_value=message.ts.getTime(message.pid);
					
						if(topmessage_value==myClockTopMsgTime+1 && hasSeenAll) 
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

			System.out.println("Inside the sendMessageMethod");
			String cmd = scanner.nextLine();
			vectClock.tick(current_pid);
			message = new Message(MessageTypes.CHAT_MSG, userName,current_pid, vectClock, cmd);
			Message.sendMessage(message, socket, address, port);

			if (cmd.equals("exit")) {
				done = true;
				System.exit(0);
			}

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