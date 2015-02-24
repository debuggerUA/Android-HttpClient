package co.tophe;

import co.tophe.parser.BodyToServerException;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformInputStreamServerException;

/**
 * A basic {@link co.tophe.ResponseHandler} that throws a raw/untyped {@link co.tophe.ServerException}.
 *
 * @param <OUTPUT> type of the parsed HTTP response body.
 * @author Created by robUx4 on 29/09/2014.
 */
public class BaseResponseHandler<OUTPUT> extends ResponseHandler<OUTPUT, ServerException> {
	/**
	 * {@link HttpResponse} handler, turns the HTTP body into a typed object/exception
	 * <p>Uses {@link XferTransformInputStreamServerException} to parse the error data</p>
	 *
	 * @param contentParser {@link co.tophe.parser.XferTransform} (with {@link co.tophe.HttpResponse} input type) that will turn the body into an Object when there is no error.
	 */
	public BaseResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		super(contentParser, BodyToServerException.INSTANCE);
	}
}
