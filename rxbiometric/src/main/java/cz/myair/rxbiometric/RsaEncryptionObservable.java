/*
 * Copyright 2017 Marvin Ramin.
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

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import javax.crypto.Cipher;

import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

class RsaEncryptionObservable implements ObservableOnSubscribe<BiometricEncryptionResult> {

	private final RsaCipherProvider cipherProvider;
	private final char[] toEncrypt;
	private final EncodingProvider encodingProvider;
	private final RxBiometricLogger logger;

	/**
	 * Creates a new RsaEncryptionObservable that will listen to biometric authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   name of the key in the keystore
	 * @param toEncrypt data to encrypt  @return Observable {@link BiometricEncryptionResult}
	 */
	static Observable<BiometricEncryptionResult> create(Context context, String keyName, char[] toEncrypt, boolean keyInvalidatedByBiometricEnrollment, RxBiometricLogger logger) {
		if (toEncrypt == null) {
			return Observable.error(new IllegalArgumentException("String to be encrypted is null. Can only encrypt valid strings"));
		}
		try {
			return Observable.create(new RsaEncryptionObservable(
					new RsaCipherProvider(context, keyName, keyInvalidatedByBiometricEnrollment, logger),
					toEncrypt,
					new Base64Provider(),
					logger));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	@VisibleForTesting
	RsaEncryptionObservable(RsaCipherProvider cipherProvider,
							char[] toEncrypt,
							EncodingProvider encodingProvider,
							RxBiometricLogger logger) {
		this.cipherProvider = cipherProvider;
		this.toEncrypt = toEncrypt;
		this.encodingProvider = encodingProvider;
		this.logger = logger;
	}

	@Override
	public void subscribe(ObservableEmitter<BiometricEncryptionResult> emitter) {
		try {
			Cipher cipher = cipherProvider.getCipherForEncryption();
			byte[] encryptedBytes = cipher.doFinal(ConversionUtils.toBytes(toEncrypt));

			String encryptedString = encodingProvider.encode(encryptedBytes);
			emitter.onNext(new BiometricEncryptionResult(BiometricResult.AUTHENTICATED, encryptedString));
			emitter.onComplete();
		} catch (Exception e) {
			logger.error(String.format("Error writing value for key: %s", cipherProvider.keyName), e);
			emitter.onError(e);
		}
	}
}
