package GoBack;

import java.util.*;

public class ChecksumMethod {
	public int generateChecksum(String s) {
		String checksumString = new String();
		int i = 0, checksum = 0;
		checksum = intermidiateChecksumComputation(s);
		checksum += checkCondition(s, i);
		checksumString = Integer.toHexString(checksum);
		if (checksumString.length() > 4) {
			int extraBit = Integer.parseInt(("" + checksumString.charAt(0)), 16);
			checksumString = checksumString.substring(1, 5);
			checksum = Integer.parseInt(checksumString, 16);
			checksum += extraBit;
		}
		checksum = this.reverse(checksum);
		return checksum;
	}
	public String generateChecksumforSender(String s) {
		String checksumString = new String();
		int  i = 0, checksum = 0;
		checksum = intermidiateChecksumComputation(s);
		checksum += checkCondition(s, i);
		checksumString = Integer.toHexString(checksum);
		if (checksumString.length() > 4) {
			int extraBit = Integer.parseInt(("" + checksumString.charAt(0)), 16);
			checksumString = checksumString.substring(1, 5);
			checksum = Integer.parseInt(checksumString, 16);
			checksum += extraBit;
		}
		checksum = this.reverse(checksum);
		String check = Integer.toBinaryString(checksum);
		for (int j = check.length(); j < 16; j++)
			check = "0" + check;
		return check;
	}
	public int reverse(int checksum) {
		checksum = 65535 - checksum;
		return checksum;
	}

	public int intermidiateChecksumComputation(String s) {
		String checksumString = new String();
		int pointer, i, checksum = 0;
		for (i = 0; i < s.length() - 2; i = i + 2) {
			checksumString = "";
			pointer = 0;
			pointer = (int) (s.charAt(i));
			checksumString = Integer.toHexString(pointer);
			pointer = (int) (s.charAt(i + 1));
			checksumString = checksumString + Integer.toHexString(pointer);
			pointer = Integer.parseInt(checksumString, 16);
			checksum += pointer;
		}
		return checksum;
	}
	public int checkCondition(String s, int i) {
		String checksumString = new String();
		int pointer;
		if (s.length() % 2 == 0) {
			pointer = (int) (s.charAt(i));
			checksumString = Integer.toHexString(pointer);
			pointer = (int) (s.charAt(i + 1));
			checksumString = checksumString + Integer.toHexString(pointer);
			pointer = Integer.parseInt(checksumString, 16);
		} else {
			pointer = (int) (s.charAt(i));
			checksumString = "00" + Integer.toHexString(pointer);
			pointer = Integer.parseInt(checksumString, 16);
		}
		return pointer;
	}

}