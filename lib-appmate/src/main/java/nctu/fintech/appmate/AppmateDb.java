package nctu.fintech.appmate;

import android.support.annotation.NonNull;

/**
 * Class {@link AppmateDb} is a outer wrapper of class {@link AppmateTable}.
 * It  represents a connection to the remote database referred by assigned host domain and handles parameters to communicate with {@code appmate} server-side api.
 *
 * <p>
 *     A {@link AppmateDb} instance is not required on creating a {@link AppmateTable} instance, or creating connection.
 *     It helps when multiple table is used, which let develop can create multiple {@link AppmateTable} instance with less hard-coded parameter and make less mistake.
 * </p>
 * <p>
 *     It should be noted that a {@link AppmateDb} instance does not establish the actual network connection.
 * </p>
 */
public class AppmateDb {

    final ConnectionParameterSet _conn;

    /**
     * Create a instance {@link AppmateDb} that represents a connection to the remote database referred by {@code host}
     * <p>
     * <p>
     * For develop whom use this constructor, it is known that frequency request without authentication
     * would trigger <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a> of the database.
     * Please consider use authenticated method instead.
     * </p>
     *
     * @param host Assigned host domain and port number. i.e. {@code www.example.com:8000}
     * @throws MalformedResourceException host string is not valid. The most common occurrence is receiving full URI . i.e. {@code http://www.example.com/}
     */
    public AppmateDb(@NonNull String host) {
        _conn = new ConnectionParameterSet(host);
    }

    /**
     * Create a instance {@link AppmateDb} that represents a connection to the remote database referred by {@code host}.
     * To compare with {@link AppmateDb#AppmateDb(String)}, this constructor return a instance with authentication message.
     *
     * @param host     Assigned host domain and port number. i.e. {@code www.example.com:8000}
     * @param username Assigned user name to login
     * @param password Assigned password to login
     * @throws MalformedResourceException host domain is not valid. The most common occurrence is receiving full URI . i.e. {@code http://www.example.com/}
     */
    public AppmateDb(@NonNull String host, @NonNull String username, @NonNull String password) {
        _conn = new ConnectionParameterSet(host, username, password);
    }

    /**
     * Get host domain
     *
     * @return host domain name
     */
    public String getHost() {
        return _conn.host;
    }

    /**
     * Get specific {@link AppmateTable} by assigned table name
     *
     * @param table table name
     * @return the specific {@link AppmateTable}
     */
    public AppmateTable getTableAdapter(@NonNull String table) {
        return new AppmateTable(this, table);
    }

}
