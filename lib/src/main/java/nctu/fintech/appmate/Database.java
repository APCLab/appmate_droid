package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static nctu.fintech.appmate.DbCore.getResponse;

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

    private final DbCore _core;

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
        return _core.useAuth;
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
        return _core.username;
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
        _core = new DbCore(host);
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
        _core = new DbCore(host, username, password);
    }

    /**
     * Create a instance {@link Database} by cloning.
     *
     * @param core another {@link Database}
     */
    Database(DbCore core) {
        this._core = core;
    }

    /*
     * Override Object methods
     */

    @Override
    public String toString() {
        return String.format("Database{%s}", _core.url.getHost());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Database && this._core.equals(((Database) obj)._core);
    }

    @Override
    public int hashCode() {
        return _core.hashCode();
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
        return new Table(_core, table);
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
        HttpURLConnection con = _core.openUrl();

        // downstream & parse
        JsonObject tables = new JsonParser()
                .parse(getResponse(con))
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
            tables[i] = new Table(_core, names[i]);
        }
        return tables;
    }

}