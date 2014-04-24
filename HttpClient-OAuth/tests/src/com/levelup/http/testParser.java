package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;

public class testParser extends TestCase {

	public testParser() {
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void testCustomParser() {
		HttpRequestGet apiGet = new HttpRequestGet("http://my.com/api.json");

		try {
			Void parsed = HttpClient.parseRequest(apiGet, new InputStreamParser<Void>() {
				@Override
				public Void parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
					// Process your InputStream
					JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
					try {
						return readMessagesArray(reader);
					} finally {
						reader.close();
					}
				}
			});
		} catch (HttpException e) {
			// shit happens
		}
	}

	private Void readMessagesArray(JsonReader reader) {
		return null;

	}
}
