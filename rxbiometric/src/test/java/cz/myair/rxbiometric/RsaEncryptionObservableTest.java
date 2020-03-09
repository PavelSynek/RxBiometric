/*
 * Copyright 2017 Marvin Ramin.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;

import cz.myair.rxbiometric.data.BiometricEncryptionResult;
import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RsaEncryptionObservableTest {

	private static final String INPUT = "TEST";

	@Mock
	RsaCipherProvider cipherProvider;

	@Mock
	RxBiometricLogger logger;

	private Cipher cipher = new NullCipher();

	private Observable<BiometricEncryptionResult> observable;

	@Before
	public void setUp() throws Exception {
		observable = Observable.create(new RsaEncryptionObservable(cipherProvider, INPUT.toCharArray(), new TestEncodingProvider(), logger));
	}

	@Test
	public void cipherCreationThrows() throws Exception {
		when(cipherProvider.getCipherForEncryption()).thenThrow(SecurityException.class);

		observable.test()
				.assertNoValues()
				.assertError(SecurityException.class);
	}

	@Test
	public void encrypt() throws Exception {
		when(cipherProvider.getCipherForEncryption()).thenReturn(cipher);

		BiometricEncryptionResult biometricEncryptionResult = observable.test()
				.assertValueCount(1)
				.assertNoErrors()
				.assertComplete()
				.values().get(0);

		assertEquals(INPUT, biometricEncryptionResult.getEncrypted());
	}
}
