package cz.myair.rxbiometric;

import androidx.fragment.app.Fragment;

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
	Fragment mockFragment;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		TestHelper.setSdkLevel(23);
		TestHelper.setRelease("Marshmallow");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withoutTitle() {
		new RxBiometric.Builder(mockFragment)
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void withoutNegativeButton() {
		new RxBiometric.Builder(mockFragment)
				.dialogTitleText(1)
				.build();
	}
}
