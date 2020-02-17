package cz.myair.rxbiometric.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiometricDecryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		char[] decrypted = "decrypted".toCharArray();
		BiometricDecryptionResult result = new BiometricDecryptionResult(BiometricResult.AUTHENTICATED, decrypted);

		assertEquals(decrypted, result.getDecryptedChars());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		BiometricDecryptionResult result = new BiometricDecryptionResult(BiometricResult.FAILED, null);

		result.getDecrypted();
	}
}