/*
 * DownLoadListAdapter.java
 * Copyright (C) 2015年7月6日 www.grandstream.com DocumentActivity
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
package com.calvin.smartfilemanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.calvin.smartfilemanager.model.DirectoryResult;

/**
 * @title the ListView Adapter for download.
 * @author zyyang@grandstream.cn
 * @since 2015/7/6
 * @version 
 */
public class DownLoadListAdapter extends BaseAdapter {

	private static final String TAG = "DownLoadListAdapter";
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	private Cursor mCursor;
	private int mCursorSize=0;
	private boolean isEdit=false;
	private boolean [] itemState;
	
	
	public DownLoadListAdapter(Context context, DirectoryResult result){
		mContext=context;
		mCursor = result != null ? result.cursor : null;
		mCursorSize= mCursor != null ? mCursor.getCount() : 0;
		mLayoutInflater= LayoutInflater.from(mContext);
		itemState=new boolean[mCursorSize];
	}
	
	public void refreshViews(DirectoryResult result){
		mCursor = result != null ? result.cursor : null;
		mCursorSize= mCursor != null ? mCursor.getCount() : 0;
		itemState=new boolean[mCursorSize];
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCursorSize;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
