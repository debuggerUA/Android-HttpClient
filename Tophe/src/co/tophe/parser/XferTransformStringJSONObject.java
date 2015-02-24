package co.tophe.parser;

import org.json.JSONException;
import org.json.JSONObject;

import co.tophe.ImmutableHttpRequest;

/**
 * <p>A {@link XferTransform} to turn a {@code String} into a {@link org.json.JSONObject}</p>
 *
 * <p>Use the {@link #INSTANCE}</p>
 *
 * @see BodyToJSONObject
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformStringJSONObject implements XferTransform<String, JSONObject> {
	/**
	 * The instance you should use when you want to get a {@link org.json.JSONObject} from an {@link java.io.InputStream}.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final XferTransformStringJSONObject INSTANCE = new XferTransformStringJSONObject();

	private XferTransformStringJSONObject() {
	}

	@Override
	public JSONObject transformData(String srcData, ImmutableHttpRequest request) throws ParserException {
		try {
			return new JSONObject(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
