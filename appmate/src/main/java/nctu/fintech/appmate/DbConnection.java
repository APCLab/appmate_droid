package nctu.fintech.appmate;

import android.support.annotation.NonNull;

import java.lang.*;

/**
 * Handle the connect to the database.
 * <p>This class helps to handle host domain and authentication to prevent rookie mistake.</p>
 *<p>usage example:
 * {@code
 *        DbConnection connection = new DbConnection("localhost:8000", "USER", "PASSW0RD");
 *        DbAdapter tableAdapter = connection.getTable("table_name");
 * } </p>
 * @version 1.0 (2016-9-22)
 * @author Tzu-ting, NCTU Fintech Center
 */
public class DbConnection {

    private final String _host;

    private final boolean _useAuth;
    private String _username = null;
    private String _password = null;

    /**
     * Create a connection to the database which does NOT require authentication
     *
     * @param host host domain where database located
     */
    public DbConnection(@NonNull String host) {
        _host = host;
        _useAuth = false;
    }

    /**
     * Create a connection to the database which requires authentication.
     *
     * @param host     host domain where database located
     * @param username username/id to login
     * @param password password to login
     */
    public DbConnection(@NonNull String host, @NonNull String username, @NonNull String password) {
        _host = host;
        _useAuth = true;
        _username = username;
        _password = password;
    }

    /**
     * Get {@code DbAdapter} for specific table
     *
     * @param tableName table name to retrieve
     * @return a {@code DbAdapter} linked to the specific table
     */
    public DbAdapter getTable(@NonNull String tableName) {
        if (_useAuth) {
            return new DbAdapter(_host, tableName, _username, _password);
        } else {
            return new DbAdapter(_host, tableName);
        }
    }

}
