package nctu.fintech.appmate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.code.regexp.Pattern;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * 資料庫連接物件，本型別適用於須存取多個 {@link Table} 時方便建立連線之用。
 *
 * <p>
 * Class {@link Database} represents a connection to the remote database referred by assigned host domain and handles parameters to communicate with {@code appmate} server-side api.
 *</p>
 * <p>
 * A {@link Database} instance is not required on creating a {@link Table} instance, or creating connection.
 * It helps when multiple table is used, which let develop can create multiple {@link Table} instance with less hard-coded parameter and make less mistake.
 * </p>
 * <p>
 * It should be noted that a {@link Database} instance does not establish the actual network connection.
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
    protected final URL _dbUrl;

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
     * indicate that use authentication or not
     *
     * @return use authentication or not
     */
    public boolean isAuth() {
        return _useAuth;
    }

    /**
     * get the username to login
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
     * 建立一個 {@link Database} 實體
     * <p>
     * <p>
     * Create a instance {@link Database} that represents a connection to the remote database referred by {@code host}
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
            _dbUrl = new URL(res.get("host")+"/api/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal host domain", e);
        }

        if (res.containsKey("auth")) {
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
     * 建立一個帶授權的 {@link Database} 實體
     * <p>
     * Create a instance {@link Database} that represents a connection to the remote database referred by {@code host}
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
            _dbUrl = new URL(res.get("host")+"/api/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal host domain", e);
        }

        _useAuth = true;
        _userName = username;
        _authStr = generateAuthString(username + ":" + password);
    }

    /**
     * Create a instance {@link Database} by cloning
     *
     * @param db another {@link Database}
     */
    Database(@NonNull Database db) {
        _dbUrl = db._dbUrl;
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
                        "^(?:https?:\\/\\/)?(?:(?<auth>(?<user>[\\w-]+):\\w+)@)?(?<host>[\\w-]+(?:\\.[\\w-]+)+(?::\\d+)?)",
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

    /*
     * members
     */

    /**
     * 取得資料表連結
     * <p>
     * Get specific {@link Table} by assigned table name
     * </p>
     * @param table 資料表名
     * @return 資料表連節器
     */
    public Table getTable(@NonNull String table) {
        throw new IllegalAccessError();
    }

}
