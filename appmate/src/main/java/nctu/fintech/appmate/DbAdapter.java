package nctu.fintech.appmate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL connect, client side
 *
 * <p>
 * Implemented features:
 * <ol>
 *     <li>Get: get all</li>
 *     <li>Get: get specific item by index</li>
 *     <li>Get: get specific items by filter</li>
 *     <li>Add: add new item</li>
 *     <li>Update: update specific item by index</li>
 *     <li>Delete: delete specific item by index</li>
 * </ol>
 * </p>
 *
 * @version 0.9.5 (2016-10-31)
 * @author  Tzu-ting, NCTU Fintech Center
 */
public class DbAdapter {

    //*******************************************************
    //  Constants
    //
    //*******************************************************
    private class ContentType {
        public static final String Form = "application/x-www-form-urlencoded";
        public static final String Json = "application/json";
    }

    @StringDef({
            ContentType.Form,
            ContentType.Json,
            "N/A",
    })
    private @interface ContentTypeStr {
    }

    @StringDef({
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE"
    })
    private @interface HttpMethodStr {
    }

    private static final String ENCODE = "utf-8";

    //*******************************************************
    //  Private fields
    //
    //*******************************************************
    private final String _base_url;
    private final String _auth;


    //*******************************************************
    //  Constructor
    //
    //*******************************************************

    /**
     * Create a connection to database
     *
     * @param host  host name
     * @param table table name
     */
    public DbAdapter(@NonNull String host, @NonNull String table) {
        _base_url = String.format("http://%s/api/%s/", host, table);
        _auth = null;
    }

    /**
     * Create a connection to database, with authentic
     *
     * @param host     host name
     * @param username user name
     * @param password user password
     * @param table    table name
     */
    public DbAdapter(@NonNull String host, @NonNull String table, @NonNull String username, @NonNull String password) {
        _base_url = String.format("http://%s/api/%s/", host, table);
        _auth = generateAuthString(username, password);
    }

    //*******************************************************
    //  Public members
    //
    //*******************************************************

