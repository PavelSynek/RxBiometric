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

package cz.myair.rxbiometric.data;

/**
 * Exception that gets thrown during biometric authentication if it fails and cannot be recovered.
 */
public class BiometricAuthenticationException extends Exception {

	private final int errorCode;
	private final String errorMessage;

	/**
	 * Creates exception that occurs during biometric authentication with message
	 *
	 * @param errorCode    error code of exception
	 * @param errorMessage message of exception
	 */
	public BiometricAuthenticationException(int errorCode, CharSequence errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage.toString();
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String getMessage() {
		return errorCode + " - " + errorMessage;
	}
}
