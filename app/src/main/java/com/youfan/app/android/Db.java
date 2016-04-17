package com.youfan.app.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.youfan.app.android.data.EventCreateParams;
import com.youfan.app.android.data.EventResponse;
import com.youfan.app.android.data.UserInfo;
import com.youfan.app.android.data.User;

public class Db {
	private static final String USER_ID = "userID";
	private static final String FACEBOOK_ID = "facebookID";
	private static final String YOUFAN_TOKEN = "youfanToken";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String PHOTO_URL = "photoUrl";
	private static final String BIO = "bio";

	private static final Db sDb = new Db();

	private Db() {
		// Cannot be instantiated
	}

	private EventCreateParams eventCreateParams;
	private User user;
	private String userId;
	private String facebookId;
	private String youFanToken;
	private EventResponse.Event event;
	private File userPhotoFile;

	public static EventCreateParams getEventCreateParams() {
		return sDb.eventCreateParams;
	}

	public static void setEventCreateParams(EventCreateParams eventCreateParams) {
		sDb.eventCreateParams = eventCreateParams;
	}

	public static User getUser() {
		return sDb.user;
	}

	public static void setUser(User user) {
		sDb.user = user;
	}

	public static String getUserId() {
		return sDb.userId;
	}

	public static void setUserId(String userId) {
		sDb.userId = userId;
	}

	public static String getYouFanToken() {
		return sDb.youFanToken;
	}

	public static void setYouFanToken(String youFanToken) {
		sDb.youFanToken = youFanToken;
	}

	public static void setFacebookId(String facebookId) {
		sDb.facebookId = facebookId;
	}

	public static String getFacebookId() {
		return sDb.facebookId;
	}

	public static void saveFacebookIdToDisk(Context context, String facebookId){
		save(context, FACEBOOK_ID, facebookId);
	}

	public static void loadFacebookIdFromDisk(Context context){
		String facebookId = get(context, FACEBOOK_ID, null);
		sDb.facebookId = facebookId;
	}

	public static void logOutClear(Context context) {
		sDb.youFanToken = null;
		sDb.userId = null;
		sDb.user = null;

		save(context, FIRST_NAME, null);
		save(context, LAST_NAME, null);
		save(context, PHOTO_URL, null);
		save(context, BIO, null);
		save(context, YOUFAN_TOKEN, null);
		save(context, USER_ID, null);
	}

	public static void saveUserAndYouFanTokenToDisk(Context context) {
		if (sDb.user != null && sDb.user.userInfo != null) {
			save(context, FIRST_NAME, sDb.user.userInfo.firstName);
			save(context, LAST_NAME, sDb.user.userInfo.lastName);
			save(context, PHOTO_URL, sDb.user.userInfo.photoUrl);
			save(context, BIO, sDb.user.userInfo.bio);
			save(context, YOUFAN_TOKEN, sDb.youFanToken);
			save(context, USER_ID, sDb.userId);
		}
	}

	public static void loadUserAndYouFanTokenFromDisk(Context context) {
		String youFanToken =  get(context, YOUFAN_TOKEN, null);
		String userId = get(context, USER_ID, null);
		if (youFanToken != null && userId != null) {
			sDb.youFanToken = youFanToken;
			sDb.userId = userId;
			loadUserFromDisk(context);
		}
	}

	private static void loadUserFromDisk(Context context) {
		String firstName = get(context, FIRST_NAME, null);
		String lastName = get(context, LAST_NAME, null);
		String photoUrl = get(context, PHOTO_URL, null);
		String bio = get(context, BIO, null);

		if (firstName != null && lastName != null) {
			sDb.user = new User();
			sDb.user.userInfo = new UserInfo(photoUrl, firstName, lastName, bio);
		}
	}

	private static void save(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static void saveBitmapFile(Bitmap bitmap, String dir) {
		FileOutputStream out = null;
		File file = new File(dir, "profile.PNG");
		try {
			// PNG is a lossless format, the compression factor (100) is ignored
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			setUserPhotoFile(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (out != null) {
					out.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String get(Context context, String key, String defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, defValue);
	}

	public static EventResponse.Event getEvent() {
		return sDb.event;
	}

	public static void setEvent(EventResponse.Event event) {
		sDb.event = event;
	}

	public static File getUserPhotoFile() {
		return sDb.userPhotoFile;
	}

	public static void setUserPhotoFile(File file) {
		sDb.userPhotoFile = file;
	}
}
