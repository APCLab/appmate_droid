package nctu.fintech.appmate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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

import nctu.fintech.appmate.core.Core;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

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

    private Table mTable;
    private Core mCore;

    private String mPrimaryKey;

    /**
     * the container where elements actually saved.
     */
    private JsonObject mData;

    /**
     * the container saved images
     */
    private Map<JsonElement, Bitmap> mImages;

    Core getCore() {
        if (mCore == null) {
            mCore = new Core(mTable.mCore, getPrimaryKey());
        }
        return mCore;
    }

    /**
     * 取得此物件主鍵。
     *
     * @return 主鍵
     * @throws UnsupportedOperationException 無此欄位
     */
    public String getPrimaryKey() {
        if (mPrimaryKey != null) {
            return mPrimaryKey;
        }
        if (!mData.has("id")) {
            return mPrimaryKey = String.valueOf(mData.get("id").getAsInt());
        }
        throw new UnsupportedOperationException("primary key not specific.");
    }

    /*
     * Constructors
     */

    /**
     * @name 建構子
     *
     * @{
     */

    /**
     * Reset tuple by specific data.
     *
     * @param table
     * @param data
     */
    void reset(Table table, JsonObject data) {
        mTable = table;
        mData = data;
        mImages = new LinkedHashMap<>();
    }

    public Tuple(JsonObject jsonObject) {
        reset(null, jsonObject);
    }

    /**
     * 建立一個空的 {@link Tuple} 實體。
     */
    public Tuple() {
        this(new JsonObject());
    }

    public Tuple(String jsonString) {
        this(new JsonParser()
                .parse(jsonString)
                .getAsJsonObject()
        );
    }

    public Tuple(JSONObject jsonObject) {
        this(jsonObject.toString());
    }

    /**
     * 以取得的 {@link JsonObject} 建立一個 {@link Tuple} 實體。
     *
     * @param table
     * @param data
     */
    Tuple(Table table, JsonObject data) {
        reset(table, data);
    }

    /**@}*/

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
            return new JSONObject(mData.toString());
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
        return mData;
    }

    /**
     * 輸出 `JSON` 字串。
     *
     * @return JSON 字串
     */
    @Override
    public String toString() {
        return mData.toString();
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
        return mData.size();
    }

    /**
     * 取得此容器是否為空。
     *
     * @return 容器是否為空
     */
    public boolean isEmpty() {
        return mData.size() == 0;
    }


    /**
     * 取得此容器是否包含某欄位。
     *
     * @param key 欄位名
     * @return 是否包含該欄位
     */
    public boolean has(String key) {
        return mData.has(key);
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
        for (Map.Entry<String, JsonElement> p : mData.entrySet()) {
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
        JsonElement element = mData.get(key);
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
        return mData.get(key).getAsBoolean();
    }

    /**
     * 取得值，並嘗試轉型為 `byte` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `byte` 型別
     */
    public byte getAsByte(String key) {
        return mData.get(key).getAsByte();
    }

    /**
     * 取得值，並嘗試轉型為 `char` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `char` 型別
     */
    public char getAsChar(String key) {
        return mData.get(key).getAsCharacter();
    }

    /**
     * 取得值，並嘗試轉型為 `float` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `float` 型別
     */
    public float getAsFloat(String key) {
        return mData.get(key).getAsFloat();
    }

    /**
     * 取得值，並嘗試轉型為 `double` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `double` 型別
     */
    public double getAsDouble(String key) {
        return mData.get(key).getAsDouble();
    }

    /**
     * 取得值，並嘗試轉型為 `int` 型別。
     *
     * @param key 欄位名
     * @return 值
     * @throws ClassCastException 當該值無法被轉型為 `int` 型別
     */
    public int getAsInt(String key) {
        return mData.get(key).getAsInt();
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
            return sdf.parse(mData.get(key).getAsString());
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
            calendar.setTime(sdf.parse(mData.get(key).getAsString()));
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
        URL url = new URL(mData.get(key).getAsString());

        return new Tuple(mTable, getCore()
                .createConnection(url)
                .getResponseAsJson()
        );
    }

//    /**
//     * 取得值所指向的圖片。
//     * <p>
//     * \attention
//     * 此方法需要使用連線相關參數，當此物件為自行建立、而非自資料表回傳時，此函式無法作用。
//     * <p>
//     * \remarks
//     * 此函式會使用網路連線。
//     *
//     * @param key 欄位名
//     * @return 該值對應的外來鍵物件
//     * @throws ClassCastException            當該值無法被視為圖片資源
//     * @throws UnsupportedOperationException 當該值所指向的資源不在同一個資料庫中
//     * @throws IOException                   資源不存在，或網路錯誤
//     * @throws NetworkOnMainThreadException  在主執行緒上使用此函式
//     */
//    public Bitmap getAsBitmap(String key) throws IOException {
//        // local operation
//        if (_img.containsKey(key)) {
//            return _img.get(key);
//        }
//
//        // check domain
//        URL url = new URL(mData.get(key).getAsString());
//        if (!url.getHost().equals(_core.url.getHost())) {
//            throw new UnsupportedOperationException("cross domain query not supported");
//        }
//
//        // create connection
//        HttpURLConnection con = _core.openUrl(url);
//
//        // check response code
//        int code = con.getResponseCode(); //TODO merge this to DbCore
//        if (code != HttpURLConnection.HTTP_OK) {
//            Log.e(getClass().getName(), "unexpected HTTP response code received: " + code);
//        }
//
//        // read response
//        try (InputStream input = con.getInputStream()) {
//            Bitmap bitmap = BitmapFactory.decodeStream(input);
//            _img.put(key, bitmap); //TODO 建立欄位修改紀錄，簡化上傳量
//            return bitmap;
//        }
//    }

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
        mData.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Number value) {
        mData.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Character value) {
        mData.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Boolean value) {
        mData.addProperty(key, value);
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        mData.addProperty(key, sdf.format(value.getTime()));
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Calendar value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        mData.addProperty(key, sdf.format(value.getTime()));
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, Tuple value) {
        mData.addProperty(key, value.getCore().toString());
    }

    /**
     * 新增一組鍵值對到容器中。
     *
     * @param key   欄位名
     * @param value 值
     */
    public void put(String key, String filename, Bitmap image) {
        if (!(filename.endsWith(".png") || filename.endsWith(".PNG"))) {
            filename += ".png";
        }

        mData.addProperty(key, filename);

        JsonElement element = mData.get(key);
        mImages.put(element, image);
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
        JsonElement element = mData.remove(key);
        if (element == null) {
            return false;
        }

        if (mImages.containsKey(element)) {
            return mImages.remove(element) == null;
        }
        return true;
    }

    /**@}*/

    RequestBody toRequestBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String, JsonElement> pair : mData.entrySet()) {
            JsonPrimitive element = (JsonPrimitive) pair.getValue();
            String key = pair.getKey();
            String value = element.isString() ? element.getAsString() : element.toString();

            if (mImages.containsKey(element)) { // the element indicate to a image
                // get image
                Bitmap image = mImages.get(element);

                // convert to byte data
                ByteArrayOutputStream tempStream = new ByteArrayOutputStream(image.getByteCount());
                image.compress(Bitmap.CompressFormat.PNG, 100, tempStream);

                // append to request body
                builder.addFormDataPart(
                        key,
                        value,
                        RequestBody.create(
                                MediaType.parse("image/png"),
                                tempStream.toByteArray()
                        )
                );

            } else { // normal situation
                builder.addFormDataPart(key, value);
            }
        }

        return builder.build();
    }

}
