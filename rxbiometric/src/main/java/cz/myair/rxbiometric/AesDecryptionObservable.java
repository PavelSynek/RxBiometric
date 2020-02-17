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

import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import javax.crypto.Cipher;

import cz.myair.rxbiometric.data.BiometricDecryptionResult;
import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Decrypts data with biometric authentication. Initializes a {@link Cipher} for decryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 * <p/>
 * The date handed in must be previously encrypted by a {@link AesEncryptionObservable}.
 */
class AesDecryptionObservable extends BiometricDialogObservable<BiometricDecryptionResult> {

	private final AesCipherProvider cipherProvider;
	private final String encryptedString;
	private final EncodingProvider encodingProvider;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param fragmentActivity      activity to use
	 * @param biometricDialogBundle
	 * @param keyName               keyName to use for the decryption
	 * @param encrypted             data to encrypt  @return Observable {@link BiometricEncryptionResult}
	 * @return Observable result of the decryption
	 */
	static Observable<BiometricDecryptionResult> create(FragmentActivity fragmentActivity,
														BiometricDialogBundle biometricDialogBundle,
														String keyName,
														String encrypted,
														boolean keyInvalidatedByBiometricEnrollment,
														RxBiometricLogger logger) {
		try {
			return Observable.create(new AesDecryptionObservable(
					fragmentActivity,
					biometricDialogBundle,
					new AesCipherProvider(fragmentActivity, keyName, keyInvalidatedByBiometricEnrollment, logger),
					encrypted,
					new Base64Provider()));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	private AesDecryptionObservable(FragmentActivity fragmentActivity,
									BiometricDialogBundle biometricDialogBundle,
									AesCipherProvider cipherProvider,
									String encrypted,
									EncodingProvider encodingProvider) {
		super(fragmentActivity, biometricDialogBundle);
		this.cipherProvider = cipherProvider;
		encryptedString = encrypted;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<BiometricDecryptionResult> subscriber) {
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = cipherProvider.getCipherForDecryption(cryptoData.getIv());
			return new BiometricPrompt.CryptoObject(cipher);
		} catch (Exception e) {
			subscriber.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<BiometricDecryptionResult> emitter, BiometricPrompt.AuthenticationResult result) {
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] bytes = cipher.doFinal(cryptoData.getMessage());

			emitter.onNext(new BiometricDecryptionResult(BiometricResult.AUTHENTICATED, ConversionUtils.toChars(bytes)));
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(cipherProvider.mapCipherFinalOperationException(e));
		}

	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<BiometricDecryptionResult> emitter) {
		emitter.onNext(new BiometricDecryptionResult(BiometricResult.FAILED, null));
	}
}
