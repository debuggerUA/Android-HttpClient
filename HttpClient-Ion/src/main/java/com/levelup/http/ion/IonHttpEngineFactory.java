package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.DummyHttpEngine;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpEngineFactory;
import com.levelup.http.HttpExceptionFactory;
import com.levelup.http.HttpResponse;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.ResponseHandler;
import com.levelup.http.parser.ErrorHandlerViaXferTransform;
import com.levelup.http.parser.Utils;
import com.levelup.http.parser.XferTransform;
import com.levelup.http.parser.XferTransformChain;
import com.levelup.http.parser.XferTransformInputStreamHttpStream;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;

	private final Context context;

	public static IonHttpEngineFactory getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new IonHttpEngineFactory(context);
		}
		return INSTANCE;
	}

	private IonHttpEngineFactory(Context context) {
		this.context = context;
	}

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		if (!canHandleXferTransform(responseHandler.contentParser))
			return new DummyHttpEngine<T>(request, responseHandler, exceptionFactory);

		if (!errorCompatibleWithData(responseHandler))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return new DummyHttpEngine<T>(request, responseHandler, exceptionFactory);

		return new HttpEngineIon<T>(request, responseHandler, context, exceptionFactory);
	}

	private static <T> boolean canHandleXferTransform(XferTransform<HttpResponse, T> contentParser) {
		if (contentParser instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, T> parser = (XferTransformChain<HttpResponse, T>) contentParser;
			for (XferTransform transform : parser.transforms) {
				if (transform == XferTransformInputStreamHttpStream.INSTANCE)
					return false;
			}
		}
		return true;
	}

	/**
	 * See if we can find common ground to parse the data and the error data inside Ion
	 * @param responseHandler
	 * @return whether Ion will be able to parse the data and the error in its processing thread
	 */
	private static boolean errorCompatibleWithData(ResponseHandler<?> responseHandler) {
		if (!(responseHandler.errorHandler instanceof ErrorHandlerViaXferTransform)) {
			// not possible to handle the error data with the data coming out of the data parser
			return false;
		}

		ErrorHandlerViaXferTransform errorHandlerParser = (ErrorHandlerViaXferTransform) responseHandler.errorHandler;
		return Utils.getCommonXferTransform(responseHandler.contentParser, errorHandlerParser.errorDataParser) != null;
	}
}
