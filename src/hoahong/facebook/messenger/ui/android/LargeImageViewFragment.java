package hoahong.facebook.messenger.ui.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.CustomViewPager;
import hoahong.facebook.messenger.ui.Utils;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnZoomImageListener;

public class LargeImageViewFragment extends Fragment implements android.view.GestureDetector.OnDoubleTapListener, OnGestureListener, OnZoomImageListener{
	
	private CustomViewPager viewPager;
	private Activity parentActivity;
	private RelativeLayout containerView;
	private android.view.GestureDetector gestureDetector;
	private PhotoViewerAdapter photoAdapter;
	public static boolean isOpening;
	public static int currentPosition;
	public boolean isSelectMode;
	public static Class<? extends Fragment> currentClass;
	public LargeImageViewFragment () {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	


	public interface PhotoSupporter {
		public int getSize ();
		public String getPhotoItem (int position);
		public void getMorePhoto ();
		public int getMaxSize ();
	}
	
    private static volatile LargeImageViewFragment Instance = null;
    public static LargeImageViewFragment getInstance() {

    	LargeImageViewFragment localInstance = Instance;
        if (localInstance == null) {
            synchronized (LargeImageViewFragment.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new LargeImageViewFragment();
                }
            }
        }
        return localInstance;
    }
	
    
    

