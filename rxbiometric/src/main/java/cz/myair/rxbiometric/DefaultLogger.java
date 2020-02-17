package cz.myair.rxbiometric;

import android.util.Log;

/**
 * Implementation of {@link RxBiometricLogger} which logs to logcat via {@link Log}
 */
class DefaultLogger implements RxBiometricLogger {
	private static final String TAG = "RxBiometric";

	@Override
	public void warn(String message) {
		Log.w(TAG, message);
	}

	@Override
	public void error(String message, Throwable throwable) {
		Log.e(TAG, message, throwable);
	}
}
