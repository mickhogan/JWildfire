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
package org.jwildfire.create.tina.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jwildfire.base.Prefs;
import org.jwildfire.base.Tools;
import org.jwildfire.base.Tools.XMLAttribute;
import org.jwildfire.base.Tools.XMLAttributes;
import org.jwildfire.base.mathlib.MathLib;
import org.jwildfire.create.tina.base.DrawMode;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.Shading;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.render.filter.FilterKernelType;
import org.jwildfire.create.tina.variation.Variation;
import org.jwildfire.create.tina.variation.VariationFunc;
import org.jwildfire.create.tina.variation.VariationFuncList;

public class Flam3Reader implements FlameReader {
  private final Prefs prefs;

  public Flam3Reader(Prefs pPrefs) {
    prefs = pPrefs;
  }

  @Override
  public List<Flame> readFlames(String pFilename) {
    try {
      String flamesXML = Tools.readUTF8Textfile(pFilename);
      return readFlamesfromXML(flamesXML);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static final String ATTR_NAME = "name";
  private static final String ATTR_SIZE = "size";
  private static final String ATTR_CENTER = "center";
  private static final String ATTR_SCALE = "scale";
  private static final String ATTR_ROTATE = "rotate";
  private static final String ATTR_FILTER = "filter";
  private static final String ATTR_FILTER_KERNEL = "filter_kernel";
  private static final String ATTR_DE_FILTER_ENABLED = "estimator_enabled";
  private static final String ATTR_DE_FILTER_MAX_RADIUS = "estimator_radius";
  private static final String ATTR_DE_FILTER_MIN_RADIUS = "estimator_minimum";
  private static final String ATTR_DE_FILTER_CURVE = "estimator_curve";
  private static final String ATTR_DE_FILTER_KERNEL = "estimator_kernel";
  private static final String ATTR_QUALITY = "quality";
  private static final String ATTR_BACKGROUND = "background";
  private static final String ATTR_BG_TRANSPARENCY = "bg_transparency";
  private static final String ATTR_BRIGHTNESS = "brightness";
  private static final String ATTR_GAMMA = "gamma";
  private static final String ATTR_GAMMA_THRESHOLD = "gamma_threshold";
  private static final String ATTR_VIBRANCY = "vibrancy";
  private static final String ATTR_CONTRAST = "contrast";
  private static final String ATTR_INDEX = "index";
  private static final String ATTR_RGB = "rgb";
  private static final String ATTR_CAM_PITCH = "cam_pitch";
  private static final String ATTR_CAM_YAW = "cam_yaw";
  private static final String ATTR_CAM_PERSP = "cam_persp";
  private static final String ATTR_CAM_PERSPECTIVE = "cam_perspective"; // old version
  private static final String ATTR_CAM_ZPOS = "cam_zpos";
  private static final String ATTR_CAM_XFOCUS = "cam_xfocus";
  private static final String ATTR_CAM_YFOCUS = "cam_yfocus";
  private static final String ATTR_CAM_ZFOCUS = "cam_zfocus";
  private static final String ATTR_CAM_ZDIMISH = "cam_zdimish";
  private static final String ATTR_CAM_DOF = "cam_dof";
  private static final String ATTR_CAM_DOF_AREA = "cam_dof_area";
  private static final String ATTR_CAM_DOF_EXPONENT = "cam_dof_exponent";
  private static final String ATTR_NEW_DOF = "new_dof";
  private static final String ATTR_CAM_ZOOM = "cam_zoom";
  private static final String ATTR_NEW_LINEAR = "new_linear";
  private static final String ATTR_SHADING_SHADING = "shading_shading";
  private static final String ATTR_SHADING_AMBIENT = "shading_ambient";
  private static final String ATTR_SHADING_DIFFUSE = "shading_diffuse";
  private static final String ATTR_SHADING_PHONG = "shading_phong";
  private static final String ATTR_SHADING_PHONGSIZE = "shading_phongSize";
  private static final String ATTR_SHADING_LIGHTCOUNT = "shading_lightCount";
  private static final String ATTR_SHADING_LIGHTPOSX_ = "shading_lightPosX_";
  private static final String ATTR_SHADING_LIGHTPOSY_ = "shading_lightPosY_";
  private static final String ATTR_SHADING_LIGHTPOSZ_ = "shading_lightPosZ_";
  private static final String ATTR_SHADING_LIGHTRED_ = "shading_lightRed_";
  private static final String ATTR_SHADING_LIGHTGREEN_ = "shading_lightGreen_";
  private static final String ATTR_SHADING_LIGHTBLUE_ = "shading_lightBlue_";
  private static final String ATTR_SHADING_BLUR_RADIUS = "shading_blurRadius";
  private static final String ATTR_SHADING_BLUR_FADE = "shading_blurFade";
  private static final String ATTR_SHADING_BLUR_FALLOFF = "shading_blurFallOff";
  private static final String ATTR_PRESERVE_Z = "preserve_z";
  private static final String ATTR_RESOLUTION_PROFILE = "resolution_profile";
  private static final String ATTR_QUALITY_PROFILE = "quality_profile";
  public static final String ATTR_SHADING_DISTANCE_COLOR_RADIUS = "shading_distanceColorRadius";
  public static final String ATTR_SHADING_DISTANCE_COLOR_SCALE = "shading_distanceColorScale";
  public static final String ATTR_SHADING_DISTANCE_COLOR_EXPONENT = "shading_distanceColorExponent";
  public static final String ATTR_SHADING_DISTANCE_COLOR_OFFSETX = "shading_distanceColorOffsetX";
  public static final String ATTR_SHADING_DISTANCE_COLOR_OFFSETY = "shading_distanceColorOffsetY";
  public static final String ATTR_SHADING_DISTANCE_COLOR_OFFSETZ = "shading_distanceColorOffsetZ";
  public static final String ATTR_SHADING_DISTANCE_COLOR_STYLE = "shading_distanceColorStyle";
  public static final String ATTR_SHADING_DISTANCE_COLOR_COORDINATE = "shading_distanceColorCoordinate";
  public static final String ATTR_SHADING_DISTANCE_COLOR_SHIFT = "shading_distanceColorShift";

  private void parseFlameAttributes(Flame pFlame, String pXML) {
    XMLAttributes atts = Tools.parseAttributes(pXML);
    String hs;
    if ((hs = atts.get(ATTR_NAME)) != null) {
      pFlame.setName(hs);
    }
    if ((hs = atts.get(ATTR_SIZE)) != null) {
      String s[] = hs.split(" ");
      pFlame.setWidth(Integer.parseInt(s[0]));
      pFlame.setHeight(Integer.parseInt(s[1]));
    }
    if ((hs = atts.get(ATTR_CENTER)) != null) {
      String s[] = hs.split(" ");
      pFlame.setCentreX(Double.parseDouble(s[0]));
      pFlame.setCentreY(Double.parseDouble(s[1]));
    }
    if ((hs = atts.get(ATTR_SCALE)) != null) {
      pFlame.setPixelsPerUnit(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_ROTATE)) != null) {
      //      pFlame.setCamRoll(-Double.parseDouble(hs) * 180.0 / Math.PI);
      pFlame.setCamRoll(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_FILTER)) != null) {
      pFlame.setSpatialFilterRadius(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_FILTER_KERNEL)) != null) {
      try {
        FilterKernelType kernel = FilterKernelType.valueOf(hs);
        pFlame.setSpatialFilterKernel(kernel);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    // Disable DE filter and check if it is set explicitely on. There are lots of Apo flames out there
    // which are carrying DE settings, but they were never used and may look terrible (e. g. DE max radius 9.0) 
    pFlame.setDeFilterEnabled(false);
    if ((hs = atts.get(ATTR_DE_FILTER_ENABLED)) != null) {
      pFlame.setDeFilterEnabled(Integer.parseInt(hs) == 1);
    }
    if ((hs = atts.get(ATTR_DE_FILTER_MAX_RADIUS)) != null) {
      pFlame.setDeFilterMaxRadius(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_DE_FILTER_MIN_RADIUS)) != null) {
      pFlame.setDeFilterMinRadius(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_DE_FILTER_CURVE)) != null) {
      pFlame.setDeFilterCurve(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_DE_FILTER_KERNEL)) != null) {
      try {
        FilterKernelType kernel = FilterKernelType.valueOf(hs);
        pFlame.setDeFilterKernel(kernel);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if ((hs = atts.get(ATTR_QUALITY)) != null) {
      pFlame.setSampleDensity(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_BACKGROUND)) != null) {
      String s[] = hs.split(" ");
      pFlame.setBGColorRed(Tools.roundColor(255.0 * Double.parseDouble(s[0])));
      pFlame.setBGColorGreen(Tools.roundColor(255.0 * Double.parseDouble(s[1])));
      pFlame.setBGColorBlue(Tools.roundColor(255.0 * Double.parseDouble(s[2])));
    }
    if ((hs = atts.get(ATTR_BRIGHTNESS)) != null) {
      pFlame.setBrightness(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_BG_TRANSPARENCY)) != null) {
      pFlame.setBGTransparency(Integer.parseInt(hs) == 1);
    }
    else {
      pFlame.setBGTransparency(prefs.isTinaDefaultBGTransparency());
    }
    if ((hs = atts.get(ATTR_GAMMA)) != null) {
      pFlame.setGamma(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_GAMMA_THRESHOLD)) != null) {
      pFlame.setGammaThreshold(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_VIBRANCY)) != null) {
      pFlame.setVibrancy(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CONTRAST)) != null) {
      pFlame.setContrast(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_PERSP)) != null) {
      pFlame.setCamPerspective(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_PERSPECTIVE)) != null) {
      pFlame.setCamPerspective(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_ZPOS)) != null) {
      pFlame.setCamZ(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_XFOCUS)) != null) {
      pFlame.setFocusX(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_YFOCUS)) != null) {
      pFlame.setFocusY(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_ZFOCUS)) != null) {
      pFlame.setFocusZ(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_ZDIMISH)) != null) {
      pFlame.setDimishZ(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_DOF)) != null) {
      pFlame.setCamDOF(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_DOF_AREA)) != null) {
      pFlame.setCamDOFArea(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_DOF_EXPONENT)) != null) {
      pFlame.setCamDOFExponent(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_CAM_PITCH)) != null) {
      pFlame.setCamPitch(Double.parseDouble(hs) * 180.0 / Math.PI);
    }
    if ((hs = atts.get(ATTR_CAM_YAW)) != null) {
      pFlame.setCamYaw(Double.parseDouble(hs) * 180.0 / Math.PI);
    }
    if ((hs = atts.get(ATTR_CAM_ZOOM)) != null) {
      pFlame.setCamZoom(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_NEW_DOF)) != null) {
      pFlame.setNewCamDOF("1".equals(hs));
    }
    // preserve-z
    if ((hs = atts.get(ATTR_PRESERVE_Z)) != null) {
      pFlame.setPreserveZ("1".equals(hs));
    }
    // profiles
    if ((hs = atts.get(ATTR_RESOLUTION_PROFILE)) != null) {
      pFlame.setResolutionProfile(hs);
    }
    if ((hs = atts.get(ATTR_QUALITY_PROFILE)) != null) {
      pFlame.setQualityProfile(hs);
    }
    // Shading    
    if ((hs = atts.get(ATTR_SHADING_SHADING)) != null) {
      try {
        pFlame.getShadingInfo().setShading(Shading.valueOf(hs));
      }
      catch (Exception ex) {
        pFlame.getShadingInfo().setShading(Shading.FLAT);
        ex.printStackTrace();
      }
    }
    if ((hs = atts.get(ATTR_SHADING_AMBIENT)) != null) {
      pFlame.getShadingInfo().setAmbient(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DIFFUSE)) != null) {
      pFlame.getShadingInfo().setDiffuse(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_PHONG)) != null) {
      pFlame.getShadingInfo().setPhong(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_PHONGSIZE)) != null) {
      pFlame.getShadingInfo().setPhongSize(Double.parseDouble(hs));
    }
    int lightCount;
    if ((hs = atts.get(ATTR_SHADING_LIGHTCOUNT)) != null) {
      lightCount = Integer.parseInt(hs);
    }
    else {
      lightCount = 0;
    }
    for (int i = 0; i < lightCount; i++) {
      if ((hs = atts.get(ATTR_SHADING_LIGHTPOSX_ + i)) != null) {
        pFlame.getShadingInfo().setLightPosX(i, Double.parseDouble(hs));
      }
      if ((hs = atts.get(ATTR_SHADING_LIGHTPOSY_ + i)) != null) {
        pFlame.getShadingInfo().setLightPosY(i, Double.parseDouble(hs));
      }
      if ((hs = atts.get(ATTR_SHADING_LIGHTPOSZ_ + i)) != null) {
        pFlame.getShadingInfo().setLightPosZ(i, Double.parseDouble(hs));
      }
      if ((hs = atts.get(ATTR_SHADING_LIGHTRED_ + i)) != null) {
        pFlame.getShadingInfo().setLightRed(i, Integer.parseInt(hs));
      }
      if ((hs = atts.get(ATTR_SHADING_LIGHTGREEN_ + i)) != null) {
        pFlame.getShadingInfo().setLightGreen(i, Integer.parseInt(hs));
      }
      if ((hs = atts.get(ATTR_SHADING_LIGHTBLUE_ + i)) != null) {
        pFlame.getShadingInfo().setLightBlue(i, Integer.parseInt(hs));
      }
    }
    if ((hs = atts.get(ATTR_SHADING_BLUR_RADIUS)) != null) {
      pFlame.getShadingInfo().setBlurRadius(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_BLUR_FADE)) != null) {
      pFlame.getShadingInfo().setBlurFade(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_BLUR_FALLOFF)) != null) {
      pFlame.getShadingInfo().setBlurFallOff(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_NEW_LINEAR)) != null) {
      pFlame.setPreserveZ(hs.length() > 0 && Integer.parseInt(hs) == 1);
    }

    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_RADIUS)) != null) {
      pFlame.getShadingInfo().setDistanceColorRadius(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_SCALE)) != null) {
      pFlame.getShadingInfo().setDistanceColorScale(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_EXPONENT)) != null) {
      pFlame.getShadingInfo().setDistanceColorExponent(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_OFFSETX)) != null) {
      pFlame.getShadingInfo().setDistanceColorOffsetX(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_OFFSETY)) != null) {
      pFlame.getShadingInfo().setDistanceColorOffsetY(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_OFFSETZ)) != null) {
      pFlame.getShadingInfo().setDistanceColorOffsetZ(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_STYLE)) != null) {
      pFlame.getShadingInfo().setDistanceColorStyle(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_COORDINATE)) != null) {
      pFlame.getShadingInfo().setDistanceColorCoordinate(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_SHADING_DISTANCE_COLOR_SHIFT)) != null) {
      pFlame.getShadingInfo().setDistanceColorShift(Double.parseDouble(hs));
    }
  }

  private static final String ATTR_WEIGHT = "weight";
  private static final String ATTR_COLOR = "color";
  private static final String ATTR_OPACITY = "opacity";
  private static final String ATTR_COEFS = "coefs";
  private static final String ATTR_POST = "post";
  private static final String ATTR_CHAOS = "chaos";
  private static final String ATTR_SYMMETRY = "symmetry";
  private static final String ATTR_ANTIALIAS_AMOUNT = "antialias_amount";
  private static final String ATTR_ANTIALIAS_RADIUS = "antialias_radius";

  private void parseXFormAttributes(XForm pXForm, String pXML) {
    XMLAttributes atts = Tools.parseAttributes(pXML);
    String hs;
    if ((hs = atts.get(ATTR_NAME)) != null) {
      pXForm.setName(hs);
    }
    if ((hs = atts.get(ATTR_WEIGHT)) != null) {
      pXForm.setWeight(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_COLOR)) != null) {
      pXForm.setColor(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_ANTIALIAS_AMOUNT)) != null) {
      pXForm.setAntialiasAmount(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_ANTIALIAS_RADIUS)) != null) {
      pXForm.setAntialiasRadius(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_OPACITY)) != null) {
      double opacity = Double.parseDouble(hs);
      pXForm.setOpacity(opacity);
      if (Math.abs(opacity) <= MathLib.EPSILON) {
        pXForm.setDrawMode(DrawMode.HIDDEN);
      }
      else if (Math.abs(opacity - 1.0) > MathLib.EPSILON) {
        pXForm.setDrawMode(DrawMode.OPAQUE);
      }
      else {
        pXForm.setDrawMode(DrawMode.NORMAL);
      }
    }
    if ((hs = atts.get(ATTR_SYMMETRY)) != null) {
      pXForm.setColorSymmetry(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_COEFS)) != null) {
      String s[] = hs.split(" ");
      pXForm.setCoeff00(Double.parseDouble(s[0]));
      pXForm.setCoeff01(Double.parseDouble(s[1]));
      pXForm.setCoeff10(Double.parseDouble(s[2]));
      pXForm.setCoeff11(Double.parseDouble(s[3]));
      pXForm.setCoeff20(Double.parseDouble(s[4]));
      pXForm.setCoeff21(Double.parseDouble(s[5]));
    }
    if ((hs = atts.get(ATTR_POST)) != null) {
      String s[] = hs.split(" ");
      pXForm.setPostCoeff00(Double.parseDouble(s[0]));
      pXForm.setPostCoeff01(Double.parseDouble(s[1]));
      pXForm.setPostCoeff10(Double.parseDouble(s[2]));
      pXForm.setPostCoeff11(Double.parseDouble(s[3]));
      pXForm.setPostCoeff20(Double.parseDouble(s[4]));
      pXForm.setPostCoeff21(Double.parseDouble(s[5]));
    }
    if ((hs = atts.get(ATTR_CHAOS)) != null) {
      String s[] = hs.split(" ");
      for (int i = 0; i < s.length; i++) {
        pXForm.getModifiedWeights()[i] = Double.parseDouble(s[i]);
      }
    }
    // variations
    {
      List<String> variationNameList = VariationFuncList.getNameList();
      Map<String, String> aliasMap = VariationFuncList.getAliasMap();

      for (XMLAttribute attr : atts.getAttributes()) {
        String rawName = attr.getName();
        String name = removeIndexFromAttr(rawName);
        String varName = name;
        boolean hasVariation = variationNameList.indexOf(varName) >= 0;
        if (!hasVariation) {
          String aliasName = aliasMap.get(name);
          if (aliasName != null) {
            varName = aliasName;
            hasVariation = variationNameList.indexOf(varName) >= 0;
          }
        }
        if (hasVariation) {
          VariationFunc varFunc = VariationFuncList.getVariationFuncInstance(varName);
          Variation variation = pXForm.addVariation(Double.parseDouble(atts.get(name)), varFunc);
          // params
          {
            String paramNames[] = variation.getFunc().getParameterNames();
            String paramAltNames[] = variation.getFunc().getParameterAlternativeNames();
            if (paramNames != null) {
              if (paramAltNames != null && paramAltNames.length != paramNames.length) {
                paramAltNames = null;
              }
              for (int i = 0; i < paramNames.length; i++) {
                String pName = paramNames[i];
                String pHs;
                if ((pHs = atts.get(rawName + "_" + pName)) != null) {
                  variation.getFunc().setParameter(pName, Double.parseDouble(pHs));
                }
                // altNames can only be come from flames which were not created by JWF, so no need to handle index here 
                else if (paramAltNames != null && ((pHs = atts.get(paramAltNames[i])) != null)) {
                  variation.getFunc().setParameter(pName, Double.parseDouble(pHs));
                }
              }
            }
          }
          // ressources 
          {
            String ressNames[] = variation.getFunc().getRessourceNames();
            if (ressNames != null) {
              for (String pName : ressNames) {
                String pHs;
                if ((pHs = atts.get(name + "_" + pName)) != null) {
                  variation.getFunc().setRessource(pName, Tools.hexStringToByteArray(pHs));
                }
              }
            }
          }
          //
        }
      }
    }
  }

  private String removeIndexFromAttr(String pName) {
    int s = pName.indexOf("#");
    if (s < 0) {
      return pName;
    }
    int e = pName.indexOf("#", s + 1);
    if (e < 0) {
      return pName;
    }
    if (e == pName.length() - 1) {
      return pName.substring(0, s);
    }
    else {
      return pName.substring(0, s) + pName.substring(e + 1, pName.length());
    }
  }

  public List<Flame> readFlamesfromXML(String pXML) {
    List<Flame> res = new ArrayList<Flame>();
    int pFlames = 0;
    while (true) {
      String flameXML;
      {
        int ps = pXML.indexOf("<flame ", pFlames);
        if (ps < 0)
          break;
        int pe = pXML.indexOf("</flame>", ps + 1);
        if (pe < 0)
          break;
        pFlames = pe + 8;
        flameXML = pXML.substring(ps, pFlames);
      }

      Flame flame = new Flame();
      res.add(flame);
      // Flame attributes
      {
        int ps = flameXML.indexOf("<flame ");
        int pe = -1;
        boolean qt = false;
        for (int i = ps + 1; i < flameXML.length(); i++) {
          if (flameXML.charAt(i) == '\"') {
            qt = !qt;
          }
          else if (!qt && flameXML.charAt(i) == '>') {
            pe = i;
            break;
          }
        }
        String hs = flameXML.substring(ps + 7, pe);
        parseFlameAttributes(flame, hs);
      }
      // XForms
      {
        int p = 0;
        while (true) {
          int ps = flameXML.indexOf("<xform ", p + 1);
          if (ps < 0)
            break;
          int pe = flameXML.indexOf("</xform>", ps + 1);
          if (pe < 0) {
            pe = flameXML.indexOf("/>", ps + 1);
          }
          String hs = flameXML.substring(ps + 7, pe);
          XForm xForm = new XForm();
          parseXFormAttributes(xForm, hs);
          flame.getXForms().add(xForm);
          p = pe + 2;
        }
      }
      // FinalXForm
      {
        int p = 0;
        while (true) {
          int ps = flameXML.indexOf("<finalxform ", p + 1);
          if (ps < 0)
            break;
          int pe = flameXML.indexOf("</finalxform>", ps + 1);
          if (pe < 0) {
            pe = flameXML.indexOf("/>", ps + 1);
          }
          String hs = flameXML.substring(ps + 7, pe);
          XForm xForm = new XForm();
          parseXFormAttributes(xForm, hs);
          flame.getFinalXForms().add(xForm);
          p = pe + 2;
        }
      }
      // Colors
      {
        int p = 0;
        while (true) {
          int ps = flameXML.indexOf("<color ", p + 1);
          if (ps < 0)
            break;
          int pe = flameXML.indexOf("/>", ps + 1);
          String hs = flameXML.substring(ps + 7, pe);
          {
            int index = 0;
            int r = 0, g = 0, b = 0;
            XMLAttributes atts = Tools.parseAttributes(hs);
            String attr;
            if ((attr = atts.get(ATTR_INDEX)) != null) {
              index = Integer.parseInt(attr);
            }
            if ((attr = atts.get(ATTR_RGB)) != null) {
              String s[] = attr.split(" ");
              r = Tools.FTOI(Double.parseDouble(s[0]));
              g = Tools.FTOI(Double.parseDouble(s[1]));
              b = Tools.FTOI(Double.parseDouble(s[2]));
            }
            flame.getPalette().setColor(index, r, g, b);
          }
          p = pe + 2;
        }
      }
      // Palette
      {
        int ps = flameXML.indexOf("<palette ");
        if (ps >= 0) {
          ps = flameXML.indexOf(">", ps + 1);
          int pe = flameXML.indexOf("</palette>", ps + 1);
          String hs = flameXML.substring(ps + 1, pe);
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < hs.length(); i++) {
            char c = hs.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
              sb.append(c);
            }
          }
          hs = sb.toString();
          if ((hs.length() % 6) != 0)
            throw new RuntimeException("Invalid/unknown palette");
          int index = 0;
          for (int i = 0; i < hs.length(); i += 6) {
            int r = Integer.parseInt(hs.substring(i, i + 2), 16);
            int g = Integer.parseInt(hs.substring(i + 2, i + 4), 16);
            int b = Integer.parseInt(hs.substring(i + 4, i + 6), 16);
            // System.out.println(hs.substring(i, i + 2) + "#" + hs.substring(i + 2, i + 4) + "#" + hs.substring(i + 4, i + 6));
            // System.out.println("  flame->palette->setColor(" + index + "," + r + "," + g + "," + b + ");");
            flame.getPalette().setColor(index++, r, g, b);
          }
        }
      }
    }
    return res;
  }

}
