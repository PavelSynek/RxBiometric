/*
 * Copyright 2015 Marvin Ramin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.myair.rxbiometric;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.security.keystore.KeyPermanentlyInvalidatedException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.FragmentActivity;

import org.reactivestreams.Subscriber;

import cz.myair.rxbiometric.data.BiometricAuthenticationResult;
import cz.myair.rxbiometric.data.BiometricDecryptionResult;
import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import cz.myair.rxbiometric.data.BiometricsUnavailableException;
import io.reactivex.Observable;

import static android.provider.Settings.ACTION_FINGERPRINT_ENROLL;
import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;

/**
 * Entry point for RxBiometric. Contains all the base methods you need to interact with the
 * biometric sensor of the device. Allows authentication of the user via the biometric
 * sensor of his/her device.
 * <p/>
 * To just authenticate the user with his biometrics, use {@link #authenticate()}.
 * <p/>
 * To encrypt given data and authenticate the user with his biometric,
 * call {@link #encrypt(String, char[])}
 * <p/>
 * To decrypt previously encrypted data via the {@link #encrypt(String, char[])}
 * method, call {@link #decrypt(String, String)}
 * <p/>
 * Helper methods provide information about the devices capability to handle biometric
 * authentication. For biometric authentication to be isAvailable, the device needs to contain the
 * necessary hardware (a sensor) and the user has to have enrolled at least one fingerprint or biometric.
 */
public class RxBiometric {

	private final FragmentActivity activity;
	private final boolean keyInvalidatedByBiometricEnrollment;
	private final EncryptionMethod encryptionMethod;
	private final RxBiometricLogger logger;
	private final BiometricDialogBundle biometricDialogBundle;

	private RxBiometric(FragmentActivity activity,
						boolean keyInvalidatedByBiometricEnrollment,
						EncryptionMethod encryptionMethod,
						RxBiometricLogger logger,
						BiometricDialogBundle biometricDialogBundle) {
		this.activity = activity;
		this.keyInvalidatedByBiometricEnrollment = keyInvalidatedByBiometricEnrollment;
		this.encryptionMethod = encryptionMethod;
		this.logger = logger;
		this.biometricDialogBundle = biometricDialogBundle;
	}

	/**
	 * Builder for {@link RxBiometric}
	 */
	public static class Builder {
		private final FragmentActivity activity;
		private boolean keyInvalidatedByBiometricEnrollment = true;
		private EncryptionMethod encryptionMethod = EncryptionMethod.RSA;
		private RxBiometricLogger logger = new DefaultLogger();
		@NonNull
		private String dialogTitleText;
		@Nullable
		private String dialogSubtitleText;
		@Nullable
		private String dialogDescriptionText;
		@NonNull
		private String dialogNegativeButtonText;

		/**
		 * Creates a new Builder for {@link RxBiometric}
		 */
		public Builder(@NonNull FragmentActivity activity) {
			this.activity = activity;
		}

		public Builder dialogTitleText(String dialogTitleText) {
			this.dialogTitleText = dialogTitleText;
			return this;
		}

		public Builder dialogSubtitleText(String dialogSubtitleText) {
			this.dialogSubtitleText = dialogSubtitleText;
			return this;
		}

		public Builder dialogDescriptionText(String dialogDescriptionText) {
			this.dialogDescriptionText = dialogDescriptionText;
			return this;
		}

		public Builder dialogNegativeButtonText(String dialogNegativeButtonText) {
			this.dialogNegativeButtonText = dialogNegativeButtonText;
			return this;
		}

		/**
		 * Changes behavior of encryption keys when a user changes their biometric enrollments.
		 * e.g. biometrics were added/removed from the system settings of the users device.
		 * By default the encryption keys will be invalidated and encrypted data will be
		 * inaccessible after changes to biometric enrollments have been made. By setting this to
		 * {@code false} this behavior can be overridden and keys will stay valid even when changes
		 * to biometric enrollments have been made on the users device.
		 *
		 * @param shouldInvalidate define whether keys should be invalidated when changes to the
		 *                         devices biometric enrollments have been made. Defaults to
		 *                         {@code true}.
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder keyInvalidatedByBiometricEnrollment(boolean shouldInvalidate) {
			this.keyInvalidatedByBiometricEnrollment = shouldInvalidate;
			return this;
		}

		/**
		 * Sets the {@link EncryptionMethod} that will be used for this instance of
		 * {@link RxBiometric}. AES requires user authentication for both
		 * encryption/decryption. RSA requires user authentication only for
		 * decryption. For more details see {@link EncryptionMethod}.
		 *
		 * @param encryptionMethod the encryption method to be used for all encryption/decryption
		 *                         operations of this RxBiometric instance. Defaults to
		 * @return the {@link Builder}
		 * @{link EncryptionMethod.RSA}.
		 */
		@NonNull
		public Builder encryptionMethod(@NonNull EncryptionMethod encryptionMethod) {
			this.encryptionMethod = encryptionMethod;
			return this;
		}

