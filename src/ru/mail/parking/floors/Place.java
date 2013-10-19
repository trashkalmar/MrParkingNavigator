package ru.mail.parking.floors;

import java.io.DataInputStream;
import java.io.IOException;

public final class Place {
  public static final int INVALID = -1;

  private final int mNumber;
  private final int mFloor;
  private final int mCoordX;
  private final int mCoordY;
  private final Side mSide;
  private final boolean mVertical;


  public enum Side {
    A, B
  }


  public Place(int number, int floor, DataInputStream dis) throws IOException {
    mNumber = number;
    mFloor = floor;
    int v = dis.readInt();
    mCoordX = (v & 0x00FFF000) >> 12;
    mCoordY = (v & 0x00000FFF);
    mSide = ((v & 0x40000000) == 0 ? Side.A : Side.B);
    mVertical = ((v & 0x80000000) != 0);
  }

  public int getNumber() {
    return mNumber;
  }

  public int getFloor() {
    return mFloor;
  }

  public int getCoordX() {
    return mCoordX;
  }

  public int getCoordY() {
    return mCoordY;
  }

  public Side getSide() {
    return mSide;
  }

  public boolean isVertical() {
    return mVertical;
  }
}