package GoBack;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Receiver {
	public int portNumber;
	String fileName;
	double probability;
	String field;
	Acknowledgement ack;
	ChecksumMethod checksummethod;

	public Receiver(int portNumber, String fileName, double probability) {
		this.portNumber = portNumber;
		this.fileName = fileName;
		this.probability = probability;
		this.field = "1010101010101010";
		this.ack = new Acknowledgement();
		this.checksummethod = new ChecksumMethod();
	}

	public static void main(String args[]) throws IOException {
		int index = 0;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the port number: ");
		int portNumber = sc.nextInt();
		System.out.println("Enter the filepath: ");
		String fileName = sc.next();
		System.out.println("Enter the probability: ");
		double probability = sc.nextDouble();
		Receiver rec = new Receiver(portNumber, fileName, probability);
		DatagramSocket server = new DatagramSocket(rec.portNumber);
		ByteArrayOutputStream incomingdata = new ByteArrayOutputStream();
		int loopBreaker = 0;
		while (loopBreaker == 0) {
			try {
				byte[] recieveData = new byte[2048];
				DatagramPacket senderData = new DatagramPacket(recieveData, recieveData.length);
				server.receive(senderData);
				String receive = new String(senderData.getData());
				receive = receive.substring(0, senderData.getLength());
				String seq = receive.substring(0, 32);
				int s = 0;
				for (int i = 0; i < seq.length(); i++) {

					if (seq.charAt(i) == '1') {
						s = s + (int) Math.pow(2, seq.length() - 1 - i);
					}
				}
				System.out.println("Received packet with seq: "+s);
				String checksum = receive.substring(32, 48);
				int c = 0;
				for (int i = 0; i < checksum.length(); i++) {

					if (checksum.charAt(i) == '1') {
						c = c + (int) Math.pow(2, checksum.length() - 1 - i);
					}
				}
				String type = receive.substring(48, 64);
				String data = receive.substring(64, receive.length());
				if (type.equals("1111111111111111")) {
					loopBreaker = 1;
					break;
				}
				byte[] d = senderData.getData();
				double r = Math.random();
				if (!type.equals("0101010101010101"))
					continue;
				if (r <= rec.probability) {
					System.out.println("Packet loss, sequence number = " + s);
				} else {
					if (rec.receive(data, c) == 0) {
						if (s == index) {
							System.out.println("Sending ack for: " + s);
							String ack = rec.ack.createACK(s);
							InetAddress addr = senderData.getAddress();
							int port = senderData.getPort();
							byte[] b;
							b = ack.getBytes();
							DatagramPacket sendpacket = new DatagramPacket(b, b.length, addr, port);
							server.send(sendpacket);
							incomingdata.write(data.getBytes());
							index++;
						}
					}
				}

			} catch (Exception e) {
				System.err.println(e);
			}
		}
		FileOutputStream out = new FileOutputStream(fileName);
		incomingdata.writeTo(out);
		out.close();
		server.close();
	}

	public int receive(String s, int checksum) {
		int check = this.checksummethod.generateChecksum(s);
		check = 65535 - check;
		int intermidiate = check + checksum;
		intermidiate = 65535 - intermidiate;
		return intermidiate;
	}
}
