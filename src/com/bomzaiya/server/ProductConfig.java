package com.bomzaiya.server;

import java.util.HashMap;

public class ProductConfig {
  public static HashMap<Integer, Integer> mPortList = new HashMap<Integer, Integer>();

  static {
    mPortList.put(0, 40081);
    mPortList.put(1, 40082);
    mPortList.put(2, 40083);
    mPortList.put(3, 40084);
  };
}
