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
package org.jwildfire.create.tina.dance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

import org.jwildfire.base.Prefs;
import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.audio.JLayerInterface;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.dance.action.ActionRecorder;
import org.jwildfire.create.tina.dance.action.PostRecordFlameGenerator;
import org.jwildfire.create.tina.dance.model.FlamePropertyPath;
import org.jwildfire.create.tina.dance.motion.Motion;
import org.jwildfire.create.tina.dance.motion.MotionCreator;
import org.jwildfire.create.tina.dance.motion.MotionCreatorType;
import org.jwildfire.create.tina.dance.motion.MotionLink;
import org.jwildfire.create.tina.dance.motion.MotionType;
import org.jwildfire.create.tina.io.Flam3Reader;
import org.jwildfire.create.tina.io.JWFDanceReader;
import org.jwildfire.create.tina.io.JWFDanceWriter;
import org.jwildfire.create.tina.randomflame.RandomFlameGenerator;
import org.jwildfire.create.tina.randomflame.RandomFlameGeneratorList;
import org.jwildfire.create.tina.randomflame.RandomFlameGeneratorSampler;
import org.jwildfire.create.tina.render.FlameRenderer;
import org.jwildfire.create.tina.render.RenderInfo;
import org.jwildfire.create.tina.render.RenderedFlame;
import org.jwildfire.create.tina.swing.FlameFileChooser;
import org.jwildfire.create.tina.swing.FlameHolder;
import org.jwildfire.create.tina.swing.FlamePanel;
import org.jwildfire.create.tina.swing.JWFDanceFileChooser;
import org.jwildfire.create.tina.swing.JWFNumberField;
import org.jwildfire.create.tina.swing.SoundFileChooser;
import org.jwildfire.create.tina.swing.TinaController;
import org.jwildfire.create.tina.variation.Linear3DFunc;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.swing.ErrorHandler;
import org.jwildfire.swing.ImagePanel;
import org.jwildfire.swing.PropertyPanel;
import org.jwildfire.transform.TextTransformer;
import org.jwildfire.transform.TextTransformer.FontStyle;
import org.jwildfire.transform.TextTransformer.HAlignment;
import org.jwildfire.transform.TextTransformer.Mode;
import org.jwildfire.transform.TextTransformer.VAlignment;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;

public class DancingFractalsController {
  public static final int PAGE_INDEX = 4;
  private final ErrorHandler errorHandler;
  private final TinaController parentCtrl;
  private final Prefs prefs;
  private final JButton loadSoundBtn;
  private final JButton addFromClipboardBtn;
  private final JButton addFromEditorBtn;
  private final JButton addFromDiscBtn;
  private final JWFNumberField randomCountIEd;
  private final JButton genRandFlamesBtn;
  private final JComboBox randomGenCmb;
  private final JPanel poolFlamePreviewPnl;
  private final JSlider borderSizeSlider;
  private final JPanel flameRootPanel;
  private final JPanel graph1RootPanel;
  private final JButton flameToEditorBtn;
  private final JButton deleteFlameBtn;
  private final JTextField framesPerSecondIEd;
  private final JTextField morphFrameCountIEd;
  private final JButton startShowButton;
  private final JButton stopShowButton;
  private final JCheckBox doRecordCBx;
  private final JComboBox flamesCmb;
  private final JCheckBox drawTrianglesCbx;
  private final JCheckBox drawFFTCbx;
  private final JCheckBox drawFPSCbx;
  private final JTree flamePropertiesTree;
  private final JPanel motionPropertyRootPnl;
  private PropertyPanel motionPropertyPnl = null;
  private final JTable motionTable;
  private final JComboBox addMotionCmb;
  private final JButton addMotionBtn;
  private final JButton deleteMotionBtn;
  private final JButton linkMotionBtn;
  private final JButton unlinkMotionBtn;
  private final JComboBox createMotionsCmb;
  private final JButton clearMotionsBtn;
  private final JButton loadProjectBtn;
  private final JButton saveProjectBtn;
  private final JTable motionLinksTable;

  private FlamePanel flamePanel = null;
  private FlamePanel poolFlamePreviewFlamePanel = null;
  private ImagePanel graph1Panel = null;
  private DancingFlameProject project = new DancingFlameProject();
  private boolean refreshing;
  private RealtimeAnimRenderThread renderThread;
  private ActionRecorder actionRecorder;

  private boolean running = false;
  private final PoolFlameHolder poolFlameHolder;
  private final FlamePropertiesTreeService flamePropertiesTreeService;
  private JLayerInterface jLayer = new JLayerInterface();

