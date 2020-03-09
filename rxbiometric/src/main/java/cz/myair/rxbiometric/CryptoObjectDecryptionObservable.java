package cz.myair.rxbiometric;

import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import cz.myair.rxbiometric.data.BiometricCryptoObjectDecryptionResult;
import cz.myair.rxbiometric.data.BiometricResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Decrypts given {@link androidx.biometric.BiometricPrompt.CryptoObject} with biometric authentication.
 */
class CryptoObjectDecryptionObservable extends BiometricDialogObservable<BiometricCryptoObjectDecryptionResult> {

	private final BiometricPrompt.CryptoObject cryptoObject;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param fragmentActivity      activity to use
	 * @param biometricDialogBundle bundle containing dialog texts
	 * @param cryptoObject          crypto object to decrypt
	 * @return Observable result of the decryption
	 */
	static Observable<BiometricCryptoObjectDecryptionResult> create(FragmentActivity fragmentActivity,
																	BiometricDialogBundle biometricDialogBundle,
																	BiometricPrompt.CryptoObject cryptoObject) {
		return Observable.create(new CryptoObjectDecryptionObservable(fragmentActivity, biometricDialogBundle, cryptoObject));
	}

	private CryptoObjectDecryptionObservable(FragmentActivity fragmentActivity,
											 BiometricDialogBundle biometricDialogBundle,
											 BiometricPrompt.CryptoObject cryptoObject) {
		super(fragmentActivity, biometricDialogBundle);
		this.cryptoObject = cryptoObject;
	}

	@Nullable
	@Override
	protected BiometricPrompt.CryptoObject initCryptoObject(ObservableEmitter<BiometricCryptoObjectDecryptionResult> subscriber) {
		return cryptoObject;
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<BiometricCryptoObjectDecryptionResult> emitter, BiometricPrompt.AuthenticationResult result) {
		try {
			emitter.onNext(new BiometricCryptoObjectDecryptionResult(BiometricResult.AUTHENTICATED, result.getCryptoObject()));
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(e);
		}
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<BiometricCryptoObjectDecryptionResult> emitter) {
		emitter.onNext(new BiometricCryptoObjectDecryptionResult(BiometricResult.FAILED, null));
	}
}
