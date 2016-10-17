package nctu.fintech.appmate;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Log output control
 */
public final class Debug {

    /*
     * Constants
     */
    /**
     * Ultimate high authority!
     */
    private static final int WTF = 0x07;

    /**
     * DO NOT throw any log
     */
    public static final int Silent = 0x07;

    /**
     * Throws Only {@code Error} log
     */
    public static final int Error = 0x06;

    /**
     * Throws {@code Error} and {@code Warn} log
     */
    public static final int Warn = 0x05;

    /**
     * Throws {@code Error}, {@code Warn} and {@code Info} log
     */
    public static final int Info = 0x04;

    /**
     * Throws all log
     */
    public static final int Debug = 0x03;

    /**
     * Throws all log, and print stack trace on exception
     */
    public static final int Verbose = 0x2;

    @IntDef({
            Silent,
            Error,
            Warn,
            Info,
            Debug,
            Verbose
    })
    public @interface LogLevel {
    }

    /*
     * Private field
     */
    private static int _level = Silent;

    /**
     * Set log message output level. Default is {@link this#Silent}
     *
     * @param level log output level
     */
    public static void setLevel(@LogLevel int level) {
        _level = level;
    }

    /*
     * Interface & implementation
     */
    private interface LogFunc {
         int getOutputLevel();
         void log(@NonNull Object sender, @NonNull String msg, Object... args);
    }

    private static final LogFunc funcWtf = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return WTF;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.wtf(sender.getClass().getName(), msg);
        }
    };

    private static final LogFunc funcError = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return Error;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.e(sender.getClass().getName(), msg);
        }
    };

    private static final LogFunc funcWarn = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return Warn;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.w(sender.getClass().getName(), msg);
        }
    };

    private static final LogFunc funcInfo = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return Info;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.i(sender.getClass().getName(), msg);
        }
    };

    private static final LogFunc funcDebug = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return Debug;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.d(sender.getClass().getName(), msg);
        }
    };

    private static final LogFunc funcVerbose = new LogFunc() {
        @Override
        public int getOutputLevel() {
            return Verbose;
        }

        @Override
        public void log(@NonNull Object sender, @NonNull String format, Object... args) {
            String msg = String.format(format, args);
            Log.v(sender.getClass().getName(), msg);
        }
    };

    /*
     * Message handlers
     */
    private static void handleMessage(LogFunc func,@NonNull Object sender,  @NonNull String msg, Object... args) {
        if (_level > func.getOutputLevel()) {
            return;
        }
        func.log(sender, msg, args);
    }

    private static void handleException(LogFunc func, @NonNull Object sender, @NonNull Exception e) {
        if (_level > func.getOutputLevel()) {
            return;
        }
        if (e.getMessage() != null) {
            func.log(sender, "Exception \"%s\" occurs: %s", e.getClass().getName(), e.getMessage());
        } else {
            func.log(sender, "Exception \"%s\" occurs", e.getClass().getName());
        }
        if (_level == Verbose) {
            e.printStackTrace();
        }
    }

    /*
     * Internal methods
     */
    static void wtf(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcWtf, sender, msg, args);
    }

    static void e(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcError, sender, msg, args);
    }

    static void w(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcWarn, sender, msg, args);
    }

    static void i(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcInfo, sender, msg, args);
    }

    static void d(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcDebug, sender, msg, args);
    }

    static void v(@NonNull Object sender, @NonNull String msg, Object... args) {
        handleMessage(funcVerbose, sender, msg, args);
    }

    static void wtf(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcWtf, sender, e);
    }

    static void e(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcError, sender, e);
    }

    static void w(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcWarn, sender, e);
    }

    static void i(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcInfo, sender, e);
    }

    static void d(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcDebug, sender, e);
    }

    static void v(@NonNull Object sender, @NonNull Exception e) {
        handleException(funcVerbose, sender, e);
    }

}

