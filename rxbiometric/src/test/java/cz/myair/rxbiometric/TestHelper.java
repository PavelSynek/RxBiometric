package cz.myair.rxbiometric;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class TestHelper {
	static void setSdkLevel(int level) throws Exception {
		Field field = Build.VERSION.class.getField("SDK_INT");

		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, level);
	}

	static void setRelease(String release) throws Exception {
		Field field = Build.VERSION.class.getField("RELEASE");

		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, release);
	}
}
