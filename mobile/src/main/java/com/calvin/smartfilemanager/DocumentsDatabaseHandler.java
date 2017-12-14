/*
 * DocumentsDatabaseHandler.java
 * Copyright (C) 2015年11月19日 www.grandstream.com GVCDocumentsUI
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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;

/**
 * @title Documents database controller
 * @author zyyang@grandstream.cn
 * @since 2015-11-19
 * @version 
 */
public class DocumentsDatabaseHandler {

	private static String DB_NAME = "downloads.db";
	private static String DB_PATH = "/data/data/com.android.providers.downloads/databases/";
	private SQLiteDatabase mDatabase;
	
	/**
	 * 
	 * @param dbName database name, default is "downloads.db"
	 * @param dbPath database path, default path is "com.android.providers.downloads/databases/"
	 */
	public DocumentsDatabaseHandler(String dbName, String dbPath) {
		// TODO Auto-generated constructor stub
		if (dbName!=null&&!dbName.isEmpty()) {
			DB_NAME=dbName;
		}
		if (dbPath!=null&&!dbPath.isEmpty()) {
			DB_PATH=dbPath;
		}
		
		File dbFile=new File(DB_PATH+DB_NAME);
		if (dbFile!=null&&dbFile.exists()) {
			try {
				mDatabase = SQLiteDatabase.openDatabase(DB_PATH+DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
			}catch(SQLiteException se){
				se.printStackTrace();
			}catch (Exception e) {
				// TODO: handle exception
			}
			
		}
	}
	
	public int deleteRowDocument(String docId){
		if (null==docId||docId.isEmpty()) {
			return -1;
		}
		try {
			Integer.parseInt(docId);
		} catch (Exception e) {
			// TODO: handle exception
			return -2;
		}
		
		if (isVialid()) {
			String clause=" where _id = ? ";
			return mDatabase.delete("downloads", clause, new String[]{docId});
		}
		return -1;
	}

	private boolean isVialid(){
		return mDatabase!=null&&mDatabase.isOpen();
	}
}