    /**
     * Get all items
     *
     * @return all items, or null when connection is failed
     */
    public JSONArray get() {
        try {
            String response = sendRequest("GET", "?format=json", HttpURLConnection.HTTP_OK, true);
            return new JSONArray(response);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Get specific item by index
     *
     * @param index item id
     * @return specified item, or null when connection is failed
     */
    public JSONObject get(int index) {
        try {
            String response = sendRequest("GET", index + "?format=json", HttpURLConnection.HTTP_OK, true);
            return new JSONObject(response);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Get specific items by filter
     *
     * @param filters filter to select items
     * @return specified items, or null when connection is failed
     */
    public JSONArray get(String... filters) {
        try {
            // parse filters
            HashMap<String, String> params = new HashMap<>();
            for (String this_filter : filters) {
                HashMap<String, String> this_params = parseFilter(this_filter);
                if (this_filter == null) {
                    continue;
                }
                params.putAll(this_params);
            }

            // query
            String encodedParam = encodeParam(params);
            String response = sendRequest("GET", "?" + encodedParam + "&format=json", HttpURLConnection.HTTP_OK, true);
            return new JSONArray(response);

        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Add a item
     *
     * @param item object to upload
     * @return add success or not
     */
    public boolean add(Map<String, String> item) {
        try {
            String encodedData = encodeParam(item);
            sendRequest("POST", "?format=json", ContentType.Form, encodedData, HttpURLConnection.HTTP_CREATED, false);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Add a item
     *
     * @param item object to upload
     * @return add success or not
     */
    public boolean add(JSONObject item) {
        try {
            sendRequest("POST", "?format=json", ContentType.Json, item.toString(), HttpURLConnection.HTTP_CREATED, false);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Update a item
     *
     * @param index   index of item which is expected to be updated
     * @param changes filed and value pairs to be updated in item
     * @return update success or not
     */
    public boolean update(int index, Map<String, String> changes) {
        try {
            String encodedData = encodeParam(changes);
            sendRequest("PATCH", index + "/?format=json", ContentType.Form, encodedData, HttpURLConnection.HTTP_OK, false);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Update a item
     *
     * @param index   index of item to be updated
     * @param changes filed and value pairs to be updated in item
     * @return update success or not
     */
    public boolean update(int index, JSONObject changes) {
        try {
            sendRequest("PATCH", index + "/?format=json", ContentType.Json, changes.toString(), HttpURLConnection.HTTP_OK, false);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Delete a item
     *
     * @param index index of item to be deleted
     * @return delete success or not
     */
    public boolean delete(int index) {
        try {
            sendRequest("DELETE", index + "/?format=json", HttpURLConnection.HTTP_NO_CONTENT, false);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    /**
     * get object by URL
     * <p>
     * <strong>NOTICE</strong>
     * This method currently run WITHOUT any anti-foolish procedure. Please DO NOT use this
     *
     * @param url request url
     * @return retrieved object, or null when connection failed
     */
    public JSONObject getByUrl(String url) {
        try {
            String response = sendRequest("GET", url, HttpURLConnection.HTTP_OK, true);
            return new JSONObject(response);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return null;
        }
    }

    //*******************************************************
    //  Internal members
    //
    //*******************************************************

    /**
     * Generate authentication string
     *
     * @param username user name to login
     * @param password password to login
     * @return HTTP header authentication sting
     */
    static String generateAuthString(@NonNull String username, @NonNull String password) {
        String auth_string = username + ":" + password;
        String auth_encoded = Base64.encodeToString(auth_string.getBytes(), Base64.DEFAULT);
        return "Basic " + auth_encoded;
    }

    //*******************************************************
    //  Private members
    //
    //*******************************************************

    /**
     * parse filter strings
     *
     * @param filter filter string to be parsed
     * @return field-value pairs if success, or null
     * @throws UnsupportedEncodingException
     */
    @Nullable
    private HashMap<String, String> parseFilter(String filter) throws UnsupportedEncodingException {
        String[] pairs = filter.split("&");
        if (pairs.length == 0) {
            return null;
        }

        HashMap<String, String> params = new HashMap<>();
        for (String pair : pairs) {
            String[] dat = pair.split("=");
            if (dat.length != 2) {
                continue;
            }
            params.put(dat[0], dat[1]);
        }

        return params;
    }

    /**
     * encode parameter set
     *
     * @param param_set parameter set
     * @return encoded string
     */
    @NonNull
    private String encodeParam(Map<String, String> param_set) throws UnsupportedEncodingException {
        StringBuilder params = new StringBuilder();
        Boolean isFirst = true;
        for (Map.Entry<String, String> pair : param_set.entrySet()) {
            if (!isFirst) {
                params.append("&");
            }

            params.append(URLEncoder.encode(pair.getKey(), ENCODE));
            params.append("=");
            params.append(URLEncoder.encode(pair.getValue(), ENCODE));

            isFirst = false;
        }
        return params.toString();
    }

    /**
     * (kernel function) proceed a connection WITHOUT output stream, and handle download stream
     *
     * @param method            request method
     * @param path              resource path
     * @param expected_response expected HTTP response code
     * @param shouldGetResponse to retrieve response content or not
     * @return response content if requested, or null
     * @throws IOException
     */
    @Nullable
    private String sendRequest(@HttpMethodStr String method, @NonNull String path, int expected_response, boolean shouldGetResponse) throws IOException {
        return sendRequest(method, path, "N/A", null, expected_response, shouldGetResponse);
    }

    /**
     * (kernel function) proceed the connection WITH output stream, and handle download stream
     *
     * @param method            request method
     * @param path              resource path
     * @param output_data       data to upload if exist, or null
     * @param expected_response expected HTTP response code
     * @param shouldGetResponse to retrieve response content or not
     * @return response content if requested, or null
     * @throws IOException
     */
    @Nullable
    private String sendRequest(@HttpMethodStr String method, @NonNull String path, @ContentTypeStr String content_type, String output_data, int expected_response, boolean shouldGetResponse) throws IOException {
        // log
        Log.v(this.getClass().getName(), String.format("Proceed %s %s", method, path));

        // open connection
        URL url = new URL(_base_url + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);

        // authorize
        if (_auth != null) {
            con.setRequestProperty("Authorization", _auth);
        }

        // upload data
        if (output_data != null) {
            con.setRequestProperty("content-type", content_type);
            con.setDoOutput(true);

            try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                writer.writeBytes(output_data);
            }
        }

        // check response code
        int code = con.getResponseCode();
        if (code != expected_response) {
            throw new IllegalArgumentException("Unexpected response code: " + code);
        }

        // retrieve response content
        String response = null;
        if (shouldGetResponse) {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String in;
                while ((in = reader.readLine()) != null) {
                    builder.append(in);
                }
            }
            response = builder.toString();
        }

        return response;
    }

}
