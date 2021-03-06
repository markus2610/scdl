package net.rdrei.android.scdl2.api;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.rdrei.android.scdl2.Config;
import roboguice.util.Ln;

import com.google.inject.Inject;

public class SoundcloudApiService {
	@Inject
	private URLWrapperFactory mURLWrapperFactory;

	private static final String BASE_URL_HTTPS = "https://api.soundcloud.com";
	private static final String BASE_URL_HTTP = "http://api.soundcloud.com";
	private static final String CONTENT_ENCODING = "UTF-8";
	private static final String CLIENT_ID_PARAMETER = "client_id";

	private boolean mUseSSL = true;

	public SoundcloudApiService() {
		super();
	}

	/**
	 * @return The BASE_URL including the correct version field, if added at
	 *         some point.
	 */
	protected String getBaseUrl() {
		if (isUseSSL()) {
			return BASE_URL_HTTPS;
		} else {
			return BASE_URL_HTTP;
		}
	}

	/**
	 * Builds a new URL based on the base url and the provided resource.
	 * 
	 * @param resource
	 * @return URL
	 * @throws MalformedURLException
	 */
	protected URLWrapper buildUrl(final String resource)
			throws MalformedURLException {
		return buildUrl(resource, null);
	}

	protected URLWrapper buildUrl(final String resource,
			Map<String, String> parameters) throws MalformedURLException {
		final StringBuilder strUrl = new StringBuilder(getBaseUrl());
		strUrl.append(resource);

		if (parameters == null) {
			parameters = new HashMap<>();
		}

		parameters.put(CLIENT_ID_PARAMETER, Config.API_CONSUMER_KEY);

		if (parameters.size() > 0) {
			strUrl.append('?');
			strUrl.append(getParametersString(parameters));
		}

		return mURLWrapperFactory.create(strUrl.toString());
	}

	/**
	 * Assemble a parameter string from a mapping.
	 * 
	 * @param parameters
	 *            Mapping of parameter names to values.
	 * @return String representation.
	 */
	protected static String getParametersString(
			final Map<String, String> parameters) {
		final StringBuilder builder = new StringBuilder();
		for (final Entry<String, String> entry : parameters.entrySet()) {
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(encodeUrl(entry.getValue()));
			builder.append('&');
		}

		// Remove last '&'
		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * Encode the URL.
	 * 
	 * @param original
	 *            Original URL.
	 * @return Encoded URL.
	 */
	private static String encodeUrl(final String original) {
		try {
			return URLEncoder.encode(original, CONTENT_ENCODING);
		} catch (final UnsupportedEncodingException e) {
			// should never be here..
			Ln.e(e);
			return original;
		}
	}

	public boolean isUseSSL() {
		return mUseSSL;
	}

	public void setUseSSL(final boolean useSSL) {
		mUseSSL = useSSL;
	}
}
