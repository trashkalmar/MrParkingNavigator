package ru.mail.parking.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import ru.mail.parking.R;


public class MultiscrollView extends View
  implements View.OnTouchListener {
  private AttachedView mAttachedView;
  private Scroller mScroller;
  private GestureDetector mScrollDetector;
  private ScaleGestureDetector mZoomDetector;
  private boolean mResizeComplete;

  private int mContentWidth;
  private int mContentHeight;


  public interface AttachedView {
    int getWidth();
    int getHeight();
    void draw(Canvas canvas, int scrollX, int scrollY);
    boolean onTap(int x, int y);
    void onHolderReady(int width, int height);
    ScaleGestureDetector.SimpleOnScaleGestureListener createZoomListener();
  }


  private class ScrollGestureDetector extends GestureDetector.SimpleOnGestureListener {
    public void onShowPress(MotionEvent event) {}
    public void onLongPress(MotionEvent event) {}

    public boolean onDown(MotionEvent event) {
      if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN &&
          !mScroller.isFinished())
        mScroller.abortAnimation();

      return true;
    }

    public boolean onSingleTapUp(MotionEvent event) {
      return mAttachedView.onTap((int)event.getX() + getScrollX(),
                                 (int)event.getY() + getScrollY());
    }

    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
      if (mContentWidth < getWidth())
        distanceX = 0;
      else {
        if (getScrollX() + distanceX < 0)
          distanceX = -getScrollX();
        else
        if (getScrollX() + getWidth() + distanceX >= mContentWidth)
          distanceX = mContentWidth - getScrollX() - getWidth();
      }

      if (mContentHeight < getHeight())
        distanceY = 0;
      else {
        if (getScrollY() + distanceY < 0)
          distanceY = -getScrollY();
        else
        if (getScrollY() + getHeight() + distanceY >= mContentHeight)
          distanceY = mContentHeight - getScrollY() - getHeight();
      }

      if (distanceX != 0 || distanceY != 0)
        scrollBy((int)distanceX, (int)distanceY);

      return true;
    }

    public boolean onFling(MotionEvent event1, MotionEvent event2, float AvelocityX, float velocityY) {
      mScroller.fling(getScrollX(), getScrollY(), -(int)AvelocityX, -(int)velocityY, 0, mContentWidth - getWidth(),
                      0, mContentHeight - getHeight());
      awakenScrollBars(mScroller.getDuration());
      return true;
    }
  }


  @Override
  protected void onDraw(Canvas canvas) {
    if (!mResizeComplete) {
      mResizeComplete = true;

      mAttachedView.onHolderReady(getWidth(), getHeight());

      setHorizontalScrollBarEnabled(true);
      setVerticalScrollBarEnabled(true);

      TypedArray attrs = getContext().obtainStyledAttributes(R.styleable.View);
      initializeScrollbars(attrs);
      attrs.recycle();
    }

    mAttachedView.draw(canvas, getScrollX(), getScrollY());
  }

  public MultiscrollView(Context context) {
    super(context);
    commonConstruct(context);
  }

  public MultiscrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    commonConstruct(context);
  }

  private void commonConstruct(Context context) {
    mScroller = new Scroller(context);

    setHorizontalFadingEdgeEnabled(false);
    setVerticalFadingEdgeEnabled(false);
    setOnTouchListener(this);
  }

  public void setAttachedView(AttachedView view) {
    mAttachedView = view;

    mScrollDetector = new GestureDetector(getContext(), new ScrollGestureDetector());
    ScaleGestureDetector.SimpleOnScaleGestureListener scale = mAttachedView.createZoomListener();
    if (scale != null)
      mZoomDetector = new ScaleGestureDetector(getContext(), scale);
  }

  public boolean onTouch(View view, MotionEvent event) {
    mScrollDetector.onTouchEvent(event);

    if (mZoomDetector != null)
      mZoomDetector.onTouchEvent(event);

    return true;
  }

  public void onAttachedViewResized() {
    mContentWidth = mAttachedView.getWidth();
    mContentHeight = mAttachedView.getHeight();

    scrollTo(getScrollX(), getScrollY());
  }

  @Override
  protected int computeHorizontalScrollRange() {
    return mContentWidth;
  }

  @Override
  protected int computeVerticalScrollRange() {
    return mContentHeight;
  }

  @Override
  public void computeScroll() {
    if (!mScroller.computeScrollOffset())
      return;

    int oldX = getScrollX();
    int oldY = getScrollY();

    scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

    if (oldX != getScrollX() || oldY != getScrollY())
      onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);

    postInvalidate();
  }

  @Override
  public void scrollTo(int x, int y) {
    if (x + getWidth() > mContentWidth)
      x = mContentWidth - getWidth();

    if (x < 0)
      x = 0;

    if (y + getHeight() > mContentHeight)
      y = mContentHeight - getHeight();

    if (y < 0)
      y = 0;

    super.scrollTo(x, y);
  }
}