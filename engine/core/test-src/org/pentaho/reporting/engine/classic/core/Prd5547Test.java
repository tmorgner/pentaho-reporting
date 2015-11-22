/*
 * This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2006 - 2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core;

import java.net.URL;
import java.util.List;

import bsh.This;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.testsupport.selector.MatchFactory;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class Prd5547Test
{
  @Before
  public void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  @Test
  public void testReport() throws Exception
  {
    //
    // This modified report shows that the footer placement is not correct at the moment.
    // the report renders correctly until page 3. Page 4 should contain the footer, but
    // the pagination shifts *all* children table to 1680 (start of page 4). It should have
    // only shifted the header and then
    URL r = getClass().getResource("/table-layout-pagination-nested-table-header-footer.prpt");
    ResourceManager mgr = new ResourceManager();
    MasterReport report = (MasterReport) mgr.createDirectly(r, MasterReport.class).getResource();
    List<LogicalPageBox> logicalPageBoxes = DebugReportRunner.layoutPagesRaw(report, 2, 3);
    for (LogicalPageBox logicalPageBox : logicalPageBoxes)
    {
      ModelPrinter.INSTANCE.print(logicalPageBox);
    }
  }

  @Test
  public void test2Report() throws Exception
  {
    URL r = getClass().getResource("/Prd-5547.prpt");
    ResourceManager mgr = new ResourceManager();
    MasterReport report = (MasterReport) mgr.createDirectly(r, MasterReport.class).getResource();
    List<LogicalPageBox> logicalPageBoxes = DebugReportRunner.layoutPagesRaw(report, 0, 1, 2, 3, 4, 5);
    for (LogicalPageBox logicalPageBox : logicalPageBoxes)
    {
   //   ModelPrinter.INSTANCE.print(logicalPageBox);
    }
  }

  @Test
  public void testReport3() throws Exception
  {
    URL r = getClass().getResource("/Prd-5547-table.prpt");
    ResourceManager mgr = new ResourceManager();
    MasterReport report = (MasterReport) mgr.createDirectly(r, MasterReport.class).getResource();
    List<LogicalPageBox> logicalPageBoxes = DebugReportRunner.layoutPagesRaw(report, 0, 1, 2);

    long[] pageStarts = new long[10];
    for (int i = 0; i < pageStarts.length; i++)
    {
      // 415 ==> page height
      pageStarts[i] = i * (415);
    }

    long[][] expectedPositionsPerPage = {
        {   30, 30 + 150,
            pageStarts[1],
            pageStarts[1] + 200,
            pageStarts[1] + 400,
            pageStarts[1] + 600,
            pageStarts[1] + 800
        }, {
            pageStarts[1], // header
            30 + 150,
            pageStarts[1] + 150,
            pageStarts[2],
            pageStarts[2] + 200,
            pageStarts[2] + 400,
            pageStarts[2] + 600
        }, {
            pageStarts[2], // header
            30 + 150,
            pageStarts[1] + 150,
            pageStarts[2] + 150,
            pageStarts[3],
            pageStarts[3] + 200,
            pageStarts[3] + 400,
            pageStarts[3] + 600
        }, {
            pageStarts[3], // header
            30 + 150,
            pageStarts[1] + 150,
            pageStarts[2] + 150,
            pageStarts[3] + 150,
            pageStarts[4],
            pageStarts[4] + 200,
            pageStarts[4] + 400,
            pageStarts[4] + 600
        }
    };

    translateToInternalCoordinates(expectedPositionsPerPage);

    assertPage0(logicalPageBoxes, 0, expectedPositionsPerPage[0]);
    assertPage0(logicalPageBoxes, 1, expectedPositionsPerPage[1]);
    assertPage0(logicalPageBoxes, 2, expectedPositionsPerPage[2]);
  }

  protected static void translateToInternalCoordinates(final long[][] expectedPositionsPerPage)
  {
    // and translate the points to the internal sizing system
    for (long[] longs : expectedPositionsPerPage)
    {
      for (int i = 0; i < longs.length; i++)
      {
        longs[i] = StrictGeomUtility.toInternalValue(longs[i]);

      }
    }
  }

  protected void assertPage0(final List<LogicalPageBox> logicalPageBoxes, int page,
                             long[] expectedPositions)
  {
    LogicalPageBox p0 = logicalPageBoxes.get(page);
    ModelPrinter.INSTANCE.print(p0);
    try
    {
      RenderNode[] rows0 = MatchFactory.findElementsByNodeType(p0, LayoutNodeTypes.TYPE_BOX_TABLE_ROW);
      Assert.assertEquals(10, rows0.length);

      for (int i = 0; i < expectedPositions.length; i++)
      {
        long expectedPosition = expectedPositions[i];
        Assert.assertEquals("Page " + page + ": On Row " + i, expectedPosition, rows0[i].getY());
      }
    }
    catch (AssertionError ae) {
      ModelPrinter.INSTANCE.print(p0);
      throw ae;
    }
  }
}
