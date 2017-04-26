package GoBack;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Sender {
	String hostName;
	int portNumber;
	String fileName;
	int windowSize;
	int mss;
	int NoOfPackets;
	InetAddress ip;
	byte[] data;
	ArrayList<String> segments;
	String dataType;

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
		this.dataType = "0101010101010101";

		this.NoOfPackets = (int) Math.ceil((double) data.length / this.mss);
		int j = 0;
		int k = mss;
		segments = new ArrayList<>();
		String dataInString = new String(data);
		for (int i = 0; i < this.NoOfPackets; i++) {
			if (k > dataInString.length()) {
				k = dataInString.length();
			}
			String segmentSize = dataInString.substring(j, k);
			j = j + mss;
			k = k + mss;
			this.segments.add(i, segmentSize);
		}
	}

	public String generateChecksum(String s) {
		String hex_value = new String();
		int x, i, checksum = 0;
		for (i = 0; i < (s.length() - 2); i = i + 2) {
			hex_value = "";
			x = 0;
			x = (int) (s.charAt(i));
			hex_value = Integer.toHexString(x);
			x = (int) (s.charAt(i + 1));
			hex_value = hex_value + Integer.toHexString(x);
			x = Integer.parseInt(hex_value, 16);
			checksum += x;
		}
		if (s.length() % 2 == 0) {
			x = (int) (s.charAt(i));
			hex_value = Integer.toHexString(x);
			x = (int) (s.charAt(i + 1));
			hex_value = hex_value + Integer.toHexString(x);
			x = Integer.parseInt(hex_value, 16);
		} else {
			x = (int) (s.charAt(i));
			hex_value = "00" + Integer.toHexString(x);
			x = Integer.parseInt(hex_value, 16);
		}
		checksum += x;
		hex_value = Integer.toHexString(checksum);
		if (hex_value.length() > 4) {
			int carry = Integer.parseInt(("" + hex_value.charAt(0)), 16);
			hex_value = hex_value.substring(1, 5);
			checksum = Integer.parseInt(hex_value, 16);
			checksum += carry;
		}
		checksum = this.generateComplement(checksum);
		String check = Integer.toBinaryString(checksum);
		for (int j = check.length(); j < 16; j++)
			check = "0" + check;
		return check;
	}

	public int generateComplement(int checksum) {
		checksum = Integer.parseInt("FFFF", 16) - checksum;
		return checksum;
	}

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

	public String createheader(int index, String segment) {
		String seq = Integer.toBinaryString(index);
		for (int i = seq.length(); i < 32; i++)
			seq = "0" + seq;
		String check = generateChecksum(segment);
		String field = "0101010101010101";
		String header = seq + check + field;
		return header;

	}

	public String checkACK(byte[] arr) {
		String ACKPacket = "";
		for (int i = 0; i < 64; i++) {
			if (arr[i] == '0')
				ACKPacket = ACKPacket + "0";
			else
				ACKPacket = ACKPacket + "1";
		}
		String sequence = ACKPacket.substring(0, 32);
		if (ACKPacket.substring(48, 64).equals("1010101010101010")) {
			return sequence;
		} else {
			return null;
		}

	}

	public int getpacket(DatagramPacket datapacket) {
		int binToDec = -1;
		String s = this.checkACK(datapacket.getData());
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

	public static void main(String args[]) throws IOException, InterruptedException {

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
		int startIndex = 0, i = 0;
		long rttArray[] = new long[5];
		long startTime = System.currentTimeMillis();
		while (startIndex < sender.NoOfPackets) {
			while (i < sender.windowSize && startIndex < sender.NoOfPackets) {
				String s = sender.segments.get(startIndex);
				String head = sender.createheader(startIndex, s);
				byte[] headbytes = head.getBytes();
				byte[] databytes = s.getBytes();
				byte[] finalPacket = new byte[headbytes.length + s.length()];
				for (int k = 0; k < headbytes.length; k++) {
					finalPacket[k] = headbytes[k];
				}
				for (int j = headbytes.length, k = 0; j < finalPacket.length; j++, k++) {
					finalPacket[j] = databytes[k];
				}
				DatagramPacket send = new DatagramPacket(finalPacket, finalPacket.length, IPAddress, sender.portNumber);
				client.send(send);
				System.out.println("Sending " + startIndex + " Packet");
				i++;
				startIndex++;
			}
			byte[] receiveData = new byte[2048];
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
				startIndex = seq + 1;
				i = 0;
			}

		}
		sender.lastPacket(client);
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		System.out.println("RTT is: " + diff);
	}

	public static String ownChecksum(String s) {
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
		String pad = Integer.toBinaryString(sum);
		for (int i = pad.length(); i < 16; i++)
			pad = "0" + pad;
		return pad;
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