		/**
		 * Sets the logging implementation to be used.
		 *
		 * @param logger Logger implementation to be used. Use {@link Builder#disableLogging} to
		 *               disable all logging from RxBiometric.
		 *               Defaults to {@link DefaultLogger} which
		 *               logs to logcat via {@link android.util.Log}.
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder logger(@NonNull RxBiometricLogger logger) {
			this.logger = logger;
			return this;
		}

		/**
		 * Disables all logging for RxBiometric
		 *
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder disableLogging() {
			this.logger = new EmptyLogger();
			return this;
		}

		public RxBiometric build() {
			if (dialogTitleText == null) {
				throw new IllegalArgumentException("RxBiometric requires a dialogTitleText.");
			}

			if (dialogNegativeButtonText == null) {
				throw new IllegalArgumentException("RxBiometric requires a dialogNegativeButtonText.");
			}

			return new RxBiometric(activity,
					keyInvalidatedByBiometricEnrollment,
					encryptionMethod,
					logger,
					new BiometricDialogBundle(
							dialogTitleText,
							dialogSubtitleText,
							dialogDescriptionText,
							dialogNegativeButtonText)
			);
		}
	}

	/**
	 * Authenticate the user with his biometrics. This will enable the biometric sensor on the
	 * device and wait for the user to touch the sensor with his finger.
	 * <p/>
	 * All possible recoverable errors will be provided in {@link Subscriber#onNext(Object)} and
	 * should be handled there. Unrecoverable errors will be provided with
	 * {@link Subscriber#onError(Throwable)} calls.
	 *
	 * @return Observable {@link BiometricAuthenticationResult}. Will complete once the
	 * authentication was successful or has failed entirely.
	 */
	public Observable<BiometricAuthenticationResult> authenticate() {
		return AuthenticationObservable.create(activity, biometricDialogBundle);
	}

