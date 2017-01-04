package nctu.fintech.appmate;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.code.regexp.Pattern;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

class DbCore {

    final URL url;
    final boolean useAuth;
    final String username;
    private final String authStr;

    /**
     * Create a {@link DbCore} instance.
     *
     * @param host     host domain
     * @param username username
     * @param password password
     */
    DbCore(String host, String username, String password) {
        // anti-foolish
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("illegal host name");
        }

        // parse host string
        Map<String, String> matcher = Pattern.compile
                (
                        "^(?:https?:\\/\\/)?(?:(?<auth>(?<user>[\\w-]+):\\w+)@)?(?<host>[\\w\\d-]+(?:\\.[\\w\\d-]+)+(?::\\d+)?)",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(host)
                .namedGroups();

        // set host
        String cleanHost = matcher.get("host");
        if (cleanHost == null) {
            throw new IllegalArgumentException("host domain not found");
        }

        // set base url
        URL url;
        try {
            url = new URL("http://" + cleanHost + "/api/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal host domain", e);
        }

        // check auth assigned
        String authStrFromHost = matcher.get("auth");

        boolean isHostSetAuth = authStrFromHost != null;
        boolean isParamSetAuth = username != null && !username.isEmpty();

        if (isHostSetAuth && isParamSetAuth) {
            throw new UnsupportedOperationException("more than 1 authentication parameter is assigned");
        } else if (isParamSetAuth) {
            this.url = url;
            this.useAuth = true;
            this.username = username;
            this.authStr = genAuthStr(username + ":" + password);
        } else if (isHostSetAuth) {
            this.url = url;
            this.useAuth = true;
            this.username = matcher.get("user");
            this.authStr = genAuthStr(authStrFromHost);
        } else {
            this.url = url;
            this.useAuth = false;
            this.username = null;
            this.authStr = null;
        }
    }

    /**
     * Alias of {@link DbCore#DbCore(String, String, String)}
     *
     * @param host host domain
     */
    DbCore(String host) {
        this(host, null, null);
    }

    DbCore(DbCore core) {
        this.url = core.url;
        this.useAuth = core.useAuth;
        this.username = core.username;
        this.authStr = core.authStr;
    }

    /**
     * create a null instance
     */
    DbCore() {
        this.url = null;
        this.useAuth = false;
        this.username = null;
        this.authStr = null;
    }

    /**
     * Generate the string used in HTTP header {@code Authorization}.
     * THIS METHOD SHOULD ONLY USE BY CONSTRUCTOR
     *
     * @param auth_string user name and password pair, set in {@code user:password} format
     * @return HTTP basic authentication string
     */
    private static String genAuthStr(String auth_string) {
        String auth_encoded = Base64.encodeToString(auth_string.getBytes(), Base64.DEFAULT);
        return "Basic " + auth_encoded;
    }

    /**
     * Create a {@link HttpURLConnection} instance
     *
     * @param url url to open
     * @return a {@link HttpURLConnection} instance, with auth header is set when authentication is required
     */
    HttpURLConnection openUrl(URL url) throws IOException {
        // open connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // set properties
        con.setRequestProperty("accept", "application/json");
        con.setRequestProperty("accept-charset", "utf-8");
        if (useAuth) {
            con.setRequestProperty("Authorization", authStr);
        }

        return con;
    }

    /**
     * Create a {@link HttpURLConnection} instance indicate to {@link DbCore#url}
     *
     * @return a {@link HttpURLConnection} instance
     */
    HttpURLConnection openUrl() throws IOException {
        return openUrl(url);
    }


    /**
     * Process request content upstream.
     *
     * @param con   {@link HttpURLConnection} instance
     * @param tuple data to be upload
     * @throws IOException
     */
    static void sendRequest(HttpURLConnection con, Tuple tuple) throws IOException {
        // initialize
        String boundary = "Boundary" + Long.toHexString(System.currentTimeMillis());

        // set property
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        con.setDoOutput(true);

        boundary = "\r\n--" + boundary;

        // build content to be uploaded
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> p : tuple.entrySet()) {
            // boundary
            builder.append(boundary);

            // field name
            builder.append("\r\nContent-Disposition: form-data; name=\"");
            builder.append(p.getKey());
            builder.append('"');

            //TODO filename!?

            // start upload
            builder.append("\r\n\r\n");
            builder.append(p.getValue());
        }

        builder.append(boundary);
        builder.append("--");

        // upstream
        try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.writeUTF(builder.toString());
        }
    }

    /**
     * Get response content.
     *
     * @param con a {@link HttpURLConnection}
     * @return response content
     * @throws IOException
     */
    @NonNull
    static String getResponse(HttpURLConnection con) throws IOException {
        // check response code
        int code = con.getResponseCode();
        if (code < HttpURLConnection.HTTP_OK || code >= HttpURLConnection.HTTP_MULT_CHOICE) {
            Log.e(DbCore.class.getName(), "unexpected HTTP response code received: " + code);
        }

        // read response
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String in;
            while ((in = reader.readLine()) != null) {
                builder.append(in);
            }
        }
        return builder.toString();
    }
}
