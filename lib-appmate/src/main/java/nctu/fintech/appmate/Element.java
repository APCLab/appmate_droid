package nctu.fintech.appmate;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by tzu-ting on 2016/12/11.
 */
public class Element extends Table implements Map<String, String> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object o) {
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    @Override
    public String get(Object o) {
        return null;
    }

    @Override
    public String put(String s, String s2) {
        return null;
    }

    @Override
    public String remove(Object o) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {

    }

    @Override
    public void clear() {

    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return null;
    }

    @NonNull
    @Override
    public Collection<String> values() {
        return null;
    }

    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }
}
