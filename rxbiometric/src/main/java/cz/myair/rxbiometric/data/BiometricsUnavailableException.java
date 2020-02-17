package cz.myair.rxbiometric.data;

import cz.myair.rxbiometric.RxBiometric;

/**
 * Exception thrown when biometric operations are invoked even though the current device doesn't
 * support it.
 * <p>
 * This can be the case if:
 * - The device is on SDK <23 (pre-Marshmallow)
 * - The device doesn't have a biometric sensor
 * - The user doesn't have any biometrics set up
 * - The current application doesn't specify the {@link android.permission.USE_FINGERPRINT}
 * permission
 * <p>
 * To avoid receiving this exception after calling RxBiometric operations prefer calling
 * {@link RxBiometric#isAvailable()} (Context)} to verify biometric operations are available.
 */
public class BiometricsUnavailableException extends Exception {

	public BiometricsUnavailableException(String s) {
		super(s);
	}
}
