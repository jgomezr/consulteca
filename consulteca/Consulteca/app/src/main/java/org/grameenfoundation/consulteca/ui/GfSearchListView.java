package org.grameenfoundation.consulteca.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 *
 */
public class GfSearchListView extends ListView {
    float mDiffX, mDiffY, mLastX, mLastY;

    public GfSearchListView(Context context) {
        super(context);
    }

    public GfSearchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GfSearchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // reset difference values
                mDiffX = 0;
                mDiffY = 0;

                mLastX = ev.getX();
                mLastY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                mDiffX += Math.abs(curX - mLastX);
                mDiffY += Math.abs(curY - mLastY);
                mLastX = curX;
                mLastY = curY;

                // don't intercept event, when user tries to scroll vertically
                if (mDiffX > mDiffY) {
                    return false; // do not react to horizontal touch events, these events will be passed to your list item view
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
