package cn.jony.libutil;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import java.net.InetSocketAddress;

/**
 * 网络状态检查类
 */

@SuppressWarnings("unused")
public class NetStatusUtil {
    private final Context mContext;
    private static volatile NetStatusUtil INSTANCE;
    private volatile WifiManager.WifiLock mWifiLock;
    private volatile PowerManager.WakeLock mWakeLock;

    private NetStatusUtil(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static NetStatusUtil getInstance(Context context) {
        Preconditions.checkNotNull(context);

        if (INSTANCE == null) {
            synchronized (NetStatusUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetStatusUtil(context);
                }
            }
        }

        return INSTANCE;
    }

    public boolean checkNetworkState() {
        final ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null && manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()) {
            return true;
        }

        return false;
    }

    public int getNetworkType() {
        final ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        int result = -1;
        final NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final NetworkInfo ethernet = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        if (wifi != null && wifi.isConnected()) {
            result = ConnectivityManager.TYPE_WIFI;
        } else if (mobile != null && mobile.isConnected()) {
            result = ConnectivityManager.TYPE_MOBILE;
        } else if (ethernet != null && ethernet.isConnected()) {
            result = ConnectivityManager.TYPE_ETHERNET;
        }

        return result;
    }

    public boolean isWifi() {
        return getNetworkType() == ConnectivityManager.TYPE_WIFI;
    }

    public boolean isMobile() {
        return getNetworkType() == ConnectivityManager.TYPE_MOBILE;
    }

    public boolean isNetworkOK() {
        return this.getNetworkType() != -1;
    }

    public InetSocketAddress getAPNProxy() {
        if (this.getNetworkType() == 0) {
            Uri uri = Uri.parse("content://telephony/carriers/preferapn");
            Cursor cursor = this.mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndex("proxy"));
                String port = cursor.getString(cursor.getColumnIndex("port"));
                if (address != null && address.trim().length() > 0) {
                    return new InetSocketAddress(address, Integer.getInteger(port, 80));
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public void acquireWakeLock() {
        this.acquireWakeLock(true);
    }

    public void acquireWakeLock(boolean isReferenceCounted) {
        synchronized (this) {
            if (this.mWakeLock == null) {
                WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
                this.mWifiLock = wifiManager.createWifiLock("wifiLock");
                PowerManager powerManager = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
                this.mWakeLock = powerManager.newWakeLock(1, "wakelock");
            }
        }

        this.mWifiLock.setReferenceCounted(isReferenceCounted);
        this.mWakeLock.setReferenceCounted(isReferenceCounted);
        this.mWifiLock.acquire();
        this.mWakeLock.acquire();
    }

    public void releaseWakeLock() {
        if (this.mWifiLock != null) {
            this.mWifiLock.release();
        }

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
        }
    }
}
