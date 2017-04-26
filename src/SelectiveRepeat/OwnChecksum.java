package SelectiveRepeat;

public class OwnChecksum {
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
//			int dec = binToDec(binary[i]);
//			sum += dec;
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
}
