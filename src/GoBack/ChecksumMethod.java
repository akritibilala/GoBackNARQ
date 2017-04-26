package GoBack;

import java.util.*;

public class ChecksumMethod {
	public int generateChecksum(String s) {
		String hex_value = new String();
		int x, i, checksum = 0;
		for (i = 0; i < s.length() - 2; i = i + 2) {
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
		checksum = generateComplement(checksum);
		return checksum;
	}

	public static int generateComplement(int checksum) {
		checksum = Integer.parseInt("FFFF", 16) - checksum;
		return checksum;
	}

}