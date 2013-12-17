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
 * Copyright (c) 2000 - 2011 Pentaho Corporation and Contributors...  
 * All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportHeader;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.output.ContentProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;

public class Prd3479Test extends TestCase
{
  public Prd3479Test()
  {
  }

  public Prd3479Test(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  private Band createBand (final String name, final float y, final float height)
  {
    final Band b = new Band();
    b.setName(name);
    b.getStyle().setStyleProperty(ElementStyleKeys.MIN_WIDTH,  100f);
    b.getStyle().setStyleProperty(ElementStyleKeys.MIN_HEIGHT,  height);
    b.getStyle().setStyleProperty(ElementStyleKeys.POS_Y,  y);
    return b;
  }

  public void testCanvasLayout () throws ReportProcessingException, ContentProcessingException
  {

    final MasterReport report = new MasterReport();
    final ReportHeader reportHeader = report.getReportHeader();
    reportHeader.addElement(createBand("large", 0, 100));
    reportHeader.addElement(createBand("rel", -25, -50));

    // Each character (regarless of font or font-size) will be 8pt high and 4pt wide.
    // this makes this test independent of the fonts installed on the system we run on.

    final LogicalPageBox logicalPageBox = DebugReportRunner.layoutSingleBand(report, reportHeader);

    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    ModelPrinter.INSTANCE.print(logicalPageBox);
    new ValidateRunner().startValidation(logicalPageBox);
  }

  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    protected boolean startBox(final RenderBox node)
    {
      final String s = node.getName();
      if ("large".equals(s))
      {
        // inline elements take the intrinsinc width/height unless explicitly defined otherwise
        assertEquals("Rect height=100pt; " + node.getName(), StrictGeomUtility.toInternalValue(100), node.getCachedHeight());
      }
      else if ("rel".equals(s))
      {
        assertEquals("Rect height=50pt; " + node.getName(), StrictGeomUtility.toInternalValue(50), node.getCachedHeight());
        assertEquals("Rect y=25pt; " + node.getName(), StrictGeomUtility.toInternalValue(25), node.getCachedY());
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
