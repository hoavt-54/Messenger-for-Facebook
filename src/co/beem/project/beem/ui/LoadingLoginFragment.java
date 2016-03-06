package co.beem.project.beem.ui;

import co.beem.project.beem.ui.wizard.Account;
import co.beem.project.beem.ui.wizard.AccountConfigureFragment;
import co.beem.project.beem.ui.wizard.WebViewFragment;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class LoadingLoginFragment extends Fragment{
	
	
	public LoadingLoginFragment () { super();}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.loading_view, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		hideKeyboard();
		view.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				try{
				if (Utils.isConnectToInternet(getActivity()))
					((Account)getActivity()).pushFragment(AccountConfigureFragment.newInstance());
				else {
					((Account)getActivity()).pushFragment(WebViewFragment.instanciate("https://m.facebook.com/loc=bottom&refid=8"));
				}
				}catch (Exception e){ e.printStackTrace();}
			}
		}, 2000);
	}
	
	public void hideKeyboard(){
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
}
