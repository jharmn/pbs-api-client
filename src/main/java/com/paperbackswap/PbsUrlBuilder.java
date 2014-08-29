package com.paperbackswap;

import gumi.builders.UrlBuilder;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PbsUrlBuilder {
    public static final Integer DEFAULT_LIMIT = 50;
    private UrlBuilder builder = UrlBuilder.empty();

    protected enum PagingParameters {
        LIMIT {
            public String toString() {
                return "Limit";
            }
        },
        OFFSET {
            public String toString() {
                return "Offset";
            }
        }
    }

    /**
     * Provides reference for oauth-related query params
     */
    protected static class Oauth {
        protected final static Set<String> Parameters;

        static {
            Parameters = new HashSet<String>();
            Parameters.add("oauth_signature");
            Parameters.add("oauth_nonce");
            Parameters.add("oauth_token");
            Parameters.add("oauth_timestamp");
            Parameters.add("oauth_consumer_key");
            Parameters.add("oauth_signature_method");
            Parameters.add("oauth_version");
        }
    }

    /**
     * Used internally to provide efficient fluent operations, as @UrlBuilder is final
     * @param builder
     */
    protected PbsUrlBuilder(UrlBuilder builder) {
        this.builder = builder;
        this.builder = addHostAndScheme(builder);
        this.builder = this.builder.setParameter(PbsUrlInfo.LIMIT_KEY.toString(), DEFAULT_LIMIT.toString());
    }

    /**
     * Creates a properly formatted URL for the paperbackswap.com API
     * @param url
     * @return
     */
    public static PbsUrlBuilder fromUrl(String url) {
        UrlBuilder builder = UrlBuilder.fromString(url);
        return new PbsUrlBuilder(builder);
    }

    /**
     * Creates a properly formatted URL for the paperbackswap.com API
     * @param url
     * @return
     */
    public static PbsUrlBuilder fromUrl(URL url) {
        UrlBuilder builder = UrlBuilder.fromUrl(url);
        return new PbsUrlBuilder(builder);
    }

    /**
     * Creates a properly formatted URL for the paperbackswap.com API
     * @param path
     * @return
     */
    public static PbsUrlBuilder fromPath(PbsUrlInfo path) {
        String[] pathAndQuery = StringUtils.split(path.toString(), "?");
        String pathPart = pathAndQuery[0];
        String queryPart = pathAndQuery[1];

        UrlBuilder builder = UrlBuilder.empty().withPath(pathPart).withQuery(queryPart);
        return new PbsUrlBuilder(builder);
    }

    public PbsUrlBuilder withOffsetLimit(Integer offset, Integer limit) {
        builder = builder.setParameter(PagingParameters.LIMIT.toString(), limit.toString())
                .setParameter(PagingParameters.OFFSET.toString(), limit.toString());
        return this;
    }

    public PbsUrlBuilder withoutOAuthQuery() {
        removeOauthQuery();
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public URI toUri() {
        return builder.toUri();
    }

    /**
     * Fills in provided ISBN in URI parameter
     * @param isbn
     * @return
     */
    public PbsUrlBuilder withIsbn(Long isbn) {
        return withIsbn(isbn.toString());
    }

    /**
     * Fills in provided ISBN in URI parameter
     * @param isbn
     * @return
     */
    public PbsUrlBuilder withIsbn(String isbn) {
        builder = builder.setParameter(PbsUrlInfo.ISBN_KEY.toString(), isbn);
        return this;
    }

    // Extract the ISBN from an PBS API URL.
    public Long extractIsbn() {

        if (builder.queryParameters.containsKey(PbsUrlInfo.ISBN_KEY)) {
            // Get first query param match
            return Long.parseLong(builder.queryParameters.get(
                    PbsUrlInfo.ISBN_KEY.toString()
            ).get(0));
        }
        return 0L;
    }

    /**
     * Strips OAuth 1.0a query parameters from query string
     * @return
     */
    protected void removeOauthQuery() {
        removeQuerystringItems(Oauth.Parameters);
/*
        Iterator<String> iterator = builder.queryParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();

            if (name.toLowerCase().startsWith("oauth")) {
                builder = builder.removeParameters(name);
            }
        }
*/
    }

    protected UrlBuilder addHostAndScheme(UrlBuilder builder) {
        return addHostAndScheme(builder, PbsUrlInfo.DEFAULT_HOST.toString(),
                PbsUrlInfo.DEFAULT_SCHEME.toString());
    }

    protected UrlBuilder addHostAndScheme(UrlBuilder builder, String host, String scheme) {
        return builder
                .withHost(host)
                .withScheme(scheme);
    }

    /**
     * Removes a specific set of querystring items
     * @param excludedItems
     * @return
     */
    protected void removeQuerystringItems(Set<String> excludedItems) {
        for (String exclude : excludedItems) {
            if (builder.queryParameters.containsKey(exclude)) {
                builder = builder.removeParameters(exclude);
            }
        }
    }

}