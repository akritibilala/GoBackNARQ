package SelectiveRepeat;

import GoBack.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SelectiveRepeatSender {
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
	ChecksumMethod checksummethod;

	public SelectiveRepeatSender(String hostName, int portNumber, String fileName, int windowSize, int mss,
			InetAddress ip) throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.mss = mss;
		this.ip = ip;
		Path file = Paths.get(fileName);
		this.data = Files.readAllBytes(file);
		this.dataType = "0101010101010101";
		this.checksummethod = new ChecksumMethod();
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

	public void lastPacket(DatagramSocket s) {
		byte[] lastpacket = "1111111111111111111111111111111111111111111111111111111111111111000000".getBytes();

		DatagramPacket lastPacket = new DatagramPacket(lastpacket, lastpacket.length, this.ip, this.portNumber);
		try {
			s.send(lastPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String createheader(int index, String segment) {
		String seq = Integer.toBinaryString(index);
		for (int i = seq.length(); i < 32; i++)
			seq = "0" + seq;
		String check = checksummethod.generateChecksumforSender(segment);
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
		SelectiveRepeatSender sender = new SelectiveRepeatSender(hostName, portNumber, fileName, windowSize, mss,
				IPAddress);
		DatagramSocket client = new DatagramSocket();

		int seq = -1;
		int startIndex = 0, i = 0;
		String[] packetArray = new String[sender.windowSize];
		Arrays.fill(packetArray, "NOTSENT");
		int l = 0;

		long rttArray[] = new long[5];
		long startTime = System.currentTimeMillis();
		while (startIndex < sender.NoOfPackets) {
			for (l = 0; l < windowSize; l++) {
				if (startIndex < sender.NoOfPackets) {
					if (packetArray[l].equals("NOTSENT") || packetArray[l].equals("SENT")
							|| packetArray[l].equals("NEWPACKET")) {
						String s = sender.segments.get(startIndex);
						String head = sender.createheader(startIndex, s);
						byte[] headbytes = head.getBytes();
						// System.out.println("sending data: " + s);
						byte[] databytes = s.getBytes();
						byte[] finalPacket = new byte[headbytes.length + s.length()];
						for (int k = 0; k < headbytes.length; k++) {
							finalPacket[k] = headbytes[k];
						}
						for (int j = headbytes.length, k = 0; j < finalPacket.length; j++, k++) {
							finalPacket[j] = databytes[k];
						}
						DatagramPacket send = new DatagramPacket(finalPacket, finalPacket.length, IPAddress,
								sender.portNumber);
						client.send(send);
						System.out.println("Sending " + startIndex + " Packet");
						i++;
						packetArray[l] = "SENT";
					}
					startIndex++;
				} else
					break;
			}
			startIndex = startIndex - l;
			byte[] receiveData = new byte[2048];
			int indexCopy = startIndex;
			DatagramPacket receivePckt = new DatagramPacket(receiveData, receiveData.length);
			try {
				client.setSoTimeout(1000);
				while (true) {
					client.receive(receivePckt);
					seq = sender.getpacket(receivePckt);
					System.out.println("ACK received with seq: " + seq);
					if (seq != -1) {
						int temp = seq - startIndex;
						packetArray[temp] = "ACKNOWLEDGED";
						if (temp == 0) {
							while (packetArray[temp].equals("ACKNOWLEDGED")) {
								System.arraycopy(packetArray, 1, packetArray, 0, packetArray.length - 1);
								packetArray[sender.windowSize - 1] = "NEWPACKET";
								indexCopy = startIndex + 1;
								startIndex++;
							}
						}
					}
				}
			} catch (SocketTimeoutException s) {
				System.out.println("Timeout, sequence number = " + seq);
			}
		}
		sender.lastPacket(client);
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		System.out.println("RTT is: " + diff);
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
