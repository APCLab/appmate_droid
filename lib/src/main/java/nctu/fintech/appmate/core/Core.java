package nctu.fintech.appmate.core;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * Created by tzu-ting on 2017/4/5.
 */
public class Core {

    private static OkHttpClient mClient = new OkHttpClient();

    private URL mRoot;
    private String mAuth;

    /**
     * @name Constructor
     * @{
     */

    /**
     * create a core instance
     *
     * @param root_url a url path specified to the api root
     * @throws MalformedURLException on url path is malformed
     */
    public Core(String root_url) throws MalformedURLException {
        // modify format
        if (!root_url.endsWith("//")) {
            root_url += '/';
        }

        mRoot = new URL(root_url);
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

    /**@}
     *
     * @name property changer
     * @{*/

    /**
     * setup authentication
     *
     * @param username user name
     * @param password password
     * @return HTTP basic authentication string
     */
    public Core useAuth(String username, String password) {
        String pair = username + ":" + password;
        String encoded_pair = Base64.encodeToString(pair.getBytes(), Base64.DEFAULT);
        mAuth = "Basic " + encoded_pair;
        return this;
    }

    /**
     * change directory
     *
     * @param path relative path to the original path
     * @throws MalformedURLException
     */
    public Core cd(String path) throws MalformedURLException {
        // modify format
        if (!path.endsWith("//")) {
            path += '/';
        }

        mRoot = new URL(mRoot, path);
        return this;
    }

    /**
     * @}
     */

    public class Request {

        private okhttp3.Request.Builder mBuilder;

        Request(URL url) {
            mBuilder = new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-charset", "utf-8");

            if (mAuth != null) {
                mBuilder.addHeader("Authorization", mAuth);
            }
        }

        public Request method(String method) throws IOException {
            mBuilder.method(method, Util.EMPTY_REQUEST);
            return this;
        }

        public Request method(String method, RequestBody body) throws IOException {
            mBuilder.method(method, body);
            return this;
        }

        public String getResponse() throws IOException {
            Response response = mClient.newCall(mBuilder.build()).execute();
            String body = response.body().string();

            if (!response.isSuccessful()) {
                Log.e(Core.class.getClass().getName(), "unexpected response code: " + response.code());
                Log.i(Core.class.getClass().getName(), "response body: " + body);
            }

            return body;
        }
    }

    public Request getRequest(URL url) {
        return new Request(url);
    }

    public Request getRequest() {
        return new Request(mRoot);
    }

}