    @SuppressLint({ "NewApi", "InflateParams", "ClickableViewAccessibility" }) 
    public void setParentActivity(Activity activity) {
        if (parentActivity == activity) {
            return;
        }
        parentActivity = activity;
        if (parentActivity != null && parentActivity instanceof BrowseImageActivity)
        	isSelectMode = true;
        else 
        	isSelectMode = false;
        containerView = (RelativeLayout) LayoutInflater.from(parentActivity)
        				.inflate(R.layout.fragment_photo_viewer, null);
  
    	
        gestureDetector = new android.view.GestureDetector(containerView.getContext(), LargeImageViewFragment.this);
        gestureDetector.setOnDoubleTapListener(LargeImageViewFragment.this);
        
        viewPager = (CustomViewPager) containerView.findViewById(R.id.photo_viewer_view_pager);
        viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onTouchEvent(event);
				return false;
			}
		});
    }
	
	
    
    
    
    public void openPhoto (final PhotoSupporter photoSupporter, int position, View view){
    	isOpening = true;
    	try{
    		((FrameLayout)parentActivity.getWindow().getDecorView()
				.findViewById(android.R.id.content)).addView(containerView);
    	}catch (Exception e) {
    		((FrameLayout)parentActivity.getWindow().getDecorView()
    				.findViewById(android.R.id.content)).removeView(containerView);
    		((FrameLayout)parentActivity.getWindow().getDecorView()
    				.findViewById(android.R.id.content)).addView(containerView);
    	}
    	
    	photoAdapter = new PhotoViewerAdapter(((FragmentActivity) parentActivity).getSupportFragmentManager(), 
    			photoSupporter, LargeImageViewFragment.this, LargeImageViewFragment.this, isSelectMode);
    	viewPager.setAdapter(photoAdapter);
    	viewPager.addOnPageChangeListener (new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(final int position) {
				LargeImageViewFragment.currentPosition = position;
				if (photoSupporter != null && position + 6 > photoSupporter.getSize())
					photoSupporter.getMorePhoto();
				if (parentActivity != null) {
					((ActionBarActivity)parentActivity).getSupportActionBar()
					.setSubtitle((position + 1) + " of " + photoSupporter.getMaxSize());
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});

    	viewPager.setCurrentItem(position);
    }
    
    
    public void closePhoto () {
    	isOpening = false;
    	currentPosition = 0;
    	if (viewPager != null)
    	viewPager.setPagingEnabled(true);
    	viewPager.setAdapter(null);
		((FrameLayout)parentActivity.getWindow().getDecorView()
				.findViewById(android.R.id.content)).removeView(containerView);
		if (isSelectMode) {
			((BrowseImageActivity)parentActivity).getSupportActionBar().setSubtitle("");
		}
    }
    
    
	
    
    public void onPhotoSupporterChanged () {
    	if (photoAdapter != null && isOpening) 
    		photoAdapter.notifyDataSetChanged();
    }
	
    private boolean onTouchEvent(MotionEvent ev) {
    	 	gestureDetector.onTouchEvent(ev);
    	return false;
    }
	
    
    
	
	
    
    
    

	/**
	 * Larger PhotoView
	 * 
	 * 
	 * **/

	public static class PhotoViewerAdapter extends FragmentStatePagerAdapter {
		
		private PhotoSupporter photoSupporter;
		private OnDoubleTapListener tapListener;
		private OnZoomImageListener mZoomingListener;
		private boolean isSinglePhoto;
		private boolean isSelectionMode;

		public PhotoViewerAdapter(FragmentManager fm, PhotoSupporter photoSupporter, 
				OnDoubleTapListener tapListener, OnZoomImageListener mZoomingListener, boolean isSelectionMode) {
			super(fm);
			this.photoSupporter = photoSupporter;
			this.tapListener = tapListener;
			this.mZoomingListener = mZoomingListener;
			this.isSelectionMode = isSelectionMode;
		}

		public boolean isSinglePhoto() {
			return isSinglePhoto;
		}

		public void setSinglePhoto(boolean isSinglePhoto) {
			this.isSinglePhoto = isSinglePhoto;
		}

		@Override
		public PhotoViewFragment getItem(int position) {
			return PhotoViewFragment.newInstance(photoSupporter.getPhotoItem(position), position, tapListener, mZoomingListener, isSelectionMode);
		}

		@Override
		public int getCount() {
			return photoSupporter.getSize();
		}

	}
    
    
    
    
	
	

	
	
	public static class PhotoViewFragment extends Fragment {
		private int position;
		private String imagePath;
		private String isSinglePhoto;
		private static OnDoubleTapListener tapListener;
		private static OnZoomImageListener mZoomingListener;
		boolean isSelectionMode;
		public PhotoViewFragment() {

		}
		

		public static PhotoViewFragment newInstance(String imagePath, int position, OnDoubleTapListener tapListener,
				OnZoomImageListener mZoomingListener, boolean isSelectionMode) {
			PhotoViewFragment previewFragment = new PhotoViewFragment();
			if (PhotoViewFragment.tapListener == null)
				PhotoViewFragment.tapListener = tapListener;
			if (PhotoViewFragment.mZoomingListener == null)
				PhotoViewFragment.mZoomingListener = mZoomingListener;
			// put data
			Bundle bundle = new Bundle();
			bundle.putString("IMAGE_PATH",
					imagePath);
			bundle.putInt("POSITION", position);
			bundle.putBoolean("SELECTION_MODE", isSelectionMode);
			previewFragment.setArguments(bundle);
			return previewFragment;

		}

		public String getIsSinglePhoto() {
			return isSinglePhoto;
		}


		public void setIsSinglePhoto(String isSinglePhoto) {
			this.isSinglePhoto = isSinglePhoto;
		}

		
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			if (!isSelectionMode){
				inflater.inflate(R.menu.photo_viewer_menu, menu);
				if (imagePath.startsWith("http"))
					menu.findItem(R.id.action_download).setVisible(true);
				else
					menu.findItem(R.id.action_download).setVisible(false);
			}else {
				inflater.inflate(R.menu.activity_browse_image_menu, menu);
			}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int itemId = item.getItemId();
			if (R.id.action_download == itemId) {
				if (imagePath.startsWith("http")) {
					String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
							Locale.US).format(new Date());
					String fileName = "IMG_" + timeStamp + ".jpg";
					Utils.downloadFileWithDownloadManager(getActivity(),
							imagePath, "Downloading Facebook photo",
							getActivity().getString(R.string.app_name),
							fileName);

				}
				return true;
			}
			if (R.id.action_done == itemId){
				((BrowseImageActivity)getActivity()).onDonePressed();
			}
			return super.onOptionsItemSelected(item);
		}
		

		/**
		 * When creating, retrieve this instance's number from its arguments.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			imagePath = getArguments() != null ? getArguments().getString(
					"IMAGE_PATH") : "http://cumbrianrun.co.uk/wp-content/uploads/2014/02/default-placeholder.png";
			if (imagePath.startsWith("http"));
			else imagePath = "file://" + imagePath; 
			if (getArguments() != null && getArguments().getBoolean("SELECTION_MODE"))
				isSelectionMode = true;
			position = getArguments().getInt("POSITION");
			setHasOptionsMenu(true);
		}
		
		
		public int getCurrentImageSelectedPos () {
			return ((BrowseImageActivity)getActivity()).getSelectedPosition();
		}
		
		public void setCurrentSelectedPhotoPos (int position) {
			((BrowseImageActivity)getActivity()).setSelectedPosition(position);
		}
		

		/**
		 * The Fragment's UI is just a simple text view showing its instance
		 * number.
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View fragmentView = inflater.inflate(R.layout.fragment_large_imagezoomview, container, false);
			ImageViewTouch imageView = (ImageViewTouch) fragmentView.findViewById(R.id.large_image_view);
			final ImageView checkView = (ImageView) fragmentView.findViewById(R.id.imageCheckStatus);
			if (isSelectionMode){
				checkView.setVisibility(View.VISIBLE);
				// displays the selected items
		        if (position == getCurrentImageSelectedPos()) {
		        	checkView
		                    .setImageResource(R.drawable.selectphoto_large);
		        	checkView.setBackgroundColor(0xff42d1f6);
		        } else {
		        	checkView.setImageResource(R.drawable.selectphoto_large);
		        	checkView.setBackgroundColor(0x501c1c1c);
		        }
				checkView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (getCurrentImageSelectedPos() == position){
							((BrowseImageActivity)getActivity()).setSelectedPosition(-1);
							checkView.setImageResource(R.drawable.selectphoto_large);
				        	checkView.setBackgroundColor(0x501c1c1c);
						}else {
							((BrowseImageActivity)getActivity()).setSelectedPosition(position);
							checkView.setImageResource(R.drawable.selectphoto_large);
							checkView.setBackgroundColor(0xff42d1f6);
						}
						
					}
				});
			}
			else
				checkView.setVisibility(View.GONE);
			imageView.setSingleTapListener(new OnImageViewTouchSingleTapListener() {
				
				@Override
				public void onSingleTapConfirmed() {
					PhotoViewFragment.tapListener.onSingleTapConfirmed(null);
					//Toast.makeText(getActivity(), "onSingleTapConfirmed", Toast.LENGTH_SHORT).show();
				}
			});
			
			imageView.setmZoomingListener(mZoomingListener);
			//imagePreview.setScaleType(ScaleType.CENTER_INSIDE);
			// check status for preview image


			// set image by Picasso
			Picasso picassoInstance = Picasso.with(getActivity());
			picassoInstance.load((imagePath))
					.error(R.drawable.nophotos)
					.placeholder(R.drawable.nophotos)
					.fit()
					.centerInside()
					.into(imageView);
			return fragmentView;
		}
		@Override
		public void onDestroyView() {super.onDestroyView();}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {super.onActivityCreated(savedInstanceState);}
		
		@Override
		public void onAttach(Activity activity) {super.onAttach(activity);}
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		//toggleActionBar(!isActionBarVisible, true);
		return false;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {return false;}


	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {return false;}


	@Override
	public boolean onDown(MotionEvent e) {return false;}


	@Override
	public void onShowPress(MotionEvent e) {}


	@Override
	public boolean onSingleTapUp(MotionEvent e) {return false;}


	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {return false;}


	@Override
	public void onLongPress(MotionEvent e) {}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {return false;}


	@Override
	public void onImageZooming(final boolean isZooming) {
		viewPager.setPagingEnabled(!isZooming);
		
	}
	
	
	
}
