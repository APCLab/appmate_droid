package nctu.fintech.appmate.core;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A class which handles the connection parameters. This class support method chaining.
 */
public class Core {

    /**
     * the http client.
     */
    private static OkHttpClient mClient = new OkHttpClient();

    /**
     * a {@link URL} refer to the specific resource
     */
    private URL mRoot;

    /**
     * the string to set as authentication header if needed
     */
    private String mAuth;

    /**
     * create a core instance
     *
     * @param root_url a url path specified to the api root
     * @throws MalformedURLException on url path is malformed
     */
    public Core(String root_url) {
        root_url = prettifyUrl(root_url);

        try {
            mRoot = new URL(root_url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("not a valid url", e);
        }
    }

    /**
     * copy core instance
     *
     * @param other other core instance
     */
    public Core(Core other) {
        this.mRoot = other.mRoot;
        this.mAuth = other.mAuth;
    }

    /**
     * setup authentication
     *
     * @param username user name
     * @param password password
     * @return HTTP basic authentication string
     */
    public Core useAuth(String username, String password) {
        String pair = username + ":" + password;
        String encoded_pair = Base64.encodeToString(pair.getBytes(), Base64.DEFAULT).trim();
        mAuth = "Basic " + encoded_pair;
        return this;
    }

    /**
     * change directory
     *
     * @param path relative path to the original path
     * @throws MalformedURLException
     */
    public Core cd(String path) {
        path = prettifyUrl(path);

        try {
            mRoot = new URL(mRoot, path);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("not a valid url", e);
        }
        return this;
    }

    /**
     * to ensure the end of URL string is a slash
     *
     * @param url url string
     * @return url sting
     */
    private String prettifyUrl(String url) {
        if (!url.endsWith("/")) {
            url += '/';
        }
        return url;
    }

    /**
     * A class handles the connection process. This class support method chaining.
     * <p>
     * This class does not have public constructor. To create a instance use {@link Core#createConnection(URL)}.
     * <p>
     */
    public class Connection {

        private Request.Builder mBuilder;

        /**
             * create a {@link  Connection} instance
             * @param url the url refer to the resource which you want to query
             */
        Connection(URL url) {
            mBuilder = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-charset", "utf-8");

            if (mAuth != null) {
                mBuilder.addHeader("Authorization", mAuth);
            }
        }

        /**
             * setup request method
             * @param method request method
             * @return itself
             */
        public Connection method(String method) {
            mBuilder.method(method, null);
            return this;
        }

        /**
             * setup request method
             * @param method request method
             * @param body request body
             * @return itself
             */
        public Connection method(String method, RequestBody body) {
            mBuilder.method(method, body);
            return this;
        }

        /**
             * get response
             *
             * @return response body
             * @throws IOException
             */
        public String getResponse() throws IOException {
            assert mBuilder != null;

            // build request
            Request request = mBuilder.build();
            mBuilder = null;

            // get response
            Response response = mClient.newCall(request).execute();
            String body = response.body().string();

            // handle result
            if (!response.isSuccessful()) {
                throw new UnexpectedResponseException(response.code(), response.message(), body);
            }

            return body;
        }

        /**
             * get response, and cast response body to {@link JsonObject}
             * @return response body
             * @throws IOException
             */
        public JsonObject getResponseAsJson() throws IOException {
            return new JsonParser()
                    .parse(getResponse())
                    .getAsJsonObject();
        }

    }

    /**
     * create a {@link Connection} instance
     *
     * @param url the url refer to the resource which you want to query
     * @return a {@link Connection} instance
     */
    public Connection createConnection(URL url) {
        return new Connection(url);
    }

    /**
     * create a {@link Connection} instance
     * @return a {@link Connection} instance refer to the root of this {@link Core}
     */
    public Connection createConnection() {
        return new Connection(mRoot);
    }

}
