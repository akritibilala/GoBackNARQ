import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Sender {
	String hostName;
	int portNumber;
	String fileName;
	int windowSize;
	int mss;
	int NoOfPackets;
	byte[] data;
	ArrayList<byte[]> segments;

	public Sender(String hostName, int portNumber, String fileName, int windowSize, int mss) throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.mss = mss;
		Path file = Paths.get(fileName);
		this.data = Files.readAllBytes(file);
		this.NoOfPackets = data.length / this.mss;
		int j = 0;
		int k = mss;
		for (int i = 0; i < this.NoOfPackets; i++) {
			byte[] segmentSize = new byte[this.mss];
			segmentSize = Arrays.copyOfRange(this.data, j, k);
			j = j + mss;
			k = k + mss;
			this.segments.add(segmentSize);
		}
	}

	public String createChecksum(byte[] segment) {
		int length = segment.length;
		int Word, Checksum = 0;

		while (length > 0) // len = Total num of bytes
		{
			int i = 0;
			Word = ((segment[i] << 8) + segment[i + 1]) + Checksum; // get two
																	// bytes at
																	// a time
																	// and add
																	// previous
																	// calculated
																	// checksum
																	// value

			Checksum = Word & 0x0FFFF; // Discard the carry if any

			Word = (Word >> 16); // Keep the carryout for value exceeding 16 Bit

			Checksum = Word + Checksum; // Add the carryout if any

			length -= 2; // decrease by 2 for 2 byte boundaries
			i += 2;
		}

		Checksum = ~Checksum;
		return Integer.toBinaryString(Checksum);

	}

	public String createheader(int seqno, ArrayList<byte[]> segments, int index) {
		String seq = Integer.toBinaryString(seqno);
		String check = createChecksum(segments.get(index));
		String field = "0101010101010101";
		String header = seq + check + field;
		System.out.println(header);
		return header;

	}

	public String checkACK(byte[] arr) {
		String ACKPacket = Arrays.toString(arr);
		String sequence = ACKPacket.substring(0, 32);
		if (ACKPacket.substring(48, 64).equals("1010101010101010")) {
			return sequence;
		} else {
			return null;
		}

	}

	public int getpacket(DatagramPacket datapacket) {
		int binToDec = 0;
		String s = checkACK(datapacket.getData());
		if (s != null) {

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
		Sender sender = new Sender(hostName, portNumber, fileName, windowSize, mss);
		DatagramSocket client = new DatagramSocket();

		byte[] sendData, recieveData;
		InetAddress IPAddress = InetAddress.getByName(sender.hostName);
		DatagramPacket recieverData = new DatagramPacket(sender.data, sender.data.length, IPAddress, sender.portNumber);
		client.send(recieverData);
	}
}
