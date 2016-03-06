package hoahong.facebook.messenger.ui.android;

import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.ImageBrowserPickOneAdapter;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.LargeImageViewFragment.PhotoSupporter;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Copyright (c) 2014 Pendulab Inc.
 * 111 N Chestnut St, Suite 200, Winston Salem, NC, 27101, U.S.A.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Pendulab Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Pendulab.
 * Created by Vu Trong Hoa on 8/6/2015.
 */
public class BrowseImageActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, PhotoSupporter{

    private boolean isRunning;
    public static final String TAG = BrowseImageActivity.class.getSimpleName();
    public static final String RETURN_IMAGE_URI_KEY = "BROWSE_IMAGE_URI";
    public static final String TITILE_KEY = "fragment_browse_images_title";

    private static final Uri SOURCE_URI			= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final int	CURSOR_CODE			= 123456789;

    private boolean 			showCameraPictures = false;

    // Prevent cursor to reload
    private boolean				cursorLoaded		= false;

    private ImageBrowserPickOneAdapter imageAdapter;
    private GridView imagesGridView;

    private String activityTitle;
    private List<String> listUris;
    private String seletedUri;
    private Cursor cursor;
    private int cursorCount;
    private LargeImageViewFragment largeImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_browse_images);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.color.trans_color);
        if(getIntent() != null){
            activityTitle = getIntent().getStringExtra(TITILE_KEY);
            setTitle(activityTitle);
        }

        imagesGridView = (GridView) findViewById(R.id.images_grid_view);
        if (listUris == null){
            listUris = new ArrayList<String>();
        }
        imageAdapter = new ImageBrowserPickOneAdapter(BrowseImageActivity.this, listUris);
        imagesGridView.setAdapter(imageAdapter);
        imagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                largeImageView = LargeImageViewFragment.getInstance();
                largeImageView.setParentActivity(BrowseImageActivity.this);
                largeImageView.openPhoto(BrowseImageActivity.this, position, view);
            }
        });
        imagesGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (cursor != null && !cursor.isClosed() && listUris.size() < cursorCount) {
                    if (firstVisibleItem + visibleItemCount + 6 > totalItemCount ) {
                        loadPhotos(listUris.size());
                    }
                }
            }
        });

        cursor =  null;

        getSupportLoaderManager().initLoader(CURSOR_CODE, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_browse_image_menu, menu);
        return true;
    }
    
    @Override
    public void onBackPressed() {
    	if (largeImageView != null && LargeImageViewFragment.isOpening){
			largeImageView.closePhoto();
			supportInvalidateOptionsMenu();
			return;
		}
    	super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (android.R.id.home == itemId){
        	if (largeImageView != null && LargeImageViewFragment.isOpening){
				largeImageView.closePhoto();
				supportInvalidateOptionsMenu();
				return true;
			}
            setResult(RESULT_CANCELED);
            finish();
        }
        else if (R.id.action_done == itemId) {
        	onDonePressed();
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void onDonePressed () {
    	
        String selectedUri = "";
        if (imageAdapter != null && listUris!= null && listUris.size() > 0 && imageAdapter.getSelectedPosition() >= 0)
            selectedUri = listUris.get(imageAdapter.getSelectedPosition());
        Log.e(TAG, "selected uri: " + selectedUri);
        if (!Utils.isEmpty(selectedUri)) {
        	if (largeImageView != null && LargeImageViewFragment.isOpening)
        		largeImageView.closePhoto();
            Intent returnedIntent = new Intent();
            returnedIntent.putExtra(RETURN_IMAGE_URI_KEY, selectedUri);
            setResult(RESULT_OK, returnedIntent);
            finish();
        }else {
            Toast.makeText(this, "Please select a image", Toast.LENGTH_SHORT).show();
        }
    }
    
    
    public int getSelectedPosition () {
    	if (imageAdapter != null){
    		return imageAdapter.getSelectedPosition();
    	}else return -1;
    }
    
    public void setSelectedPosition (int selectedPosition) {
    	if (imageAdapter != null)
    		imageAdapter.setSelectedPosition(selectedPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }
    
    @Override
    protected void onPause() {
    	if (largeImageView != null && largeImageView.isOpening)
    		largeImageView.closePhoto();
    	super.onPause();
    }

    public boolean isActivityRunning() {
        return isRunning;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (cursorLoaded == false) {
            final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
            String comparator = showCameraPictures == true ? " = ?" : " != ?";
            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + comparator;
            String[] selectionArgs = new String[] { "Camera" };
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            return new CursorLoader(BrowseImageActivity.this, SOURCE_URI, projection, selection, selectionArgs, orderBy);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.e(TAG, "onLoadFinished");
        setCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.e(TAG, "onLoaderReset");
    }

    public void setCursor(Cursor cursor) {
        if (cursorLoaded == false) {
            if (cursor != null && cursor.isClosed() == false) {

                this.cursor = cursor;
                this.cursorCount = cursor.getCount();

                loadPhotos(0);
            }
        }
        cursorLoaded = true;
    }

    private void loadPhotos(int position) {
        if (position < cursorCount) {
            new LoadPhotos().execute(position);
        }
        if (position == cursorCount) {
            cursor.close();
        }
    }




    // Async Task Class
    class LoadPhotos extends AsyncTask<Integer, Void, Void> {

        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Download Music File from Internet
        @Override
        protected Void doInBackground(Integer... positions) {
            int position = positions[0];
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int maxCursor = position + 12;
            if (maxCursor > cursorCount) {
                maxCursor = cursorCount;
            }
            for (int i = position; i < maxCursor; i++) {
                cursor.moveToPosition(i);

                listUris.add(cursor.getString(columnIndex));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nSelected) {
            imageAdapter.notifyDataSetChanged();
            if (largeImageView != null) {
            	largeImageView.onPhotoSupporterChanged();
            }
        }
    }




	@Override
	public int getSize() {
		return listUris == null ? 0 : listUris.size();
	}

	@Override
	public String getPhotoItem(int position) {
		return listUris.get(position);
	}

	@Override
	public void getMorePhoto() {
		if (listUris != null)
			loadPhotos(listUris.size());
		
	}

	@Override
	public int getMaxSize() {
		return cursorCount;
	}


}
