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
 *  Copyright (c) 2006 - 2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import junit.framework.TestCase;
import org.junit.Assert;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportHeader;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.filter.types.LabelType;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.output.ContentProcessingException;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.testsupport.selector.MatchFactory;

public class Prd4760Test extends TestCase
{
  public Prd4760Test()
  {
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testReport() throws ReportProcessingException, ContentProcessingException
  {
    final Band b = new Band();
    b.setLayout(BandStyleKeys.LAYOUT_ROW);
    b.addElement(createDataItem("Test"));
    b.setVisible(false);

    final MasterReport report = new MasterReport();
    final ReportHeader reportHeader = report.getReportHeader();
    reportHeader.setLayout(BandStyleKeys.LAYOUT_ROW);
    reportHeader.addElement(b);
    reportHeader.getStyle().setStyleProperty(ElementStyleKeys.INVISIBLE_CONSUMES_SPACE, false);

    final LogicalPageBox logicalPageBox = DebugReportRunner.layoutSingleBand(report, reportHeader);
    ModelPrinter.INSTANCE.print(logicalPageBox);
    final RenderNode[] elementsByNodeType = MatchFactory.findElementsByNodeType(logicalPageBox, LayoutNodeTypes.TYPE_NODE_TEXT);
    Assert.assertEquals(0, elementsByNodeType.length);
  }

  public void testSimpleReport() throws ReportProcessingException, ContentProcessingException
  {
    final MasterReport report = new MasterReport();
    final ReportHeader reportHeader = report.getReportHeader();
    reportHeader.setLayout(BandStyleKeys.LAYOUT_ROW);
    reportHeader.addElement(createDataItem("Test"));
    reportHeader.getStyle().setStyleProperty(ElementStyleKeys.INVISIBLE_CONSUMES_SPACE, false);
    reportHeader.setVisible(false);

    final LogicalPageBox logicalPageBox = DebugReportRunner.layoutSingleBand(report, reportHeader);
    final RenderNode[] elementsByNodeType = MatchFactory.findElementsByNodeType(logicalPageBox, LayoutNodeTypes.TYPE_NODE_TEXT);
    Assert.assertEquals(0, elementsByNodeType.length);
  }

  public void testSimpleReport2() throws ReportProcessingException, ContentProcessingException
  {
    final Element test = createDataItem("Test");
    test.setVisible(false);

    final MasterReport report = new MasterReport();
    final ReportHeader reportHeader = report.getReportHeader();
    reportHeader.setLayout(BandStyleKeys.LAYOUT_ROW);
    reportHeader.addElement(test);
    reportHeader.getStyle().setStyleProperty(ElementStyleKeys.INVISIBLE_CONSUMES_SPACE, false);
    reportHeader.setVisible(false);

    final LogicalPageBox logicalPageBox = DebugReportRunner.layoutSingleBand(report, reportHeader);
    final RenderNode[] elementsByNodeType = MatchFactory.findElementsByNodeType(logicalPageBox, LayoutNodeTypes.TYPE_NODE_TEXT);
    Assert.assertEquals(0, elementsByNodeType.length);
  }

  public static Element createDataItem(final String text)
  {
    final Element label = new Element();
    label.setElementType(LabelType.INSTANCE);
    label.setName(text);
    label.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, text);
    label.getStyle().setStyleProperty(ElementStyleKeys.MIN_WIDTH, 200f);
    label.getStyle().setStyleProperty(ElementStyleKeys.MIN_HEIGHT, 20f);
    return label;
  }

}
