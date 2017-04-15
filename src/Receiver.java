import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class Receiver {
	int portNumber;
	String fileName;
	double probability;

	public Receiver(int portNumber, String fileName, double probability) {
		this.portNumber = portNumber;
		this.fileName = fileName;
		this.probability = probability;
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
		double r = Math.random();
		Receiver rec = new Receiver(portNumber, fileName, probability);
		// BufferedWriter out = new BufferedWriter(new FileWriter(args[1],
		// true));
		DatagramSocket server = new DatagramSocket(rec.portNumber);
		ByteArrayOutputStream incomingdata = new ByteArrayOutputStream();
		int flag = 0;
		while (flag == 0) {
			/*
			 * BufferedWriter out = null; File myfile = new File(args[1]);
			 * Writer writer = new FileWriter(myfile,true); out = new
			 * BufferedWriter(writer);
			 */
			try {
				byte[] recieveData = new byte[1024];
				DatagramPacket senderData = new DatagramPacket(recieveData, recieveData.length);
				server.receive(senderData);
				String receive = new String(senderData.getData());
				String seq = receive.substring(0, 32);
				int s = 0;
				for (int i = 0; i < seq.length(); i++) {

					if (seq.charAt(i) == '1') {
						s = s + (int) Math.pow(2, seq.length() - 1 - i);
					}
				}
				String checksum = receive.substring(32, 48);
				String type = receive.substring(48, 64);
				String data = receive.substring(64, receive.length());
				if (type.equals("1111111111111111")) {
					flag = 1;
				}
				if (r <= rec.probability) {
					System.out.println("Packet Loss Sequence Number =" + s);
				} else if ((rec.createChecksum(data.getBytes(), checksum.getBytes()).equals("0000000000000000"))) {
					if (s == index) {
						String ack = rec.createACK(s);
						InetAddress addr = senderData.getAddress();
						int port = senderData.getPort();
						byte[] b = ack.getBytes();
						DatagramPacket sendpacket = new DatagramPacket(b, b.length, addr, port);
						server.send(sendpacket);
						incomingdata.write(data.getBytes());
						index++;
					}

				}

				// incomingdata.write(senderData.getData());
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println(e);
			}
			 FileOutputStream out = new FileOutputStream(fileName);
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

	public String createACK(int seqno) {
		String seq = Integer.toBinaryString(seqno);
		String check = "0000000000000000";
		String field = "1010101010101010";
		String ack = seq + check + field;
		System.out.println(ack);
		return ack;

	}

	public String createChecksum(byte[] segment, byte[] check) {
		int length = segment.length;
		int Word, Checksum = 0;

		while (length > 0) // len = Total num of bytes
		{
			int i = 0;
			Word = ((segment[i] << 8) + segment[i + 1]) + Checksum + ((check[i] << 8) + check[i + 1]);

			Checksum = Word & 0x0FFFF; // Discard the carry if any

			Word = (Word >> 16); // Keep the carryout for value exceeding 16 Bit

			Checksum = Word + Checksum; // Add the carryout if any

			length -= 2; // decrease by 2 for 2 byte boundaries
			i += 2;
		}

		Checksum = ~Checksum;
		return Integer.toBinaryString(Checksum);

	}
}
