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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;

import cz.myair.rxbiometric.data.BiometricAuthenticationException;
import io.reactivex.Emitter;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Authenticates the user with their fingerprint via a {@link BiometricPrompt}.
 */
abstract class BiometricDialogObservable<T> implements ObservableOnSubscribe<T> {

	private final ActivityOrFragment activityOrFragment;
	private final BiometricDialogBundle biometricDialogBundle;

	BiometricDialogObservable(ActivityOrFragment activityOrFragment, BiometricDialogBundle biometricDialogBundle) {
		this.activityOrFragment = activityOrFragment;
		this.biometricDialogBundle = biometricDialogBundle;
	}

	@Override
	public void subscribe(ObservableEmitter<T> emitter) {
		Executor executor = new Executor() {
			@Override
			public void execute(@NonNull Runnable runnable) {
				runnable.run();
			}
		};

		String subtitleText = biometricDialogBundle.getSubtitleText() != null ? activityOrFragment.getContext().getString(biometricDialogBundle.getSubtitleText()) : null;
		String descriptionText = biometricDialogBundle.getDescriptionText() != null ? activityOrFragment.getContext().getString(biometricDialogBundle.getDescriptionText()) : null;

		BiometricPrompt.AuthenticationCallback authenticationCallback = createAuthenticationCallback(emitter);
		BiometricPrompt.CryptoObject cryptoObject = initCryptoObject(emitter);
		BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
				.setTitle(activityOrFragment.getContext().getString(biometricDialogBundle.getTitleText()))
				.setSubtitle(subtitleText)
				.setDescription(descriptionText)
				.setNegativeButtonText(activityOrFragment.getContext().getString(biometricDialogBundle.getNegativeButtonText()))
				.setConfirmationRequired(biometricDialogBundle.isConfirmationRequired())
				.build();

		BiometricPrompt biometricPrompt;
		if (activityOrFragment.hasActivity()) {
			biometricPrompt = new BiometricPrompt(activityOrFragment.getActivity(), executor, authenticationCallback);
		} else {
			biometricPrompt = new BiometricPrompt(activityOrFragment.getFragment(), executor, authenticationCallback);
		}
		if (cryptoObject == null) {
			biometricPrompt.authenticate(promptInfo);
		} else {
			biometricPrompt.authenticate(promptInfo, cryptoObject);
		}
	}

	private BiometricPrompt.AuthenticationCallback createAuthenticationCallback(final ObservableEmitter<T> emitter) {
		return new BiometricPrompt.AuthenticationCallback() {
			@Override
			public void onAuthenticationError(int errorCode, @NonNull CharSequence errorMessage) {
				if (!emitter.isDisposed()) {
					emitter.onError(new BiometricAuthenticationException(errorCode, errorMessage));
				}
			}

			@Override
			public void onAuthenticationFailed() {
				BiometricDialogObservable.this.onAuthenticationFailed(emitter);
			}

			@Override
			public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
				BiometricDialogObservable.this.onAuthenticationSucceeded(emitter, result);
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
