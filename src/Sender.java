import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import sun.font.CreatedFontTracker;

public class Sender {
	String hostName;
	int portNumber;
	String fileName;
	int windowSize;
	int mss;
	int NoOfPackets;
	InetAddress ip;
	byte[] data;
	ArrayList<byte[]> segments;

	public Sender(String hostName, int portNumber, String fileName, int windowSize, int mss, InetAddress ip)
			throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.mss = mss;
		this.ip = ip;
		Path file = Paths.get(fileName);
		this.data = Files.readAllBytes(file);
		this.NoOfPackets = (int) Math.ceil((double)data.length / this.mss);
		int j = 0;
		int k = mss;
		segments = new ArrayList<>();
		System.out.println("Data length: " + data.length);
		System.out.println("Packets: " + this.NoOfPackets);
		System.out.println("mss: " + this.mss);
		for (int i = 0; i < this.NoOfPackets; i++) {
			byte[] segmentSize = new byte[this.mss];
			for (int l = 0; l < this.mss; l++) {
				if ((l + j) < this.data.length)
					segmentSize[l] = this.data[l + j];
			}
			j = j + mss;
			System.out.println("size " + segmentSize.length);
			this.segments.add(i, segmentSize);
			System.out.println(this.segments);
		}
	}

	public String generateChecksum(String s) {
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
		return Integer.toBinaryString(checksum);
	}

	public int generateComplement(int checksum) {
		// Generates 15's complement of a hexadecimal value
		checksum = Integer.parseInt("FFFF", 16) - checksum;
		return checksum;
	}

	// public String createChecksum(byte[] segment) {
	// int length = segment.length;
	// int Word, Checksum = 0;
	//
	// int i = 0;
	// while (length > 0 && i<segment.length-1) // len = Total num of bytes
	// {
	// Word = ((segment[i] << 8) + segment[i + 1]) + Checksum;
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
	public void lastPacket(DatagramSocket s) {
		byte[] lastpacket = "1111111111111111111111111111111111111111111111111111111111111111000000".getBytes();

		DatagramPacket lastPacket = new DatagramPacket(lastpacket, lastpacket.length, this.ip, this.portNumber);
		try {
			s.send(lastPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// remove seqno

	public String createheader(int index, byte[] segment) {
		String seq = Integer.toBinaryString(index);
		String check = this.generateChecksum(segment.toString());
		String field = "0101010101010101";
		for (int i = seq.length(); i < 32; i++)
			seq = "0" + seq;
		String header = seq + check + field;
		System.out.println("seq:" + seq + " check: " + check + " field: " + field);
		return header;

	}

	public String checkACK(byte[] arr) {
		String ACKPacket = "";
		for(int i=0;i<64;i++){
			if(arr[i]==48)
				ACKPacket = ACKPacket + "0";
			else
				ACKPacket = ACKPacket +  "1";
		}
		System.out.println("string ack: "+ACKPacket);
		String sequence = ACKPacket.substring(0, 32);
		System.out.println("sequence: "+sequence);
		System.out.println("type: "+ACKPacket.substring(48, 64));
		if (ACKPacket.substring(48, 64).equals("1010101010101010")) {
			return sequence;
		} else {
			return null;
		}

	}

	public int getpacket(DatagramPacket datapacket) {
		int binToDec = -1;
		String s = this.checkACK(datapacket.getData());
		System.out.println("check ack: "+s);
		if (s != null) {
			binToDec = 0;
			int j = s.length() - 1;
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == '1') {
					binToDec = binToDec + (int) (Math.pow(2, j));
				}
				j--;
			}
		}
		return binToDec;
	}

	public static void main(String args[]) throws IOException {

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the host name: ");
		String hostName = sc.next();
		System.out.println("Enter the port number: ");
		int portNumber = sc.nextInt();
		System.out.println("Enter the filename: ");
		String fileName = sc.next();
		System.out.println("Enter the window size(N): ");
		int windowSize = sc.nextInt();
		System.out.println("Enter the MSS: ");
		int mss = sc.nextInt();
		InetAddress IPAddress = InetAddress.getByName(hostName);
		Sender sender = new Sender(hostName, portNumber, fileName, windowSize, mss, IPAddress);
		DatagramSocket client = new DatagramSocket();

		int seq = -1;
		int startIndex = 0, i = 0, count = 0;
		while (startIndex < sender.NoOfPackets) {
			while (i < sender.windowSize && startIndex < sender.NoOfPackets) {
				String head = sender.createheader(startIndex, sender.segments.get(startIndex));
				byte[] headbytes = head.getBytes();
				byte[] databytes = sender.segments.get(startIndex);
				byte[] finalPacket = new byte[headbytes.length + sender.mss];
				for (int k = 0; k < headbytes.length; k++) {
					finalPacket[k] = headbytes[k];
				}
				for (int j = headbytes.length, k = 0; j < headbytes.length + sender.mss; j++, k++) {
					finalPacket[j] = databytes[k];
				}
				DatagramPacket send = new DatagramPacket(finalPacket, finalPacket.length, IPAddress, sender.portNumber);
				client.send(send);
				System.out.println("sent:" + startIndex);
				i++;
				startIndex++;
				count++;
			}
			System.out.println("Receiving with i:" + i + " index: " + startIndex);
			byte[] receiveData = new byte[1024];
			int indexCopy = startIndex;
			DatagramPacket receivePckt = new DatagramPacket(receiveData, receiveData.length);
			try {
				client.setSoTimeout(1000);
				while (true) { // do while the client is receiving packets
					client.receive(receivePckt);
					seq = sender.getpacket(receivePckt); // getting the sequence
															// no of the
															// received packet
					System.out.println("ACK received with seq: " + seq);
					if (seq == indexCopy - 1) { // if last acknowledgement is
													// received
						i = 0;
						startIndex = indexCopy;
						break;
					} else if (seq != -1) { // if last is not received, then
											// change window size and retransmit
											// the packets
											// from the last received ACK.
						i = startIndex - seq - 1;
						startIndex = seq + 1;
					}
				}
			} catch (SocketTimeoutException s) {
				System.out.println("Timeout, sequence number = " + seq);
				startIndex = seq+1;
				i = 0;
			}

		}
		sender.lastPacket(client);
	}

}
