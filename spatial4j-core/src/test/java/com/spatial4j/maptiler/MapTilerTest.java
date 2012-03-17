/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.maptiler;

import com.spatial4j.core.RandomSeed;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.simple.SimpleSpatialContext;
import com.spatial4j.core.context.simple.SimpleSpatialContextFactory;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;


public class MapTilerTest {

  
  @Test
  public void testSomeDistances() {
    GlobalMercator mercator = new GlobalMercator();
    
    // san francisco
    double[] pt = new double[] {37.7793, -122.4192};
    int zoom = 11;
    
    double[] meters = mercator.LatLonToMeters(pt[0], pt[1]);
    int[] tms = mercator.MetersToTile(meters[0],meters[1],zoom);
    int[] google = mercator.GoogleTile(tms[0],tms[1],zoom);
    String quad = mercator.QuadTree(tms[0],tms[1],zoom);
    double[] bounds = mercator.TileBounds(tms[0],tms[1],zoom);
    
    System.out.printf( "bounds: %.5f,%.5f,%.5f,%.5f\n", bounds[0],bounds[1],bounds[2],bounds[3] );
    System.out.printf( "quad: %s\n", quad );
    System.out.printf( "google: %d,%d\n", google[0],google[1] );
    
    assertArrayEquals(new int[]{327,791}, google);
    assertArrayEquals(new int[]{327,1256}, tms);
  }

}
