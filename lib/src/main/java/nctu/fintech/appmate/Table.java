package nctu.fintech.appmate;

import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import nctu.fintech.appmate.core.Core;

/**
 * 指向資料表的連接物件。
 *
 * \note
 * 此物件為指向 `http://<host>/api/<table name>/`
 *
 * \remarks
 * 建立 {@link Table} 實體(instance)時候並不會建立網路連線。
 */
public class Table {

//    /*
//     * Constant define
//     */
//
//    private static final String DEFAULT_CHARSET = "utf-8";

    /*
     * Global variables
     */
    final Core mCore;
    public final String name;
    public final Database parent;

    /*
     * Constructors
     */

    /** @name 建構子
     * @{*/

    /**
     * 建立一個不帶授權的 {@link Table} 實體。
     * <p>
     * \attention
     * 基於安全性因素，單一裝置上高頻率地進行操作會觸發
     * <a href="https://docs.djangoproject.com/es/1.10/ref/csrf/">CSRF protection</a>
     * 保護機制。
     * 對於需要頻繁進行操作的資料表，請考慮使用 {@link Table#Table(String, String, String, String)}。
     *
     * @param host  指派的主機位置，若有，請附上連接阜。如：`www.example.com:8000`
     * @param table 資料表名稱
     */
    public Table(@NonNull String api_root, @NonNull String table) {
        this.parent = new Database(new Core(api_root));
        this.name = trimString(table);
        this.mCore = parent
                .mCore
                .cd(this.name);
    }

    /**
     * 建立一個帶授權的 {@link Table} 實體。
     *
     * @param host     指派的主機位置，若有，請附上連接阜。如：`www.example.com:8000`
     * @param username 登入用使用者名稱
     * @param password 登入用密碼
     * @param table    資料表名稱
     */
    public Table(@NonNull String api_root, @NonNull String table, @NonNull String username, @NonNull String password) {
        this.parent = new Database(
                new Core(api_root)
                        .useAuth(username, password)
        );

        this.name = trimString(table);
        this.mCore = parent
                .mCore
                .cd(this.name);
    }

    /**
     * 自母資料庫建立表格連結。
     *
     * @param database 母資料庫
     * @param table 表格名稱
     */
    public Table(@NonNull Database database, @NonNull String table) {
        this.parent = database;
        this.name = trimString(table);
        this.mCore = parent
                .mCore
                .cd(this.name);
    }

    /**
     * (assist func for constructor) trim input string.
     * @param string string
     * @return trimmed string
     */
    @NonNull
    private static String trimString(@NonNull String string) {
        return string
                .replace("/", "")
                .replace("\\", "")
                .trim();
    }

    /**@}*/

    /*
     * Override Object methods
     */

//    @Override
//    public String toString() {
//        return String.format("Table{%s@%s}", _core.tableName, _core.url.getHost());
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof Table && _core.equals(((Table) obj)._core);
//    }
//
//    @Override
//    public int hashCode() {
//        return _core.hashCode();
//    }

    /*
     * 其他功能
     */

    /**@{*/

    /**
     * 取得資料表結構敘述。
     *
     * 如：
     * \code{.json}
     * {
     *  "id": {
     *      "type": "integer",
     *      "required": false,
     *      "read_only": true,
     *      "label": "ID"
     *  },
     *  "name": {
     *      "type": "string",
     *      "required": true,
     *      "read_only": false,
     *      "label": "Name",
     *      "max_length": 42
     *  },
     *  "msg": {
     *      "type": "string",
     *      "required": false,
     *      "read_only": false,
     *      "label": "Msg"
     *  },
     *  "timestamp": {
     *      "type": "datetime",
     *      "required": false,
     *      "read_only": true,
     *      "label": "Timestamp"
     *  }
     * }
     * \endcode
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @return 資料表結構，如上述範例
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public Tuple getSchema() throws IOException {
        JsonObject schema = mCore.createConnection()
                .method("OPTION")
                .getResponseAsJson()
                .get("actions")
                .getAsJsonObject()
                .get("POST")
                .getAsJsonObject();
        return new Tuple(this, schema);
    }

    /**@}*/

