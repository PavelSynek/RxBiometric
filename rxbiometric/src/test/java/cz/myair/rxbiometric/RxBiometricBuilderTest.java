package cz.myair.rxbiometric;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link RxBiometric.Builder}
 */
@RunWith(MockitoJUnitRunner.class)
public class RxBiometricBuilderTest {

	@Mock
	FragmentActivity mockActivity;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		TestHelper.setSdkLevel(23);
		TestHelper.setRelease("Marshmallow");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withoutTitle() {
		new RxBiometric.Builder(mockActivity)
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void withoutNegativeButton() {
		new RxBiometric.Builder(mockActivity)
				.dialogTitleText(1)
				.build();
	}
}
