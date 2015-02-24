package co.tophe.parser;

import java.io.InputStream;

import co.tophe.HttpResponse;

/**
 * Transform the HTTP response body into type {@link T} using a chain of transformations.
 * <p>Helper class for {@link co.tophe.parser.XferTransformChain} with an {@link co.tophe.HttpResponse} source.</p>
 *
 * @param <T> Output type after Gson parsing.
 * @author Created by robUx4 on 21/08/2014.
 * @see co.tophe.gson.BodyViaGson
 * @see co.tophe.parser.BodyToJSONObject
 * @see co.tophe.parser.BodyToJSONArray
 * @see co.tophe.parser.BodyToHttpStream
 */
public class BodyTransformChain<T> extends XferTransformChain<HttpResponse, T> {

	/**
	 * Create a {@link co.tophe.parser.BodyTransformChain.Builder} that can add chained transformations, starting with a transformation from a {@link co.tophe.HttpResponse}.
	 *
	 * @param firstTransform the initial transformation to apply to the {@link co.tophe.HttpResponse} to type {@link T}.
	 * @param <T>            the output type of the transformation that will be built.
	 * @return a Builder that can be passed to the {@link BodyTransformChain} constructor.
	 * @see co.tophe.parser.XferTransformChain.Builder#addDataTransform(XferTransform)
	 */
	public static <T> Builder<T> createBuilder(XferTransform<HttpResponse, T> firstTransform) {
		return XferTransformChain.initBuilder(firstTransform, new Builder<T>());
	}

	/**
	 * Builder to chain multiple transformations to end with a {@link T} type, starting with a transformation from a {@link co.tophe.HttpResponse}.
	 * <p>Start your Builder chain with {@link #createBuilder(XferTransform)}, add transformations with {@link #addDataTransform(XferTransform)}
	 * and finish by calling the {@link co.tophe.parser.BodyTransformChain} constructor with the last Builder in the chain.</p>
	 *
	 * @param <T>
	 * @see co.tophe.parser.XferTransformChain.Builder
	 */
	public static class Builder<T> extends XferTransformChain.Builder<HttpResponse, T> {

		@Override
		protected BodyTransformChain<T> buildInstance(XferTransformChain.Builder<HttpResponse, T> builder) {
			return new BodyTransformChain((Builder<T>) builder);
		}

		@Override
		public <V> Builder<V> addDataTransform(XferTransform<T, V> endTransform) {
			return (Builder<V>) super.addDataTransform(endTransform);
		}
	}

	/**
	 * Helper constructor to do a single transformation from an {@link java.io.InputStream}.
	 *
	 * @param endTransform the transformation that will turn the {@link java.io.InputStream} into type {@link T}.
	 */
	public BodyTransformChain(XferTransform<InputStream, T> endTransform) {
		this(createBuilder(XferTransformResponseInputStream.INSTANCE).addDataTransform(endTransform));
	}

	/**
	 * Default constructor to build the chained transformation from the {@link co.tophe.parser.BodyTransformChain.Builder}.
	 */
	public BodyTransformChain(Builder<T> builder) {
		super(builder);
	}
}
