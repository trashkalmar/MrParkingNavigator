package ru.mail.parking.floors;

import android.content.res.AssetManager;
import android.util.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru.mail.parking.App;
import ru.mail.parking.utils.Utils;

public class FloorNavigator {
  private static final String FN_FLOORS = "floors.map";

  private final List<Floor> mFloors = new ArrayList<Floor>();
  private final List<Pair<Integer, Integer>> mInvalidRegions = new ArrayList<Pair<Integer,Integer>>();

  public FloorNavigator() {
    InputStream ais = null;
    DataInputStream dis = null;
    try {
      ais = App.app().getAssets().open(FN_FLOORS, AssetManager.ACCESS_BUFFER);
      dis = new DataInputStream(ais);

      int count = dis.readByte();
      for (int i = 0; i < count; i++)
        mFloors.add(new Floor(dis));

      // Invalid regions
      byte head = dis.readByte();
      while (head == 0x1A) {
        int start = dis.readShort();
        int end = start + dis.readByte();

        mInvalidRegions.add(new Pair<Integer,Integer>(start, end));

        head = dis.readByte();
      }
    } catch (IOException ignored) {
    } finally {
      Utils.safeClose(dis);
      Utils.safeClose(ais);
    }
  }

  private boolean isInvalidPlace(int number) {
    if (number == Place.INVALID)
      return true;

    for (Pair<Integer, Integer> p: mInvalidRegions)
      if (number >= p.first && number <= p.second)
        return true;

    return false;
  }

  public Floor findFloor(int place) {
    if (!isInvalidPlace(place))
      for (Floor f: mFloors)
        if (place >= f.getMinPlace() && place <= f.getMaxPlace())
          return f;

    return null;
  }

  public Floor getFloor(int floor) {
    for (Floor f: mFloors)
      if (floor == f.getNumber())
        return f;

    return null;
  }

  public Place getPlace(int place) {
    Floor f = findFloor(place);
    return (f == null ? null : f.getPlace(place));
  }
}