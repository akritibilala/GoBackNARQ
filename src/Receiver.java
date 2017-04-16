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
	/* public static String trimTrailingBlanks( String str)
	  {
	    if( str == null)
	      return null;
	    int len = str.length();
	    for( ; len > 0; len--)
	    {
	      if( ! Character.isWhitespace( str.charAt( len - 1)))
	         break;
	    }
	    return str.substring( 0, len);
	  }*/

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
				System.out.println("here");
				DatagramPacket senderData = new DatagramPacket(recieveData, recieveData.length);
				server.receive(senderData);
				System.out.println("rec len: "+senderData.getLength());
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

					if (seq.charAt(i) == '1') {
						s = s + (int) Math.pow(2, checksum.length() - 1 - i);
					}
				}
				String type = receive.substring(48, 64);
				String data = receive.substring(64, receive.length());
				System.out.println("seq: " + seq);
				System.out.println("checksum: " + checksum);
				System.out.println("type: " + type);
				//String right = trimTrailingBlanks(data);
				
				System.out.println("data: " + data+" with length: "+data.length());
				if (type.equals("1111111111111111")) {
					flag = 1;
				}

				double r = Math.random();
				System.out.println("random: "+r);
				 if (r <= rec.probability) {
				 System.out.println("Packet Loss Sequence Number =" + s);
				 } else{
//				if (rec.receive(data, c) == 0) {
					System.out.println("checksum is ok");
					if (s == index) {
						System.out.println("Sending ack for: "+s);
						String ack = rec.createACK(s);
						InetAddress addr = senderData.getAddress();
						int port = senderData.getPort();
						byte[] b;
						b = ack.getBytes();
//						for(int i=0;i<b.length;i++)
//							System.out.print(b[i]+" ");
//						System.out.println();
						DatagramPacket sendpacket = new DatagramPacket(b, b.length, addr, port);
						server.send(sendpacket);
						incomingdata.write(data.getBytes());
						index++;
					}
				 }
//				}

				// incomingdata.write(senderData.getData());
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println(e);
			}
		}
		FileOutputStream out = new FileOutputStream(fileName);
		incomingdata.writeTo(out);
		out.close();
		server.close();
	}

	public String createACK(int seqno) {
		String seq = Integer.toBinaryString(seqno);
		for(int i=seq.length();i<32;i++)
			seq = "0" + seq;
		String check = "0000000000000000";
		String field = "1010101010101010";
		String ack = seq + check + field;
		System.out.println(ack);
		return ack;

	}

	public int generateChecksum(String s) {
		System.out.println("checksum for " + s);
		String hex_value = new String();
		// 'hex_value' will be used to store various hex values as a string
		int x, i, checksum = 0;
		// 'x' will be used for general purpose storage of integer values
		// 'i' is used for loops
		// 'checksum' will store the final checksum
		for (i = 0; i < s.length() - 2; i = i + 2) {
			x = (int) (s.charAt(i));
			hex_value = Integer.toHexString(x);
			x = (int) (s.charAt(i + 1));
			hex_value = hex_value + Integer.toHexString(x);
			// Extract two characters and get their hexadecimal ASCII values
			System.out.println(s.charAt(i) + "" + s.charAt(i + 1) + " : " + hex_value);
			x = Integer.parseInt(hex_value, 16);
			// Convert the hex_value into int and store it
			checksum += x;
			// Add 'x' into 'checksum'
		}
		if (s.length() % 2 == 0) {
			// If number of characters is even, then repeat above loop's steps
			// one more time.
			x = (int) (s.charAt(i));
			hex_value = Integer.toHexString(x);
			x = (int) (s.charAt(i + 1));
			hex_value = hex_value + Integer.toHexString(x);
			System.out.println(s.charAt(i) + "" + s.charAt(i + 1) + " : " + hex_value);
			x = Integer.parseInt(hex_value, 16);
		} else {
			// If number of characters is odd, last 2 digits will be 00.
			x = (int) (s.charAt(i));
			hex_value = "00" + Integer.toHexString(x);
			x = Integer.parseInt(hex_value, 16);
			System.out.println(s.charAt(i) + " : " + hex_value);
		}
		checksum += x;
		// Add the generated value of 'x' from the if-else case into 'checksum'
		hex_value = Integer.toHexString(checksum);
		// Convert into hexadecimal string
		if (hex_value.length() > 4) {
			// If a carry is generated, then we wrap the carry
			int carry = Integer.parseInt(("" + hex_value.charAt(0)), 16);
			// Get the value of the carry bit
			hex_value = hex_value.substring(1, 5);
			// Remove it from the string
			checksum = Integer.parseInt(hex_value, 16);
			// Convert it into an int
			checksum += carry;
			// Add it to the checksum
		}
		checksum = this.generateComplement(checksum);
		// Get the complement
		System.out.println("data Checksum: " + Integer.toBinaryString(checksum));
		return checksum;
	}

	public int receive(String s, int checksum) {
		int generated_checksum = this.generateChecksum(s);
		// Calculate checksum of received data
		generated_checksum = this.generateComplement(generated_checksum);
		System.out.println("checksum comp: " + Integer.toBinaryString(generated_checksum));
		// Then get its complement, since generated checksum is complemented
		int syndrome = generated_checksum + checksum;
		// Syndrome is addition of the 2 checksums
		syndrome = this.generateComplement(syndrome);
		// It is complemented
		System.out.println("Syndrome = " + Integer.toHexString(syndrome));
		return syndrome;
	}

	public int generateComplement(int checksum) {
		// Generates 15's complement of a hexadecimal value
		checksum = Integer.parseInt("FFFF", 16) - checksum;
		return checksum;
	}
}
// public String createChecksum(byte[] segment, byte[] check) {
// int length = segment.length;
// int Word, Checksum = 0;
//
// while (length > 0) // len = Total num of bytes
// {
// int i = 0;
// Word = ((segment[i] << 8) + segment[i + 1]) + Checksum + ((check[i] << 8) +
// check[i + 1]);
//
// Checksum = Word & 0x0FFFF; // Discard the carry if any
//
// Word = (Word >> 16); // Keep the carryout for value exceeding 16 Bit
//
// Checksum = Word + Checksum; // Add the carryout if any
//
// length -= 2; // decrease by 2 for 2 byte boundaries
// i += 2;
// }
//
// Checksum = ~Checksum;
// return Integer.toBinaryString(Checksum);
//
// }
