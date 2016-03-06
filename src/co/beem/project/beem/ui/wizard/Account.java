/*
    BEEM is a video conference application on the Android Platform.

    Copyright (C) 2009 by Frederic-Charles Barthelery,
                          Jean-Manuel Da Silva,
                          Nikita Kozlov,
                          Philippe Lago,
                          Jean Baptiste Vergely,
                          Vincent Veronis.

    This file is part of BEEM.

    BEEM is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BEEM is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BEEM.  If not, see <http://www.gnu.org/licenses/>.

    Please send bug reports with examples or suggestions to
    contact@beem-project.com or http://dev.beem-project.com/

    Epitech, hereby disclaims all copyright interest in the program "Beem"
    written by Frederic-Charles Barthelery,
               Jean-Manuel Da Silva,
               Nikita Kozlov,
               Philippe Lago,
               Jean Baptiste Vergely,
               Vincent Veronis.

    Nicolas Sadirac, November 26, 2009
    President of Epitech.

    Flavien Astraud, November 26, 2009
    Head of the EIP Laboratory.

 */
package co.beem.project.beem.ui.wizard;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import co.beem.project.beem.ui.LoadingLoginFragment;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;

/**
 * The first activity of an user friendly wizard to configure a XMPP account.
 */
public class Account extends FragmentActivity{


	private FragmentManager fragmentMgr;

	/**
	 * Constructor.
	 */
	public Account() {
	}

	/*
	 * We have 2 fragments on this activity
	 * one is to select type of config (having an account or create new one)
	 * and when user select next, the options will be handle  
	 * 
	 * */
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentMgr = getSupportFragmentManager();
		if (savedInstanceState == null) {
			if (((FbTextApplication)getApplication()).isUsingWebview){
				pushFragment(WebViewFragment.instanciate(AccountConfigureFragment.FB_WEB_URL));
				return;
			}
			FragmentTransaction t = fragmentMgr.beginTransaction();
			t.add(android.R.id.content, new LoadingLoginFragment(), "loading_fragment");
			t.commit();

		}
	}
	
	@Override
	public void onBackPressed() {
		Fragment fragment = getSupportFragmentManager()
				.findFragmentByTag(WebViewFragment.class.getName());
		if (fragment != null && fragment.isVisible()){
			((WebViewFragment)fragment).goback();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);
	    Log.e(Account.class.getName(), "onActivityResult");
	    Fragment mFragment = getSupportFragmentManager()
				.findFragmentByTag(WebViewFragment.class.getName());
	    if (mFragment != null) {
	        mFragment.onActivityResult(requestCode, resultCode, intent);
	    }
	}
	
	public void pushFragment (Fragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment, fragment.getClass().getName());
        invalidateOptionsMenu();
        ft.addToBackStack(null);
        ft.commit();
	}
	
	public void popFragment () {
		getSupportFragmentManager().popBackStack();
	}
	
	/*
	 * For ads
	 * 
	 * */
	
	public static class AdFragment extends Fragment {
		AdView mAdView;
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
	    	if (getActivity() != null) {
	    		FbTextApplication application = (FbTextApplication) getActivity().getApplication();	
	    		if (application.isUpgraged())
	    			return inflater.inflate(R.layout.fragment_transperant_ads, container, false);
	    	}
	        return inflater.inflate(R.layout.fragment_banner_ads_on_mainac, container, false);
	    }

	    @Override
	    public void onActivityCreated(Bundle bundle) {
	        super.onActivityCreated(bundle);
	        if (getActivity() != null) {
	    		FbTextApplication application = (FbTextApplication) getActivity().getApplication();	
	    		if (application.isUpgraged())
	    			return;
	    	}
	        mAdView = (AdView) getView().findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        if (mAdView != null)
	        mAdView.loadAd(adRequest);
	    }
	    
	    @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        //** Called when returning to the activity *//*
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        //** Called before the activity is destroyed *//*
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }
	    
	    
	    
	}


}
