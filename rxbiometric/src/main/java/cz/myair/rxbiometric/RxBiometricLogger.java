package cz.myair.rxbiometric;

/**
 * Logger for RxBiometric to support custom logging behavior
 */
public interface RxBiometricLogger {

	/**
	 * Log a warning message
	 *
	 * @param message
	 */
	void warn(String message);

	/**
	 * Log an error message
	 *
	 * @param message
	 */
	void error(String message, Throwable throwable);
}
