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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors...  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout;

import java.awt.print.PageFormat;
import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.output.ContentProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Creation-Date: 05.04.2007, 17:35:00
 *
 * @author Thomas Morgner
 */
public class WeirdLayoutTest extends TestCase
{
  public WeirdLayoutTest()
  {
  }

  public static void testLayout() throws ResourceException, ContentProcessingException, ReportProcessingException
  {
    ClassicEngineBoot.getInstance().start();

    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();
    final DebugRenderer debugLayoutSystem = new DebugRenderer();
    debugLayoutSystem.startReport(basereport, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);
    debugLayoutSystem.add(new Band(), runtime, stateKey);

    final URL target = WeirdLayoutTest.class.getResource("weird-layouting.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();

    final Band band = report.getReportHeader();
    band.setName("ReportHeader1");
    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader2");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader3");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader4");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader5");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader6");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader7");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader8");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader9");
//    debugLayoutSystem.add(band, runtime, stateKey);
//    band.setName("ReportHeader10");
//    debugLayoutSystem.add(band, runtime, stateKey);

//    final BandInBandStackingDemoHandler bandStackingDemoHandler = new BandInBandStackingDemoHandler();
//    final JFreeReport bandReport = bandStackingDemoHandler.createReport();
//    debugLayoutSystem.add(bandReport.getReportHeader(), runtime);

    debugLayoutSystem.endSection();
    debugLayoutSystem.endReport();
    assertEquals(Renderer.LayoutResult.LAYOUT_PAGEBREAK, debugLayoutSystem.validatePages());

  }

}
