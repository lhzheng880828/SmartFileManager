package com.calvin.smartfilemanager;

import android.os.FileObserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @title 
 * @author zyyang@grandstream.cn
 * @since 2015-11-20
 * @version
 */
public class DownLoadFileObserver extends FileObserver {

		private static final String TAG =  "DownLoadFileObserver";
		private static final String FPATH = "/mnt/sdcard/Download/";
		private static int mMask= FileObserver.DELETE| FileObserver.MOVED_FROM;
		private static DownLoadFileObserver mDownLoadFileObserver=null;
		private EventCallback mCallback;
		private static ExecutorService mService;
		public static synchronized DownLoadFileObserver instance(){
			if (mDownLoadFileObserver==null) {
				mDownLoadFileObserver = new DownLoadFileObserver(FPATH, mMask);
			}
			return mDownLoadFileObserver;
		}
		/**
		 * @param path
		 * @param mask
		 */
		private DownLoadFileObserver(String path, int mask) {
			super(path, mask);
			// TODO Auto-generated constructor stub
			mService = Executors.newCachedThreadPool();
		}

		/* (non-Javadoc)
		 * @see android.os.FileObserver#onEvent(int, java.lang.String)
		 */
		@Override
		public void onEvent(int event, String path) {
			// TODO Auto-generated method stub
			//LogUtils.printLog(TAG, ">>>>>>>>>> event = " +event);
			//LogUtils.printLog(TAG, ">>>>>>>>>> path = " +path);
			if (mCallback!=null) {
				final int levent=event;
				final String lpath=path;
				mService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mCallback.onEvent(levent, lpath);
					}
				});
			}
		}
		
		public interface EventCallback{
			void onEvent(int event, String path);
		}
		
		public void setEventListener(EventCallback callback){
			mCallback=callback;
		}
	 }