  public DancingFractalsController(TinaController pParent, ErrorHandler pErrorHandler, JPanel pRealtimeFlamePnl, JPanel pRealtimeGraph1Pnl,
      JButton pLoadSoundBtn, JButton pAddFromClipboardBtn, JButton pAddFromEditorBtn, JButton pAddFromDiscBtn, JWFNumberField pRandomCountIEd,
      JButton pGenRandFlamesBtn, JComboBox pRandomGenCmb, JPanel pPoolFlamePreviewPnl, JSlider pBorderSizeSlider,
      JButton pFlameToEditorBtn, JButton pDeleteFlameBtn, JTextField pFramesPerSecondIEd, JTextField pMorphFrameCountIEd,
      JButton pStartShowButton, JButton pStopShowButton, JCheckBox pDoRecordCBx, JComboBox pFlamesCmb, JCheckBox pDrawTrianglesCbx, JCheckBox pDrawFFTCbx, JCheckBox pDrawFPSCbx, JTree pFlamePropertiesTree,
      JPanel pMotionPropertyRootPnl, JTable pMotionTable, JComboBox pAddMotionCmb, JButton pAddMotionBtn, JButton pDeleteMotionBtn,
      JButton pLinkMotionBtn, JButton pUnlinkMotionBtn, JComboBox pCreateMotionsCmb, JButton pClearMotionsBtn,
      JButton pLoadProjectBtn, JButton pSaveProjectBtn, JTable pMotionLinksTable) {
    flamePropertiesTreeService = new FlamePropertiesTreeService();

    parentCtrl = pParent;
    errorHandler = pErrorHandler;
    prefs = parentCtrl.getPrefs();
    flameRootPanel = pRealtimeFlamePnl;
    graph1RootPanel = pRealtimeGraph1Pnl;
    loadSoundBtn = pLoadSoundBtn;
    addFromClipboardBtn = pAddFromClipboardBtn;
    addFromEditorBtn = pAddFromEditorBtn;
    addFromDiscBtn = pAddFromDiscBtn;
    randomCountIEd = pRandomCountIEd;
    genRandFlamesBtn = pGenRandFlamesBtn;
    randomGenCmb = pRandomGenCmb;
    poolFlamePreviewPnl = pPoolFlamePreviewPnl;
    borderSizeSlider = pBorderSizeSlider;
    flameToEditorBtn = pFlameToEditorBtn;
    deleteFlameBtn = pDeleteFlameBtn;
    framesPerSecondIEd = pFramesPerSecondIEd;
    morphFrameCountIEd = pMorphFrameCountIEd;
    startShowButton = pStartShowButton;
    stopShowButton = pStopShowButton;
    doRecordCBx = pDoRecordCBx;
    flamesCmb = pFlamesCmb;
    drawTrianglesCbx = pDrawTrianglesCbx;
    drawFFTCbx = pDrawFFTCbx;
    drawFPSCbx = pDrawFPSCbx;
    motionPropertyRootPnl = pMotionPropertyRootPnl;
    motionTable = pMotionTable;
    addMotionCmb = pAddMotionCmb;
    addMotionBtn = pAddMotionBtn;
    deleteMotionBtn = pDeleteMotionBtn;
    linkMotionBtn = pLinkMotionBtn;
    unlinkMotionBtn = pUnlinkMotionBtn;
    createMotionsCmb = pCreateMotionsCmb;
    clearMotionsBtn = pClearMotionsBtn;
    loadProjectBtn = pLoadProjectBtn;
    saveProjectBtn = pSaveProjectBtn;
    motionLinksTable = pMotionLinksTable;

    addMotionCmb.addItem(MotionType.FFT);
    addMotionCmb.addItem(MotionType.SAWTOOTH);
    addMotionCmb.addItem(MotionType.SPLINE);
    addMotionCmb.setSelectedItem(MotionType.FFT);
    createMotionsCmb.addItem(MotionCreatorType.DEFAULT);
    createMotionsCmb.setSelectedItem(MotionCreatorType.DEFAULT);

    flamePropertiesTree = pFlamePropertiesTree;
    poolFlameHolder = new PoolFlameHolder();

    refreshProjectFlames();

    enableControls();
  }

