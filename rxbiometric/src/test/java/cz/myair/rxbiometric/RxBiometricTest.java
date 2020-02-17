package cz.myair.rxbiometric;

import android.content.Context;
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

	private RxBiometric rxBiometric;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		TestHelper.setSdkLevel(23);
		TestHelper.setRelease("Marshmallow");
		PowerMockito.mockStatic(FingerprintManager.class);
		PowerMockito.mockStatic(Log.class);

		rxBiometric = new RxBiometric.Builder(mockActivity)
				.disableLogging()
				.dialogTitleText("TITLE")
				.dialogNegativeButtonText("CANCEL")
				.build();
	}

	@Test
	public void testKeyInvalidatedException() throws Exception {
		Throwable throwable = new KeyPermanentlyInvalidatedException();
		assertTrue("Should result to true", RxBiometric.keyInvalidated(throwable));
	}

	@Test
	public void testAvailable() throws Exception {
		when(mockActivity.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

		assertTrue("RxBiometric should be available", rxBiometric.isAvailable());
		assertFalse("RxBiometric should be available", rxBiometric.isUnavailable());
	}

	@Test
	public void testUnavailableWithNoHardware() throws Exception {
		when(mockActivity.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

		assertFalse("RxBiometric should be unavailable", rxBiometric.isAvailable());
		assertTrue("RxBiometric should be unavailable", rxBiometric.isUnavailable());
	}

	@Test
	public void testUnavailableWithNoFingerprint() throws Exception {
		when(mockActivity.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

		assertFalse("RxBiometric should be unavailable", rxBiometric.isAvailable());
		assertTrue("RxBiometric should be unavailable", rxBiometric.isUnavailable());
	}

	@Test
	public void testUnavailable() throws Exception {
		when(mockActivity.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
		when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
		when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

		assertFalse("RxBiometric should be unavailable", rxBiometric.isAvailable());
		assertTrue("RxBiometric should be unavailable", rxBiometric.isUnavailable());
	}

	@Test
	public void apisUnavailable() throws Exception {
		when(mockActivity.getSystemService(Context.FINGERPRINT_SERVICE)).thenThrow(new NoClassDefFoundError());

		assertFalse("RxBiometric should be unavailable", rxBiometric.isAvailable());
	}

	@Test
	public void sdkNotSupported() throws Exception {
		TestHelper.setSdkLevel(21);
		assertFalse("RxBiometric should be unavailable", rxBiometric.isAvailable());
	}
}
