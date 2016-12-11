package nctu.fintech.appmate;

import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link Tuple}是對應{@code appmate}資料庫中的值組，即資料表中的一列
 *
 * <p>
 * Class {@link Tuple} represents a tuple on {@code appmate} database.
 * This class  offers {@link JSONObject}-like API
 * </p>
 * <p>
 * For concern of compatible, developer can cast this class to Android {@link JSONObject} by using {@link Tuple#toJSONObject()},
 * or Google Gson {@link JsonObject} by {@link Tuple#toJsonObject()}
 * </p>
 */
public class Tuple {

    /*
     * Constants
     */

    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /*
     * Global variables
     */

    /**
     * the {@link Table} which owns this {@link Tuple}
     */
    private Table _table;

    /**
     * the container where elements actually saved
     */
    private JsonObject _obj;

    /*
     * Constructors
     */

    /**
     * Create an empty {@link Tuple} instance
     */
    public Tuple() {
        _table = null;
        _obj = new JsonObject();
        JSONObject o;
    }

    /**
     * Create an empty {@link Tuple} instance from retrieved {@link JsonObject}
     *
     * @param table source {@link Table}
     * @param o     retrieved {@link JsonObject}
     */
    Tuple(Table table, JsonObject o) {
        _table = table;
        _obj = o;
    }

    /*
     * Type conversion
     */

    /**
     * Cast type to {@link JSONObject}
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
     * Cast type to GSON {@link JsonObject}, notice this it NOT {@link JSONObject}.
     * For usage of {@link JSONObject}, use {@link Tuple#toJSONObject()} instead
     *
     * @return a {@link JsonObject} instance
     */
    public JsonObject toJsonObject() {
        return _obj;
    }

    /**
     * Get JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        return _obj.toString();
    }

    /*
     * Properties
     */

    /**
     * Get number of key-value pairs in this container
     *
     * @return size of this container
     */
    public int size() {
        return _obj.size();
    }

    /**
     * Whether the set is empty or not
     *
     * @return this container is empty or not
     */
    public boolean isEmpty() {
        return _obj.size() == 0;
    }

    /**
     * Get id of this container
     *
     * @return id, or -1 when this element is local
     */
    public int getId() {
        return _obj.has("id") ? _obj.get("id").getAsInt() : -1;
    }

    /**
     * Get if this container has certain key or not
     *
     * @param key key to search
     * @return whether this container has certain key or not
     */
    public boolean has(String key) {
        return _obj.has(key);
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this container
     *
     * @return a set view of the mappings contained in this container
     */
    @NonNull
    public Set<Map.Entry<String, String>> entrySet() {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> p : _obj.entrySet()) {
            map.put(p.getKey(), p.getValue().toString());
        }
        return map.entrySet();
    }

    /*
     * `Get` methods
     */

    /**
     * Get value as {@link String} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid string value.
     */
    public String get(String key) {
        return _obj.get(key).toString();
    }

    /**
     * Get value as {@code boolean} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid boolean value.
     */
    public boolean getAsBoolean(String key) {
        return _obj.get(key).getAsBoolean();
    }

    /**
     * Get value as {@code byte} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid byte value.
     */
    public byte getAsByte(String key) {
        return _obj.get(key).getAsByte();
    }

    /**
     * Get value as {@code char} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid char value.
     */
    public char getAsChar(String key) {
        return _obj.get(key).getAsCharacter();
    }

    /**
     * Get value as {@code float} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid float value.
     */
    public float getAsFloat(String key) {
        return _obj.get(key).getAsFloat();
    }

    /**
     * Get value as {@code double} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid double value.
     */
    public double getAsDouble(String key) {
        return _obj.get(key).getAsDouble();
    }

    /**
     * Get value as {@code int} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid integer value.
     */
    public int getAsInt(String key) {
        return _obj.get(key).getAsInt();
    }

    /**
     * Get value as {@link Date} type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid date value.
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
     * Get value as {@link Calendar}(aka timestamp) type
     *
     * @param key key
     * @return value
     * @throws ClassCastException if the element not a valid timestamp value.
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

    //TODO put tuple
    //TODO put drawable

    /*
     * `Put` methods
     */

    /**
     * Add a {@link String} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, String value) {
        _obj.addProperty(key, value);
    }

    /**
     * Add a {@link Number} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Number value) {
        _obj.addProperty(key, value);
    }

    /**
     * Add a {@link Character} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Character value) {
        _obj.addProperty(key, value);
    }

    /**
     * Add a {@link Boolean} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Boolean value) {
        _obj.addProperty(key, value);
    }

    /**
     * Add a {@link Calendar} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        _obj.addProperty(key, sdf.format(value.getTime()));
    }

    /**
     * Add a {@link Calendar} value into this container
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Calendar value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        _obj.addProperty(key, sdf.format(value.getTime()));
    }

    //TODO put tuple
    //TODO put drawable

    /*
     * `Remove` method
     */

    /**
     * Remove a key-value pair
     *
     * @param key key
     * @return success or not
     */
    public boolean remove(String key) {
        return !_obj.remove(key).equals(JsonNull.INSTANCE);
    }

    /**
     * Clear the whole container
     */
    public void clear() {
        _table = null;
        _obj = new JsonObject();
    }

}