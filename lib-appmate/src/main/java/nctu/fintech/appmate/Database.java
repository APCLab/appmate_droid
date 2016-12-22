package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.code.regexp.Pattern;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 資料庫連接物件，本型別適用於須存取多個 {@link Table} 時方便建立連線之用。
 *
 * <p>
 *     Class {@link Database} represents an {@code appmate} database.
 *</p>
 * <p>
 *     A {@link Database} instance is not required on creating a {@link Table} instance, or creating connection.
 *     It helps when multiple table is used, which let develop can create multiple {@link Table} instance with less hard-coded parameter and make less mistake.
 * </p>
 * <p>
 *     It should be noted that a {@link Database} instance does not establish the actual network connection.
 * </p>
 */
public class Database {

    /*
     * Global variables
     */

    //--- basic connection parameters

    /**
     * database root api URL
     */
    final URL _baseUrl;

    //--- authentication information

    /**
     * use authentication or not
     */
    private final boolean _useAuth;

    /**
     * username to login (null if {@link Database#_useAuth} is {@code false})
     */
    private final String _userName;

    /**
     * authentication string, using for HTTP header (null if {@link Database#_useAuth} is {@code false})
     */
    private final String _authStr;

    /*
     * read-only fields returning
     */

    /**
     * 取得此資料庫使用授權與否。
     * <p>
     * Return that it use authentication or not.
     * </p>
     *
     * @return use authentication or not
     */
    public boolean isAuth() {
        return _useAuth;
    }

    /**
     * 取得使用者名稱。
     * <p>
     * Return the username.
     * </p>
     *
     * @return username
     */
    public String getUserName() {
        return _userName;
    }

    /*
     * constructors
     */

    /**
     * 建立一個 {@link Database} 實體。
     * <p>
     * Create a {@link Database} instance which represents a connection to the remote database referred by {@code host}
     * </p>
     * <p>
     * For develop whom use this constructor, it is known that frequency request without authentication
     * would trigger <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a> of the database.
     * Please consider use authenticated method instead.
     * </p>
     *
     * @param host Assigned host domain and port number. i.e. {@code www.example.com:8000}
     */
    public Database(@NonNull String host) {
        Map<String, String> res = resolveHost(host);
        if (res == null) {
            throw new IllegalArgumentException("host domain not found");
        }

        try {
            _baseUrl = generateApiRootUrl(res.get("host"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal host domain", e);
        }

        if (res.get("auth") != null) {
            _useAuth = true;
            _userName = res.get("user");
            _authStr = generateAuthString(res.get("auth"));
        } else {
            _useAuth = false;
            _userName = null;
            _authStr = null;
        }
    }

    /**
     * 建立一個帶授權的 {@link Database} 實體。
     * <p>
     * Create a {@link Database} instance with authentication information
     * </p>
     *
     * @param host     Assigned host domain and port number. i.e. {@code www.example.com:8000}
     * @param username Assigned username to login
     * @param password Assigned password to login
     */
    public Database(@NonNull String host, @NonNull String username, @NonNull String password) {
        Map<String, String> res = resolveHost(host);
        if (res == null) {
            throw new IllegalArgumentException("host domain not found");
        }

        try {
            _baseUrl = generateApiRootUrl(res.get("host"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal host domain", e);
        }

        _useAuth = true;
        _userName = username;
        _authStr = generateAuthString(username + ":" + password);
    }

    /**
     * Create a instance {@link Database} by cloning.
     *
     * @param db another {@link Database}
     */
    Database(@NonNull Database db) {
        _baseUrl = db._baseUrl;
        _useAuth = db._useAuth;
        _userName = db._userName;
        _authStr = db._authStr;
    }

    /*
     * assisting member for constructors
     */

    /**
     * Analyze {@code host} string. Check format and retrieve {@code authentication} and {@code host domain} info.
     * Implemented simple anti-foolish procedure.
     *
     * @param host received host string
     * @return data retrieve form {@code host}, must contains host domain string tags {@code host}, could  contains authentication info tags
     */
    @Nullable
    private static Map<String, String> resolveHost(String host) {
        Map<String, String> val = Pattern.compile
                (
                        "^(?:https?:\\/\\/)?(?:(?<auth>(?<user>[\\w-]+):\\w+)@)?(?<host>[\\w\\d-]+(?:\\.[\\w\\d-]+)+(?::\\d+)?)",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(host)
                .namedGroups();
        if (!val.containsKey("host")) {
            return null;
        }
        return val;
    }

    /**
     * Get URL of api root to the specific host domain.
     *
     * @param host host sting
     * @return a {@link URL} instance
     */
    private static URL generateApiRootUrl(String host) throws MalformedURLException {
        return new URL("http://" + host + "/api/");
    }

    /**
     * Generate the string used in HTTP header {@code Authorization}.
     * THIS METHOD SHOULD ONLY USE BY CONSTRUCTOR
     *
     * @param auth_string user name and password pair, set in {@code user:password} format
     * @return HTTP basic authentication string
     */
    private static String generateAuthString(String auth_string) {
        String auth_encoded = Base64.encodeToString(auth_string.getBytes(), Base64.DEFAULT);
        return "Basic " + auth_encoded;
    }

    /*
     * members
     */

    /**
     * 取得資料表連結器。
     * <p>
     * Get specific {@link Table} by assigned table name
     * </p>
     *
     * @param table 資料表名
     * @return 資料表連節器
     */
    public Table getTable(@NonNull String table) {
        return new Table(this, table);
    }

    /**
     * 取得資料庫中的所有表單名稱。
     * <p>
     * Get all table names in this database.
     * </p>
     *
     * @return all table names
     * @throws IOException                  table not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public String[] getTableNames() throws IOException {
        // open connection
        HttpURLConnection con = openUrl(_baseUrl);

        // downstream & parse
        JsonObject tables = new JsonParser()
                .parse(Table.getResponse(con))
                .getAsJsonObject();

        // retrieve result
        List<String> names = new LinkedList<>();
        for (Map.Entry<String, JsonElement> pair : tables.entrySet()) {
            names.add(pair.getKey());
        }

        // return
        return names.toArray(new String[tables.size()]);
    }

    /**
     * 取得資料庫中的所有資料表。
     * <p>
     * Get all tables in this database.
     * </p>
     *
     * @return all tables
     * @throws IOException                  table not exist or network error
     * @throws NetworkOnMainThreadException if this method is called on main thread
     */
    public Table[] getTables() throws IOException {
        String[] names = getTableNames();
        Table[] tables = new Table[names.length];
        for (int i = 0; i < names.length; i++) {
            tables[i] = new Table(this, names[i]);
        }
        return tables;
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
        if (_useAuth) {
            con.setRequestProperty("Authorization", _authStr);
        }

        return con;
    }

    /**
     * 覆寫{@link Object#equals(Object)}方法。
     * <p>
     * Override the equals method
     * </p>
     *
     * @param obj other object
     * @return is equals or not
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Database)) {
            return false;
        }

        Database other = (Database) obj;
        return other._baseUrl.equals(this._baseUrl)
                && other._useAuth == this._useAuth
                && other._authStr.equals(this._authStr);
    }
}
