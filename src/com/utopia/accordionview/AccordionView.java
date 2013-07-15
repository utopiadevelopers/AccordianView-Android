
package com.utopia.accordionview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import com.utopia.accordianview.R;


public class AccordionView extends LinearLayout implements OnTouchListener {
    private View handle, content;
    private int handleId, contentId,backgroundOpenId,backgroundClosedId;
    private boolean contentVisible, playClickSound = true;
    private GestureDetector mDetector;
    OnHandleClickListener mHandleClickListener;
    private Drawable backgroundOpen,backgroundClosed;
    private Context context;

    public interface OnHandleClickListener
    {
        public void OnDrawerOpened(AccordionView view, View handle);

        public void OnDrawerClosed(AccordionView view, View handle);
    }

    public void setPlaySound(boolean playSound)
    {
        playClickSound = playSound;
    }

    public AccordionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AccordionView,
                0, 0);

        try
        {
            handleId = a.getResourceId(R.styleable.AccordionView_addHandle, -1);
            contentId = a.getResourceId(R.styleable.AccordionView_addContent, -1);
            contentVisible = a.getBoolean(R.styleable.AccordionView_contentVisible, false);
            backgroundOpenId = a.getResourceId(R.styleable.AccordionView_backgroundOpen, -1);
            backgroundClosedId = a.getResourceId(R.styleable.AccordionView_backgroundClosed, -1);
        } finally {
            a.recycle();
        }

        Log.d("Handle", handleId + " " + R.styleable.AccordionView_addHandle);
        Log.d("Content", contentId + " " + R.styleable.AccordionView_addContent);
        if (handleId == -1 || contentId == -1)
            throw new RuntimeException("Please Set both handle and content views by using the addHandle and addContent XML attributes.");
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater mInflater;
        mInflater = LayoutInflater.from(context);
        handle = mInflater.inflate(handleId, AccordionView.this, false);
        content = mInflater.inflate(contentId, AccordionView.this, false);
        handle.setOnTouchListener(this);
        if(backgroundClosedId != -1)
            backgroundClosed = context.getResources().getDrawable(backgroundClosedId);
        else
            backgroundClosed = context.getResources().getDrawable(R.drawable.open);
        
        if(backgroundOpenId != -1)
            backgroundOpen = context.getResources().getDrawable(backgroundOpenId);
        else
            backgroundOpen = context.getResources().getDrawable(R.drawable.close);

        if (!contentVisible)
        {
            setContentVisible(contentVisible);
        }
            
        addView(handle);
        addView(content);

        mDetector = new GestureDetector(AccordionView.this.getContext(), new GestureListener());
    }

    public boolean isContentVisible()
    {
        return contentVisible;
    }

    public void setHandleBackgrounds(Drawable backgroundClosed,Drawable backgroundOpen)
    {
        this.backgroundClosed = backgroundClosed;
        this.backgroundOpen = backgroundOpen;
        if(isContentVisible())
            setHandleDrawable(backgroundOpen);
        else
            setHandleDrawable(backgroundClosed);
        invalidate();
        requestLayout();
    }
    
    public void setHandleBackgrounds(int backgroundClosedId,int backgroundOpenId)
    {
        this.backgroundClosed = context.getResources().getDrawable(backgroundClosedId);
        this.backgroundOpen = content.getResources().getDrawable(backgroundOpenId);
        if(isContentVisible())
            setHandleDrawable(backgroundOpen);
        else
            setHandleDrawable(backgroundClosed);
    }
    
    @SuppressLint("NewApi")
    private void setHandleDrawable(Drawable background)
    {
        if(android.os.Build.VERSION.SDK_INT < 16)
            handle.setBackgroundDrawable(background);
        else
            handle.setBackground(background);
    }
    
    @SuppressLint("NewApi")
    public void setContentVisible(boolean visible)
    {
        contentVisible = visible;
        if (!(contentVisible == true && content.getVisibility() == View.VISIBLE)
                || !(contentVisible == false && content.getVisibility() == View.GONE))
        {
            if (contentVisible == true)
            {
                setHandleDrawable(backgroundOpen);
                content.setVisibility(View.VISIBLE);
            }
            else
            {
                setHandleDrawable(backgroundClosed);
                content.setVisibility(View.GONE);
            }
            invalidate();
            requestLayout();
        }
    }

    public void setHandle(View handle)
    {
        this.addView(handle, indexOfChild(this.handle));
        this.removeView(this.handle);
        this.handle = handle;
        if(isContentVisible())
            setHandleDrawable(backgroundOpen);
        else
            setHandleDrawable(backgroundClosed);
        this.handle.setOnTouchListener(this);
        invalidate();
        requestLayout();
    }

    public void setContent(View content)
    {
        this.addView(content, indexOfChild(this.content));
        this.removeView(this.content);
        this.content = content;
        if(!isContentVisible())
            this.content.setVisibility(View.GONE);
        invalidate();
        requestLayout();
    }

    public View getContent()
    {
        return content;
    }

    public View getHandle()
    {
        return handle;
    }

    public void SetOnHandleClickListener(OnHandleClickListener listener)
    {
        mHandleClickListener = listener;
    }

    class GestureListener extends SimpleOnGestureListener
    {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d("Single", "Single Tap");
            if (playClickSound)
                playSoundEffect(SoundEffectConstants.CLICK);
            if (handle != null) {
                handle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
            if (mHandleClickListener != null)
            {
                if (isContentVisible())
                    mHandleClickListener.OnDrawerClosed(AccordionView.this, handle);
                else
                    mHandleClickListener.OnDrawerOpened(AccordionView.this, handle);

            }

            setContentVisible(!isContentVisible());
            return true;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);
        return result;        
    }

}
