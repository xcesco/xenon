package com.abubusoft.xenon.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

/** Provides utility methods to copy streams */
public class StreamUtility {

	public static final int DEFAULT_BUFFER_SIZE = 8192;
	public static final byte[] EMPTY_BYTES = new byte[0];

	/**
	 * Copy the data from an {@link InputStream} to an {@link OutputStream} without closing the stream.
	 * 
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output) throws IOException {
		copyStream(input, output, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copy the data from an {@link InputStream} to an {@link OutputStream} without closing the stream.
	 * 
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	/**
	 * Copy the data from an {@link InputStream} to a byte array without closing the stream.
	 * 
	 * @throws IOException
	 */
	public static byte[] copyStreamToByteArray(InputStream input) throws IOException {
		return copyStreamToByteArray(input, input.available());
	}

	/**
	 * Copy the data from an {@link InputStream} to a byte array without closing the stream.
	 * 
	 * @param estimatedSize
	 *            Used to preallocate a possibly correct sized byte array to avoid an array copy.
	 * @throws IOException
	 */
	public static byte[] copyStreamToByteArray(InputStream input, int estimatedSize) throws IOException {
		ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(Math.max(0, estimatedSize));
		copyStream(input, baos);
		return baos.toByteArray();
	}

	/**
	 * Copy the data from an {@link InputStream} to a string using the default charset without closing the stream.
	 * 
	 * @throws IOException
	 */
	public static String copyStreamToString(InputStream input) throws IOException {
		return copyStreamToString(input, input.available());
	}

	/**
	 * Copy the data from an {@link InputStream} to a string using the default charset.
	 * 
	 * @param approxStringLength
	 *            Used to preallocate a possibly correct sized StringBulder to avoid an array copy.
	 * @throws IOException
	 */
	public static String copyStreamToString(InputStream input, int approxStringLength) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringWriter w = new StringWriter(Math.max(0, approxStringLength));
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];

		int charsRead;
		while ((charsRead = reader.read(buffer)) != -1) {
			w.write(buffer, 0, charsRead);
		}

		return w.toString();
	}

	/** Close and ignore all errors. */
	public static void closeQuietly(Closeable c) {
		if (c != null)
			try {
				c.close();
			} catch (Exception e) {
				// ignore
			}
	}

	/** A ByteArrayOutputStream which avoids copying of the byte array if not necessary. */
	private static class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
		OptimizedByteArrayOutputStream(int initialSize) {
			super(initialSize);
		}

		@Override
		public synchronized byte[] toByteArray() {
			if (count == buf.length)
				return buf;
			return super.toByteArray();
		}
	}
}
