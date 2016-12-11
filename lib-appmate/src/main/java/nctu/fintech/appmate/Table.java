package nctu.fintech.appmate;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 資料表連接物件
 */
public class Table extends Database {

    /*
     * Global variables
     */
    /**
     * table api URL
     */
    protected final URL _tbUrl;

    /**
     * table name
     */
    private final String _table;

    /*
     * Constructors
     */

    /**
     * 建立一個資料表連線
     *
     * <p>
     *     Create a {@link Table} instance
     * </p>
     *
     * @param host  資料庫位址，包含port
     * @param table 資料表名稱
     */
    public Table(@NonNull String host, @NonNull String table) {
        super(host);

        _table = table;

        try {
            _tbUrl = new URL(_dbUrl, "/api/" + table);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal table name", e);
        }
    }

    /**
     * 建立一個資料表連線
     *
     * <p>
     *     Create a authenticated {@link Table} instance
     * </p>
     *
     * @param host     資料庫位址，包含port
     * @param username 登入用使用者名稱
     * @param password 登入用密碼
     * @param table    資料表名稱
     */
    public Table(@NonNull String host, @NonNull String username, @NonNull String password, @NonNull String table) {
        super(host, username, password);

        _table = table;

        try {
            _tbUrl = new URL(_dbUrl, "/api/" + table);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal table name", e);
        }
    }

    /**
     * Create a {@link Table} instance using parameter in {@link Database} and assigned table name
     * This constructor may be more efficient since it dose not recheck parameters
     *
     * @param db    mother {@link Database}
     * @param table table name
     */
    Table(@NonNull Database db, @NonNull String table) {
        super(db);

        _table = table;

        try {
            _tbUrl = new URL(_dbUrl, "/api/" + table);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal table name", e);
        }
    }

}
