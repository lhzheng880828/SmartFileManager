package com.calvin.smartfilemanager.model;

import android.content.ContentProviderClient;
import android.database.Cursor;

import com.calvin.smartfilemanager.libcore.io.IoUtils;
import com.calvin.smartfilemanager.misc.ContentProviderClientCompat;

import java.io.Closeable;

import static com.calvin.smartfilemanager.BaseActivity.State.MODE_UNKNOWN;
import static com.calvin.smartfilemanager.BaseActivity.State.SORT_ORDER_UNKNOWN;

public class DirectoryResult implements Closeable {
	public ContentProviderClient client;
    public Cursor cursor;
    public Exception exception;

    public int mode = MODE_UNKNOWN;
    public int sortOrder = SORT_ORDER_UNKNOWN;

    @Override
    public void close() {
        IoUtils.closeQuietly(cursor);
        ContentProviderClientCompat.releaseQuietly(client);
        cursor = null;
        client = null;
    }
}