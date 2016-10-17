package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Connection scheme handler to specific {@code appmate} table
 */
public class TableAdapter {

    /*
     * Private class
     */
    private class UnexpectedResponseException extends Exception {

        final int code;

        UnexpectedResponseException(int code) {
            this.code = code;
        }

    }

    /*
     * Constants, Macros
     */
    private class ContentType {
        static final String Form = "application/x-www-form-urlencoded";
        static final String Json = "application/json";
        static final String None = "n/a";
    }

    private class HttpRequestMethod {
        static final String Get = "GET";
        static final String Post = "POST";
        static final String Put = "PUT";
        static final String Delete = "DELETE";
    }

    /*
     * Private fields
     */
    private final ConnParam _conn;

    /*
     * Constructor
     */

    /**
     * Build a connection handler to specific table
     *
     * @param baseDb database connection parameter
     * @param table  table name
     */
    TableAdapter(ConnParam baseDb, String table) {
        _conn = new ConnParam(baseDb, table);
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
    public JSONObject[] getAll() {
        throw new UnsupportedOperationException();
    }

    public JSONObject[] getFiltered(String... filters) {
        throw new UnsupportedOperationException();
    }

    /*
     * private:: proceed request
     */
    private String request(String method, @NonNull String spec, int exceptedResponse) {
        return request(method, spec, ContentType.None, null, exceptedResponse);
    }


    //TODO 改用 multi-part 上傳
    //TODO 加上Accept: json 標頭
    @Nullable
    private String request(String method, @NonNull String spec, String contentType, String upstreamData, int exceptedResponse) {
        // log
        Debug.i(this, "Proceed [%s] %s", method, spec);

        try {
            // open connection
            HttpURLConnection con = _conn.openHttpConnection(spec);
            con.setRequestMethod(method);

            // upload data
            if (upstreamData != null && !contentType.equals(ContentType.None)) {
                con.setRequestProperty("content-type", contentType);
                con.setDoOutput(true);

                try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                    writer.writeBytes(upstreamData);
                }
            }

            // check response code
            int code = con.getResponseCode();
            if (code != exceptedResponse) {
                throw new UnexpectedResponseException(code);
            }

            // retrieve response content
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String in;
                while ((in = reader.readLine()) != null) {
                    builder.append(in);
                }
            }
            return builder.toString();

        } catch (UnexpectedResponseException e) {
            switch (e.code) {
                case HttpURLConnection.HTTP_UNAUTHORIZED: // 401: Unauthorized
                    Debug.w(this, "%s@%s Authentication failed", _conn.user, _conn.host);
                    break;
                case HttpURLConnection.HTTP_FORBIDDEN: // 403: Forbidden
                    Debug.w(this, "%s@%s Request forbidden", _conn.user, _conn.host);
                case HttpURLConnection.HTTP_NOT_FOUND: // 404: Not found
                    Debug.v(this, "Data not found");
                default:
                    Debug.e(this, "Unexpected response code: %d", e.code);
                    break;
            }
        } catch (NetworkOnMainThreadException e) {
            Debug.wtf(this, e);
        } catch (IOException e) {
            Debug.e(this, e);
        }
        return null;
    }

}
