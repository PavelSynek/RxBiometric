package cz.myair.rxbiometric.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiometricEncryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		String decrypted = "decrypted";
		BiometricEncryptionResult result = new BiometricEncryptionResult(BiometricResult.AUTHENTICATED, decrypted);

		assertEquals(decrypted, result.getEncrypted());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		BiometricEncryptionResult result = new BiometricEncryptionResult(BiometricResult.FAILED, null);

		result.getEncrypted();
	}
}