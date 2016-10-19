package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.code.regexp.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Class {@link AppmateTable} helps the data transfer and communicating with {@code appmate} server-side api.
 * <p>
 * <p>
 *     For develops whom use the database without  authentication,
 *     frequency request  would trigger <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a> of the database.
 *     To resolve this issue a {@code CSRF token} is required but currently it is not provided and is not implemented in this client library.
 *     If frequently usage of non-authenticated database is required and reasonable, please contact the manager.
 * </p>
 * <p>
 *     It should be noted that most of methods in {@link AppmateTable} create a network connection but it <strong>doesn't</strong> handle the threading.
 *     Due to Android's policy, developers should avoid any network progress run on main thread.
 *     You can avoid {@link NetworkOnMainThreadException} by using {@link android.os.AsyncTask} or {@link Thread}.
 *     See <a href="https://developer.android.com/guide/components/processes-and-threads.html">Android API Guides: Processes and Threads</a>.
 * </p>
 *
 * @version 2.0.0
 * @since Sep 2016
 * @author Tzu-ting, fintech center, NCTU
 */
public class AppmateTable {

    /*
     * Constants, Macros
     */
    private class RequestMethod {
        static final String Get = "GET";
        static final String Post = "POST";
        static final String Put = "PUT";
        static final String Delete = "DELETE";
    }

    /**
     * Default character set to encode string for query parameters or request contents
     */
    private static final String DEFAULT_CHARSET = "utf-8";

    /*
     * Private fields
     */
    private final ConnectionParameterSet _conn;

    /*
     * Constructor
     */

    /**
     * Create a {@link AppmateTable} instance using parameters in {@link AppmateDb}
     *
     * @param baseDb database connection parameter
     * @param table  table name
     */
    public AppmateTable(@NonNull AppmateDb baseDb, @NonNull String table) {
        _conn = new ConnectionParameterSet(baseDb._conn, table);
    }

    /**
     * Create a {@link AppmateTable} instance referrer to an {@code appmate} database with specified host domain and table name.
     * <p>
     * <p>
     * For develop whom use this constructor, it is known that frequency request without authentication
     * would trigger <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a> of the database.
     * Please consider use authenticated method instead.
     * </p>
     * <p>
     * This constructor offers a shortcut to create a instance referrer to an authenticated database by assigning a host URL with username and password.
     * i.e. {@code user:password@example.com} or {@code user:pass@exampleWithPort.com:8000}.
     * For situation that username or password contains a special character like {@code @} or {@code .},
     * please use the standard constructor {@link AppmateTable#AppmateTable(String, String, String, String)} instead.
     * </p>
     *
     * @param host  host domain string
     * @param table table name
     */
    public AppmateTable(@NonNull String host, @NonNull String table) {
        _conn = new ConnectionParameterSet(host, table);
    }

    /**
     * Create a {@link AppmateTable} instance referrer to an authenticated {@code appmate} database with specified host domain and table name.
     *
     * @param host     host domain string
     * @param username user name to login
     * @param password password
     * @param table    table name
     */
    public AppmateTable(@NonNull String host, @NonNull String username, @NonNull String password, @NonNull String table) {
        _conn = new ConnectionParameterSet(host, username, password, table);
    }

    /*
     * public members: get attribute
     */

    /**
     * Obtain host domain, probably contains a port indicator
     *
     * @return host domain string
     */
    public String getHost() {
        return _conn.host;
    }

    /**
     * Obtain the user name, or null when database is deemed not required a login
     *
     * @return user name
     */
    public String getUsername() {
        return _conn.user;
    }

    /**
     * Obtain the table name
     *
     * @return table name
     */
    public String getTable() {
        return _conn.table;
    }

    /*
     * public members: operation
     */

    /**
     * Get all items in the table
     *
     * @return a {@link JSONObject} array, could be length 0 on success, or null on error
     */
    public JSONObject[] getAll() {
        try {
            JSONArray array = new JSONArray(request(RequestMethod.Get, "", HttpURLConnection.HTTP_OK));
            JSONObject[] result = new JSONObject[array.length()];
            for (int i = 0; i < array.length(); i++) {
                result[i] = array.getJSONObject(i);
            }
            return result;
        } catch (JSONException e) {
            Debug.e(this, e);
        }
        return null;
    }

    /**
     * Get item by id
     *
     * @param id id of specified item
     * @return specified item on success, or null on error
     */
    public JSONObject getItem(int id) {
        try {
            return new JSONObject(request(RequestMethod.Get, Integer.toString(id), HttpURLConnection.HTTP_OK));
        } catch (JSONException e) {
            Debug.e(this, e);
        }
        return null;
    }