    /*
     * Get objects
     */

    /**@{*/

    /**
     * 取得資料表上的所有內容。
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @return 資料表上的所有內容，當資料表為空的時候會回傳一個長度為0的陣列
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public Tuple[] get() throws IOException {
        // downstream & parse
        JsonArray jArray = new JsonParser()
                .parse(mCore.createConnection().getResponse())
                .getAsJsonArray();

        // build
        Tuple[] tArray = new Tuple[jArray.size()];
        int idx = 0;
        for (JsonElement e : jArray) {
            tArray[idx++] = new Tuple(this, e.getAsJsonObject());
        }

        // return
        return tArray;
    }

    /**
     * 以 `id` 取得資料表上的特定物件。
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param id 物件 `id`
     * @return 指定的物件
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public Tuple get(int id) throws IOException {
        return new Tuple(this, mCore
                .cd(String.valueOf(id))
                .createConnection()
                .getResponseAsJson()
        );
    }

//    /**
//     * 取得符合特定條件的物件。
//     *
//     * 可指定一個或多個過濾條件，用以篩選出指定的物件。
//     * 所有過濾條件皆需以字串形式給予，如：
//     *
//     * \code{.java}
//     * Tuple[] result1 = table.get("qty>50"); // 查詢 qty 大於 50 的清單
//     * Tuple[] result2 = table.get("pri<=2.8", "qty>50"); // 查詢 pri 小於等於 2.8 且 qty 大於 50 的清單
//     * \endcode
//     *
//     * \note
//     * 使用多個條件時回傳清單為所有條件之交集（`AND`運算），目前不支援聯集運算(`OR`運算)
//     *
//     * 現階段支援以下數種過濾條件
//     *
//     * - \b 完全比對 （適用型別： {@link String}）
//     *
//     *  使用符號： `=`, `==`
//     *
//     *  \attention
//     *  比對的字串中不允許出現引號，但允許空白
//     *
//     * - \b 完全等於 （適用型別：{@link Integer}, {@link Boolean}）
//     *
//     *  使用符號： `=`, `==`
//     *
//     *  \note
//     *  布林型別使用 `0`, `1` 而非 `True`, `False`
//     *
//     * - \b 不等式 （適用型別：{@link Integer}, {@link Float}, {@link Double}）
//     *
//     *  使用符號： `>`, `>=`, `<`, `<=`
//     *
//     * \remarks
//     * 此函式會使用網路連線。
//     *
//     * @param filters 篩選規則
//     * @return 符合所有篩選條件的物件清單，當無物件符合時回傳長度為0的陣列
//     * @throws IOException                  資料表不存在，或網路錯誤
//     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
//     */
//    public Tuple[] get(String... filters) throws IOException {
//        // parse filters and build query string
//        Pattern pattern = Pattern.compile("(?<field>\\w+)\\s?(?<sym>=|==|>|>=|<|<=)\\s?(?<value>.+)");
//        StringBuilder builder = new StringBuilder("./?");
//
//        for (String f : filters) {
//            // parse filter
//            Map<String, String> match = pattern
//                    .matcher(f)
//                    .namedGroups();
//
//            // break on filter not match
//            if (match.isEmpty()) {
//                continue;
//            }
//
//            // retrieve each part
//            String field = URLEncoder.encode(match.get("field"), DEFAULT_CHARSET);
//            String value = URLEncoder.encode(match.get("value"), DEFAULT_CHARSET);
//
//            // select query parameter format by symbol
//            switch (match.get("sym")) {
//                case "=":
//                case "==":
//                    // do nothing
//                    break;
//                case ">":
//                    field += "__gt";
//                    break;
//                case ">=":
//                    field += "__gte";
//                    break;
//                case "<":
//                    field += "__lt";
//                    break;
//                case "<=":
//                    field += "__lte";
//                    break;
//            }
//
//            // build query string
//            if (builder.length() > 1) {
//                builder.append("&");
//            }
//
//            // append
//            builder.append(field);
//            builder.append("=");
//            builder.append(value);
//        }
//
//        // open connection
//        HttpURLConnection con = _core.openUrl(builder.toString());
//
//        // downstream & parse
//        JsonArray jArray = new JsonParser()
//                .parse(getResponse(con))
//                .getAsJsonArray();
//
//        // build
//        int len = jArray.size();
//        Tuple[] tArray = new Tuple[len];
//        for (int i = 0; i < len; i++) {
//            tArray[i] = new Tuple(_core, jArray.get(i).getAsJsonObject());
//        }
//
//        // return
//        return tArray;
//    }

