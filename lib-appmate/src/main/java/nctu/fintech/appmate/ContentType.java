package nctu.fintech.appmate;

import android.support.annotation.NonNull;

/**
 * Content type for HTTP request header
 */
public enum ContentType {

    /**
     * Transfer data using RFC-7578 standard, aka {@code multipart/form-data}
     *
     * @see <a href="https://tools.ietf.org/html/rfc7578">RFC-7578</a>
     */
    FormData,

    /**
     * Transfer data using JavaScript object notation(JSON), aka {@code application/json}
     */
    Json,

    /**
     * Transfer data using old-style form post encoded method, aka {@code application/x-www-form-urlencoded}
     */
    FormUrlencoded;

    /**
     * Convert enum item to the value of {@code Content-Type} field in HTTP request header
     *
     * @return content-type string
     */
    @NonNull
    String getHeaderString() {
        switch (this) {
            case FormData:
                return "multipart/form-data";
            case Json:
                return "application/json";
            case FormUrlencoded:
                return "application/x-www-form-urlencoded";
            default:
                throw new IllegalArgumentException();
        }
    }
}
