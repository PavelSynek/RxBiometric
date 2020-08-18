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
import androidx.annotation.VisibleForTesting;
import androidx.biometric.BiometricPrompt;

import cz.myair.rxbiometric.data.BiometricAuthenticationResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Authenticates the user with their biometric.
 */
class AuthenticationObservable extends BiometricDialogObservable<BiometricAuthenticationResult> {

	/**
	 * Creates an Observable that will enable the biometric scanner of the device and listen for
	 * the users biometric for authentication
	 *
	 * @param activityOrFragment activity or fragment wrapper
	 * @return Observable {@link BiometricAuthenticationResult}
	 */
	static Observable<BiometricAuthenticationResult> create(ActivityOrFragment activityOrFragment, BiometricDialogBundle biometricDialogBundle) {
		return Observable.create(new AuthenticationObservable(activityOrFragment, biometricDialogBundle));
	}

	@VisibleForTesting
	AuthenticationObservable(ActivityOrFragment activityOrFragment, BiometricDialogBundle biometricDialogBundle) {
		super(activityOrFragment, biometricDialogBundle);
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<BiometricAuthenticationResult> subscriber) {
		// Simple authentication does not need CryptoObject
		return null;
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<BiometricAuthenticationResult> emitter, BiometricPrompt.AuthenticationResult result) {
		emitter.onNext(new BiometricAuthenticationResult(BiometricResult.AUTHENTICATED));
		emitter.onComplete();
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<BiometricAuthenticationResult> emitter) {
		emitter.onNext(new BiometricAuthenticationResult(BiometricResult.FAILED));
	}

	@Override
	protected boolean isCryptoObjectRequired() {
		return false;
	}
}
