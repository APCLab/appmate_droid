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
 * 指向資料庫的連接物件，本類別適用於須存取多個 {@link Table} 時方便建立連線之用。
 *
 * \note
 * 此物件指向 `http://<host>/api/`
 *
 * 在一般使用情境下，{@link Database} 並非必須建立的物件，唯當要自同一個資料庫建立多組 {@link Table} 時候，
 * 可利用 {@link Database#getTable(String)} 方法減少寫死參數的使用，
 * 或利用 {@link Database#getTables()} 可進行查詢並取得所有的資料表物件。如：
 *
 * \code{.java}
 * Database db = new Database("www.example.com:8000", "user", "passw0rd");
 * Table[] tables = db.getTables();
 * \endcode
 *
 * \remarks
 * 建立 {@link Database} 實體(instance)時候並不會建立網路連線。
 */
public class Database {

    /*
     * Global variables
     */

    private final DbCore _core;

    /*
     * read-only fields returning
     */

    /** @name 屬性查詢
     * @{*/

    /**
     * 取得是否使用授權。
     * <p>
     * 本函式回傳此物件內登記為使用授權（需要登入）與否，
     * 此屬性來自物件建構時依輸入參數所做之判斷，並不代表資料庫端認定需要登入與否。
     *
     * @return 使用授權與否
     */
    public boolean isAuth() {
        return _core.useAuth;
    }

    /**
     * 取得使用者名稱。
     * <p>
     * 本函式回傳此物件內登記的使用者名稱，
     * 此屬性來自物件建構時依輸入參數所做之判斷，並不代表資料庫端認定該使用者存在與否。
     *
     * @return 使用者名稱，當此資料庫為不使用授權時，回傳 `null`
     */
    public String getUserName() {
        return _core.username;
    }

    /**@}*/

    /*
     * constructors
     */

    /** @name 建構子
     * @{*/

    /**
     * 建立一個不帶授權的 {@link Database} 實體。
     * <p>
     * \attention
     * 基於安全性因素，單一裝置上高頻率地進行操作會觸發
     * <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a>
     * 保護機制。
     * 對於需要頻繁進行操作的資料表，請考慮使用 {@link Database#Database(String, String, String)}。
     *
     * @param host 指派的主機位置，若有，請附上連接阜。如：`www.example.com:8000`
     */
    public Database(@NonNull String host) {
        _core = new DbCore(host);
    }

    /**
     * 建立一個帶授權的 {@link Database} 實體。
     *
     * @param host     指派的主機位置，若有，請附上連接阜。如：`www.example.com:8000`
     * @param username 登入所使用的使用者名稱
     * @param password 登入所使用的密碼
     */
    public Database(@NonNull String host, @NonNull String username, @NonNull String password) {
        _core = new DbCore(host, username, password);
    }

    /**
     * 以複製參數方式建立一個 {@link Database} 實體。
     *
     * @param core 另一個 {@link Database}
     */
    Database(DbCore core) {
        this._core = core;
    }

    /**@}*/

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

    /**@name 操作
     * @{*/

    /**
     * 取得資料表連結器。
     * <p>
     * 此函式不會使用網路連線，僅以參數疊加方式建立連結接口。
     *
     * @param table 資料表名
     * @return 資料表連節器
     */
    public Table getTable(@NonNull String table) {
        return new Table(_core, table);
    }

    /**
     * 取得資料庫中的所有資料表的名稱。
     * <p>
     * \remarks
     * 此函式會使用網路連線。
     *
     * @return 此資料庫下所有的資料表的名稱
     * @throws IOException                  資料庫不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
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
     * \remarks
     * 此函式會使用網路連線。
     *
     * @return 此資料庫下所有的資料表的 {@link Table}
     * @throws IOException                  資料庫不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public Table[] getTables() throws IOException {
        String[] names = getTableNames();
        Table[] tables = new Table[names.length];
        for (int i = 0; i < names.length; i++) {
            tables[i] = new Table(_core, names[i]);
        }
        return tables;
    }

    /**@}*/

}
