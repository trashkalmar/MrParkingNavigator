package ru.mail.parking.sw2.screens;

import com.sonyericsson.extras.liveware.aef.control.Control;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import ru.mail.parking.App;
import ru.mail.parking.R;
import ru.mail.parking.floors.Floor;
import ru.mail.parking.floors.Place;
import ru.mail.parking.sw2.SwControlFlow;

import static ru.mail.parking.App.app;
import static ru.mail.parking.App.prefs;

public class SwNavigatorScreen extends SwBaseScreen {
  private static final float SCALE = 0.6616f;
  private static final int TOP_OFFSET = 5;

  private static final int MARKER_COLOR = 0x707070FF;
  private static final int WINDOW_COLOR = 0x60D0D0F0;
  private static final int MARKER_WIDTH = 33;
  private static final int MARKER_HEIGHT = 13;


  private final Bitmap mFloor;
  private final Bitmap mFloorThumb;

  private final Bitmap mDrawBitmap;
  private final Canvas mDrawCanvas;
  private final Paint mDrawPaint = new Paint();
  private final Rect mDrawRect;

  private final Paint mMarkerPaint = new Paint();
  private final Paint mThumbWindowPaint = new Paint();

  private final int mScreenWidth;
  private final int mScreenHeight;
  private final int mFloorSize;
  private final int mThumbSize;
  private final int mThumbWindowWidth;
  private final int mThumbWindowHeight;
  private final int mMarkerLeft;
  private final int mMarkerTop;
  private final int mMarkerWidth;
  private final int mMarkerHeight;

  private final int mScrollDeltaX;
  private final int mScrollDeltaY;


  private int mScrollX;
  private int mScrollY;


  public SwNavigatorScreen(SwControlFlow flow) {
    super(flow);

    mFloorThumb = BitmapFactory.decodeResource(app().getResources(), R.drawable.thumb_sw);
    mThumbSize = mFloorThumb.getWidth();

    mScreenWidth = app().getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    mScreenHeight = app().getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    mDrawRect = new Rect(0, 0, mScreenWidth, mScreenHeight);
    mScrollDeltaX = mScreenWidth / 2;
    mScrollDeltaY = mScreenHeight / 2;

    mDrawBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
    mDrawCanvas = new Canvas(mDrawBitmap);

    Place place = prefs().getStoredPlace();
    Floor f = App.floors().getFloor(place.getFloor());
    mFloor = BitmapFactory.decodeResource(app().getResources(), f.getSwImageRes());
    mThumbWindowPaint.setColor(WINDOW_COLOR);
    mMarkerPaint.setColor(MARKER_COLOR);

    mFloorSize = mFloor.getWidth();
    mThumbWindowWidth = (mThumbSize * mScreenWidth) / mFloorSize;
    mThumbWindowHeight = (mThumbSize * mScreenHeight) / mFloorSize;

    mMarkerLeft = (int)(place.getCoordX() * SCALE);
    mMarkerTop = (int)(place.getCoordY() * SCALE);
    mMarkerWidth = (place.isVertical() ? MARKER_HEIGHT : MARKER_WIDTH);
    mMarkerHeight = (place.isVertical() ? MARKER_WIDTH : MARKER_HEIGHT);

    mScrollX = mMarkerLeft + mMarkerWidth - mScreenWidth / 2;
    mScrollY = mMarkerTop + mMarkerHeight - mScreenHeight / 2;
    adjustScroll();
  }

  private void adjustScroll() {
    if (mScrollX < 0)
      mScrollX = 0;

    if (mScrollY < 0)
      mScrollY = 0;

    if (mScrollX + mScreenWidth > mFloorSize)
      mScrollX = mFloorSize - mScreenWidth;

    if (mScrollY + mScreenHeight > mFloorSize)
      mScrollY = mFloorSize - mScreenHeight;
  }

  private void draw() {
    mDrawCanvas.drawColor(Color.BLACK);

    // Image
    Rect r = new Rect(mScrollX, mScrollY, mScrollX + mScreenWidth, mScrollY + mScreenHeight);
    mDrawCanvas.drawBitmap(mFloor, r, mDrawRect, mDrawPaint);

    mDrawCanvas.save();
    mDrawCanvas.translate(mMarkerLeft - mScrollX, mMarkerTop - mScrollY - TOP_OFFSET);
    mDrawCanvas.drawRect(0, 0, mMarkerWidth, mMarkerHeight, mMarkerPaint);
    mDrawCanvas.restore();

    // Thumb
    int xoff = mScreenWidth - mThumbSize;
    int yoff = mScreenHeight - mThumbSize;
    mDrawCanvas.drawBitmap(mFloorThumb, xoff, yoff, mDrawPaint);

    xoff += (mThumbSize * mScrollX) / mFloorSize;
    yoff += (mThumbSize * mScrollY) / mFloorSize;

    mDrawCanvas.save();
    mDrawCanvas.translate(xoff, yoff);
    mDrawCanvas.drawRect(0, 0, mThumbWindowWidth, mThumbWindowHeight, mThumbWindowPaint);
    mDrawCanvas.restore();

    mFlow.showBitmap(mDrawBitmap);
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.sw_navigator;
  }

  @Override
  public void onResume() {
    draw();
  }

  @Override
  public void onSwipe(int direction) {
    int oldx = mScrollX;
    int oldy = mScrollY;

    switch (direction) {
      case Control.Intents.SWIPE_DIRECTION_LEFT:
        mScrollX += mScrollDeltaX;
        break;

      case Control.Intents.SWIPE_DIRECTION_RIGHT:
        mScrollX -= mScrollDeltaX;
        break;

      case Control.Intents.SWIPE_DIRECTION_UP:
        mScrollY += mScrollDeltaY;
        break;

      case Control.Intents.SWIPE_DIRECTION_DOWN:
        mScrollY -= mScrollDeltaY;
        break;
    }

    adjustScroll();

    if (oldx != mScrollX || oldy != mScrollY)
      draw();
  }
}