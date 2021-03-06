package com.calvin.smartfilemanager.setting;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.calvin.smartfilemanager.DocumentsApplication;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:17-12-13
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String KEY_THEME_STYLE = "themeStyle";
    public static final String KEY_PRIMARY_COLOR = "primaryColor";
    public static final String KEY_ACCENT_COLOR = "accentColor";
    public static final String KEY_FILE_HIDDEN = "fileHidden";
    public static final String KEY_ADVANCED_DEVICES = "advancedDevices";
    public static final String KEY_ROOT_MODE = "rootMode";
    public static final String KEY_FOLDER_ANIMATIONS = "folderAnimations";



    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static String getThemeStyle() {
        return PreferenceManager.getDefaultSharedPreferences(DocumentsApplication.getInstance().getBaseContext())
                .getString(KEY_THEME_STYLE, "1");
    }

    public static int getPrimaryColor() {
        return PreferenceManager.getDefaultSharedPreferences(DocumentsApplication.getInstance().getBaseContext())
                .getInt(KEY_PRIMARY_COLOR, Color.parseColor("#0288D1"));
    }

    public static int getAccentColor() {
        return PreferenceManager.getDefaultSharedPreferences(DocumentsApplication.getInstance().getBaseContext())
                .getInt(KEY_ACCENT_COLOR, Color.parseColor("#EF3A0F"));
    }

    public static boolean getDisplayFileHidden(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_FILE_HIDDEN, false);
    }

    public static boolean getDisplayAdvancedDevices(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ADVANCED_DEVICES, true);
    }

    public static boolean getRootMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ROOT_MODE, true);
    }

    public static boolean getFolderAnimation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_FOLDER_ANIMATIONS, false);
    }
}
