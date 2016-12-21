package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;

import com.google.code.regexp.Pattern;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Map;

/**
 * 資料表連接器。
 * <p>
 *     Class {@link Table} represent a table on an {@code appmate} server.
 * </p>
 * <p>
 *     For develops whom use the database without authentication,
 *     frequency request would trigger <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a> of the database.
 *     To resolve this issue a {@code CSRF token} is required but currently it is not provided and is not implemented in this client library.
 *     If frequently usage of non-authenticated database is required and reasonable, please contact the manager.
 * </p>
 * <p>
 *     It should be noted that most of methods in {@link Table} create a network connection but it <strong>doesn't</strong> handle the threading.
 *     Due to Android's policy, developers should avoid any network progress run on main thread.
 *     You can avoid {@link NetworkOnMainThreadException} by using {@link android.os.AsyncTask} or {@link Thread}.
 *     See <a href="https://developer.android.com/guide/components/processes-and-threads.html">Android API Guides: Processes and Threads</a>.
 * </p>
 */
public class Table {

    /*
     * Constant define
     */

    private static final String DEFAULT_CHARSET = "utf-8";

    /*
     * Global variables
     */

    /**
     * Mother database.
     */
    private final Database _db;

    /**
     * URL referrer to table api.
     */
    private final URL _baseUrl;

    /**
     * table name.
     */
    private final String _table;

    /*
     * Constructors
     */

    /**
     * 建立一個資料表連線實體。
     * <p>
     * Create a {@link Table} instance.
     * </p>
     *
     * @param host  資料庫位址，包含port
     * @param table 資料表名稱
     */
    public Table(@NonNull String host, @NonNull String table) {
        _db = new Database(host);
        _table = table;
        _baseUrl = generateTableUrl(_db._baseUrl, table);
    }

    /**
     * 建立一個資料表連線實體。
     * <p>
     * Create a authenticated {@link Table} instance.
     * </p>
     *
     * @param host     資料庫位址，包含port
     * @param username 登入用使用者名稱
     * @param password 登入用密碼
     * @param table    資料表名稱
     */
    public Table(@NonNull String host, @NonNull String username, @NonNull String password, @NonNull String table) {
        _db = new Database(host, username, password);
        _table = table;
        _baseUrl = generateTableUrl(_db._baseUrl, table);
    }

    /**
     * Create a {@link Table} instance.
     * This constructor may be more efficient since it dose not recheck parameters
     *
     * @param db    mother {@link Database}
     * @param table table name
     */
    Table(@NonNull Database db, @NonNull String table) {
        _db = db;
        _table = table;
        _baseUrl = generateTableUrl(_db._baseUrl, table);
    }

    /*
     * assisting functions
     */

