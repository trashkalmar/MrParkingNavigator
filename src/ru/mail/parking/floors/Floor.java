package ru.mail.parking.floors;

import android.util.SparseArray;

import java.io.DataInputStream;
import java.io.IOException;

import ru.mail.parking.R;

public final class Floor {
  private final int mNumber;
  private final int mMinPlace;
  private final int mMaxPlace;

  private final SparseArray<Place> mPlaces;


  public Floor(DataInputStream dis) throws IOException {
    mNumber = (dis.readByte() & 0x0F) + 1;
    mMinPlace = dis.readShort();

    int count = dis.readByte() + 1;
    mMaxPlace = mMinPlace + count - 1;

    mPlaces = new SparseArray<>(count);
    for (int i = mMinPlace; i <= mMaxPlace; i++) {
      Place p = new Place(i, mNumber, dis);
      mPlaces.put(i, p);
    }
  }

  public int getNumber() {
    return mNumber;
  }

  public int getMinPlace() {
    return mMinPlace;
  }

  public int getMaxPlace() {
    return mMaxPlace;
  }

  public Place getPlace(int number) {
    return mPlaces.get(number);
  }

  public int getImageRes() {
    switch (mNumber) {
      case 3:
        return R.drawable.floor3;

      case 4:
        return R.drawable.floor4;

      case 5:
        return R.drawable.floor5;

      default:
        return 0;
    }
  }

  public int getSwImageRes() {
    switch (mNumber) {
      case 3:
        return R.drawable.floor3_sw;

      case 4:
        return R.drawable.floor4_sw;

      case 5:
        return R.drawable.floor5_sw;

      default:
        return 0;
    }
  }
}