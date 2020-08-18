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

import javax.crypto.Cipher;

import cz.myair.rxbiometric.data.BiometricDecryptionResult;
import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

class RsaDecryptionObservable extends BiometricDialogObservable<BiometricDecryptionResult> {

	private final RsaCipherProvider cipherProvider;
	private final String encryptedString;
	private final EncodingProvider encodingProvider;
	private final RxBiometricLogger logger;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param activityOrFragment    activity or fragment wrapper
	 * @param biometricDialogBundle
	 * @param keyName               keyName to use for the decryption
	 * @param encrypted             data to encrypt  @return Observable {@link BiometricEncryptionResult}
	 * @return Observable result of the decryption
	 */
	static Observable<BiometricDecryptionResult> create(ActivityOrFragment activityOrFragment,
														BiometricDialogBundle biometricDialogBundle,
														String keyName,
														String encrypted,
														boolean keyInvalidatedByBiometricEnrollment,
														RxBiometricLogger logger) {
		try {
			return Observable.create(new RsaDecryptionObservable(
					activityOrFragment,
					biometricDialogBundle,
					new RsaCipherProvider(activityOrFragment.getContext(), keyName, keyInvalidatedByBiometricEnrollment, logger),
					encrypted,
					new Base64Provider(),
					logger));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	private RsaDecryptionObservable(ActivityOrFragment activityOrFragment,
									BiometricDialogBundle biometricDialogBundle,
									RsaCipherProvider cipherProvider,
									String encrypted,
									EncodingProvider encodingProvider,
									RxBiometricLogger logger) {
		super(activityOrFragment, biometricDialogBundle);
		this.cipherProvider = cipherProvider;
		encryptedString = encrypted;
		this.encodingProvider = encodingProvider;
		this.logger = logger;
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<BiometricDecryptionResult> subscriber) {
		try {
			Cipher cipher = cipherProvider.getCipherForDecryption();
			return new BiometricPrompt.CryptoObject(cipher);
		} catch (Exception e) {
			subscriber.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<BiometricDecryptionResult> emitter, BiometricPrompt.AuthenticationResult result) {
		try {
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] bytes = cipher.doFinal(encodingProvider.decode(encryptedString));

			emitter.onNext(new BiometricDecryptionResult(BiometricResult.AUTHENTICATED, ConversionUtils.toChars(bytes)));
			emitter.onComplete();
		} catch (Exception e) {
			logger.error("Unable to decrypt given value. RxBiometric is only able to decrypt values previously encrypted by RxBiometric with the same encryption mode.", e);
			emitter.onError(cipherProvider.mapCipherFinalOperationException(e));
		}

	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<BiometricDecryptionResult> emitter) {
		emitter.onNext(new BiometricDecryptionResult(BiometricResult.FAILED, null));
	}

	@Override
	protected boolean isCryptoObjectRequired() {
		return true;
	}
}
