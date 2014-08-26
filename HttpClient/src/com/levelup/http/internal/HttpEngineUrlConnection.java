package com.levelup.http.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.os.Build;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.DataErrorException;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpStream;
import com.levelup.http.LogManager;
import com.levelup.http.LoggerTagged;
import com.levelup.http.HttpResponseHandler;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineUrlConnection<T> extends BaseHttpEngine<T,HttpResponseUrlConnection> {
	final HttpURLConnection urlConnection;
	private final boolean isStreaming;

	public HttpEngineUrlConnection(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		super(builder);
		this.isStreaming = builder.isStreaming();

		try {
			this.urlConnection = (HttpURLConnection) new URL(getUri().toString()).openConnection();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Bad uri: " + getUri(), e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @param request
	 * @return an {@link java.net.HttpURLConnection} with the network response
	 * @throws com.levelup.http.HttpException
	 */
	private void getQueryResponse(HttpRequest request, boolean allowGzip) throws HttpException {
		try {
			if (allowGzip && request.getHeader(HttpClient.ACCEPT_ENCODING)==null) {
				request.setHeader(HttpClient.ACCEPT_ENCODING, "gzip,deflate");
			}

			prepareRequest(request);

			final LoggerTagged logger = request.getLogger();
			if (null != logger) {
				logger.v(request.getHttpMethod() + ' ' + request.getUri());
				for (Map.Entry<String, List<String>> header : urlConnection.getRequestProperties().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

			doConnection();

			if (null != logger) {
				logger.v(urlConnection.getResponseMessage());
				for (Map.Entry<String, List<String>> header : urlConnection.getHeaderFields().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

		} catch (SecurityException e) {
			throw exceptionToHttpException(request, e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(request, e).build();

		} finally {
			try {
				setRequestResponse(request, new HttpResponseUrlConnection(this));
			} catch (IllegalStateException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				LogManager.getLogger().d("connection closed ? for "+request+' '+e);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}
	}

	@Override
	public boolean isStreaming() {
		return isStreaming;
	}

	@SuppressLint("NewApi")
	@Override
	public void settleHttpHeaders(HttpRequest request) throws HttpException {
		try {
			urlConnection.setRequestMethod(getHttpMethod());

		} catch (ProtocolException e) {
			throw exceptionToHttpException(request, e).build();
		}

		final long contentLength;
		if (null != bodyParams) {
			setHeader(HTTP.CONTENT_TYPE, bodyParams.getContentType());
			contentLength = bodyParams.getContentLength();
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
		} else {
			contentLength = 0L;
		}
		setHeader(HTTP.CONTENT_LEN, Long.toString(contentLength));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			urlConnection.setFixedLengthStreamingMode((int) contentLength);
		else
			urlConnection.setFixedLengthStreamingMode(contentLength);

		super.settleHttpHeaders(request);

		for (Entry<String, String> entry : mRequestSetHeaders.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		for (Entry<String, HashSet<String>> entry : mRequestAddHeaders.entrySet()) {
			for (String value : entry.getValue()) {
				urlConnection.addRequestProperty(entry.getKey(), value);
			}
		}

		if (null != followRedirect) {
			urlConnection.setInstanceFollowRedirects(followRedirect);
		}

		if (null != getHttpConfig()) {
			int readTimeout = getHttpConfig().getReadTimeout(request);
			if (readTimeout >= 0)
				urlConnection.setReadTimeout(readTimeout);
		}
	}

	@Override
	public final void setupBody() {
		// do nothing
	}

	@Override
	public final void doConnection() throws IOException {
		urlConnection.connect();

		if (null != bodyParams) {
			OutputStream output = urlConnection.getOutputStream();
			try {
				outputBody(output, this);
			} finally {
				output.close();
			}
		}
	}

	@Override
	public InputStream getInputStream(HttpRequest request, HttpResponseHandler<?> responseHandler) throws HttpException {
		getQueryResponse(request, true);
		try {
			return getHttpResponse().getInputStream();

		} catch (FileNotFoundException e) {
			DataErrorException exceptionWithData = responseHandler.errorHandler.handleError(getHttpResponse(), this, e);

			HttpException.Builder exceptionBuilder = exceptionToHttpException(request, exceptionWithData);
			throw exceptionBuilder.build();

		} catch (IOException e) {
			throw exceptionToHttpException(request, e).build();

		}
	}

	@Override
	public <P> P parseRequest(HttpResponseHandler<P> responseHandler, HttpRequest request) throws HttpException {
		if (request.isStreaming()) {
			// special case: streaming with HttpRequestUrlConnection
			getQueryResponse(request, true);
			try {
				return (P) new HttpStream(getHttpResponse().getInputStream(), request);

			} catch (IOException e) {
				throw exceptionToHttpException(request, e).build();

			}
		}

		return super.parseRequest(responseHandler, request);
	}

	@Override
	protected InputStream getParseableErrorStream() throws IOException {
		HttpResponseUrlConnection response = getHttpResponse();
		InputStream errorStream = response.getErrorStream();
		if (null == errorStream)
			errorStream = response.getInputStream();

		return errorStream;
	}
}
