package com.soso.evaextra.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import cn.edu.hust.cm.common.util.ObjectPool;

import com.google.common.io.Files;

public class SosoLocUtils {

	public static String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	/**
	 * 通过交换数组内字节的顺序混淆数组，并对交换的字节进行异或打乱值
	 * 
	 * @param bytesData
	 * @return
	 */
	private static byte[] swapBytes(byte[] bytesData, int o, int l) {
		int length = l;
		byte[] bytes = OBJ_POOL != null ? (byte[]) OBJ_POOL
				.getObject(byte[].class) : new byte[length]; // #1_from_pool
		System.arraycopy(bytesData, o, bytes, 0, length);
		int offset = (1 << 3) - 1 + length % 5;
		byte tmp, xor;
		for (int i = 0; i + (offset << 1) < bytes.length; i += (offset << 1)) {
			xor = Integer.valueOf(i).byteValue();
			for (int j = 0; j < offset; ++j) {
				tmp = bytes[i + j];
				bytes[i + j] = (byte) (bytes[i + offset + j] ^ xor);
				bytes[i + offset + j] = (byte) (tmp ^ xor);
			}
		}
		return bytes;
	}

	private static byte[] swapBytes(byte[] bytesData) {
		return swapBytes(bytesData, 0, bytesData.length);
	}

	/**
	 * 通过压缩及交换字节顺序对数组加密，并在数组头部增加四个字节，表示加密后字节数
	 * 
	 * @param bytesData
	 * @return
	 */
	public static byte[] encryptBytes(byte[] bytesData) {
		return encryptBytes(bytesData, 0, bytesData.length);
	}

	public static byte[] encryptBytes(byte[] bytesData, int o, int l) {
		byte[] compressed = SosoMapUtils.deflate(bytesData, o, l); // LocationUtil.deflate(bytesData,
		// false);
		byte[] swaped = swapBytes(compressed);
		byte[] encrypted = new byte[4 + swaped.length];
		int length = swaped.length;
		// int 最右一字节为第一字节，则存储顺序为2 4 3 1
		encrypted[0] = Integer.valueOf((length >> 8)).byteValue();
		encrypted[1] = Integer.valueOf((length >> 24)).byteValue();
		encrypted[2] = Integer.valueOf((length >> 16)).byteValue();
		encrypted[3] = Integer.valueOf(length).byteValue();
		System.arraycopy(swaped, 0, encrypted, 4, length);
		return encrypted;
	}

	public static byte[] decryptBytes(byte[] bytesData) {
		return decryptBytes(bytesData, 0, bytesData.length);
	}

	private static byte[] decryptBytes(byte[] bytesData, int offset, int len) {
		byte[] decrypted = swapBytes(bytesData, offset, len);
		decrypted = SosoMapUtils.inflate(decrypted, 0, len);
		return decrypted;
	}

	public static void decrypteFile(File mSelectedFile, File out) {
		byte[] buf = new byte[8192];

		try {
			byte[] intbuf = new byte[4];
			long totalbytes = mSelectedFile.length();
			FileInputStream input = new FileInputStream(mSelectedFile);
			while (totalbytes > 0L) {
				input.read(intbuf, 0, 4);
				int length = 0;
				length |= intbuf[1] & 0xFF;
				length <<= 8;
				length |= intbuf[2] & 0xFF;
				length <<= 8;
				length |= intbuf[0] & 0xFF;
				length <<= 8;
				length |= intbuf[3] & 0xFF;
				int len = input.read(buf, 0, length);
				totalbytes -= 4 + len;
				// append(new String(decryptBytes(buf), "GBK"));

				byte[] decrypted = decryptBytes(buf, 0, len); // 调用了
																// swapBytes和inflate,
																// 导致大量创建 byte[]
				Files.append(new String(decrypted), out,
						Charset.defaultCharset());

				if (OBJ_POOL != null) {
				}
			}
			input.close();
		} catch (FileNotFoundException localFileNotFoundException) {
		} catch (IOException localIOException) {
		}
	}

	public static final ObjectPool OBJ_POOL = null;//new ObjectPool();
	static {
		//OBJ_POOL.register(byte[].class);
	}
}
