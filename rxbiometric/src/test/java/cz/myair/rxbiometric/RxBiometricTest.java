package cz.myair.rxbiometric;

import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for various helper methods included in {@link RxBiometric}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FingerprintManager.class)
public class RxBiometricTest {

	@Mock
	FragmentActivity mockActivity;
	@Mock
	FingerprintManager mockFingerprintManager;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		TestHelper.setSdkLevel(23);
		TestHelper.setRelease("Marshmallow");
		PowerMockito.mockStatic(FingerprintManager.class);
		PowerMockito.mockStatic(Log.class);
	}

	@Test
	public void testKeyInvalidatedException() throws Exception {
		Throwable throwable = new KeyPermanentlyInvalidatedException();
		assertTrue("Should result to true", RxBiometric.keyInvalidated(throwable));
	}

	@Test
	public void testAvailable() throws Exception {
		when(mockActivity.getSystemService(FingerprintManager.class)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

		assertTrue("RxBiometric should be available", RxBiometric.isAvailable(mockActivity));
		assertFalse("RxBiometric should be available", RxBiometric.isUnavailable(mockActivity));
	}

	@Test
	public void testUnavailableWithNoHardware() throws Exception {
		when(mockActivity.getSystemService(FingerprintManager.class)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

		assertFalse("RxBiometric should be unavailable", RxBiometric.isAvailable(mockActivity));
		assertTrue("RxBiometric should be unavailable", RxBiometric.isUnavailable(mockActivity));
	}

	@Test
	public void testUnavailableWithNoFingerprint() throws Exception {
		when(mockActivity.getSystemService(FingerprintManager.class)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

		assertFalse("RxBiometric should be unavailable", RxBiometric.isAvailable(mockActivity));
		assertTrue("RxBiometric should be unavailable", RxBiometric.isUnavailable(mockActivity));
	}

	@Test
	public void testUnavailable() throws Exception {
		when(mockActivity.getSystemService(FingerprintManager.class)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

		assertFalse("RxBiometric should be unavailable", RxBiometric.isAvailable(mockActivity));
		assertTrue("RxBiometric should be unavailable", RxBiometric.isUnavailable(mockActivity));
	}

	@Test
	public void sdkNotSupported() throws Exception {
		TestHelper.setSdkLevel(21);
		assertFalse("RxBiometric should be unavailable", RxBiometric.isAvailable(mockActivity));
	}
}
