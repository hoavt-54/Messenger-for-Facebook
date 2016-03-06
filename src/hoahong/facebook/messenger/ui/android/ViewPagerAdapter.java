package hoahong.facebook.messenger.ui.android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
	SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs = 2; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
 
 
    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, int mNumbOfTabsumb) {
        super(fm);
 
        this.Titles = new String [] {"Friends","Chats"};
        this.NumbOfTabs = mNumbOfTabsumb;
 
    }
 
    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
 
        if(position == 0) // if the position is 0 we are returning the First tab
        {
            UserFragment userFragment = UserFragment.newInstance();
            registeredFragments.put(position, userFragment);
            return userFragment;
        }
        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            ChatFragment chatFragment = ChatFragment.newInstance();
            registeredFragments.put(position, chatFragment);
            return chatFragment;
        }
 
 
    }
 
    // This method return the titles for the Tabs in the Tab Strip
 
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }
 
    // This method return the Number of tabs for the tabs Strip
 
    @Override
    public int getCount() {
        return NumbOfTabs;
    }
    
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}