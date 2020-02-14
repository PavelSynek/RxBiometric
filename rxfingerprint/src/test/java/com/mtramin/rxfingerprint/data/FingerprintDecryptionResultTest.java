package com.mtramin.rxfingerprint.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FingerprintDecryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		char[] decrypted = "decrypted".toCharArray();
		FingerprintDecryptionResult result = new FingerprintDecryptionResult(FingerprintResult.AUTHENTICATED, decrypted);

		assertEquals(decrypted, result.getDecryptedChars());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		FingerprintDecryptionResult result = new FingerprintDecryptionResult(FingerprintResult.FAILED, null);

		result.getDecrypted();
	}
}