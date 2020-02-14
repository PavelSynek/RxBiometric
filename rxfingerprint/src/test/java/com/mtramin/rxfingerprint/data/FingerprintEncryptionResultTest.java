package com.mtramin.rxfingerprint.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FingerprintEncryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		String decrypted = "decrypted";
		FingerprintEncryptionResult result = new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, decrypted);

		assertEquals(decrypted, result.getEncrypted());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		FingerprintEncryptionResult result = new FingerprintEncryptionResult(FingerprintResult.FAILED, null);

		result.getEncrypted();
	}
}