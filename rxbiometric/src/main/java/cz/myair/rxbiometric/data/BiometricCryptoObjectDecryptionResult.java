package cz.myair.rxbiometric.data;

import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

/**
 * Result of a decryption operation with biometric authentication.
 */
public class BiometricCryptoObjectDecryptionResult extends BiometricAuthenticationResult {

	private final BiometricPrompt.CryptoObject cryptoObject;

	/**
	 * Default constructor
	 *
	 * @param result       result of the biometric authentication
	 * @param cryptoObject unlocked crypto object
	 */
	public BiometricCryptoObjectDecryptionResult(BiometricResult result, @Nullable BiometricPrompt.CryptoObject cryptoObject) {
		super(result);
		this.cryptoObject = cryptoObject;
	}

	/**
	 * @return unlocked crypto object
	 */
	public BiometricPrompt.CryptoObject getCryptoObject() {
		return cryptoObject;
	}
}