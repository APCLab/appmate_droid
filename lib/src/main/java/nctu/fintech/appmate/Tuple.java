package nctu.fintech.appmate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 值組，資料的基礎型別。
 * <p>
 * 此類別操作方式類似於 {@link JSONObject}，由於資料庫支援部分較特殊之操作，故另建本類別作為預設回傳型態。
 * 對於程式有相容性需求者，請使用 {@link Tuple#toJSONObject()} 方法轉型。
 */
public class Tuple {

    /*
     * Constants
     */

    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static int INDEX_TABLE_NAME = 2;
    private final static int INDEX_ITEM_ID = 3;
    private final static String PREFIX_IMG = "IMG";

    /*
     * Global variables
     */

    /**
     * the {@link Table} which owns this {@link Tuple}.
     */
    private TableCore _core;

    /**
     * the container where elements actually saved.
     */
    private JsonObject _obj;

    /**
     * the container saved images
     */
    private Map<String, Bitmap> _img;

    /*
     * Constructors
     */

    /**
     * @name 建構子
     *
     * \todo
     * 自 {@link String} 建立
     *
     * \todo
     * 自 {@link JSONObject} 建立
     *
     * \todo
     * 自 {@link JsonObject} 建立
     *
     * @{
     */

    /**
     * 建立一個空的 {@link Tuple} 實體。
     */
    public Tuple() {
        reset(new NullCore(), new JsonObject());
    }

    /**
     * 以取得的 {@link JsonObject} 建立一個 {@link Tuple} 實體。
     *
     * @param core
     * @param o
     */
    Tuple(TableCore core, JsonObject o) {
        reset(core, o);
    }

    /**@}*/

    /*
     * Basic operation
     */

    /**
     * Reset tuple by specific data.
     *
     * @param core
     * @param o
     */
    void reset(TableCore core, JsonObject o) {
        _core = core;
        _obj = o;
        _img = new LinkedHashMap<>();
    }

    /*
     * Type conversion
     */

    /**
     * @name 型別轉換
     * @{
     */

    /**
     * 轉型為 {@link org.json.JSONObject}。
     *
     * @return a {@link JSONObject} instance
     */
    public JSONObject toJSONObject() {
        try {
            return new JSONObject(_obj.toString());
        } catch (JSONException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 轉型為 {@link com.google.gson.JsonObject}。
     *
     * @return a {@link JsonObject} instance
     */
    public JsonObject toJsonObject() {
        return _obj;
    }

    /**
     * 輸出 `JSON` 字串。
     *
     * @return JSON 字串
     */
    @Override
    public String toString() {
        return _obj.toString();
    }

    /**@}*/

    /*
     * Properties
     */

    /**
     * @name 屬性查詢
     * @{
     */

    /**
     * 取得容器內資料數。
     *
     * @return 容器內資料數
     */
    public int size() {
        return _obj.size();
    }

    /**
     * 取得此容器是否為空。
     *
     * @return 容器是否為空
     */
    public boolean isEmpty() {
        return _obj.size() == 0;
    }

    /**
     * 取得此物件ID。
     *
     * @return `id`
     * @throws UnsupportedOperationException 無此欄位
     */
    public int getId() {
        if (!_obj.has("id")) {
            throw new UnsupportedOperationException("item id is not assigned.");
        }
        return _obj.get("id").getAsInt();
    }

    /**
     * 取得此容器是否包含某欄位。
     *
     * @param key 欄位名
     * @return 是否包含該欄位
     */
    public boolean has(String key) {
        return _obj.has(key);
    }

    /**
     * 取得所有內容，便於使用 `foreach` 陳述。
     *
     * @return a set view of the mappings contained in this container
     */
    @NonNull
    public Set<Map.Entry<String, String>> entrySet() {
        //TODO 簡化
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> p : _obj.entrySet()) {
            JsonElement element = p.getValue();
            if (!(element instanceof JsonPrimitive)) {
                map.put(p.getKey(), element.toString());
                continue;
            }

            JsonPrimitive primitive = (JsonPrimitive) element;
            if (primitive.isString()) {
                map.put(p.getKey(), primitive.getAsString());
            } else {
                map.put(p.getKey(), primitive.toString());
            }
        }
        return map.entrySet();
    }

    /**@}*/

    /**
     * Get item url on db.
     *
     * @return url
     * @throws UnsupportedOperationException when item id is not assigned.
     */
    String toUrl() {
        return String.format("%s%s/", _core.url, getId());
    }

    /**
     * Get image to upload.
     *
     * @param key ~
     * @return ~
     */
    Bitmap getUploadBitmap(String key) {
        return (!key.startsWith(PREFIX_IMG) || !_img.containsKey(key)) ? null : _img.get(key); //TODO remove it! 不該這樣判定
    }

    /*
     * `Get` methods
     */

    /**@{*/

    /**
     * 取得值，以 {@link String} 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 {@link String} 型別
     */
    public String get(String key) {
        JsonElement element = _obj.get(key);
        if (!(element instanceof JsonPrimitive)) {
            return element.toString();
        }

        JsonPrimitive primitive = (JsonPrimitive) element;
        if (primitive.isString()) {
            return primitive.getAsString();
        } else {
            return primitive.toString();
        }
    }

    /**
     * 取得值，並嘗試轉型為 `boolean` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `boolean` 型別
     */
    public boolean getAsBoolean(String key) {
        return _obj.get(key).getAsBoolean();
    }

    /**
     * 取得值，並嘗試轉型為 `byte` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `byte` 型別
     */
    public byte getAsByte(String key) {
        return _obj.get(key).getAsByte();
    }

    /**
     * 取得值，並嘗試轉型為 `char` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `char` 型別
     */
    public char getAsChar(String key) {
        return _obj.get(key).getAsCharacter();
    }

    /**
     * 取得值，並嘗試轉型為 `float` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `float` 型別
     */
    public float getAsFloat(String key) {
        return _obj.get(key).getAsFloat();
    }

    /**
     * 取得值，並嘗試轉型為 `double` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `double` 型別
     */
    public double getAsDouble(String key) {
        return _obj.get(key).getAsDouble();
    }

    /**
     * 取得值，並嘗試轉型為 `int` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `int` 型別
     */
    public int getAsInt(String key) {
        return _obj.get(key).getAsInt();
    }

    /**
     * 取得值，並嘗試轉型為日期（{@link Date}）型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 {@link Date} 型別
     */
    public Date getAsDate(String key) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        try {
            return sdf.parse(_obj.get(key).getAsString());
        } catch (ParseException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    /**
     * 取得值，並嘗試轉型為日期時間（{@link Calendar}）型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 {@link Calendar} 型別
     */
    public Calendar getAsCalendar(String key) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(_obj.get(key).getAsString()));
            return calendar;
        } catch (ParseException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    /**
     * 取得值所指向的外來鍵。
     * <p>
     * \attention
     * 此方法需要使用連線相關參數，當此物件為自行建立、而非自資料表回傳時，此函式無法作用。
     * <p>
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param key 欄位名
     * @return 該值對應的外來鍵物件
     * @throws ClassCastException            當該值無法被視為外來鍵索引
     * @throws UnsupportedOperationException 當該值所指向的資源不在同一個資料庫中
     * @throws IOException                   資源不存在，或網路錯誤
     * @throws NetworkOnMainThreadException  在主執行緒上使用此函式
     */
    public Tuple getAsTuple(String key) throws IOException {
        URL url = new URL(_obj.get(key).getAsString());

        // check domain
        if (!url.getHost().equals(_core.url.getHost())) {
            throw new UnsupportedOperationException("cross domain query not supported");
        }

        // build table core
        String[] param = url.getPath().split("/");
        String tableNm = param[INDEX_TABLE_NAME];
        int id = Integer.parseInt(param[INDEX_ITEM_ID]);

        Table table = new Table(_core, tableNm);
        return table.get(id);
    }

    /**
     * 取得值所指向的圖片。
     * <p>
     * \attention
     * 此方法需要使用連線相關參數，當此物件為自行建立、而非自資料表回傳時，此函式無法作用。
     * <p>
     * \remarks
     * 此函式會使用網路連線。
     *
     * @param key 欄位名
     * @return 該值對應的外來鍵物件
     * @throws ClassCastException            當該值無法被視為圖片資源
     * @throws UnsupportedOperationException 當該值所指向的資源不在同一個資料庫中
     * @throws IOException                   資源不存在，或網路錯誤
     * @throws NetworkOnMainThreadException  在主執行緒上使用此函式
     */
    public Bitmap getAsBitmap(String key) throws IOException {
        // local operation
        if (_img.containsKey(key)) {
            return _img.get(key);
        }

        // check domain
        URL url = new URL(_obj.get(key).getAsString());
        if (!url.getHost().equals(_core.url.getHost())) {
            throw new UnsupportedOperationException("cross domain query not supported");
        }

        // create connection
        HttpURLConnection con = _core.openUrl(url);

        // check response code
        int code = con.getResponseCode(); //TODO merge this to DbCore
        if (code != HttpURLConnection.HTTP_OK) {
            Log.e(getClass().getName(), "unexpected HTTP response code received: " + code);
        }

        // read response
        try (InputStream input = con.getInputStream()) {
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            _img.put(key, bitmap); //TODO 建立欄位修改紀錄，簡化上傳量
            return bitmap;
        }
    }

    /**@}*/

    /*
     * `Put` methods
     */

    /**@{*/

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, String value) {
        _obj.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Number value) {
        _obj.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Character value) {
        _obj.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Boolean value) {
        _obj.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        _obj.addProperty(key, sdf.format(value.getTime()));
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Calendar value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        _obj.addProperty(key, sdf.format(value.getTime()));
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Tuple value) {
        _obj.addProperty(key, value.toUrl());
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Bitmap value) {
        String name = String.format("%s%x%x", PREFIX_IMG, System.currentTimeMillis(), new Random().nextInt());
        _obj.addProperty(key, name);
        _img.put(key, value);
    }

    /**@}*/

    /*
     * `Remove` method
     */

    /**@{*/

    /**
     * 自容器中移除一組鍵值對。
     *
     * @param key 欄位名
     * @return 成功移除與否
     */
    public boolean remove(String key) {
        return (_obj.remove(key) != null)
                && (_img.remove(key) != null);
    }

    /**@}*/

}
