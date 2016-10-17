package nctu.fintech.appmate;

import android.support.annotation.NonNull;

/**
 * Class {@link DbAdapter} represents a connection to the remote database referred by {@link DbAdapter#getHost}.
 * It handles data transfer format and authentication to the {@code appmate} api.
 *
 * <p>It should be noted that a {@link DbAdapter} instance does not establish the actual network connection on creation.</p>
 */
public class DbAdapter {

    private final ConnParam _conn;

    /**
     * Create a instance {@link DbAdapter} that represents a connection to the remote database referred by {@code host}
     *
     * @param host Assigned host domain and port number. i.e. {@code www.example.com:8000}
     * @throws MalformedResourceException host string is not valid. The most common occurrence is receiving full URI . i.e. {@code http://www.example.com/}
     */
    public DbAdapter(@NonNull String host) {
        _conn = new ConnParam(host);
    }

    /**
     * Create a instance {@link DbAdapter} that represents a connection to the remote database referred by {@code host}.
     * To compare with {@link DbAdapter#DbAdapter(String)}, this constructor return a instance with authentication message.
     *
     * @param host     Assigned host domain and port number. i.e. {@code www.example.com:8000}
     * @param username Assigned user name to login
     * @param password Assigned password to login
     * @throws MalformedResourceException host domain is not valid. The most common occurrence is receiving full URI . i.e. {@code http://www.example.com/}
     */
    public DbAdapter(@NonNull String host, @NonNull String username, @NonNull String password) {
        _conn = new ConnParam(host, username, password);
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
     * Get specific {@link TableAdapter} by assigned table name
     *
     * @param table table name
     * @return the specific {@link TableAdapter}
     */
    public TableAdapter getTableAdapter(@NonNull String table) {
        return new TableAdapter(_conn, table);
    }

}