    /**
     * Get items by filter.
     * <p>
     * <p>
     *     This method can parse natural mathematical expressions into query parameters. See the following filter writing rules:
     *     <ul>
     *         <li>
     *             <strong>Full match</strong> (type: {@link String})<br/>
     *             use {@code =} or {@code ==}<br/>
     *             notice that quote is not allowed, or it will be parsed into part of matching string. i.e. <br/>
     *             {@code stringField=matching string with whitespace}<br/>
     *             {@code stringField == matching_string_without_whitespace}
     *         </li>
     *         <li>
     *             <strong>Exact equals</strong> (type: {@link Integer}, {@link Boolean})<br/>
     *             use {@code =} or {@code ==}<br/>
     *             i.e. {@code integerField=100}, {@code boolField==0}
     *         </li>
     *         <li>
     *             <strong>Inequality</strong> (type: {@link Integer}, {@link Float}, {@link Double})<br/>
     *             support symbols: {@code >}, {@code >=}, {@code <}, {@code <=}<br/>
     *             i.e. {@code floatField > 5.00}, {@code integerField<=99}
     *         </li>
     *     </ul>
     *     Notice that all filters is giving as {@link String} type, numeric and logical types should converted to string type before set.
     * </p>
     * <p>
     *     This method supports multiple filter querying, the result will be the intersection of given rules.
     *     To set multiple filters use commas to separate each rule. i.e. {@code adapter.getFiltered("stringField=Hello", "integerField>=99");}
     * </p>
     *
     * @param filters filter rules
     * @return items match specified rules, or null on error
     */
    public JSONObject[] getFiltered(String... filters) {

        // parse filters and build query string
        Pattern pattern = Pattern.compile("(?<field>\\w+)\\s?(?<sym>=|==|>|>=|<|<=)\\s?(?<value>.+)");
        StringBuilder builder = new StringBuilder("?");

        for (String f : filters) {
            // parse filter
            Map<String, String> match = pattern
                    .matcher(f)
                    .namedGroups();

            // break on filter not match
            if (match.isEmpty()) {
                continue;
            }

            // retrieve each part
            String field, sym, value;
            try {
                field = URLEncoder.encode(match.get("field"), DEFAULT_CHARSET);
                sym = URLEncoder.encode(match.get("sym"), DEFAULT_CHARSET);
                value = URLEncoder.encode(match.get("value"), DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                Debug.d(this, e);
                return null;
            }

            // select query parameter format by symbol
            switch (match.get("sym")) {
                case "=":
                case "==":
                    // do nothing
                    break;
                case ">":
                    field += "__gt";
                    break;
                case ">=":
                    field += "__gte";
                    break;
                case "<":
                    field += "__lt";
                    break;
                case "<=":
                    field += "__lte";
                    break;
            }

            // build query string
            if (builder.length() > 1) {
                builder.append("&");
            }
            builder.append(field);
            builder.append("=");
            builder.append(value);
        }

        // send request
        try {
            JSONArray array = new JSONArray(request(RequestMethod.Get, builder.toString(), HttpURLConnection.HTTP_OK));
            JSONObject[] result = new JSONObject[array.length()];
            for (int i = 0; i < array.length(); i++) {
                result[i] = array.getJSONObject(i);
            }
            return result;

        } catch (JSONException e) {
            Debug.e(this, e);
        }

        return null;
    }

    /*
     * private:: proceed request
     */

    /**
     * Shortcut for {@link AppmateTable#request(String, String, ContentType, String, int)}, for action which requires no upstream data
     *
     * @param method           request method
     * @param path             resource path
     * @param expectedResponse expected response code
     * @return response content
     */
    private String request(String method, @NonNull String path, int expectedResponse) {
        return request(method, path, null, null, expectedResponse);
    }

    /**
     * This method process request action, including {@link HttpURLConnection} usage, exception handling and response parse.
     *
     * @param method           request method
     * @param path             resource path
     * @param contentType      upstream content type
     * @param upstreamData     upstream data
     * @param expectedResponse expected response code
     * @return response content
     */
    //TODO 改用 multi-part 上傳
    @Nullable
    private String request(@NonNull String method, @NonNull String path, ContentType contentType, @Nullable String upstreamData, int expectedResponse) {
        // log
        Debug.i(this, "Proceed [%s] %s", method, path);

        try {
            // open connection
            HttpURLConnection con = _conn.openHttpConnection(path);
            con.setRequestMethod(method);
            con.setRequestProperty("accept", ContentType.Json.getHeaderString());

            // upload data
            if (upstreamData != null) {
                con.setRequestProperty("content-type", contentType.getHeaderString());
                con.setDoOutput(true);

                try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                    writer.writeBytes(upstreamData);
                }
            }

            // check response code
            int code = con.getResponseCode();
            boolean isAccepted = code == expectedResponse;

            // retrieve response content
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String in;
                while ((in = reader.readLine()) != null) {
                    builder.append(in);
                }
            }

            // log information when a unexpected response code
            if (!isAccepted) {
                // parse response
                JSONObject response = new JSONObject(builder.toString());
                String detail = response.getString("detail");

                // output
                switch (code) {
                    case HttpURLConnection.HTTP_BAD_REQUEST: // 400: Bad request
                        Debug.w(this, "HTTP 400 - Bad request");
                        Debug.w(this, detail);
                        break;

                    case HttpURLConnection.HTTP_UNAUTHORIZED: // 401: Unauthorized
                        Debug.w(this, "HTTP 401 - Unauthorized");
                        Debug.d(this, detail);
                        break;

                    case HttpURLConnection.HTTP_FORBIDDEN: // 403: Forbidden
                        Debug.w(this, "HTTP 403 - Forbidden");
                        Debug.d(this, detail);
                        break;

                    case HttpURLConnection.HTTP_NOT_FOUND: // 404: Not found
                        Debug.w(this, "HTTP 404 - Not found");
                        Debug.d(this, detail);
                        break;

                    default:
                        Debug.e(this, "Unexpected response code: %d", code);
                        break;
                }

                return null;
            }

            return builder.toString();

        } catch (JSONException e) {
            Debug.d(this, e);
        } catch (NetworkOnMainThreadException e) {
            Debug.e(this, e);
        } catch (IOException e) {
            Debug.e(this, e);
        }

        return null;
    }

}