  private FlamePanel getFlamePanel() {
    if (flamePanel == null && flameRootPanel != null) {
      int borderWidth = flameRootPanel.getBorder().getBorderInsets(flameRootPanel).left;
      int width = flameRootPanel.getWidth() - borderWidth;
      int height = flameRootPanel.getHeight() - borderWidth;
      if (width < 16 || height < 16)
        return null;
      SimpleImage img = new SimpleImage(width, height);
      img.fillBackground(0, 0, 0);
      flamePanel = new FlamePanel(img, 0, 0, flameRootPanel.getWidth() - borderWidth, null);
      flamePanel.setRenderWidth(640);
      flamePanel.setRenderHeight(480);
      flameRootPanel.add(flamePanel, BorderLayout.CENTER);
      flameRootPanel.getParent().validate();
      flameRootPanel.repaint();
    }
    flamePanel.setFlameHolder(renderThread);
    return flamePanel;
  }

  private FlamePanel getPoolPreviewFlamePanel() {
    if (poolFlamePreviewFlamePanel == null && poolFlamePreviewPnl != null) {
      int width = poolFlamePreviewPnl.getWidth();
      int height = poolFlamePreviewPnl.getHeight();
      SimpleImage img = new SimpleImage(width, height);
      img.fillBackground(0, 0, 0);
      poolFlamePreviewFlamePanel = new FlamePanel(img, 0, 0, poolFlamePreviewPnl.getWidth(), poolFlameHolder);
      poolFlamePreviewFlamePanel.setRenderWidth(640);
      poolFlamePreviewFlamePanel.setRenderHeight(480);
      poolFlamePreviewFlamePanel.setDrawTriangles(false);
      poolFlamePreviewPnl.add(poolFlamePreviewFlamePanel, BorderLayout.CENTER);
      poolFlamePreviewPnl.getParent().validate();
      poolFlamePreviewPnl.repaint();
    }
    return poolFlamePreviewFlamePanel;
  }

  private ImagePanel getGraph1Panel() {
    if (graph1Panel == null && graph1RootPanel != null) {
      int width = graph1RootPanel.getWidth();
      int height = graph1RootPanel.getHeight();
      SimpleImage img = new SimpleImage(width, height);
      img.fillBackground(0, 0, 0);
      graph1Panel = new ImagePanel(img, 0, 0, graph1RootPanel.getWidth());
      graph1RootPanel.add(graph1Panel, BorderLayout.CENTER);
      graph1RootPanel.getParent().validate();
      graph1RootPanel.repaint();
    }
    return graph1Panel;
  }

