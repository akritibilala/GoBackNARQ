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
		int flag = 0;
		while (flag == 0) {
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
					flag = 1;
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
		int syndrome = check + checksum;
		syndrome = 65535 - syndrome;
		return syndrome;
	}

	public static int ownChecksum(String s) {
		System.out.println("data for checksum: " + s);
		int row = s.length() / 2;
		int flag = 0;
		if (s.length() % 2 != 0) {
			row = (int) Math.ceil((double) s.length() / 2);
			flag = 1;
		}
		String[] binary = new String[row];
		for (int i = 0; i < (row); i++) {
			String left = Integer.toBinaryString(s.charAt(i * 2));
			for (int k = 0; k < (8 - left.length()); k++) {
				left = "0" + left;

			}
			String right = "";
			if (s.length() <= ((i * 2) + 1)) {
				right += "00000000";
				binary[i] = right + left;
			} else {
				right += Integer.toBinaryString(s.charAt((i * 2) + 1));
				for (int k = 0; k < (8 - right.length()); k++) {
					right = "0" + right;
				}
				binary[i] = left + right;
			}
		}
		int sum = 0;
		for (int i = 0; i < row; i++) {
			int dec = binToDec(binary[i]);
			sum += dec;
		}
		String hex = Integer.toHexString(sum);
		while (hex.length() > 4) {
			int carry = Integer.parseInt(hex.substring(0, 1), 16);
			hex = hex.substring(1, hex.length());
			sum = Integer.parseInt(hex, 16) + carry;
		}
		sum = 65535 - sum;
		System.out.println("sum: " + sum);
		String pad = Integer.toBinaryString(sum);
		for (int i = pad.length(); i < 16; i++)
			pad = "0" + pad;
		System.out.println("sum binary: " + pad);
		return sum;
	}

	public static int binToDec(String s) {
		int dec = 0;
		for (int i = 0; i < s.length(); i++) {

			if (s.charAt(i) == '1') {
				dec = dec + (int) Math.pow(2, s.length() - 1 - i);
			}
		}
		return dec;
	}
}
