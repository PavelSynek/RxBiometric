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

package com.mtramin.rxfingerprint.data;

/**
 * Result of a fingerprint based authentication.
 */
public class FingerprintAuthenticationResult {

	private final FingerprintResult result;

	/**
	 * Default constructor
	 *
	 * @param result result of the fingerprint authentication
	 */
	public FingerprintAuthenticationResult(FingerprintResult result) {
		this.result = result;
	}

	/**
	 * @return result of fingerprint authentication operation
	 */
	public FingerprintResult getResult() {
		return result;
	}

	/**
	 * @return {@code true} if authentication was successful
	 */
	public boolean isSuccess() {
		return result == FingerprintResult.AUTHENTICATED;
	}

	@Override
	public String toString() {
		return "FingerprintResult {" + "result=" + result.name() + "}";
	}
}