    /**@}*/

    /*
     * Add methods
     */

    /**@{*/

    /**
     * 新增物件到資料表中。
     *
     * \remarks
     * 此方法允許一次上傳多個 {@link Tuple}。
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param items 要加入資料表的物件清單
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public void add(Tuple... items) throws IOException {
        for (Tuple item : items) {
            item.reset(this, mCore
                    .createConnection()
                    .method("POST", item.toRequestBody())
                    .getResponseAsJson()
            );
        }
    }

    /**@}*/

    /*
     * Update methods
     */

    /**@{*/

    /**
     * 更新一個資料表上的項目。
     *
     * 參數 `overwrite` 指示是否進行完全複寫：
     *
     * - 若為 `True`，則所有欄位將會重置並覆寫
     *
     *  \warning
     *  進行完全覆寫時，所有必要欄位（schema: `required`）皆需要提供，即使不進行更新
     *
     * - 若為 `False`，則僅 `item` 中所帶的欄位會被覆寫
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param id        要更新的項目 `id`
     * @param item      要更新的資料
     * @param overwrite 是否進行完全覆寫
     * @throws IOException                  資料表不存在、網路錯誤，或當 `overwrite` 為 `True` 卻缺少部分欄位資料
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public void update(String primaryKey, Tuple item, boolean overwrite) throws IOException {
        mCore.cd(primaryKey)
                .createConnection()
                .method(overwrite ? "PUT" : "PATCH", item.toRequestBody())
                .getResponse();
    }

    public void update(int id, Tuple item, boolean overwrite) throws IOException {
        update(String.valueOf(id), item, overwrite);
    }

    /**
     * 更新一個資料表上的項目。適用於當 `item` 已經帶有 `id` 時。
     *
     * 本函式將會更新資料表上第 {@link Tuple#getId()} 個項目，且 `overwrite` 設為 `False`
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param item item to be updated
     * @throws UnsupportedOperationException `item` 沒有 `id` 欄位
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     * @see Table#update(int, Tuple, boolean)
     */
    public void update(Tuple item) throws IOException {
        update(item.getPrimaryKey(), item, false);
    }

    /**@}*/

    /*
     * Delete methods
     */

    /**@{*/

    /**
     * 刪除一些項目。
     *
     * \remarks
     * 此方法允許一次刪除多個項目。
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param ids 要被刪除的項目的 `id`
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public void delete(int... ids) throws IOException {
        for (int id : ids) {
            delete(String.valueOf(id));
        }
    }

    /**
     * 刪除一些項目。
     *
     * \remarks
     * 此方法允許一次刪除多個項目。
     *
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param primaryKeys 要被刪除的項目的 `id`
     * @throws IOException                  資料表不存在，或網路錯誤
     * @throws NetworkOnMainThreadException 在主執行緒上使用此函式
     */
    public void delete(String... primaryKeys) throws IOException {
        for (String key : primaryKeys) {
            mCore.cd(key)
                    .createConnection()
                    .method("DELETE")
                    .getResponse();
        }
    }

    /**@}*/

}
