package com.levelup.http;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

/**
 * An Http GET request that doesn't follow redirections
 * <p>
 * Using {@link HttpURLConnection#setInstanceFollowRedirects(boolean)
 * setInstanceFollowRedirects()} in
 * {@link #setConnectionProperties(HttpURLConnection) setConnectionProperties()}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestGetNoRedirect extends HttpRequestGet<Void> {

	public HttpRequestGetNoRedirect(String baseUrl) {
		super(baseUrl, null);
	}

	public HttpRequestGetNoRedirect(String baseUrl, HttpUriParameters httpParams) {
		super(baseUrl, httpParams, null);
	}

	public HttpRequestGetNoRedirect(Uri baseUrl, HttpUriParameters httpParams) {
		super(baseUrl, httpParams, null);
	}

	@Override
	public void setConnectionProperties(HttpURLConnection connection) throws ProtocolException {
		super.setConnectionProperties(connection);
		connection.setInstanceFollowRedirects(false);
	}
}