	/**
	 * Encrypt data and authenticate the user with his biometrics. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * If you want to provide a custom key name use {@link #encrypt(String, String)}
	 * instead.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with biometric authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link BiometricEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link BiometricEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link BiometricEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<BiometricEncryptionResult> encrypt(@NonNull String toEncrypt) {
		return encrypt(toEncrypt.toCharArray());
	}

	/**
	 * Encrypt data and authenticate the user with his biometrics. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String, String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with biometric authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link BiometricEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link BiometricEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link BiometricEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<BiometricEncryptionResult> encrypt(@Nullable String keyName, @NonNull String toEncrypt) {
		return encrypt(keyName, toEncrypt.toCharArray());
	}

	/**
	 * Encrypt data and authenticate the user with his biometrics. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * If you want to provide a custom key name use {@link #encrypt(String, char[])}
	 * instead.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with biometric authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link BiometricEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link BiometricEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link BiometricEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<BiometricEncryptionResult> encrypt(@NonNull char[] toEncrypt) {
		return encrypt(null, toEncrypt);
	}

	/**
	 * Encrypt data with the given {@link EncryptionMethod}. Depending on the given method, the
	 * biometric sensor might be enabled and waiting for the user to authenticate before the
	 * encryption step. All encrypted data can only be accessed again by calling
	 * {@link #decrypt(String, String)} with the same
	 * {@link EncryptionMethod} that was used for encryption of the given value.
	 * <p>
	 * Take more details about the encryption method and how they behave from {@link EncryptionMethod}
	 * <p>
	 * The resulting {@link BiometricEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link BiometricEncryptionResult#getEncrypted()} if the
	 * operation was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param keyName   name of the key to store in the Android {@link java.security.KeyStore}
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link BiometricEncryptionResult} that will contain the encrypted data.
	 * Will complete once the operation was successful or failed entirely.
	 */
	public Observable<BiometricEncryptionResult> encrypt(@Nullable String keyName, @NonNull char[] toEncrypt) {
		switch (encryptionMethod) {
			case AES:
				return AesEncryptionObservable.create(activity, biometricDialogBundle, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
			case RSA:
				// RSA encryption implementation does not depend on biometric authentication!
				if (isAvailable(activity)) {
					return RsaEncryptionObservable.create(activity, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
				} else {
					return Observable.error(new BiometricsUnavailableException("Biometric authentication is not available on this device! Ensure that the device has a biometric sensor and enrolled biometrics by calling RxBiometric#isAvailable(Context) first"));
				}
			default:
				return Observable.error(new IllegalArgumentException("Unknown encryption method: " + encryptionMethod));
		}
	}

	/**
	 * Decrypt data previously encrypted with {@link #encrypt(String)}.
	 * <p/>
	 * The encrypted string should be exactly the one you previously received as a result of the
	 * {@link #encrypt(String)} method.
	 * <p/>
	 * The resulting {@link BiometricDecryptionResult} will contain the decrypted string as a
	 * String and is accessible via {@link BiometricDecryptionResult#getDecrypted()} if the
	 * authentication and decryption was successful.
	 *
	 * @param encrypted String of encrypted data previously encrypted with
	 *                  {@link #encrypt(String, char[])}.
	 * @return Observable {@link BiometricDecryptionResult} that will contain the decrypted data.
	 * Will complete once the authentication and decryption were
	 * successful or have failed entirely.
	 * decrypted string if decryption was successful.
	 */
	public Observable<BiometricDecryptionResult> decrypt(@NonNull String encrypted) {
		return decrypt(null, encrypted);
	}

	/**
	 * Decrypt data previously encrypted with {@link #encrypt(String, char[])}.
	 * Make sure the {@link EncryptionMethod} matches to one that was used for encryption of this value.
	 * To decrypt, you have to provide the same keyName that you used for encryption.
	 * <p/>
	 * The encrypted string should be exactly the one you previously received as a result of the
	 * {@link #encrypt(String, char[])} method.
	 * <p/>
	 * The resulting {@link BiometricDecryptionResult} will contain the decrypted string as a
	 * String and is accessible via {@link BiometricDecryptionResult#getDecrypted()} if the
	 * authentication and decryption was successful.
	 * <p>
	 * This operation will require the user to authenticate with their biometric.
	 *
	 * @param keyName   name of the key in the keystore to use
	 * @param toDecrypt String of encrypted data previously encrypted with
	 *                  {@link #encrypt(String, char[])}.
	 * @return Observable  {@link BiometricDecryptionResult} that will contain the decrypted data.
	 * Will complete once the authentication and decryption were successful or
	 * have failed entirely.
	 */
	public Observable<BiometricDecryptionResult> decrypt(@Nullable String keyName, @NonNull String toDecrypt) {
		switch (encryptionMethod) {
			case AES:
				return AesDecryptionObservable.create(activity, biometricDialogBundle, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
			case RSA:
				return RsaDecryptionObservable.create(activity, biometricDialogBundle, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
			default:
				return Observable.error(new IllegalArgumentException("Unknown decryption method: " + encryptionMethod));
		}
	}

	/**
	 * Provides information if biometric authentication is currently available.
	 * <p/>
	 * The device needs to have a biometric hardware and the user needs to have enrolled
	 * at least one biometrics in the system.
	 *
	 * @return {@code true} if biometric authentication is isAvailable
	 */
	public static boolean isAvailable(Context context) {
		return BiometricManager.from(context).canAuthenticate() == BIOMETRIC_SUCCESS;
	}

	/**
	 * Provides information if biometric authentication is unavailable.
	 * <p/>
	 * The device needs to have a biometric hardware and the user needs to have enrolled
	 * at least one biometrics in the system.
	 *
	 * @return {@code true} if biometric authentication is unavailable
	 */
	public static boolean isUnavailable(Context context) {
		return !isAvailable(context);
	}

	@RequiresApi(api = Build.VERSION_CODES.P)
	public void launchBiometricEnrollment() {
		activity.startActivity(new Intent(ACTION_FINGERPRINT_ENROLL));
	}

	/**
	 * Checks if the provided {@link Throwable} is of type {@link KeyPermanentlyInvalidatedException}
	 * <p/>
	 * This would mean that the user has disabled the lock screen on his device or changed the
	 * fingerprints stored on the device for authentication.
	 * <p/>
	 * If the user does this all keys encrypted by {@link RxBiometric} become permanently
	 * invalidated by the Android system. To continue using encryption you have to ask the user to
	 * encrypt the original data again. The old data is not accessible anymore.
	 *
	 * @param throwable Throwable received in {@link Subscriber#onError(Throwable)} from
	 *                  an {@link RxBiometric} encryption method
	 * @return {@code true} if the requested key was permanently invalidated and cannot be used
	 * anymore
	 */
	@RequiresApi(23)
	public static boolean keyInvalidated(Throwable throwable) {
		return throwable instanceof KeyPermanentlyInvalidatedException;
	}
}
