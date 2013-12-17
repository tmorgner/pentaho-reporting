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
 * Copyright (c) 2007 - 2009 Pentaho Corporation,  ..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import java.awt.print.PageFormat;
import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.Renderer;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
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
 * Creation-Date: 10.08.2007, 13:45:14
 *
 * @author Thomas Morgner
 */
public class Pre422Test extends TestCase
{
  public Pre422Test()
  {
  }

  public Pre422Test(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }


  public void testSubReportDoesNotCrash() throws Exception
  {
    final URL target = Pre422Test.class.getResource("Pre-422.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();


    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();
    final DebugRenderer debugLayoutSystem = new DebugRenderer();
    debugLayoutSystem.startReport(basereport, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);

    final Band band = report.getReportHeader();
    band.setName("ReportHeader1");
    debugLayoutSystem.add(band, runtime, stateKey);

    debugLayoutSystem.endSection();
    debugLayoutSystem.endReport();

    assertEquals(Renderer.LayoutResult.LAYOUT_PAGEBREAK, debugLayoutSystem.validatePages());

    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    new ValidateRunner().startValidation(logicalPageBox);

  }

  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    public void startValidation(final LogicalPageBox logicalPageBox)
    {
      startProcessing(logicalPageBox);
    }

    protected boolean startCanvasBox(final CanvasRenderBox box)
    {
      if (box.getName().equals("test"))
      {
        assertEquals("Y=10pt", StrictGeomUtility.toInternalValue(10), box.getY());
        assertEquals("Height=90pt", StrictGeomUtility.toInternalValue(90), box.getHeight());
      }
      return super.startCanvasBox(box);
    }
  }
}
