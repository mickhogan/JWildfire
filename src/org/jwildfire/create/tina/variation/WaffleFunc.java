/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.variation;

import static org.jwildfire.base.mathlib.MathLib.cos;
import static org.jwildfire.base.mathlib.MathLib.sin;
import static org.jwildfire.create.tina.base.Constants.AVAILABILITY_CUDA;
import static org.jwildfire.create.tina.base.Constants.AVAILABILITY_JWILDFIRE;

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class WaffleFunc extends VariationFunc {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_SLICES = "slices";
  private static final String PARAM_XTHICKNESS = "xthickness";
  private static final String PARAM_YTHICKNESS = "ythickness";
  private static final String PARAM_ROTATION = "rotation";

  private static final String[] paramNames = { PARAM_SLICES, PARAM_XTHICKNESS, PARAM_YTHICKNESS, PARAM_ROTATION };

  private int slices = 6;
  private double xThickness = 0.5;
  private double yThickness = 0.5;
  private double rotation = 0.0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // Waffle plugin by Jed Kelsey, http://lu-kout.deviantart.com/art/Apophysis-Plugin-Pack-1-v0-4-59907275
    double a = 0.0, r = 0.0;
    switch (pContext.random(5)) {
      case 0:
        a = (pContext.random(slices) + pContext.random() * xThickness) / slices;
        r = (pContext.random(slices) + pContext.random() * yThickness) / slices;
        break;
      case 1:
        a = (pContext.random(slices) + pContext.random()) / slices;
        r = (pContext.random(slices) + yThickness) / slices;
        break;
      case 2:
        a = (pContext.random(slices) + xThickness) / slices;
        r = (pContext.random(slices) + pContext.random()) / slices;
        break;
      case 3:
        a = pContext.random();
        r = (pContext.random(slices) + yThickness + pContext.random() * (1 - yThickness)) / slices;
        break;
      case 4:
        a = (pContext.random(slices) + xThickness + pContext.random() * (1 - xThickness)) / slices;
        r = pContext.random();
        break;
    }
    pVarTP.x += (vcosr * a + vsinr * r); // note that post-transforms make this redundant!
    pVarTP.y += (-vsinr * a + vcosr * r);
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[] { slices, xThickness, yThickness, rotation };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_SLICES.equalsIgnoreCase(pName))
      slices = Tools.FTOI(pValue);
    else if (PARAM_XTHICKNESS.equalsIgnoreCase(pName))
      xThickness = pValue;
    else if (PARAM_YTHICKNESS.equalsIgnoreCase(pName))
      yThickness = pValue;
    else if (PARAM_ROTATION.equalsIgnoreCase(pName))
      rotation = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "waffle";
  }

  private double vsinr, vcosr;

  @Override
  public void init(FlameTransformationContext pContext, XForm pXForm, double pAmount) {
    vcosr = pAmount * cos(rotation);
    vsinr = pAmount * sin(rotation);
  }

  @Override
  public int getAvailability() {
    return AVAILABILITY_JWILDFIRE | AVAILABILITY_CUDA;
  }
}
