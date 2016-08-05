package indi.yume.tools.fragmentmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import lombok.Getter;
import lombok.Setter;

import static android.support.v4.widget.ViewDragHelper.EDGE_LEFT;

/**
 * Created by yume on 16-8-5.
 */

public class SwipeBackLayout extends FrameLayout {
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.4f;
    private static final int OVERSCROLL_DISTANCE = 10;

    private float mScrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD;

    @Getter(lazy = true)
    private final ViewDragHelper dragHelper = provideDragHelper();

    private BaseFragmentManagerActivity managerActivity;
    private View contentView;

    private float scrollPercent = 0;
    private int currentSwipeOrientation;

    @Setter
    @Getter
    private boolean enableSwipe = false;

    public SwipeBackLayout(Context context) {
        super(context);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    ViewDragHelper provideDragHelper() {
        return ViewDragHelper.create(this, new ViewDragCallBack());
    }

    public void attachToActivity(BaseFragmentManagerActivity activity) {
        managerActivity = activity;
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
        contentView = decorChild;
        decor.addView(this);

        enableSwipe = true;
    }

    public View attachToFragment(BaseManagerFragment fragment, View view) {
        if(!(fragment.getActivity() instanceof BaseFragmentManagerActivity))
            throw new RuntimeException("Main Activity must be BaseFragmentManagerActivity");

        managerActivity = (BaseFragmentManagerActivity) fragment.getActivity();

        addView(view);
        contentView = view;
        enableSwipe = true;

        return this;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean isDrawView = child == contentView;
        boolean drawChild = super.drawChild(canvas, child, drawingTime);
        if (isDrawView && scrollPercent < 1 && getDragHelper().getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawAlpha(canvas, child);
        }
        return drawChild;
    }

    private void drawAlpha(Canvas canvas, View child) {
        child.setAlpha(1 - scrollPercent);
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (DEFAULT_SCRIM_COLOR & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * (1 - scrollPercent));
        final int color = alpha << 24;

        if ((currentSwipeOrientation & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        }
        canvas.drawColor(color);
    }

    @Override
    public void computeScroll() {
        if (scrollPercent <= 1) {
            if (getDragHelper().continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!enableSwipe)
            return super.onInterceptTouchEvent(ev);
        return getDragHelper().shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!enableSwipe)
            return super.onTouchEvent(event);
        getDragHelper().processTouchEvent(event);
        return true;
    }

    class ViewDragCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean dragEnable = getDragHelper().isEdgeTouched(EDGE_LEFT, pointerId);

            if(dragEnable) {
                if (getDragHelper().isEdgeTouched(EDGE_LEFT, pointerId)) {
                    currentSwipeOrientation = EDGE_LEFT;
                }

                managerActivity.showPreFragment();
            }

            return dragEnable;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int ret = 0;
            if ((currentSwipeOrientation & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            }
            return ret;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if ((currentSwipeOrientation & EDGE_LEFT) != 0) {
                scrollPercent = Math.abs((float) left / getWidth());
            }
            invalidate();

            if (scrollPercent > 1) {
                managerActivity.removeFragmentWithoutAnim(managerActivity.getCurrentStackTag());
            } else if(Math.abs(scrollPercent) < 0.001) {
                managerActivity.hidePreFragment();
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (enableSwipe) {
                return 1;
            }
            return 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();

            int left = 0, top = 0;
            if ((currentSwipeOrientation & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && scrollPercent > mScrollFinishThreshold ? (childWidth
                        + OVERSCROLL_DISTANCE) : 0;
            }

            getDragHelper().settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
            if ((EDGE_LEFT & edgeFlags) != 0) {
                currentSwipeOrientation = edgeFlags;
            }
        }
    }
}
