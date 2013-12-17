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

import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphPoolBox;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;


/**
 * Creation-Date: 14.04.2007, 15:18:02
 *
 * @author Thomas Morgner
 */
public class AlignmentRightTest extends TestCase
{
  public AlignmentRightTest()
  {
  }

  public AlignmentRightTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testAlignmentCenter() throws Exception
  {

    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final URL target = AlignmentRightTest.class.getResource("alignment-right.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();

    final DebugRenderer debugLayoutSystem = new DebugRenderer();
    debugLayoutSystem.startReport(report, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);

    final ReportStateKey stateKey = new ReportStateKey();
    debugLayoutSystem.add(report.getItemBand(), runtime, stateKey);
    debugLayoutSystem.endSection();

    debugLayoutSystem.validatePages();
    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    new ValidateRunner().startValidation(logicalPageBox);
  }

  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    private int count;

    protected void processParagraphChilds(final ParagraphRenderBox box)
    {
      count = 0;
      processBoxChilds(box);
      TestCase.assertEquals("Line-Count", 1, count);
    }

    protected boolean startInlineBox(final InlineRenderBox box)
    {
      if (box instanceof ParagraphPoolBox)
      {
        count += 1;
        final long x = box.getX();
        if ("A".equals(box.getName()))
        {
          if (x < 400000 || x > 485000)
          {
            TestCase.fail("X position is wrong: " + x);
          }
        }
        if ("B".equals(box.getName()))
        {
          if (x < 485000 || x > 560000)
          {
            TestCase.fail("X position is wrong: " + x);
          }
        }
      }
      return super.startInlineBox(box);
    }

    public void startValidation(final LogicalPageBox logicalPageBox)
    {
      startProcessing(logicalPageBox);
    }
  }
}