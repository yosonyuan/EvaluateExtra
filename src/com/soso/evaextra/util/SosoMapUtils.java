package com.soso.evaextra.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class SosoMapUtils {
	/********************************* 压缩解压缩相关 **********************************/

	/**
	 * 压缩二进制数据。
	 *
	 * @param data
	 *            要压缩的数据
	 * @return 压缩后的数据
	 */
	public static byte[] deflate(byte[] data) {
		return deflate(data, 0, data.length);
	}

	public static byte[] deflate(byte[] data, int offset, int len) {
		if (data == null) {
			return null;
		}
		ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
		DeflaterOutputStream out = new DeflaterOutputStream(bufferOut);
		try {
			out.write(data, offset, len);
			out.finish();
			out.flush();
			out.close();
		} catch (Exception e) {
			return null;
		}
		return bufferOut.toByteArray();
	}

	/**
	 * 解压二进制数据。
	 *
	 * @param data
	 *            要解压的数据
	 * @return 解压后的数据
	 */
	public static byte[] inflate(byte[] data) {
		return inflate(data, 0, data.length);
	}

	public static byte[] inflate(byte[] data, int o, int l) {
		if (data == null) {
			return null;
		}
		ByteArrayInputStream bufferIn = new ByteArrayInputStream(data, o, l);
		InflaterInputStream is = new InflaterInputStream(bufferIn);
//		byte[] rdata = new byte[0];
//		int total = 0;
//		byte[] data_atime = new byte[1024];
//		int len;
//		do {
//			try {
//				len = is.read(data_atime);
//				if (len > 0) {
//					total += len;
//					byte[] temp = new byte[total];
//					System.arraycopy(rdata, 0, temp, 0, rdata.length);
//					System.arraycopy(data_atime, 0, temp, rdata.length, len);
//					rdata = temp;
//				}
//			} catch (Exception e) {
//				return null;
//			}
//		} while (len > 0);
//
//		try {
//			bufferIn.close();
//			is.close();
//		} catch (IOException e) {
//			return null;
//		}
//
//		return rdata;

		///////////////////////////////////////
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int length = -1;
		try {
			while (true) {
				if ((length = is.read(buf)) == -1) {
					break;
				}
				out.write(buf, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

}
