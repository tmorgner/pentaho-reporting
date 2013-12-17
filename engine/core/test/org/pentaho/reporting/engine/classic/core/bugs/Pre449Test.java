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

package org.pentaho.reporting.engine.classic.core.bugs;

import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.Renderer;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class Pre449Test extends TestCase
{
  public Pre449Test()
  {
  }

  public Pre449Test(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }


  public void testWatermarkCrash() throws Exception
  {
    final URL url = getClass().getResource("Pre-449.xml");
    assertNotNull(url);
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final Resource directly = resourceManager.createDirectly(url, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();


    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final ReportStateKey stateKey = new ReportStateKey();
    final DebugRenderer debugLayoutSystem = new DebugRenderer();
    debugLayoutSystem.startReport(report, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);

    final Band band = report.getReportHeader();
    debugLayoutSystem.add(band, runtime, stateKey);

    debugLayoutSystem.endSection();
    debugLayoutSystem.endReport();

    assertEquals(Renderer.LayoutResult.LAYOUT_PAGEBREAK, debugLayoutSystem.validatePages());

    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    ModelPrinter.INSTANCE.print(logicalPageBox);
    new ValidateRunner().startValidation(logicalPageBox);
  }


  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    public void startValidation(final LogicalPageBox logicalPageBox)
    {
      startProcessing(logicalPageBox);
    }

    protected boolean startBlockBox(final BlockRenderBox box)
    {
      if ("reportheader".equals(box.getName()))
      {
        assertEquals("X=0pt", 0, box.getX());
        assertEquals("Y=0pt", 0, box.getY());
        assertEquals("Height=150pt", StrictGeomUtility.toInternalValue(150), box.getHeight());
        assertEquals("Width=500pt", StrictGeomUtility.toInternalValue(500), box.getWidth());
      }
      return true;
    }
  }
}
