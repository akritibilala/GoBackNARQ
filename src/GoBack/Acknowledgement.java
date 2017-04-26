package GoBack;

public class Acknowledgement {
	public String createACK(int seqno) {
		String seq = Integer.toBinaryString(seqno);
		for (int i = seq.length(); i < 32; i++)
			seq = "0" + seq;
		String check = "0000000000000000";
		String field = "1010101010101010";
		String ack = seq + check + field;
		return ack;

	}
}
