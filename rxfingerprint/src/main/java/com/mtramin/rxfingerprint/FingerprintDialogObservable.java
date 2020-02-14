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

package com.mtramin.rxfingerprint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;

import java.util.concurrent.Executor;

import io.reactivex.Emitter;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Authenticates the user with their fingerprint via a {@link BiometricPrompt}.
 */
abstract class FingerprintDialogObservable<T> implements ObservableOnSubscribe<T> {

	private final FragmentActivity fragmentActivity;
	private final FingerprintDialogBundle fingerprintDialogBundle;

	FingerprintDialogObservable(FragmentActivity fragmentActivity, FingerprintDialogBundle fingerprintDialogBundle) {
		this.fragmentActivity = fragmentActivity;
		this.fingerprintDialogBundle = fingerprintDialogBundle;
	}

	@Override
	public void subscribe(ObservableEmitter<T> emitter) {
		Executor executor = new Executor() {
			@Override
			public void execute(@NonNull Runnable runnable) {
				runnable.run();
			}
		};

		BiometricPrompt.AuthenticationCallback authenticationCallback = createAuthenticationCallback(emitter);
		BiometricPrompt.CryptoObject cryptoObject = initCryptoObject(emitter);
		BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
				.setTitle(fingerprintDialogBundle.getDialogTitleText())
				.setSubtitle(fingerprintDialogBundle.getDialogSubtitleText())
				.setDescription(fingerprintDialogBundle.getDialogDescriptionText())
				.setNegativeButtonText(fingerprintDialogBundle.getDialogNegativeButtonText())
				.build();

		BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, authenticationCallback);
		if (cryptoObject == null) {
			biometricPrompt.authenticate(promptInfo);
		} else {
			biometricPrompt.authenticate(promptInfo, cryptoObject);
		}
	}

	private BiometricPrompt.AuthenticationCallback createAuthenticationCallback(final ObservableEmitter<T> emitter) {
		return new BiometricPrompt.AuthenticationCallback() {
			@Override
			public void onAuthenticationError(int errMsgId, @NonNull CharSequence errString) {
				if (!emitter.isDisposed()) {
					emitter.onError(new FingerprintAuthenticationException(errString));
				}
			}

			@Override
			public void onAuthenticationFailed() {
				FingerprintDialogObservable.this.onAuthenticationFailed(emitter);
			}

			@Override
			public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
				FingerprintDialogObservable.this.onAuthenticationSucceeded(emitter, result);
			}
		};
	}

	/**
	 * Method to initialize the {@link BiometricPrompt.CryptoObject}
	 * used for the fingerprint authentication.
	 *
	 * @param subscriber current subscriber
	 * @return a {@link BiometricPrompt.CryptoObject}
	 * that is to be used in the authentication. May be {@code null}.
	 */
	@Nullable
	protected abstract BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<T> subscriber);

	/**
	 * Action to execute when fingerprint authentication was successful.
	 * Should return the needed result via the given {@link Emitter}.
	 * <p/>
	 * Should call {@link Emitter#onComplete()}.
	 *
	 * @param emitter current subscriber
	 * @param result  result of the successful fingerprint authentication
	 */
	protected abstract void onAuthenticationSucceeded(ObservableEmitter<T> emitter, BiometricPrompt.AuthenticationResult result);

	/**
	 * Action to execute when the fingerprint authentication failed.
	 * Should return the needed action to the given {@link Emitter}.
	 * <p/>
	 * Should only call {@link Emitter#onComplete()} when fingerprint authentication should be
	 * canceled due to the failed event.
	 *
	 * @param emitter current subscriber
	 */
	protected abstract void onAuthenticationFailed(ObservableEmitter<T> emitter);
}
