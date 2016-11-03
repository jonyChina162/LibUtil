package cn.jony.libutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import cn.jony.libutil.io.IOUtils;

import java.io.InputStream;

import static cn.jony.libutil.Constants.UTF_8;


@SuppressWarnings("unused")
public class ResourceUtil {
	public static String getFromRaw(Context context, @RawRes int rawId) {
		String result = "";
		InputStream in = null;
		try {
			in = context.getResources().openRawResource(rawId);
			result = IOUtils.toString(in, UTF_8);
		} catch (Exception e) {
			LogUtils.e("getFromRaw", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return result;
	}

	public static String getFromAssets(Context context, String fileName) {
		String result = "";
		InputStream in = null;
		try {
			in = context.getResources().getAssets().open(fileName);
			result = IOUtils.toString(in, UTF_8);
		} catch (Exception e) {
			LogUtils.e("getFromAssets", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return result;
	}

	@TargetApi(21)
	public static Drawable getDrawable(Context context, @DrawableRes int resId) {
		if (OsVersionUtil.hasLollipop()) {
			return context.getDrawable(resId);
		} else {
			return context.getResources().getDrawable(resId);
		}
	}
}
