package cn.jony.libutil;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

@SuppressWarnings("unused")
public class ClipboardUtil {
    /**
     * 复制文本到粘贴板
     * @param context
     * @param text
     */
    public static void copyText2Clipboard(Context context, String text){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", text);
        clipboardManager.setPrimaryClip(clipData);
    }
}
