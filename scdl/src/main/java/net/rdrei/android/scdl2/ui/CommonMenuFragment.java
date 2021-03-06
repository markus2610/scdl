package net.rdrei.android.scdl2.ui;

import net.rdrei.android.scdl2.Config;
import net.rdrei.android.scdl2.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CommonMenuFragment extends Fragment {
	public static CommonMenuFragment newInstance() {
		final CommonMenuFragment fragment = new CommonMenuFragment();

		return fragment;
	}

	public static void injectMenu(final FragmentActivity mActivity) {
		final CommonMenuFragment menuFragment = CommonMenuFragment
				.newInstance();

		mActivity.getSupportFragmentManager().beginTransaction()
				.add(menuFragment, "MENU").commit();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.main, menu);

		if (Config.PAID_BUILD) {
			removeAdfreeItem(menu);
		}
	}

	private void removeAdfreeItem(final Menu menu) {
		MenuItem menuItem = menu.findItem(R.id.buy_adfree);
		if (menuItem != null) {
			menuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.preferences) {
			final Intent intent = new Intent(this.getActivity(),
					ApplicationPreferencesActivity.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.changelog) {
			ChangelogDialog.showDialogForActivity(getActivity());
			return true;
		} else if (item.getItemId() == R.id.buy_adfree) {
			final Intent intent = new Intent(this.getActivity(),
					BuyAdFreeActivity.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}