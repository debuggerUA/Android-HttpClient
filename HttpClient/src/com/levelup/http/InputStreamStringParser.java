package com.levelup.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Singleton {@link InputStreamParser} class to get a {@link String} from an {@link InputStream}
 * @see #instance
 */
public class InputStreamStringParser implements InputStreamParser<String> {

	public static final InputStreamStringParser instance = new InputStreamStringParser();

	private InputStreamStringParser() {
	}

	@Override
	public String parseInputStream(InputStream is, HttpRequest request) throws IOException {
		final StringBuilder sb = new StringBuilder();

		int contentLength = -1;
		if (null != request) {
			contentLength = request.getResponse().getContentLength();
			if (contentLength > 0)
				sb.ensureCapacity(contentLength);
		}
		if (contentLength != 0) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is, Util.getInputCharsetOrUtf8(request)), 1250);
				for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
					if (sb.length()>0)
						sb.append('\n');
					sb.append(line);
				}
			} finally {
				if (null!=reader)
					try {
						reader.close();
					} catch (ArrayIndexOutOfBoundsException ignored) {
						// okhttp 1.5.3 issue https://github.com/square/okhttp/issues/658
					}
			}
		}

		if (null != request && null != request.getLogger() && sb != null) {
			request.getLogger().d(request.toString() + '>' + sb.toString());
		}

		return sb.toString();
	}
}
