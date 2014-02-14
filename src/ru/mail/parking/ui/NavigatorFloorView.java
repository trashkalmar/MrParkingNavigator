package ru.mail.parking.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.ScaleGestureDetector;

import ru.mail.parking.App;
import ru.mail.parking.floors.Floor;
import ru.mail.parking.floors.Place;
import ru.mail.parking.utils.Utils;

public class NavigatorFloorView implements MultiscrollView.AttachedView {
  private static final int MARKER_COLOR = 0x707070FF;
  private static final int MARKER_WIDTH = 49;
  private static final int MARKER_HEIGHT = 19;

  private final MultiscrollView mScrollView;
  private final Paint mFloorPaint = new Paint();
  private final Paint mMarkerPaint = new Paint();

  private final float mMarkerLeft;
  private final float mMarkerTop;
  private final float mMarkerWidth;
  private final float mMarkerHeight;

  private final Bitmap mFloor;

  private float mZoom;
  private final int mWidth;
  private final int mHeight;


  public NavigatorFloorView(Place place, MultiscrollView scrollView) {
    mMarkerLeft = Utils.dp(place.getCoordX());
    mMarkerTop = Utils.dp(place.getCoordY());
    mMarkerWidth = Utils.dp(place.isVertical() ? MARKER_HEIGHT : MARKER_WIDTH);
    mMarkerHeight = Utils.dp(place.isVertical() ? MARKER_WIDTH : MARKER_HEIGHT);

    Floor f = App.floors().getFloor(place.getFloor());
    mFloor = BitmapFactory.decodeResource(App.app().getResources(), f.getImageRes());

    mMarkerPaint.setColor(MARKER_COLOR);

    mWidth = mFloor.getWidth();
    mHeight = mFloor.getHeight();
    mScrollView = scrollView;

    mZoom = 0.5f;
  }

  @Override
  public int getWidth() {
    return (int)(mWidth * mZoom);
  }

  @Override
  public int getHeight() {
    return (int)(mHeight * mZoom);
  }

  @Override
  public void draw(Canvas canvas, int scrollX, int scrollY) {
    canvas.save();
    canvas.scale(mZoom, mZoom);
    canvas.drawBitmap(mFloor, 0, 0, mFloorPaint);

    canvas.translate(mMarkerLeft, mMarkerTop);
    canvas.drawRect(0, 0, mMarkerWidth, mMarkerHeight, mMarkerPaint);
    canvas.restore();
  }

  @Override
  public boolean onTap(int x, int y) {
    return false;
  }

  @Override
  public void onHolderReady(int width, int height) {
    mScrollView.onAttachedViewResized();
  }

  @Override
  public ScaleGestureDetector.SimpleOnScaleGestureListener createZoomListener() {
    return new ScaleGestureDetector.SimpleOnScaleGestureListener() {
      private float mInitialZoom = 1.0f;

      @Override
      public boolean onScale(ScaleGestureDetector detector) {
        float zoom = mInitialZoom * detector.getScaleFactor();
        if (zoom < 0.5f || zoom > 3.0f) {
          mInitialZoom = zoom;
          return true;
        }

        mZoom = zoom;
        mScrollView.onAttachedViewResized();
        mScrollView.invalidate();
        return false;
      }

      @Override
      public boolean onScaleBegin(ScaleGestureDetector detector) {
        mInitialZoom = mZoom;
        return super.onScaleBegin(detector);
      }
    };
  }
}