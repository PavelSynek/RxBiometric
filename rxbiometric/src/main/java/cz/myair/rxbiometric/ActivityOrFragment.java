package cz.myair.rxbiometric;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

class ActivityOrFragment {

	private final FragmentActivity activity;
	private final Fragment fragment;

	public ActivityOrFragment(@NonNull FragmentActivity activity) {
		this.activity = activity;
		this.fragment = null;
	}

	public ActivityOrFragment(@NonNull Fragment fragment) {
		this.activity = null;
		this.fragment = fragment;
	}

	public boolean hasActivity() {
		return activity != null && fragment == null;
	}

	@SuppressWarnings("ConstantConditions")
	@NonNull
	public Context getContext() {
		if (hasActivity()) {
			return activity;
		} else {
			return fragment.requireContext();
		}
	}

	public FragmentActivity getActivity() {
		return activity;
	}

	public Fragment getFragment() {
		return fragment;
	}
}
