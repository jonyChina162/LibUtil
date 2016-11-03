package cn.jony.libutil;

import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * 日志封装类
 */
@SuppressWarnings("unused")
public class LogUtils {
    private static boolean VERBOSE = true;
    private static boolean ERROR = true;
    private static boolean INFO = true;
    private static boolean WARN = true;
    private static boolean DEBUG = true;

    public static void setDebug(boolean open) {
        DEBUG = open;
    }

    public static void setInfo(boolean open) {
        INFO = open;
    }

    public static void setError(boolean open) {
        ERROR = open;
    }

    public static void setWarn(boolean open) {
        WARN = open;
    }

    public static void setVerbose(boolean open) {
        VERBOSE = open;
    }

    public static void v(String tag, String message) {
        if (VERBOSE) {
            Log.v(tag, message);
        }

    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (DEBUG) {
            d(tag, formatString(msg, args));
        }

    }

    public static void e(String tag, String message) {
        if (ERROR) {
            Log.e(tag, message);
        }

    }

    public static void i(String tag, String message) {
        if (INFO) {
            Log.i(tag, message);
        }

    }

    public static void i(String tag, String msg, Object arg1) {
        if (INFO) {
            Log.i(tag, formatString(msg, arg1));
        }

    }

    public static void i(String tag, String msg, Object arg1, Object arg2) {
        if (INFO) {
            Log.i(tag, formatString(msg, arg1, arg2));
        }

    }

    public static void w(String tag, String message) {
        if (WARN) {
            Log.w(tag, message);
        }
    }

    public static void v(String tag, Throwable ex) {
        if (VERBOSE) {
            Log.v(tag, basicExceptionMsg(ex), ex);
        }

    }

    public static void d(String tag, Throwable ex) {
        if (DEBUG) {
            Log.d(tag, basicExceptionMsg(ex), ex);
        }

    }

    public static void e(String tag, Throwable ex) {
        if (ERROR) {
            Log.e(tag, basicExceptionMsg(ex), ex);
        }

    }

    public static void e(String tag, String msg, Throwable ex) {
        if (ERROR) {
            Log.e(tag, msg, ex);
        }

    }

    public static void e(Class<?> cls, Throwable tr, String msg, Object... args) {
        if (ERROR) {
            Log.e(getTag(cls), formatString(msg, args), tr);
        }
    }

    public static void e(String tag, Throwable tr, String msg, Object... args) {
        if (ERROR) {
            Log.e(tag, formatString(msg, args), tr);
        }
    }

    public static void i(String tag, Throwable ex) {
        if (INFO) {
            Log.i(tag, basicExceptionMsg(ex), ex);
        }

    }

    public static void w(String tag, Throwable ex) {
        if (WARN) {
            Log.w(tag, basicExceptionMsg(ex), ex);
        }
    }

    public static String basicExceptionMsg(Throwable e) {
        if (e == null) {
            return "null exception";
        }
        return TextUtils.isEmpty(e.getLocalizedMessage()) ? e.toString() : e.getLocalizedMessage();
    }

    private static String formatString(String str, Object... args) {
        return String.format(Locale.CHINA, str, args);
    }

    private static String getTag(Class<?> clazz) {
        return clazz.getSimpleName();
    }
}
