# RxBiometric: Android Fingerprint Authentication and Encryption in RxJava2

RxBiometric wraps the Android Biometric APIs (introduced in Android Marshmallow+) and makes it easy to:
- Authenticate your users with their biometrics
- Encrypt and decrypt their data with biometric authentication

RxBiometric supports AndroidX BiometricPrompt

This library has a minSdkVersion of `19`, but will only really work on API level `23` and above. Below that it will provide no functionality due to the missing APIs.

## Usage

To use RxBiometric in your project, add the library as a dependency in your `build.gradle` file:
```groovy
dependencies {
    implementation 'com.mtramin:RxBiometric:1.0.0'
}
```

Furthermore, you have to declare the Fingerprint permission in your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```

### Building and configuring an RxBiometric instance

An instance of `RxBiometric` is created by it's builder. The builder gives many options to configure
the behavior of biometric operations.

``` java
RxBiometric RxBiometric = new RxBiometric.Builder(context)
                                               .encryptionMethod(EncryptionMethod.RSA)
                                               .keyInvalidatedByBiometricEnrollment(true)
                                               .dialogTitleText("RxBiometric")
                                               .dialogSubtitleText("Subtitle")
                                               .dialogDescriptionText("Description")
                                               .dialogNegativeButtonText("Cancel")
                                               .build();
```

### Checking for availability

Before using any biometric related operations it should be verified that `RxBiometric` can be used by calling:

``` java
if (rxBiometric.isAvailable()) {
    // proceed with biometric operation
} else {
    // biometric is not available, provide other authentication options
}
```

Reasons for `RxBiometric` to report that it is not available include:
- The current device doesn't have a biometric sensor
- The user is not using the biometrics sensor of the device
- The device is running an Android version that doesn't support the Android Biometrics APIs

If you call any biometric operation on the RxBiometric object without verifying that the biometric
APIs are available to be used the resulting `Observable` will return an error callback.

### Authenticating a user with their biometric

To authenticate the user with their biometric, call the following:

``` java
Disposable disposable = RxBiometric.authenticate()
                .subscribe(biometricAuthenticationResult -> {
                    switch (biometricAuthenticationResult.getResult()) {
                        case FAILED:
                            setStatusText("Biometric not recognized, try again!");
                            break;
                        case AUTHENTICATED:
                            setStatusText("Successfully authenticated!");
                            break;
                    }
                }, throwable -> {
                    Log.e("ERROR", "authenticate", throwable);
                });
```

By subscribing to `RxBiometric.authenticate(Context)` the biometric sensor of the device will be activated.
Should the device not contain a biometric sensor or the user has not enrolled any biometrics, `onError` will be called.

By disposing the `Disposable`, the biometric sensor will be disabled again with no result.

### Encryption-and-decryption

Usage of the Encryption and decryption features of RxBiometric are very similar to simple authentication calls.

`RxBiometric` supports encryption with both the AES and RSA encryption standards. They differ in the way the user needs to interact with their biometric sensor.
For encryption and decryption the same `EncryptionMethod` should be used. Otherwise the encrypted data cannot be decrypted.

Encryption and Decryption in RxBiometric is backed by the [Android KeyStore System](https://developer.android.com/training/articles/keystore.html).

After the encryption step all results will be Base64 encoded for easier transportation and storage.
 
#### AES

When choosing AES for encryption and decryption the user will have to approve both actions by authentication with their biometric.
The encryption then relies on the [Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with a 256-bit keysize.

The usage flow for AES is as follows:
- Build an instance of RxBiometric and set the `.encryptionMethod(EncryptionMethod.AES)`
- Call `RxBiometric.encrypt("String to encrypt")` to start the encryption flow
- User authenticates by activating the biometric sensor
- Store the encrypted data returned in the `onNext` callback

- Call `RxBiometric.decrypt("Encrypted String")` to start the decryption flow
- User authenticates by activating the biometric sensor
- Receive the decrypted data in the `onNext` callback

#### RSA

[RSA](https://en.wikipedia.org/wiki/RSA_(cryptosystem)) encryption allows you to encrypt a value without any user action. The data to encrypt can be encrypted and a user won't need to authenticate oneself.
The encrypted data can only be decrypted again when the user authenticates by using the biometric sensor on their device.

The usage flow for RSA is as follows:
- Build an instance of RxBiometric and set the `.encryptionMethod(EncryptionMethod.RSA)`
- Call `RxBiometric.encrypt("String to encrypt")` to initialize the encryption flow
- Store the encrypted data returned in the `onNext` callback

- Call `RxBiometric.decrypt("Encrypted string")` to initialize the decryption flow
- User authenticates by touching the biometric sensor
- Receive the decrypted data in the `onNext` callback


#### Encrypting and decrypting values

``` java
Disposable disposable = RxBiometric.encrypt(stringToEncrypt, keyName)
                   .subscribe(encryptionResult -> {
                       switch (encryptionResult.getResult()) {
                           case FAILED:
                               setStatusText("Biometric not recognized, try again!");
                               break;
                           case AUTHENTICATED:
                               setStatusText("Successfully authenticated!");
                               break;
                       }
                   }, throwable -> {
                       Log.e("ERROR", "authenticate", throwable);
                       setStatusText(throwable.getMessage());
                   });
```

`RxBiometric.encrypt(String, String)` takes the String you want to encrypt (which might be a token, user password, or any other String) and a key name.
The given String will be encrypted with a key in the Android KeyStore and returns an encrypted String. The key used in the encryption is only accessible from your own app.
Store the encrypted String anywhere and use it later to decrypt the original value by calling:

``` java
Disposable disposable = RxBiometric.decrypt(encryptedValue, keyName)
                    .subscribe(decryptionResult -> {
                        switch (decryptionResult.getResult()) {
                            case FAILED:
                                setStatusText("Biometric not recognized, try again!");
                                break;
                            case AUTHENTICATED:
                                setStatusText("decrypted:\n" + decryptionResult.getDecrypted());
                                break;
                        }
                    }, throwable -> {
                        //noinspection StatementWithEmptyBody
                        if (RxBiometric.keyInvalidated(throwable)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the biometrics stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "decrypt", throwable);
                        setStatusText(throwable.getMessage());
                    });
```

Be aware that all encryption keys will be invalidated once the user changes their lockscreen or changes any of their enrolled biometrics. If you receive an `onError` event
during decryption check if the keys were invalidated with `RxBiometric.keyInvalidated(Throwable)` and prompt the user to encrypt their data again.

Once the encryption keys are invalidated RxBiometric will delete and renew the keys in the Android Keystore on the next call to `RxBiometric.encrypt(...)`. 

### Best-practices

To prevent errors and ensure a good user experience, make sure to think of these cases:

- Before calling any RxBiometric authentication, check if the user can use biometric authentication by calling: `RxBiometric.isAvailable(Context)` or `RxBiometric.isUnavailable(Context)`
- If keys were invalidated due to the user changing their lockscreen or enrolled biometrics provide them with a way to encrypt their data again.

## Dependencies

RxBiometric contains the following dependencies:

- RxJava2
- Android Support Annotations

## LICENSE

Copyright 2015-2018 Marvin Ramin.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
