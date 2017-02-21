package org.mockito.release.notes.util;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * This class contains standard operations for skim over GitHub API responses.
 */
public abstract class GitHubFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubFetcher.class);

    private static final String RELATIVE_LINK_NOT_FOUND = "none";
    private String nextPageUrl;

    public GitHubFetcher(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    //TODO SF is this method needed? It's never used
    public GitHubFetcher init() throws IOException {
        URLConnection urlConnection = new URL(nextPageUrl).openConnection();
        nextPageUrl = extractRelativeLink(urlConnection.getHeaderField("Link"), "next");
        return this;
    }

    public boolean hasNextPage() {
        return !RELATIVE_LINK_NOT_FOUND.equals(nextPageUrl);
    }

    public List<JSONObject> nextPage() throws IOException {
        if(RELATIVE_LINK_NOT_FOUND.equals(nextPageUrl)) {
            throw new IllegalStateException("GitHub API no more issues to fetch");
        }
        URL url = new URL(nextPageUrl);
        LOG.info("GitHub API querying issue page {}", queryParamValue(url, "page"));
        URLConnection urlConnection = url.openConnection();

        LOG.info("GitHub API rate info => Remaining : {}, Limit : {}",
                urlConnection.getHeaderField("X-RateLimit-Remaining"),
                urlConnection.getHeaderField("X-RateLimit-Limit")
        );
        nextPageUrl = extractRelativeLink(urlConnection.getHeaderField("Link"), "next");

        return parseJsonFrom(urlConnection);
    }

    private String queryParamValue(URL url, String page) {
        String query = url.getQuery();
        for (String param : query.split("&")) {
            if(param.startsWith(page)) {
                return param.substring(param.indexOf('=') + 1, param.length());
            }
        }
        return "N/A";
    }

    private List<JSONObject> parseJsonFrom(URLConnection urlConnection) throws IOException {
        InputStream response = urlConnection.getInputStream();

        String content = IOUtil.readFully(response);
        LOG.info("GitHub API responded successfully.");
        @SuppressWarnings("unchecked")
        List<JSONObject> issues = (List<JSONObject>) JSONValue.parse(content);
        LOG.info("GitHub API returned {} issues.", issues.size());
        return issues;
    }


    private String extractRelativeLink(String linkHeader, final String relativeType) {
        if (linkHeader == null) {
            return RELATIVE_LINK_NOT_FOUND;
        }

        // See GitHub API doc : https://developer.github.com/guides/traversing-with-pagination/
        // Link: <https://api.github.com/repositories/6207167/issues?access_token=a0a4c0f41c200f7c653323014d6a72a127764e17&state=closed&filter=all&page=2>; rel="next",
        //       <https://api.github.com/repositories/62207167/issues?access_token=a0a4c0f41c200f7c653323014d6a72a127764e17&state=closed&filter=all&page=4>; rel="last"
        for (String linkRel : linkHeader.split(",")) {
            if (linkRel.contains("rel=\"" + relativeType + "\"")) {
                return linkRel.substring(
                        linkRel.indexOf("http"),
                        linkRel.indexOf(">; rel=\"" + relativeType + "\""));
            }
        }
        return RELATIVE_LINK_NOT_FOUND;
    }
}
