package cn.jony.libutil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class PackageUtil {
    private static final String TAG = "PackageUtils";

    /**
     * 获得签名信息
     *
     * @param context
     * @param pName
     * @return
     */
    public static String getSign(Context context, String pName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iterator = apps.iterator();
        while (iterator.hasNext()) {
            PackageInfo packageinfo = iterator.next();
            String packageName = packageinfo.packageName;
            if (TextUtils.equals(packageName, pName)) {
                return packageinfo.signatures[0].toCharsString();
            }
        }
        return null;
    }

    /**
     * 判断是否重新打包
     *
     * @return true 重新打包过 ； false 没有
     */
    public static boolean isRePackage(Context context, String packageName, String originSign) {
        boolean result = false;
        String signature = getSign(context, packageName); // 获得签名信息
        String md5Signature = EncryptUtil.md5(signature);
        LogUtils.d(TAG, "signature---->" + signature);
        LogUtils.d(TAG, "md5Signature---->" + md5Signature);
        if (!BuildConfig.DEBUG && !TextUtils.equals(md5Signature, originSign)) { // 判断是否是debug或release版的签名
            result = true;
        }

        return result;
    }

    /**
     * 检查action是否有对应的接受对象
     * @param context
     * @param action
     * @return
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * @deprecated 使用 {@link BuildConfig#VERSION_NAME} 代替
     * @param context
     * @return
     */
    @Deprecated
    public static String getAppVersionName(Context context) {
        PackageInfo pkg;
        try {
            pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pkg.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("getVersion", e);
            return null;
        }
    }

    /**
     * @deprecated 使用 {@link BuildConfig#VERSION_CODE} 代替
     * @param context
     * @return
     */
    @Deprecated
    public static int getAppVersionCode(Context context) {
        PackageInfo pkg;
        try {
            pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pkg.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("getVersion", e);
            return 0;
        }
    }

    /**
     * 获取友盟渠道名
     *
     * @param ctx
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName(Context ctx) {
        return getAppMetaData(ctx, "UMENG_CHANNEL");
    }


    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.get(key) + "";
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }
}