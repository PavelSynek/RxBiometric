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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Authenticates the user with their fingerprint.
 */
class AuthenticationObservable extends FingerprintDialogObservable<FingerprintAuthenticationResult> {

	/**
	 * Creates an Observable that will enable the fingerprint scanner of the device and listen for
	 * the users fingerprint for authentication
	 *
	 * @param fragmentActivity activity to use
	 * @return Observable {@link FingerprintAuthenticationResult}
	 */
	static Observable<FingerprintAuthenticationResult> create(FragmentActivity fragmentActivity, FingerprintDialogBundle fingerprintDialogBundle) {
		return Observable.create(new AuthenticationObservable(fragmentActivity, fingerprintDialogBundle));
	}

	@VisibleForTesting
	AuthenticationObservable(FragmentActivity fragmentActivity, FingerprintDialogBundle fingerprintDialogBundle) {
		super(fragmentActivity, fingerprintDialogBundle);
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<FingerprintAuthenticationResult> subscriber) {
		// Simple authentication does not need CryptoObject
		return null;
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<FingerprintAuthenticationResult> emitter, BiometricPrompt.AuthenticationResult result) {
		emitter.onNext(new FingerprintAuthenticationResult(FingerprintResult.AUTHENTICATED));
		emitter.onComplete();
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<FingerprintAuthenticationResult> emitter) {
		emitter.onNext(new FingerprintAuthenticationResult(FingerprintResult.FAILED));
	}
}
