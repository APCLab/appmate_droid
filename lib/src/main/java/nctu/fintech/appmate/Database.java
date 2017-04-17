package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

import nctu.fintech.appmate.core.Core;

/**
 * 指向資料庫的連接物件，本類別適用於須存取多個 {@link Table} 時方便建立連線之用。
 *
 * <p>
 * 在一般使用情境下，{@link Database} 並非必須建立的物件，唯當要自同一個資料庫建立多組 {@link Table} 時候，
 * 可利用 {@link Database#getTable(String)} 方法減少寫死參數的使用，
 * 或利用 {@link Database#getTables()} 可進行查詢並取得所有的資料表物件。如：
 * </p>
 *
 * \code{.java}
 * Database db = new Database("www.example.com:8000", "user", "passw0rd");
 * Table[] tables = db.getTables();
 * \endcode
 *
 * <p>
 * 本架構設計上係以適應Django rest-framework為主，但並不做檢查，期望能適應多數restful api標準。
 * </p>
 *
 * \remarks
 * 建立 {@link Database} 實體(instance)時候並不會建立網路連線。
 */
public class Database {

    /*
     * Global variables
     */

    /**
     * core refer to the api root
     */
    final Core mCore;

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
     * @param apiRoot api root所在位置，須包含傳輸阜，如：`http://example:8000/api/`
     */
    public Database(@NonNull String apiRoot) {
            mCore = new Core(apiRoot);
    }

    /**
     * 建立一個帶授權的 {@link Database} 實體。
     *
     * @param apiRoot api root所在位置，須包含傳輸阜，如：`http://example:8000/api/`
     * @param username 登入所使用的使用者名稱
     * @param password 登入所使用的密碼
     */
    public Database(@NonNull String apiRoot, @NonNull String username, @NonNull String password) {
            mCore = new Core(apiRoot)
                    .useAuth(username, password);
    }

    /**
     * 以複製參數方式建立一個 {@link Database} 實體。
     *
     * @param core 另一個 {@link Database}
     */
    Database(Core core) {
        this.mCore = new Core(core);
    }

    /**@}*/

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
        return new Table(this, table);
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
        JsonObject tables = mCore.createConnection()
                .method("GET")
                .getResponseAsJson();

        // retrieve result
        String[] names = new String[tables.size()];
        int idx = 0;

        for (Map.Entry<String, JsonElement> pair : tables.entrySet()) {
            names[idx++] = pair.getKey();
        }

        return names;
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
            tables[i] = new Table(this, names[i]);
        }
        return tables;
    }

    /**@}*/

}
