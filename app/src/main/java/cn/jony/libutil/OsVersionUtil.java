package cn.jony.libutil;

import android.os.Build;

@SuppressWarnings("unused")
public class OsVersionUtil {
	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= 8;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= 9;
	}

	public static boolean hasGingerbreadMR1() {
		return Build.VERSION.SDK_INT >= 10;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= 11;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= 12;
	}

	public static boolean hasIceCreamSandwish() {
		return Build.VERSION.SDK_INT >= 14;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= 19;
	}

	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= 21;
	}

	public static boolean hasM(){
        return Build.VERSION.SDK_INT >= 23;
    }

	public static boolean isKindleFire() {
		return Build.MANUFACTURER.equals("Amazon")
				&& (Build.MODEL.equals("Kindle Fire") || Build.MODEL.startsWith("KF"));
	}
}
