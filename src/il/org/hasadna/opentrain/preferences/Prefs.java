package il.org.hasadna.opentrain.preferences;

import java.util.Calendar;
import 	java.security.SecureRandom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public final class Prefs {
	private static final String LOGTAG = Prefs.class.getName();
	private static final String PREFS_FILE = Prefs.class.getName();
	private static final String REPORTS_PREF = "reports";
	private static final String SEED_CREATED_ON = "seed_created_on";
	private static final String DAILY_SEED = "daily_seed";

	private static final int DATE_CHANGE_DELAY_HOURS = 5; // late trains will
															// count in the
															// previous day

	public final int VERSION_CODE;
	public final String VERSION_NAME;
	private Context mContext;

	public Prefs(Context context) {
		int versionCode;
		String versionName;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_ACTIVITIES);
			versionCode = pi.versionCode;
			versionName = pi.versionName;
		} catch (PackageManager.NameNotFoundException exception) {
			Log.e(LOGTAG, "getPackageInfo failed", exception);
			versionCode = 0;
			versionName = "";			
		}
		VERSION_CODE = versionCode;
		VERSION_NAME = versionName;
		mContext = context;
	}

	public void setReports(String json) {
		setStringPref(REPORTS_PREF, json);
	}

	public String getReports() {
		return getStringPref(REPORTS_PREF);
	}

	public String getDailyID() {
		// Prepare a string consisting of the year and day. This string will not
		// change at midnight, but rather in the early morning.
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, -DATE_CHANGE_DELAY_HOURS);
		String nowDate = "" + now.get(Calendar.YEAR)
				+ now.get(Calendar.DAY_OF_YEAR);
		String existingSeedDate = getStringPref(SEED_CREATED_ON);

		// Create a new seed if current one is out of date
		if (existingSeedDate == null || !existingSeedDate.equals(nowDate)) {
			
			Log.d(LOGTAG, "Creating new daily random ID...");
			SecureRandom sr = new SecureRandom();
			// take 4 bytes -> 32 bits -> over 4 billion id options 
			byte[] randomIdByteArray = new byte[4];
			sr.nextBytes(randomIdByteArray);
			String randomId = byteArrayToString(randomIdByteArray);
			
			SharedPreferences.Editor editor = getPrefs().edit();
			editor.putString(DAILY_SEED, randomId);
			editor.putString(SEED_CREATED_ON, nowDate);
			apply(editor);
			Log.d(LOGTAG, "New daily random ID is: " + randomId);
		}
		return getPrefs().getString(DAILY_SEED, "");
	}
	
	private static String byteArrayToString(byte[] ba)
	{
	  StringBuilder hex = new StringBuilder(ba.length * 2);
	  for (byte b : ba)
	    hex.append(String.format("%02x", b));
	  return hex.toString();
	}	

	private String getStringPref(String key) {
		return getPrefs().getString(key, null);
	}

	private void setStringPref(String key, String value) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString(key, value);
		apply(editor);
	}

	@SuppressLint("NewApi")
	private static void apply(SharedPreferences.Editor editor) {
		if (VERSION.SDK_INT >= 9) {
			editor.apply();
		} else if (!editor.commit()) {
			Log.e(LOGTAG, "", new IllegalStateException("commit() failed?!"));
		}
	}

	@SuppressLint("InlinedApi")
	private SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(PREFS_FILE,
				Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
	}

	public static String bytesToHex(byte[] in) {
		final StringBuilder builder = new StringBuilder();
		for (byte b : in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();

	}
}
