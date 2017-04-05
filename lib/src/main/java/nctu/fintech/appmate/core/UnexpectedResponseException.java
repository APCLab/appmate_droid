package nctu.fintech.appmate.core;

import android.util.Log;

import java.io.IOException;

/**
 * An exception indicate that the response result is not expected.
 */
public class UnexpectedResponseException extends IOException {

    private int mCode;
    private String mMessage;
    private String mBody;

    UnexpectedResponseException(int code, String message, String body) {
        super("unexpected response result: " + code + message);
        Log.e(this.getClass().getSimpleName(), "unexpected response code: " + code);

        mCode = code;
        mMessage = message;
        mBody = body;
    }

    /**
     * get response code
     *
     * @return response code
     */
    public int code() {
        return mCode;
    }

    /**
     * get response message (correspond to {@link #code()}
     *
     * @return response message
     */
    public String message() {
        return mMessage;
    }

    /**
     * get response body
     *
     * @return response body
     */
    public String body() {
        return mBody;
    }

}