  public void refreshFlameImage(Flame flame, boolean pDrawTriangles, double pFPS, boolean pDrawFPS) {
    FlamePanel imgPanel = getFlamePanel();
    if (imgPanel == null)
      return;
    Rectangle bounds = imgPanel.getImageBounds();
    int width = bounds.width;
    int height = bounds.height;
    if (width >= 16 && height >= 16) {
      RenderInfo info = new RenderInfo(width, height);
      if (flame != null) {
        double oldSpatialFilterRadius = flame.getSpatialFilterRadius();
        double oldSampleDensity = flame.getSampleDensity();
        imgPanel.setDrawTriangles(pDrawTriangles);
        try {
          double wScl = (double) info.getImageWidth() / (double) flame.getWidth();
          double hScl = (double) info.getImageHeight() / (double) flame.getHeight();
          flame.setPixelsPerUnit((wScl + hScl) * 0.5 * flame.getPixelsPerUnit());
          flame.setWidth(info.getImageWidth());
          flame.setHeight(info.getImageHeight());

          Flame renderFlame = new FlamePreparer(prefs).createRenderFlame(flame);
          FlameRenderer renderer = new FlameRenderer(renderFlame, prefs, false, false);
          renderer.setProgressUpdater(null);
          RenderedFlame res = renderer.renderFlame(info);
          SimpleImage img = res.getImage();
          if (pDrawFPS) {
            TextTransformer txt = new TextTransformer();
            txt.setText1("fps: " + Tools.doubleToString(pFPS));
            txt.setAntialiasing(false);
            txt.setColor(Color.LIGHT_GRAY);
            txt.setMode(Mode.NORMAL);
            txt.setFontStyle(FontStyle.PLAIN);
            txt.setFontName("Arial");
            txt.setFontSize(10);
            txt.setHAlign(HAlignment.LEFT);
            txt.setVAlign(VAlignment.BOTTOM);
            txt.transformImage(img);
          }
          imgPanel.setImage(img);
        }
        finally {
          flame.setSpatialFilterRadius(oldSpatialFilterRadius);
          flame.setSampleDensity(oldSampleDensity);
        }
      }
    }
    else {
      try {
        imgPanel.setImage(new SimpleImage(width, height));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    flameRootPanel.repaint();
  }

  public void refreshPoolPreviewFlameImage(Flame flame) {
    FlamePanel imgPanel = getPoolPreviewFlamePanel();
    if (imgPanel == null)
      return;
    Rectangle bounds = imgPanel.getImageBounds();
    int width = bounds.width;
    int height = bounds.height;
    if (width >= 16 && height >= 16) {
      RenderInfo info = new RenderInfo(width, height);
      if (flame != null) {
        imgPanel.setDrawTriangles(false);
        double wScl = (double) info.getImageWidth() / (double) flame.getWidth();
        double hScl = (double) info.getImageHeight() / (double) flame.getHeight();
        flame.setPixelsPerUnit((wScl + hScl) * 0.5 * flame.getPixelsPerUnit());
        flame.setWidth(info.getImageWidth());
        flame.setHeight(info.getImageHeight());
        Flame renderFlame = new FlamePreparer(prefs).createRenderFlame(flame);
        FlameRenderer renderer = new FlameRenderer(renderFlame, prefs, false, false);
        renderer.setProgressUpdater(null);
        RenderedFlame res = renderer.renderFlame(info);
        imgPanel.setImage(res.getImage());
      }
      else {
        imgPanel.setImage(new SimpleImage(width, height));
      }
    }
    else {
      imgPanel.setImage(new SimpleImage(width, height));
    }
    poolFlamePreviewPnl.repaint();
  }

  public void startShow() {
    try {
      if (project.getFlames().size() == 0)
        throw new Exception("No flames to animate");
      jLayer.stop();
      if (project.getSoundFilename() != null && project.getSoundFilename().length() > 0) {
        jLayer.play(project.getSoundFilename());
      }
      startRender();
      running = true;
      enableControls();
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void importFlame(Flame pFlame) {
    if (pFlame != null) {
      project.getFlames().add(validateDancingFlame(pFlame.makeCopy()));
      refreshProjectFlames();
      enableControls();
    }
  }

  public void startRender() throws Exception {
    stopRender();
    Flame selFlame = flamesCmb.getSelectedIndex() >= 0 && flamesCmb.getSelectedIndex() < project.getFlames().size() ? project.getFlames().get(flamesCmb.getSelectedIndex()) : null;
    renderThread = new RealtimeAnimRenderThread(this);
    renderThread.getFlameStack().addFlame(selFlame, 0, project.getMotions(selFlame));
    actionRecorder = new ActionRecorder(renderThread);
    renderThread.setFFTData(project.getFFT());
    renderThread.setMusicPlayer(jLayer);
    renderThread.setFFTPanel(getGraph1Panel());
    renderThread.setFramesPerSecond(Integer.parseInt(framesPerSecondIEd.getText()));
    renderThread.setDrawTriangles(drawTrianglesCbx.isSelected());
    renderThread.setDrawFFT(drawFFTCbx.isSelected());
    renderThread.setDrawFPS(drawFPSCbx.isSelected());
    actionRecorder.recordStart(selFlame);
    new Thread(renderThread).start();
  }

  public void stopRender() throws Exception {
    if (renderThread != null) {
      if (doRecordCBx.isSelected()) {
        actionRecorder.recordStop();
      }
      renderThread.setForceAbort(true);
      if (doRecordCBx.isSelected()) {
        JFileChooser chooser = new FlameFileChooser(prefs);
        if (prefs.getOutputFlamePath() != null) {
          try {
            chooser.setCurrentDirectory(new File(prefs.getOutputFlamePath()));
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        if (chooser.showSaveDialog(flameRootPanel) == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          prefs.setLastOutputFlameFile(file);
          PostRecordFlameGenerator generator = new PostRecordFlameGenerator(getParentCtrl().getPrefs(), project, actionRecorder, renderThread, project.getFFT());
          generator.createRecordedFlameFiles(file.getAbsolutePath());
        }
      }
      renderThread = null;
      actionRecorder = null;
    }
  }

  public void genRandomFlames() {
    try {
      final int IMG_WIDTH = 80;
      final int IMG_HEIGHT = 60;
      int count = (int) ((Double) randomCountIEd.getValue() + 0.5);
      for (int i = 0; i < count; i++) {

        RandomFlameGenerator randGen = RandomFlameGeneratorList.getRandomFlameGeneratorInstance((String) randomGenCmb.getSelectedItem(), true);
        int palettePoints = 3 + (int) (Math.random() * 68.0);
        RandomFlameGeneratorSampler sampler = new RandomFlameGeneratorSampler(IMG_WIDTH, IMG_HEIGHT, prefs, randGen, palettePoints);
        project.getFlames().add(validateDancingFlame(sampler.createSample().getFlame()));
      }
      refreshProjectFlames();
      enableControls();
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void loadSoundButton_clicked() {
    try {
      JFileChooser chooser = new SoundFileChooser(prefs);
      if (prefs.getInputFlamePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getInputSoundFilePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (chooser.showOpenDialog(flameRootPanel) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        prefs.setLastInputSoundFile(file);
        project.setSoundFilename(jLayer, file.getAbsolutePath());
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  private void refreshProjectFlames() {
    boolean oldRefreshing = refreshing;
    refreshing = true;
    try {
      refreshFlamesCmb();
      flamePropertiesTreeService.refreshFlamePropertiesTree(flamePropertiesTree, project);
      refreshMotionTable();
    }
    finally {
      refreshing = oldRefreshing;
    }
  }

  private void refreshMotionTable() {
    final int COL_MOTION = 0;
    final int COL_START_FRAME = 1;
    final int COL_END_FRAME = 2;
    motionTable.setModel(new DefaultTableModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public int getRowCount() {
        return project.getMotions().size();
      }

      @Override
      public int getColumnCount() {
        return 3;
      }

      @Override
      public String getColumnName(int columnIndex) {
        switch (columnIndex) {
          case COL_MOTION:
            return "Motion";
          case COL_START_FRAME:
            return "Start";
          case COL_END_FRAME:
            return "End";
        }
        return null;
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
        Motion motion = rowIndex < project.getMotions().size() ? project.getMotions().get(rowIndex) : null;
        if (motion != null) {
          switch (columnIndex) {
            case COL_MOTION:
              return motion.toString();
            case COL_START_FRAME:
              return (motion.getStartFrame() != null) ? String.valueOf(motion.getStartFrame()) : "";
            case COL_END_FRAME:
              return (motion.getEndFrame() != null) ? String.valueOf(motion.getEndFrame()) : "";
          }
        }
        return null;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

    });
    motionTable.getTableHeader().setFont(motionTable.getFont());
    motionTable.getColumnModel().getColumn(COL_MOTION).setWidth(120);
    motionTable.getColumnModel().getColumn(COL_START_FRAME).setPreferredWidth(10);
    motionTable.getColumnModel().getColumn(COL_END_FRAME).setWidth(10);
  }

  private void refreshMotionLinksTable() {
    final int COL_FLAME = 0;
    final int COL_PROPERTY = 1;
    final Motion currMotion = getSelectedMotion();
    motionLinksTable.setModel(new DefaultTableModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public int getRowCount() {
        return currMotion != null ? currMotion.getMotionLinks().size() : 0;
      }

      @Override
      public int getColumnCount() {
        return 2;
      }

      @Override
      public String getColumnName(int columnIndex) {
        switch (columnIndex) {
          case COL_FLAME:
            return "Flame";
          case COL_PROPERTY:
            return "Property";
        }
        return null;
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
        MotionLink motionLink = rowIndex < currMotion.getMotionLinks().size() ? currMotion.getMotionLinks().get(rowIndex) : null;
        if (motionLink != null) {
          switch (columnIndex) {
            case COL_FLAME:
              return flamePropertiesTreeService.getFlameCaption(motionLink.getProperyPath().getFlame());
            case COL_PROPERTY:
              return motionLink.getProperyPath().getPath();
          }
        }
        return null;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

    });
    motionLinksTable.getTableHeader().setFont(motionLinksTable.getFont());
    motionLinksTable.getColumnModel().getColumn(COL_FLAME).setWidth(80);
    motionLinksTable.getColumnModel().getColumn(COL_PROPERTY).setPreferredWidth(120);
  }

  private Motion getSelectedMotion() {
    return motionTable.getSelectedRow() >= 0 && motionTable.getSelectedRow() < project.getMotions().size() ? project.getMotions().get(motionTable.getSelectedRow()) : null;
  }

  private void refreshFlamesCmb() {
    Flame selFlame = flamesCmb.getSelectedIndex() >= 0 && flamesCmb.getSelectedIndex() < project.getFlames().size() ? project.getFlames().get(flamesCmb.getSelectedIndex()) : null;
    int newSelIdx = -1;
    flamesCmb.removeAllItems();
    for (int i = 0; i < project.getFlames().size(); i++) {
      Flame flame = project.getFlames().get(i);
      if (newSelIdx < 0 && flame.equals(selFlame)) {
        newSelIdx = i;
      }
      flamesCmb.addItem(flamePropertiesTreeService.getFlameCaption(flame));
    }
    flamesCmb.setSelectedIndex(newSelIdx >= 0 ? newSelIdx : project.getFlames().size() > 0 ? 0 : -1);
  }

  public void loadFlameFromClipboardButton_clicked() {
    List<Flame> newFlames = null;
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents(clipboard);
      if (clipData != null) {
        if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          String xml = (String) (clipData.getTransferData(
              DataFlavor.stringFlavor));
          newFlames = new Flam3Reader(prefs).readFlamesfromXML(xml);
        }
      }
      if (newFlames == null || newFlames.size() < 1) {
        throw new Exception("There is currently no valid flame in the clipboard");
      }
      else {
        for (Flame newFlame : newFlames) {
          project.getFlames().add(validateDancingFlame(newFlame));
        }
        refreshProjectFlames();
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void loadFlameButton_clicked() {
    try {
      JFileChooser chooser = new FlameFileChooser(prefs);
      if (prefs.getInputFlamePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getInputFlamePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      chooser.setMultiSelectionEnabled(true);
      if (chooser.showOpenDialog(poolFlamePreviewPnl) == JFileChooser.APPROVE_OPTION) {
        for (File file : chooser.getSelectedFiles()) {
          List<Flame> newFlames = new Flam3Reader(prefs).readFlames(file.getAbsolutePath());
          prefs.setLastInputFlameFile(file);
          if (newFlames != null && newFlames.size() > 0) {
            for (Flame newFlame : newFlames) {
              project.getFlames().add(validateDancingFlame(newFlame));
            }
          }
        }
        refreshProjectFlames();
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void borderSizeSlider_changed() {
    int value = borderSizeSlider.getValue();
    int currValue = flameRootPanel.getBorder().getBorderInsets(flameRootPanel).left;
    if (currValue != value) {
      flameRootPanel.setBorder(new EmptyBorder(0, 0, value, value));
      if (flamePanel != null) {
        FlamePanel oldFlamePanel = flamePanel;
        flamePanel = null;
        flameRootPanel.remove(oldFlamePanel);
        flameRootPanel.getParent().validate();
        flameRootPanel.repaint();
      }
    }
  }

  public void flameToEditorBtn_clicked() {
    Flame flame = poolFlameHolder.getFlame();
    if (flame != null) {
      parentCtrl.importFlame(flame, true);
      parentCtrl.getRootTabbedPane().setSelectedIndex(0);
    }
  }

  public void deleteFlameBtn_clicked() {
    Flame flame = poolFlameHolder.getFlame();
    if (flame != null) {
      int idx = flamePropertiesTree.getSelectionRows()[0];
      // cant remove the flame by object reference because its a clone
      project.getFlames().remove(idx);
      refreshProjectFlames();
      if (project.getFlames().size() == 0) {
        flamePropertiesTree_changed(null);
      }
      enableControls();
    }
  }

  public void stopShow() {
    try {
      if (jLayer != null) {
        jLayer.stop();
      }
      stopRender();
      running = false;
      enableControls();
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  private void enableControls() {
    loadSoundBtn.setEnabled(!running);
    addFromClipboardBtn.setEnabled(!running);
    addFromEditorBtn.setEnabled(!running);
    addFromDiscBtn.setEnabled(!running);
    randomCountIEd.setEnabled(!running);
    genRandFlamesBtn.setEnabled(!running);
    randomGenCmb.setEnabled(!running);
    boolean flameSelected = poolFlameHolder.getFlame() != null;
    flameToEditorBtn.setEnabled(flameSelected);
    deleteFlameBtn.setEnabled(flameSelected);

    framesPerSecondIEd.setEnabled(!running);
    borderSizeSlider.setEnabled(true);
    morphFrameCountIEd.setEnabled(true);
    startShowButton.setEnabled(!running && project.getFlames().size() > 0);
    stopShowButton.setEnabled(running);
    doRecordCBx.setEnabled(!running);

    motionTable.setEnabled(!running);
    addMotionCmb.setEnabled(!running);
    addMotionBtn.setEnabled(!running);
    Motion selMotion = getSelectedMotion();

    deleteMotionBtn.setEnabled(!running && selMotion != null);
    boolean plainPropertySelected = flamePropertiesTreeService.isPlainPropertySelected(flamePropertiesTree);
    {
      boolean linkMotionEnabled = false;
      if (!running && selMotion != null && selMotion.getParent() == null) {
        if (plainPropertySelected) {
          FlamePropertyPath selPath = flamePropertiesTreeService.getSelectedPropertyPath(flamePropertiesTree);
          linkMotionEnabled = !selMotion.hasLink(selPath);
        }
      }
      linkMotionBtn.setEnabled(linkMotionEnabled);
      unlinkMotionBtn.setEnabled(selMotion != null && motionLinksTable.getSelectedRow() >= 0 && motionLinksTable.getSelectedRow() < selMotion.getMotionLinks().size());
    }

    createMotionsCmb.setEnabled(!running);
    clearMotionsBtn.setEnabled(!running && project.getMotions().size() > 0);
    loadProjectBtn.setEnabled(!running);
    saveProjectBtn.setEnabled(!running);
  }

  protected TinaController getParentCtrl() {
    return parentCtrl;
  }

  public void drawTrianglesCBx_changed() {
    if (renderThread != null) {
      renderThread.setDrawTriangles(drawTrianglesCbx.isSelected());
    }
  }

  public void drawFFTCBx_changed() {
    if (renderThread != null) {
      renderThread.setDrawFFT(drawFFTCbx.isSelected());
    }
  }

  public void drawFPSCBx_changed() {
    if (renderThread != null) {
      renderThread.setDrawFPS(drawFPSCbx.isSelected());
    }
  }

  public void flameCmb_changed() {
    if (!refreshing && renderThread != null) {
      Flame selFlame = flamesCmb.getSelectedIndex() >= 0 && flamesCmb.getSelectedIndex() < project.getFlames().size() ? project.getFlames().get(flamesCmb.getSelectedIndex()) : null;
      if (selFlame != null && renderThread != null) {
        int morphFrameCount = Integer.parseInt(morphFrameCountIEd.getText());
        renderThread.getFlameStack().addFlame(selFlame, morphFrameCount, project.getMotions(selFlame));
        if (actionRecorder != null)
          actionRecorder.recordFlameChange(selFlame, morphFrameCount);
      }
    }
  }

  private class PoolFlameHolder implements FlameHolder {

    @Override
    public Flame getFlame() {
      if (flamePropertiesTree.getSelectionPath() != null) {
        Object[] selection = flamePropertiesTree.getSelectionPath().getPath();
        if (selection != null && selection.length > 1) {
          FlamePropertiesTreeNode<?> flameNode = (FlamePropertiesTreeNode<?>) selection[1];
          Flame selFlame = (Flame) flameNode.getNodeData();
          return selFlame;
        }
      }
      return null;
    }
  }

  public void flamePropertiesTree_changed(TreeSelectionEvent e) {
    if (!refreshing) {
      Flame selFlame = null;
      TreePath path = e != null ? e.getNewLeadSelectionPath() : null;
      if (path != null) {
        Object[] selection = path.getPath();
        if (selection != null && selection.length > 1) {
          FlamePropertiesTreeNode<?> flameNode = (FlamePropertiesTreeNode<?>) selection[1];
          selFlame = (Flame) flameNode.getNodeData();
        }
      }
      refreshPoolPreviewFlameImage(selFlame);
      enableControls();
    }
  }

  public void addMotionBtn_clicked() {
    try {
      MotionType motionType = (MotionType) addMotionCmb.getSelectedItem();
      if (motionType != null) {
        Motion newMotion = motionType.getMotionClass().newInstance();
        project.getMotions().add(newMotion);

        boolean oldRefreshing = refreshing;
        refreshing = true;
        try {
          refreshMotionTable();
        }
        finally {
          refreshing = oldRefreshing;
        }

        int selectRow = project.getMotions().size() - 1;
        motionTable.getSelectionModel().setSelectionInterval(selectRow, selectRow);
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public static class MotionTypeEditor extends ComboBoxPropertyEditor {
    public MotionTypeEditor(List<Motion> pMotions) {
      super();
      Motion[] motions = new Motion[0];
      if (pMotions != null) {
        motions = pMotions.toArray(motions);
      }
      setAvailableValues(motions);
    }
  }

  public void motionTableClicked() {
    if (!refreshing) {
      if (motionPropertyPnl != null) {
        motionPropertyRootPnl.remove(motionPropertyPnl);
        motionPropertyPnl = null;
      }
      if (project.getMotions().size() > 0 && motionTable.getSelectedRow() >= 0 && motionTable.getSelectedRow() < project.getMotions().size()) {
        Motion motion = project.getMotions().get(motionTable.getSelectedRow());

        @SuppressWarnings("rawtypes")
        Map<Class, PropertyEditor> editors = new HashMap<Class, PropertyEditor>();
        editors.put(Motion.class, new MotionTypeEditor(project.getMotions()));

        motionPropertyPnl = new PropertyPanel(motion, editors);

        motionPropertyPnl.setDescriptionVisible(false);

        PropertyChangeListener listener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            refreshing = true;
            try {
              int oldSel = motionTable.getSelectedRow();
              refreshMotionTable();
              motionTable.getSelectionModel().setSelectionInterval(oldSel, oldSel);
              enableControls();
            }
            finally {
              refreshing = false;
            }
          }
        };
        motionPropertyPnl.addPropertySheetChangeListener(listener);

        motionPropertyRootPnl.add(motionPropertyPnl,
            BorderLayout.CENTER);
        enableControls();
      }
    }
    refreshMotionLinksTable();
    motionPropertyRootPnl.invalidate();
    motionPropertyRootPnl.validate();
  }

  public void linkMotionBtn_clicked() {
    Motion currMotion = getSelectedMotion();
    if (currMotion != null && flamePropertiesTreeService.isPlainPropertySelected(flamePropertiesTree)) {
      FlamePropertyPath propertyPath = flamePropertiesTreeService.getSelectedPropertyPath(flamePropertiesTree);
      currMotion.getMotionLinks().add(new MotionLink(propertyPath));
      refreshMotionLinksTable();
      int selectRow = currMotion.getMotionLinks().size() - 1;
      motionLinksTable.getSelectionModel().setSelectionInterval(selectRow, selectRow);
      enableControls();
    }
  }

  public void deleteMotionBtn_clicked() {
    int row = motionTable.getSelectedRow();
    if (row >= 0 && row < project.getMotions().size()) {
      project.getMotions().remove(row);
      refreshMotionTable();
      if (row >= project.getMotions().size()) {
        row--;
      }
      if (row >= 0) {
        motionTable.getSelectionModel().setSelectionInterval(row, row);
      }
      motionTableClicked();
      enableControls();
    }
  }

  public void clearMotionsBtn_clicked() {
    project.getMotions().clear();
    refreshMotionTable();
    motionTableClicked();
    enableControls();
  }

  public void unlinkMotionBtn_clicked() {
    Motion selMotion = getSelectedMotion();
    int row = motionLinksTable.getSelectedRow();
    if (selMotion != null && row >= 0 && row <= selMotion.getMotionLinks().size()) {
      int selRow = motionLinksTable.getSelectedRow();
      selMotion.getMotionLinks().remove(row);
      motionTableClicked();
      if (selRow >= selMotion.getMotionLinks().size()) {
        selRow--;
      }
      motionLinksTable.getSelectionModel().setSelectionInterval(selRow, selRow);
      enableControls();
    }
  }

  public void createMotionsBtn_clicked() {
    try {
      MotionCreatorType creatorType = (MotionCreatorType) createMotionsCmb.getSelectedItem();
      if (creatorType != null) {
        MotionCreator creator = creatorType.getMotionCreatorClass().newInstance();
        creator.createMotions(project);
        refreshMotionTable();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void dancingFlamesLoadProjectBtn_clicked() {
    try {
      JFileChooser chooser = new JWFDanceFileChooser(prefs);
      if (prefs.getInputJWFMoviePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getInputJWFMoviePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (chooser.showOpenDialog(poolFlamePreviewPnl) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        project = new JWFDanceReader().readProject(file.getAbsolutePath());
        refreshProjectFlames();
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void dancingFlamesSaveProjectBtn_clicked() {
    try {
      JFileChooser chooser = new JWFDanceFileChooser(prefs);
      if (prefs.getOutputJWFMoviePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getOutputJWFMoviePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (chooser.showSaveDialog(poolFlamePreviewPnl) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        new JWFDanceWriter().writeProject(project, file.getAbsolutePath());
        prefs.setLastOutputJWFMovieFile(file);
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  private Flame validateDancingFlame(Flame pFlame) {
    if (pFlame.getFinalXForms().size() == 0) {
      XForm xForm = new XForm();
      xForm.addVariation(1.0, new Linear3DFunc());
      xForm.setAntialiasAmount(prefs.getTinaDefaultAntialiasingAmount());
      xForm.setAntialiasRadius(prefs.getTinaDefaultAntialiasingRadius());
      pFlame.getFinalXForms().add(xForm);
    }
    return pFlame;
  }

}
