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

package com.spatial4j.core.context;

import com.spatial4j.core.context.simple.SimpleSpatialContext;
import com.spatial4j.core.context.simple.SimpleSpatialContextFactory;
import com.spatial4j.core.distance.*;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.simple.RectangleImpl;
import com.spatial4j.proj4j.CRSFactory;
import com.spatial4j.proj4j.CoordinateReferenceSystem;
import com.spatial4j.proj4j.datum.Datum;
import com.spatial4j.proj4j.proj.LongLatProjection;
import com.spatial4j.proj4j.proj.Projection;
import com.spatial4j.proj4j.units.Unit;
import com.spatial4j.proj4j.units.Units;

import java.util.Map;

/**
 * Factory for a SpatialContext.
 */
public abstract class SpatialContextFactory {
  public static final String PROJ4_WGS84 = "+title=WGS84 +proj=longlat +datum=WGS84 +units=degrees";
  public static final CoordinateReferenceSystem CRS_WGS84;
  static {
    Projection geoProj = new LongLatProjection();
    geoProj.setEllipsoid(Datum.WGS84.getEllipsoid());
    geoProj.setUnits(Units.DEGREES);
    geoProj.initialize();
    CRS_WGS84 = new CoordinateReferenceSystem("WGS84", null, Datum.WGS84, geoProj); // Assume it will not change?
  }
  protected ClassLoader classLoader;
  
  protected CoordinateReferenceSystem crs;
  protected Unit units;
  protected DistanceCalculator calculator;
  protected Rectangle worldBounds;

  /**
   * The factory class is lookuped up via "spatialContextFactory" in args
   * then falling back to a Java system property (with initial caps). If neither are specified
   * then {@link SimpleSpatialContextFactory} is chosen.
   * @param args
   * @param classLoader
   */
  public static SpatialContext makeSpatialContext(Map<String,String> args, ClassLoader classLoader) {
    SpatialContextFactory instance;
    String cname = args.get("spatialContextFactory");
    if (cname == null)
      cname = System.getProperty("SpatialContextFactory");
    if (cname == null)
      instance = new SimpleSpatialContextFactory();
    else {
      try {
        Class c = classLoader.loadClass(cname);
        instance = (SpatialContextFactory) c.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    instance.init(args,classLoader);
    return instance.newSpatialContext();
  }

  protected void init(Map<String, String> args, ClassLoader classLoader) {
    this.classLoader = classLoader;

    String v = args.get("units");
    this.units = (v==null)?Units.KILOMETRES : Units.findUnits(v);
    
    v = args.get("worldBounds");
    if(v!=null) {
      // ugly way to read the bounds!
      SimpleSpatialContext simpleCtx = new SimpleSpatialContext(new RectangleImpl(0, 0, 0, 0),null);
      worldBounds = (Rectangle) simpleCtx.readShape(v);

      this.calculator = initCalculator(args.get("distCalculator"),-1); // no valid radius!
    }
    else {
      CRSFactory factory = new CRSFactory();
      v = args.get("crs");
      if(v==null || "WGS84".equals(v)) {
        this.crs = factory.createFromParameters("WGS84", PROJ4_WGS84);
      }
      else {
        this.crs = factory.createFromName(v);
      }
      
      this.calculator = initCalculator(args.get("distCalculator"),crs.getProjection().getEquatorRadius());
    }
    
  }

  protected DistanceCalculator initCalculator(String calcStr, double radius) {
    if (calcStr == null)
      return null;
    
    if (calcStr.equalsIgnoreCase("haversine")) {
      return new GeodesicSphereDistCalc.Haversine(radius);
    } else if (calcStr.equalsIgnoreCase("lawOfCosines")) {
      return new GeodesicSphereDistCalc.LawOfCosines(radius);
    } else if (calcStr.equalsIgnoreCase("vincentySphere")) {
      return new GeodesicSphereDistCalc.Vincenty(radius);
    } else if (calcStr.equalsIgnoreCase("cartesian")) {
      return new CartesianDistCalc();
    } else if (calcStr.equalsIgnoreCase("cartesian^2")) {
      return new CartesianDistCalc(true);
    } else {
      throw new RuntimeException("Unknown calculator: "+calcStr);
    }
  }
  
  public SpatialContext newMaxCartesian() {
    this.worldBounds = new RectangleImpl(Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE);
    return newSpatialContext();
  }

  /** Subclasses should simply construct the instance from the initialized configuration. */
  protected abstract SpatialContext newSpatialContext();
}
