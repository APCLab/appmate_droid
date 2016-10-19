package nctu.fintech.appmate;

import android.util.Base64;

import com.google.code.regexp.Pattern;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Connection parameters set
 */
final class ConnectionParameterSet {

    final String host;
    final String user;
    final String table;

    private final URL _baseUrl;
    private final String _auth;

    /**
     * Create parameter set of connection parameter to specific database
     *
     * @param hostStr host domain and port number. i.e. {@code db.example.com}, {@code db.example.com:8000}, {@code user:passwd@example.com}
     * @throws MalformedResourceException on host domain info not found
     */
    ConnectionParameterSet(String hostStr) {
        Map<String, String> val = resolveHost(hostStr);
        host = val.get("host");

        if (val.containsKey("_auth")) {
            user = val.get("user");
            _auth = generateAuthString(val.get("_auth"));
        } else {
            user = null;
            _auth = null;
        }

        table = null;
        _baseUrl = generateURL();
    }

    /**
     * Create parameter set of authenticated connection parameter to specific database
     *
     * @param hostStr  host domain and port number, SHOULD NOT contain username/password information
     * @param username username to login
     * @param password password to login
     * @throws MalformedResourceException on host domain info not found
     * @throws IllegalArgumentException   Username and password assignment duplicated. Occur on
     */
    ConnectionParameterSet(String hostStr, String username, String password) {
        Map<String, String> val = resolveHost(hostStr);
        host = val.get("host");

        if (val.containsKey("_auth")) {
            throw new IllegalArgumentException("Duplicate username/password assigned");
        } else {
            user = username;
            _auth = generateAuthString(username + ":" + password);
        }

        table = null;
        _baseUrl = generateURL();
    }

    /**
     * Create parameter set of connection parameter to specific table
     *
     * @param base    base connection parameter set
     * @param tableNm name of table to serve
     */
    ConnectionParameterSet(ConnectionParameterSet base, String tableNm) {
        verifyTableName(tableNm);

        host = base.host;
        user = base.user;
        _auth = base._auth;
        table = tableNm;
        _baseUrl = generateURL();
    }

    /**
     * Create parameter set of connection parameter to specific table
     *
     * @param hostStr host domain and port number
     * @throws MalformedResourceException on host domain info not found
     */
    ConnectionParameterSet(String hostStr, String tableNm) {
        verifyTableName(tableNm);

        Map<String, String> val = resolveHost(hostStr);
        host = val.get("host");

        if (val.containsKey("_auth")) {
            user = val.get("user");
            _auth = generateAuthString(val.get("_auth"));
        } else {
            user = null;
            _auth = null;
        }

        table = tableNm;
        _baseUrl = generateURL();
    }

    /**
     * Create parameter set of authenticated connection parameter to specific table
     *
     * @param hostStr  host domain and port number, SHOULD NOT contain username/password information
     * @param username username to login
     * @param password password to login
     * @throws MalformedResourceException on host domain info not found
     * @throws IllegalArgumentException   Username and password assignment duplicated. Occur on
     */
    ConnectionParameterSet(String hostStr, String username, String password, String tableNm) {
        verifyTableName(tableNm);

        Map<String, String> val = resolveHost(hostStr);
        host = val.get("host");

        if (val.containsKey("_auth")) {
            throw new IllegalArgumentException("Duplicate username/password assigned");
        } else {
            user = username;
            _auth = generateAuthString(username + ":" + password);
        }

        table = tableNm;
        _baseUrl = generateURL();
    }

    /**
     * Analyze {@code host} string. Check format and retrieve {@code authentication} and {@code host domain} info.
     * Implemented simple anti-foolish procedure.
     *
     * @param host received host string
     * @return data retrieve form {@code host}, must contains host domain string tags {@code host}, could  contains authentication info tags {@code _auth} and {@code user}
     * @throws MalformedResourceException on host domain info not found
     */
    private static Map<String, String> resolveHost(String host) {
        Map<String, String> val = Pattern.compile
                (
                        "^(?:https?:\\/\\/)?(?:(?<_auth>(?<user>[\\w-]+):\\w+)@)?(?<host>[\\w-]+(?:\\.[\\w-]+)+(?::\\d+)?)",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(host)
                .namedGroups();
        if (!val.containsKey("host")) {
            throw new MalformedResourceException(host);
        }
        return val;
    }

    /**
     * Verify table name. Prevent illegal character involved in table name.
     *
     * @param table table name
     */
    private static void verifyTableName(String table) {
        if (!Pattern.compile("^[\\w-]+$")
                .matcher(table)
                .matches()) {
            throw new IllegalArgumentException("Table name is illegal");
        }
    }

    /**
     * THIS METHOD SHOULD ONLY USE BY CONSTRUCTOR
     * Generate the string used in HTTP header {@code Authorization}
     *
     * @param auth_string user name and password pair, set in {@code user:password} format
     * @return HTTP basic authentication string
     */
    private static String generateAuthString(String auth_string) {
        String auth_encoded = Base64.encodeToString(auth_string.getBytes(), Base64.DEFAULT);
        return "Basic " + auth_encoded;
    }

    /**
     * THIS METHOD SHOULD ONLY USE BY CONSTRUCTOR
     * Generate base URL for all query
     *
     * @return a {@link URL} point to api root
     */
    private URL generateURL() {
        try {
            if (table == null) {
                return new URL("http://" + host + "/api/");
            } else {
                return new URL("http://" + host + "/api/" + table);
            }
        } catch (MalformedURLException e) {
            Debug.e(this, e);
            throw new MalformedResourceException(host);
        }
    }

    /**
     * Open a {@link HttpURLConnection} to api root, with authentication header appended if need
     *
     * @return a connection to api root
     */
    HttpURLConnection openHttpConnection() throws IOException {
        return openHttpConnection(null);
    }

    /**
     * Open a {@link HttpURLConnection} to specific resource, with authentication header appended if need
     *
     * @param resourceUri resource path. SHOULD NOT include domain info or {@code }
     * @return a connection to specific resource
     */
    HttpURLConnection openHttpConnection(String resourceUri) throws IOException {
        try {
            URL url = resourceUri == null ? _baseUrl : new URL(_baseUrl, resourceUri);

            //= new URL(_baseUrl, spec);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // authorize
            if (_auth != null) {
                con.setRequestProperty("Authorization", _auth);
            }

            return con;

        } catch (MalformedURLException e) {
            Debug.d(this, e);
            throw new MalformedResourceException(resourceUri);
        }
    }

}
