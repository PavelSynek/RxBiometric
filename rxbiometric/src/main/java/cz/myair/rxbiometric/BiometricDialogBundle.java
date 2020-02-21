/*
 * Copyright 2018 Marvin Ramin.
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

import androidx.annotation.Nullable;

class BiometricDialogBundle {

	private final int titleText;
	@Nullable
	private final Integer subtitleText;
	@Nullable
	private final Integer descriptionText;
	private final int negativeButtonText;
	private final boolean confirmationRequired;

	BiometricDialogBundle(int titleText,
						  @Nullable Integer subtitleText,
						  @Nullable Integer descriptionText,
						  int negativeButtonText,
						  boolean confirmationRequired) {
		this.titleText = titleText;
		this.subtitleText = subtitleText;
		this.descriptionText = descriptionText;
		this.negativeButtonText = negativeButtonText;
		this.confirmationRequired = confirmationRequired;
	}

	public int getTitleText() {
		return titleText;
	}

	@Nullable
	public Integer getSubtitleText() {
		return subtitleText;
	}

	@Nullable
	public Integer getDescriptionText() {
		return descriptionText;
	}

	public int getNegativeButtonText() {
		return negativeButtonText;
	}

	public boolean isConfirmationRequired() {
		return confirmationRequired;
	}
}
