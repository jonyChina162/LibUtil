package cn.jony.libutil;

import android.os.Environment;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import cn.jony.libutil.io.IOUtils;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings("unused")
public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String SD_PATH = "/sdcard";
    public final static String FILE_TYPE_UNKNOWN = "unknow";
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};
    private final static int SIZE_UNIT = 1024;

    public final static int B = 0;
    public final static int KB = 1;
    public final static int MB = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({B, KB, MB})
    public @interface SizeUnit {
    }

    /**
     * 删除path对应的文件
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        File f = new File(path);
        return f.exists() && f.delete();
    }

    /**
     * 递归删除file对应的文件(夹)及其所有子文件
     *
     * @param file
     * @return 删除文件数
     */
    public static int deleteAllFiles(File file) {
        if (file == null || !file.exists())
            return 0;

        if (file.isDirectory()) {
            int result = 0;
            File[] subs = file.listFiles();
            if (subs == null) {
                return 0;
            }

            for (File sub : subs) {
                result += deleteAllFiles(sub);
            }

            result += file.delete() ? 1 : 0;
            return result;
        } else {
            LogUtils.i(TAG, file.getName() + " deleted");
            return file.delete() ? 1 : 0;
        }
    }

    /**
     * 复制文件
     *
     * @param src    原文件路径
     * @param target 目标文件路径
     * @return
     */
    public static boolean copyFile(String src, String target) {
        File s = new File(src);
        createFile(target);
        File t = new File(target);
        if (!s.exists())
            return false;

        deleteFile(target);

        try {
            return copyFile(new FileInputStream(s), new FileOutputStream(t));
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * @param src
     * @param target
     * @return
     * @see #copyFile(String, String)
     */
    public static boolean copyFile(InputStream src, File target) {
        deleteFile(target.getAbsolutePath());

        try {
            return copyFile(src, new FileOutputStream(target));
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * @param src
     * @param target
     * @return
     * @see #copyFile(String, String)
     */
    public static boolean copyFile(InputStream src, OutputStream target) {
        try {
            IOUtils.copy(src, target);
        } catch (IOException e) {
            LogUtils.e(TAG, e);
            return false;
        } finally {
            IOUtils.closeQuietly(src);
            IOUtils.closeQuietly(target);
        }

        return true;
    }

    /**
     * 检测sd卡是否加载成功
     *
     * @return
     */
    public static boolean isSdCardOK() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD路径 *
     */
    public static String getSDPath() {
        // 判断sd卡是否存在
        if (isSdCardOK()) {
            File sdDir = Environment.getExternalStorageDirectory();
            return sdDir.getAbsolutePath();
        }
        return SD_PATH;
    }

    /**
     * 转换文件大小 *
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String fileSizeString;
        if (fileSize < 1024) {
            fileSizeString = fileSize + " B";
        } else if (fileSize < 1048576) {
            fileSizeString = decimalFormat.format((double) fileSize / 1024) + " K";
        } else if (fileSize < 1073741824) {
            fileSizeString = decimalFormat.format((double) fileSize / 1048576) + " M";
        } else {
            fileSizeString = decimalFormat.format((double) fileSize / 1073741824) + " G";
        }
        return fileSizeString;
    }

    /**
     * 获取文件后缀名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return FILE_TYPE_UNKNOWN;
    }

    /**
     * 判断文件是链接
     *
     * @param file
     * @return 如果文件为null，也返回true
     * @throws IOException
     */
    public static boolean isSymlinkOrEmpty(File file) throws IOException {
        if (file == null)
            return true;
        return !file.getCanonicalFile().equals(file.getAbsoluteFile());
    }

    /**
     * 获取文件名
     */
    public static String getFileName(File file) {
        if (file.isDirectory())
            return file.getName();
        else {
            int la = file.getName().lastIndexOf('.');
            if (la > 0) {
                return file.getName().substring(0, la);
            }
            return file.getName();
        }
    }

    /**
     * 计算文件内容数量 *
     */
    public static int calcFileCount(File file, final List<String> types) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File filterFile) {
                    try {
                        if (filterFile.isHidden() || !filterFile.canRead() || isSymlinkOrEmpty(filterFile))
                            return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return filterFile.isDirectory()
                            || (types.contains(getExtensionName(filterFile.getName().toLowerCase())) && filterFile
                            .length() > 0);
                }
            });
            return files == null ? 0 : files.length;
        }
        return -1;
    }

    /**
     * 获取MIME类型 *
     */
    public static String getMIMEType(String name) {
        String type, subType = "*";
        String end = getExtensionName(name);

        switch (end) {
            case "apk":
                return "application/vnd.android.package-archive";
            case "mp4":
            case "avi":
            case "3gp":
            case "rmvb":
                type = "video";
                break;
            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                type = "audio";
                break;
            case "jpg":
                type = "image";
                subType = "jpeg";
                break;
            case "gif":
                type = "image";
                subType = "gif";
                break;
            case "png":
                type = "image";
                subType = "png";
                break;
            case "jpeg":
                type = "image";
                subType = "jpeg";
                break;
            case "bmp":
                type = "image";
                subType = "bmp";
                break;
            case "webp":
                type = "image";
                subType = "webp";
                break;
            case "txt":
            case "log":
                type = "text";
                break;
            default:
                type = "*";
                break;
        }
        type += "/" + subType;
        return type;
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            sb.append(HEX_DIGITS[(aB & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[aB & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 计算文件的md5
     *
     * @param filename
     * @return
     */
    public static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            System.out.println("error");
            return null;
        }
    }

    /**
     * @param file
     * @return 文件大小，默认为MB
     * @see #getSize(File, int)
     */
    public static float getSize(File file) {
        return getSize(file, MB);
    }

    /**
     * @param file 文件
     * @param unit 大小单位
     * @return 文件大小
     * @see #getSize(File)
     */
    public static float getSize(File file, @SizeUnit int unit) {
        if (file.exists()) {
            // 如果是目录则递归计算其内容的总大小，如果是文件则直接返回其大小
            if (!file.isFile()) {
                // 获取文件大小
                File[] fl = file.listFiles();
                float ss = 0;
                for (File f : fl)
                    ss += getSize(f, unit);
                return ss;
            } else {
                return convertSize((float) file.length(), unit);
            }
        } else {
            return 0;
        }
    }

    /**
     * 以B为基准，转换文件大小
     *
     * @param size
     * @param unit
     * @return
     */
    public static float convertSize(float size, @SizeUnit int unit) {
        for (int i = 0; i < unit; i++) {
            size /= SIZE_UNIT;
        }
        return size;
    }

    /**
     * 创建文件
     *
     * @param path
     * @return 闯将成功返回true, 如果存在则返回false
     */
    public static boolean createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                return file.createNewFile();
            } catch (IOException e) {
                LogUtils.e(TAG, e);
                return false;
            }
        } else {
            return false;
        }
    }

    private static final String FILE_URL_PREFIX = "file://";

    /**
     * @param url
     * @return 在url前加上'file://'
     */
    public static String getFileUrl(String url) {
        if (!TextUtils.isEmpty(url) && !url.startsWith(FILE_URL_PREFIX)) {
            return FILE_URL_PREFIX + url;
        } else {
            return url;
        }
    }

    /**
     * 获取文件最后修改时间
     * @param path
     * @return
     */
    public static long getFileLastModified(String path) {
        File file = new File(path);
        return file.exists() ? file.lastModified() : 0;
    }

    /**
     * 在sd卡下创建文件
     * @param dir
     * @return
     */
    public static String createSDCardDir(String dir) {
        // 创建一个文件夹对象，赋值为外部存储器的目录
        File sdcardDir = Environment.getExternalStorageDirectory();
        //得到一个路径，内容是sdcard的文件夹路径和名字
        String path = sdcardDir.getPath() + dir;
        File file = new File(path);
        if (!file.exists()) {
            //若不存在，创建目录，可以在应用启动的时候创建
            file.mkdirs();
        }
        return path;

    }
}
