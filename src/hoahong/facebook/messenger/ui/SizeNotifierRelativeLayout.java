/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package hoahong.facebook.messenger.ui;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

@SuppressLint("NewApi") public class SizeNotifierRelativeLayout extends RelativeLayout implements Target{

    private Rect rect = new Rect();
    private Drawable backgroundDrawable;
    public SizeNotifierRelativeLayoutDelegate delegate;

    public abstract interface SizeNotifierRelativeLayoutDelegate {
        public abstract void onSizeChanged(int keyboardHeight);
    }

    public SizeNotifierRelativeLayout(Context context) {
        super(context);
    }

    public SizeNotifierRelativeLayout(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs); 
    }

    public SizeNotifierRelativeLayout(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBackgroundImage(int resourceId) {
        try {
            backgroundDrawable = getResources().getDrawable(resourceId);
        } catch (Throwable e) {
           	e.printStackTrace();
        }
    }

    public void setBackgroundImage(Drawable bitmap) {
        backgroundDrawable = bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	try{
        super.onLayout(changed, l, t, r, b);
    	}catch (Exception e){e.printStackTrace();}
        if (delegate != null) {
            int usableViewHeight = this.getRootView().getHeight() - Utils.statusBarHeight;
            this.getWindowVisibleDisplayFrame(rect);
            int keyboardHeight = usableViewHeight - (rect.bottom - rect.top);
            delegate.onSizeChanged(keyboardHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundDrawable != null) {
            float scaleX = (float)getMeasuredWidth() / (float)backgroundDrawable.getIntrinsicWidth();
            float scaleY = (float)getMeasuredHeight() / (float)backgroundDrawable.getIntrinsicHeight();
            float scale = scaleX < scaleY ? scaleY : scaleX;
            int width = (int)Math.ceil(backgroundDrawable.getIntrinsicWidth() * scale);
            int height = (int)Math.ceil(backgroundDrawable.getIntrinsicHeight() * scale);
            int x = (getMeasuredWidth() - width) / 2;
            int y = (getMeasuredHeight() - height) / 2;
            backgroundDrawable.setBounds(x, y, x + width, y + height);
            backgroundDrawable.draw(canvas);
        } else {
            super.onDraw(canvas);
        }
    }

	@Override
	public void onBitmapFailed(Drawable arg0) {
		setBackground(arg0);
	}

	@Override
	public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
		setBackground(new BitmapDrawable(arg0));
		
	}

	@Override
	public void onPrepareLoad(Drawable arg0) {
		setBackground(arg0);
		
	}
}
