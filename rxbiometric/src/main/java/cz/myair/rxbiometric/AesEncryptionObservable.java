/*
 * Copyright 2018 Marvin Ramin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.myair.rxbiometric;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Encrypts data with biometric authentication. Initializes a {@link Cipher} for encryption which
 * can only be used with biometric authentication and uses it once authentication was successful
 * to encrypt the given data.
 */
@SuppressLint("NewApi")
// SDK check happens in {@link BiometricObservable#subscribe}
class AesEncryptionObservable extends BiometricDialogObservable<BiometricEncryptionResult> {

	private final char[] toEncrypt;
	private final EncodingProvider encodingProvider;
	private final AesCipherProvider cipherProvider;

	/**
	 * Creates a new AesEncryptionObservable that will listen to biometric authentication
	 * to encrypt the given data.
	 *
	 * @param activityOrFragment    activity or fragment wrapper
	 * @param biometricDialogBundle
	 * @param keyName               name of the key in the keystore
	 * @param toEncrypt             data to encrypt  @return Observable {@link BiometricEncryptionResult}
	 */
	static Observable<BiometricEncryptionResult> create(ActivityOrFragment activityOrFragment,
														BiometricDialogBundle biometricDialogBundle,
														String keyName,
														char[] toEncrypt,
														boolean keyInvalidatedByBiometricEnrollment,
														RxBiometricLogger logger) {
		try {
			return Observable.create(new AesEncryptionObservable(
					activityOrFragment,
					biometricDialogBundle,
					new AesCipherProvider(activityOrFragment.getContext(), keyName, keyInvalidatedByBiometricEnrollment, logger),
					toEncrypt,
					new Base64Provider()));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	private AesEncryptionObservable(ActivityOrFragment activityOrFragment,
									BiometricDialogBundle biometricDialogBundle,
									AesCipherProvider cipherProvider,
									char[] toEncrypt,
									EncodingProvider encodingProvider) {
		super(activityOrFragment, biometricDialogBundle);
		this.cipherProvider = cipherProvider;

		if (toEncrypt == null) {
			throw new NullPointerException("String to be encrypted is null. Can only encrypt valid strings");
		}
		this.toEncrypt = toEncrypt;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<BiometricEncryptionResult> emitter) {
		try {
			Cipher cipher = cipherProvider.getCipherForEncryption();
			return new BiometricPrompt.CryptoObject(cipher);
		} catch (Exception e) {
			emitter.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<BiometricEncryptionResult> emitter, BiometricPrompt.AuthenticationResult result) {
		try {
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] encryptedBytes = cipher.doFinal(ConversionUtils.toBytes(toEncrypt));
			byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

			String encryptedString = CryptoData.fromBytes(encodingProvider, encryptedBytes, ivBytes).toString();
			CryptoData.verifyCryptoDataString(encryptedString);

			emitter.onNext(new BiometricEncryptionResult(BiometricResult.AUTHENTICATED, encryptedString));
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(cipherProvider.mapCipherFinalOperationException(e));
		}
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<BiometricEncryptionResult> emitter) {
		emitter.onNext(new BiometricEncryptionResult(BiometricResult.FAILED, null));
	}
}
