/*
 * DownloadListActivity.java
 * Copyright (C) 2015年6月30日 www.grandstream.com DocumentActivity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.calvin.smartfilemanager;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.calvin.smartfilemanager.model.DocumentInfo;
import com.calvin.smartfilemanager.model.RootInfo;

import java.util.List;


/**
 * @title 
 * @author zyyang@grandstream.cn
 * @since 2015/6/30
 * @version 
 */
public class DownloadListActivity extends BaseActivity {

	public DownloadListActivity(String tag){
		super(tag);
	}

	private static final String TAG="DownloadListActivity";
	private Context mContext;
	
		@Override
	    public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
	        setContentView(R.layout.layout_download_main);
	        
	    }

	@Override
	public State getDisplayState() {
		return null;
	}

	@Override
	public void onDocumentPicked(DocumentInfo doc) {

	}

	@Override
	public void onDocumentsPicked(List<DocumentInfo> docs) {

	}

	@Override
	void onTaskFinished(Uri... uris) {

	}

	@Override
	void onDirectoryChanged(int anim) {

	}

	@Override
	void updateActionBar() {

	}

	@Override
	void saveStackBlocking() {

	}

	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {

	}

	@Override
	public String getTag() {
		return null;
	}

	@Override
	public void onRootPicked(RootInfo info, RootInfo parent) {

	}

	@Override
	public void onRootPicked(RootInfo info, boolean closeDrawer) {

	}

	@Override
	public void again() {

	}

	@Override
	public void onAppPicked(ResolveInfo info) {

	}
}
