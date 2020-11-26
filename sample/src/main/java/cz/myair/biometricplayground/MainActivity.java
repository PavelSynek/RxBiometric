/*
 * Copyright 2015 Marvin Ramin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.myair.biometricplayground;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cz.myair.rxbiometric.EncryptionMethod;
import cz.myair.rxbiometric.RxBiometric;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

/**
 * Shows example usage of RxBiometric
 */
public class MainActivity extends AppCompatActivity {

	private TextView statusText;
	private EditText input;
	private ViewGroup layout;
	private int key;

	private Disposable biometric = Disposables.empty();

	private RxBiometric rxBiometric;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		rxBiometric = new RxBiometric.Builder(this)
				.encryptionMethod(EncryptionMethod.RSA)
				.keyInvalidatedByBiometricEnrollment(true)
				.confirmationRequired(true)
				.dialogTitleText(R.string.titleText)
				.dialogSubtitleText(R.string.subtitleText)
				.dialogDescriptionText(R.string.descriptionText)
				.dialogNegativeButtonText(R.string.negativeButtonText)
				.build();

		this.statusText = findViewById(R.id.status);

		findViewById(R.id.authenticate).setOnClickListener(v -> authenticate());
		findViewById(R.id.encrypt).setOnClickListener(v -> encrypt());

		input = findViewById(R.id.input);
		layout = findViewById(R.id.layout);
	}

	@Override
	protected void onStop() {
		super.onStop();

		biometric.dispose();
	}

	private void setStatusText(String text) {
		statusText.setText(text);
	}

	private void setStatusText() {
		if (!RxBiometric.isAvailable(this)) {
			setStatusText("Biometrics not available");
			return;
		}

		setStatusText("Touch the sensor!");
	}

	private void authenticate() {
		setStatusText();

		if (RxBiometric.isUnavailable(this)) {
			return;
		}

		biometric = rxBiometric.authenticate()
				.subscribeOn(AndroidSchedulers.mainThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(biometricAuthenticationResult -> {
					switch (biometricAuthenticationResult.getResult()) {
						case FAILED:
							setStatusText("Biometrics not recognized, try again!");
							break;
						case AUTHENTICATED:
							setStatusText("Successfully authenticated!");
							break;
					}
				}, throwable -> {
					Log.e("ERROR", "authenticate", throwable);
					setStatusText(throwable.getMessage());
				});
	}

	@SuppressLint("NewApi")
	private void encrypt() {
		setStatusText();

		if (RxBiometric.isUnavailable(this)) {
			setStatusText("rxBiometric unavailable");
			return;
		}

		String toEncrypt = input.getText().toString();
		if (TextUtils.isEmpty(toEncrypt)) {
			setStatusText("Please enter a text to encrypt first");
			return;
		}

		biometric = rxBiometric.encrypt(String.valueOf(key), toEncrypt)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(biometricEncryptionResult -> {
					switch (biometricEncryptionResult.getResult()) {
						case FAILED:
							setStatusText("Biometrics not recognized, try again!");
							break;
						case AUTHENTICATED:
							String encrypted = biometricEncryptionResult.getEncrypted();
							setStatusText("encryption successful");
							createDecryptionButton(encrypted);
							key++;
							break;
					}
				}, throwable -> {
					//noinspection StatementWithEmptyBody
					if (RxBiometric.keyInvalidated(throwable)) {
						// The keys you wanted to use are invalidated because the user has turned off his
						// secure lock screen or changed the biometric credentials stored on the device
						// You have to re-encrypt the data to access it
					}
					Log.e("ERROR", "encrypt", throwable);
					setStatusText(throwable.getMessage());
				});
	}

	@SuppressLint("NewApi")
	private void decrypt(String key, String encrypted) {
		setStatusText();

		if (!RxBiometric.isAvailable(this)) {
			return;
		}

		biometric = rxBiometric.decrypt(key, encrypted)
				.subscribeOn(AndroidSchedulers.mainThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(biometricDecryptionResult -> {
					switch (biometricDecryptionResult.getResult()) {
						case FAILED:
							setStatusText("Biometrics not recognized, try again!");
							break;
						case AUTHENTICATED:
							setStatusText("decrypted:\n" + biometricDecryptionResult.getDecrypted());
							break;
					}
				}, throwable -> {
					//noinspection StatementWithEmptyBody
					if (RxBiometric.keyInvalidated(throwable)) {
						// The keys you wanted to use are invalidated because the user has turned off his
						// secure lock screen or changed the biometric credentials stored on the device
						// You have to re-encrypt the data to access it
					}
					Log.e("ERROR", "decrypt", throwable);
					setStatusText(throwable.getMessage());
				});
	}

	private void createDecryptionButton(final String encrypted) {
		Button button = new Button(this);
		button.setText(String.format("decrypt %d", key));
		button.setTag(new EncryptedData(key, encrypted));
		button.setOnClickListener(v -> {
			EncryptedData encryptedData = (EncryptedData) v.getTag();
			decrypt(encryptedData.key, encryptedData.encrypted);
		});
		layout.addView(button);
	}

	private static class EncryptedData {
		final String key;
		final String encrypted;

		EncryptedData(int key, String encrypted) {
			this.key = String.valueOf(key);
			this.encrypted = encrypted;
		}
	}

}