    /**
     * Get table api URL.
     *
     * @param apiRoot a url referrer to api root
     * @param table   table name
     * @return a url referrer to table root
     */
    private static URL generateTableUrl(URL apiRoot, String table) {
        try {
            return new URL(apiRoot, "./" + table + "/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal table name", e);
        }
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
        String boundary = "LibAppmateBoundary" + Long.toHexString(Calendar.getInstance().getTimeInMillis());

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
            writer.writeBytes(builder.toString());
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
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String in;
            while ((in = reader.readLine()) != null) {
                builder.append(in);
            }
        }
        return builder.toString();
    }

    /*
     * Get objects
     */

    /**
     * 取得資料表上的所有物件。
     * <p>
     * Get all items on this table.
     * </p>
     *
     * @return all objects on the table, size could be 0 when table is empty
     * @throws IOException                  table not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public Tuple[] get() throws IOException {
        // open connection
        HttpURLConnection con = _db.openUrl(_baseUrl);

        // downstream & parse
        JsonArray jArray = new JsonParser()
                .parse(getResponse(con))
                .getAsJsonArray();

        // build
        int len = jArray.size();
        Tuple[] tArray = new Tuple[len];
        for (int i = 0; i < len; i++) {
            tArray[i] = new Tuple(this, jArray.get(i).getAsJsonObject());
        }

        // return
        return tArray;
    }

    /**
     * 以 {@code id} 取得資料表上的特定物件。
     * <p>
     * Get specific object on this table by id.
     * </p>
     *
     * @param id object id
     * @return the specific object
     * @throws IOException                  object not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public Tuple get(int id) throws IOException {
        URL url = new URL(_baseUrl, "./" + id + "/");
        HttpURLConnection con = _db.openUrl(url);
        JsonObject obj = new JsonParser()
                .parse(getResponse(con))
                .getAsJsonObject();
        return new Tuple(this, obj);
    }

    /**
     * 使用篩選器取得物件。
     * <p>
     * Get items by filter.
     * </p>
     * <p>
     * This method can parse natural mathematical expressions into query parameters. See the following filter writing rules:
     * <ul>
     * <li>
     * <strong>Full match</strong> (type: {@link String})<br/>
     * use {@code =} or {@code ==}<br/>
     * notice that quote is not allowed, or it will be parsed into part of matching string. i.e. <br/>
     * {@code stringField=matching string with whitespace}<br/>
     * {@code stringField == matching_string_without_whitespace}
     * </li>
     * <li>
     * <strong>Exact equals</strong> (type: {@link Integer}, {@link Boolean})<br/>
     * use {@code =} or {@code ==}<br/>
     * i.e. {@code integerField=100}, {@code boolField==0}
     * </li>
     * <li>
     * <strong>Inequality</strong> (type: {@link Integer}, {@link Float}, {@link Double})<br/>
     * support symbols: {@code >}, {@code >=}, {@code <}, {@code <=}<br/>
     * i.e. {@code floatField > 5.00}, {@code integerField<=99}
     * </li>
     * </ul>
     * </p>
     * <p>
     * Notice that all filters is giving as {@link String} type, numeric and logical types should converted to string type before assigned.
     * </p>
     * <p>
     * This method supports multiple filter querying, the result will be the intersection of given rules.
     * To set multiple filters use commas to separate each rule. i.e. {@code get("stringField=Hello", "integerField>=99");}
     * </p>
     *
     * @param filters filter rules
     * @return items match specified rules, or null on error
     * @throws IOException                  table not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public Tuple[] get(String... filters) throws IOException {
        // parse filters and build query string
        Pattern pattern = Pattern.compile("(?<field>\\w+)\\s?(?<sym>=|==|>|>=|<|<=)\\s?(?<value>.+)");
        StringBuilder builder = new StringBuilder("./?");

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
            String field = URLEncoder.encode(match.get("field"), DEFAULT_CHARSET);
            String value = URLEncoder.encode(match.get("value"), DEFAULT_CHARSET);

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

            // append
            builder.append(field);
            builder.append("=");
            builder.append(value);
        }

        // open connection
        URL url = new URL(_baseUrl, builder.toString());
        HttpURLConnection con = _db.openUrl(url);

        // downstream & parse
        JsonArray jArray = new JsonParser()
                .parse(getResponse(con))
                .getAsJsonArray();

        // build
        int len = jArray.size();
        Tuple[] tArray = new Tuple[len];
        for (int i = 0; i < len; i++) {
            tArray[i] = new Tuple(this, jArray.get(i).getAsJsonObject());
        }

        // return
        return tArray;
    }

    /*
     * Add methods
     */

    /**
     * 新增物件到資料表中。
     * <p>
     * Add an item into the table.
     * </p>
     *
     * @param items items to be added
     * @throws IOException                  table not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public void add(Tuple... items) throws IOException {
        for (Tuple tuple : items) {
            // open connection
            HttpURLConnection con = _db.openUrl(_baseUrl);
            con.setRequestMethod("POST");

            // upstream
            sendRequest(con, tuple);

            // downstream
            JsonObject obj = new JsonParser()
                    .parse(getResponse(con))
                    .getAsJsonObject();

            tuple.reset(this, obj);
        }
    }

    /*
     * Update methods
     */

    /**
     * 更新一個資料表上的項目。
     * <p>
     * Update an item on the table.
     * </p>
     * <p>
     * This method is the full version of update function. For convenient usage, use other alias instead.
     * </p>
     *
     * @param id        item id to be updated.
     * @param item      data to be updated.
     * @param overwrite overwrite or not.
     *                  If {@code TRUE}, all fields will be cleared and re-upload.
     *                  If {@code FALSE}, only fields which is in {@code item} will be updated.
     *                  NOTICE that all non-nullable field is required if {@code overwrite} is {@code TRUE}, default is {@code false}
     * @throws IOException                  table/item not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public void update(int id, Tuple item, boolean overwrite) throws IOException {
        // open connection
        URL url = new URL(_baseUrl, "./" + id + "/");
        HttpURLConnection con = _db.openUrl(url);
        con.setRequestMethod(overwrite ? "PUT" : "PATCH");

        // upstream
        sendRequest(con, item);

        // downstream
        JsonObject obj = new JsonParser()
                .parse(getResponse(con))
                .getAsJsonObject();

        item.reset(this, obj);
    }

    /**
     * 更新一個資料表上的項目。
     * <p>
     * 本方法為 {@link Table#update(int, Tuple, boolean)} 之短版API，其使用參數為更新第 {@link Tuple#getId()} 個項目。
     * </p>
     * <p>
     * Update an item on the table.
     * This method will update the {@link Tuple#getId()}-th item on the table.
     * </p>
     *
     * @param item item to be updated
     * @throws UnsupportedOperationException if item id not specified
     * @throws IOException                   table/item not exist or network error
     * @throws NetworkOnMainThreadException  if this method is called on main thread
     * @see Table#update(int, Tuple, boolean)
     */
    public void update(Tuple item) throws IOException {
        if (item.getId() == -1) {
            throw new UnsupportedOperationException("item id not specified");
        }
        update(item.getId(), item, false);
    }

    /*
     * Delete methods
     */

    /**
     * 刪除一些項目。
     * <p>
     * Delete an item on the table.
     * </p>
     *
     * @param ids id of items to be deleted
     * @return operation success or not
     * @throws IOException                  table/item not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public boolean delete(int... ids) throws IOException {
        boolean isSuccess = true;

        for (int id : ids) {
            // open connection
            URL url = new URL(_baseUrl, "./" + id + "/");
            HttpURLConnection con = _db.openUrl(url);
            con.setRequestMethod("DELETE");

            // check result
            isSuccess &= con.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT;
        }

        return isSuccess;
    }

}
