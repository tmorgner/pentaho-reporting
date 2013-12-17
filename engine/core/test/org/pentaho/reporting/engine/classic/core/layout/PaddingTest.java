/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout;

import java.awt.Color;
import java.awt.print.PageFormat;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ElementAlignment;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.elementfactory.LabelElementFactory;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.FontDefinition;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugOutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.libraries.base.performance.NoOpPerformanceMonitorContext;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontStorage;
import org.pentaho.reporting.libraries.fonts.monospace.MonospaceFontRegistry;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class PaddingTest extends TestCase
{
  public PaddingTest()
  {
  }

  public PaddingTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testPaddingFromFile() throws Exception
  {
    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();

    final URL target = LayoutTest.class.getResource("padding-test.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();

    final Band containerBand = report.getReportHeader();

    // Each character (regarless of font or font-size) will be 8pt high and 4pt wide.
    // this makes this test independent of the fonts installed on the system we run on.
    final DebugOutputProcessorMetaData metaData = new DebugOutputProcessorMetaData(report.getConfiguration(),
        new DefaultFontStorage(new MonospaceFontRegistry(9, 18)));
    final DebugRenderer debugLayoutSystem = new DebugRenderer(metaData);
    debugLayoutSystem.startReport(basereport, processingContext, NoOpPerformanceMonitorContext.INSTANCE);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);
    debugLayoutSystem.add(containerBand, runtime, stateKey);
    debugLayoutSystem.endSection();
    debugLayoutSystem.validatePages();
    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    ModelPrinter.INSTANCE.print(logicalPageBox);

  }

  public void testFailure() throws Exception
  {
    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();

    final MasterReport report = createReport();

    final Band containerBand = report.getReportHeader();

    // Each character (regarless of font or font-size) will be 8pt high and 4pt wide.
    // this makes this test independent of the fonts installed on the system we run on.
    final DebugOutputProcessorMetaData metaData = new DebugOutputProcessorMetaData(report.getConfiguration(),
        new DefaultFontStorage(new MonospaceFontRegistry(9, 18)));
    final DebugRenderer debugLayoutSystem = new DebugRenderer(metaData);
    debugLayoutSystem.startReport(basereport, processingContext, NoOpPerformanceMonitorContext.INSTANCE);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);
    debugLayoutSystem.add(containerBand, runtime, stateKey);
    debugLayoutSystem.endSection();
    debugLayoutSystem.validatePages();
    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
     ModelPrinter.INSTANCE.print(logicalPageBox);

  }

  private MasterReport createReport()
  {
    MasterReport report = new MasterReport();

    report.setName("BorderTest");

    Element label1 = LabelElementFactory.createLabelElement("Label1",
        new Rectangle2D.Double(0, 0, 200, 100),
        Color.RED,
        ElementAlignment.LEFT,
        new FontDefinition("Arial", 12),
        "Label1");

    report.getReportHeader().addElement(label1);

    label1.getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, new Color(255, 127, 127, 120));

    Element label2 = LabelElementFactory.createLabelElement("Label2",
        new Rectangle2D.Double(0, 110, 200, 100),
        Color.RED,
        ElementAlignment.LEFT,
        new FontDefinition("Arial", 12),
        "Label2");

    report.getReportHeader().addElement(label2);

    label2.getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, new Color(255, 127, 127, 120));
    label2.getStyle().setStyleProperty(ElementStyleKeys.PADDING_TOP, new Float(10));
    label2.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, new Float(10));
    label2.getStyle().setStyleProperty(ElementStyleKeys.PADDING_RIGHT, new Float(10));
    label2.getStyle().setStyleProperty(ElementStyleKeys.PADDING_BOTTOM, new Float(10));


    Element label3 = LabelElementFactory.createLabelElement("Label3",
        new Rectangle2D.Double(210, 0, 200, 100),
        Color.RED,
        ElementAlignment.LEFT,
        new FontDefinition("Arial", 12),
        "Label3");

    report.getReportHeader().addElement(label3);

    label3.getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, new Color(255, 127, 127, 120));
    label3.getStyle().setStyleProperty(ElementStyleKeys.PADDING_TOP, new Float(10));
    label3.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, new Float(10));
    label3.getStyle().setStyleProperty(ElementStyleKeys.PADDING_RIGHT, new Float(10));
    label3.getStyle().setStyleProperty(ElementStyleKeys.PADDING_BOTTOM, new Float(10));

    report.setDataFactory(new TableDataFactory("default", new DefaultTableModel(1, 1)));
    return report;
  }

}