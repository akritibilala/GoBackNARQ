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
		byte[] data = Files.readAllBytes(file);
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
	public String createchecksum(byte[] data){
		
		return null;
		
	}
	public String createheader(int seqno, ArrayList<byte[]> segments, int index){
		String seq= Integer.toBinaryString(seqno);
		String field = "0101010101010101";
		
		
		
		return null;
		
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
		Sender sender = new Sender(hostName,portNumber,fileName, windowSize , mss);
		DatagramSocket client = new DatagramSocket();

		byte[] sendData, recieveData;
		InetAddress IPAddress = InetAddress.getByName(sender.hostName);
		DatagramPacket recieverData = new DatagramPacket(sender.data, sender.data.length, IPAddress, sender.portNumber);
		client.send(recieverData);
	}
}
