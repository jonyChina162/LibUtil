package cn.jony.libutil;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

@SuppressWarnings("unused")
public class DeviceUtil {
    /**
     * 生成手机唯一标志id，生成两次可能不同
     * @param context
     * @return
     */
    public static String getUid(Context context){
        return getDeviceId(context) + getMac(context) + getAndroidId(context);
    }

    /**
     * 获取手机设备id
     * @param context
     * @return
     */
	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = telephonyManager.getDeviceId();
		return TextUtils.isEmpty(deviceId) || deviceId.equals("000000000000000") || deviceId.equals("0") ? ""
				: deviceId.toLowerCase();
	}

    /**
     * 获取手机mac地址
     * @param context
     * @return
     */
	public static String getMac(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null) {
			return "";
		} else {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String mac = wifiInfo.getMacAddress();
			if (TextUtils.isEmpty(mac)) {
				return "";
			} else {
				mac = mac.replace(":", "");
			}
			return TextUtils.isEmpty(mac) ? "" : mac.toUpperCase();
		}
	}

    /**
     * 获取手机androidid。
     * @param context
     * @return
     */
	public static String getAndroidId(Context context) {
		String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		if (androidId == null || androidId.equals("9774d56d682e549c") || androidId.length() < 15) {
			// if ANDROID_ID is null, or it's equals to the GalaxyTab generic
			// ANDROID_ID or bad, generates a new one
			final SecureRandom random = new SecureRandom();
			androidId = new BigInteger(64, random).toString(16);
		}
		return androidId;
	}
}
