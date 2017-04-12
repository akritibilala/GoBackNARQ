import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Receiver {
	public static void main(String args[]) throws IOException {
		
		Scanner sc = new Scanner(System.in);
		    System.out.println("Enter the port number: ");
			int portnumber = sc.nextInt();
			System.out.println("Enter the filepath: ");
			String filename = sc.next();
			System.out.println("Enter the probability: ");
			double probability = sc.nextDouble();
			
	

		// BufferedWriter out = new BufferedWriter(new FileWriter(args[1],
		// true));
		DatagramSocket server = new DatagramSocket(portnumber);
		byte[] recieveData = new byte[1024];
		ByteArrayOutputStream incomingdata = new ByteArrayOutputStream();
		int flag = 0;
		while (flag == 0) {
			/*
			 * BufferedWriter out = null; File myfile = new File(args[1]);
			 * Writer writer = new FileWriter(myfile,true); out = new
			 * BufferedWriter(writer);
			 */
			try {
				DatagramPacket senderData = new DatagramPacket(recieveData, recieveData.length);
				server.receive(senderData);
				incomingdata.write(senderData.getData());
			} catch (Exception e) {
				// TODO: handle exception
				flag = 1;

			}
			FileOutputStream out = new FileOutputStream(filename);
			incomingdata.writeTo(out);
			out.close();
			server.close();

			/*
			 * String recievedData = new String(senderData.getData());
			 * 
			 * out.write(recievedData); out.close();
			 */
			// System.out.print(" Recieved :" + recievedData);
		}
	}
}
