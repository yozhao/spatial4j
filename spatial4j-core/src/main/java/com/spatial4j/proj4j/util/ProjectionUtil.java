package com.spatial4j.proj4j.util;

import com.spatial4j.proj4j.ProjCoordinate;

public class ProjectionUtil 
{
  public static String toString(ProjCoordinate p)
  {
    return "[" + p.x + ", " + p.y + "]";
  }

}
