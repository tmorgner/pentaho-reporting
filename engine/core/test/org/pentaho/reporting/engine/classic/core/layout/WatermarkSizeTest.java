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

import java.awt.print.PageFormat;
import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableReplacedContentBox;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugOutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.libraries.fonts.monospace.MonospaceFontRegistry;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontStorage;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class WatermarkSizeTest extends TestCase
{
  public WatermarkSizeTest()
  {
  }

  public WatermarkSizeTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testNestedRows() throws Exception
  {
    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();

    final URL target = LayoutTest.class.getResource("watermark-size.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();

    final Band containerBand = report.getWatermark();

    // Each character (regarless of font or font-size) will be 8pt high and 4pt wide.
    // this makes this test independent of the fonts installed on the system we run on.
    final DebugOutputProcessorMetaData metaData = new DebugOutputProcessorMetaData(report.getConfiguration(),
        new DefaultFontStorage(new MonospaceFontRegistry(9, 18)));
    final DebugRenderer debugLayoutSystem = new DebugRenderer(metaData);
    debugLayoutSystem.startReport(basereport, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_WATERMARK);
    debugLayoutSystem.add(containerBand, runtime, stateKey);
    debugLayoutSystem.endSection();
    debugLayoutSystem.validatePages();
    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    new ValidateRunner().startValidation(logicalPageBox);
  }

  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    protected void processRenderableContent(final RenderableReplacedContentBox box)
    {

    }

    protected boolean startBox(final RenderBox box)
    {
      final String name = box.getName();
      if (name == null)
      {
        return true;
      }
      if (box instanceof ParagraphRenderBox)
      {
        assertNotNull("Have at only one child", box.getFirstChild());
        assertNotNull("Have at only one child", box.getLastChild());
        assertSame("Have at only one child", box.getFirstChild(), box.getLastChild());
      }

      return true;
    }

    protected boolean startCanvasBox(final CanvasRenderBox box)
    {
      return startBox(box);
    }

    protected boolean startBlockBox(final BlockRenderBox box)
    {
      return startBox(box);
    }

    protected boolean startInlineBox(final InlineRenderBox box)
    {
      return startBox(box);
    }

    protected boolean startRowBox(final RenderBox box)
    {
      return startBox(box);
    }

    protected void processParagraphChilds(final ParagraphRenderBox box)
    {
      processBoxChilds(box);
    }

    public void startValidation(final LogicalPageBox logicalPageBox)
    {
      startProcessing(logicalPageBox);
    }
  }

}
