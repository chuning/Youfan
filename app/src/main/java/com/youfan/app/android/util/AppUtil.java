package com.youfan.app.android.util;


import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.youfan.app.android.Db;

public class AppUtil {
	public static boolean isLogin() {
		return Db.getYouFanToken() != null && Db.getUser() != null && Db.getUserId() != null;
	}

	public static void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static DateTime parseToDateTime(String dateyyyyMMddTHHmmss) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		DateTime dateTime = DateTime.parse(dateyyyyMMddTHHmmss, fmt).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));
		return dateTime;
	}

	public static String dateTimeToMMMd(DateTime date) {
		return date.toString("MMM d");
	}

	public static String dateTimeTohhmmaE(DateTime date) {
		return date.toString("h:mm a, E");
	}

	public static String dateTimeToMMMdhhmmaE(DateTime date) {
		return date.toString("MMM d, h:mm a, E");
	}

	public static String dateTimeToMMMdhhmma(DateTime date) {
		return date.toString("MMM d h:mm a");
	}

}
