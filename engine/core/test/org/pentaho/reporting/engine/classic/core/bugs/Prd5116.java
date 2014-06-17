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
 *  Copyright (c) 2006 - 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.ClassicEngineCoreModule;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.RelationalGroup;
import org.pentaho.reporting.engine.classic.core.SubReport;
import org.pentaho.reporting.engine.classic.core.filter.types.LabelType;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableText;
import org.pentaho.reporting.engine.classic.core.layout.table.TableTestUtil;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.testsupport.selector.MatchFactory;

public class Prd5116
{
  @Before
  public void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public static Element createDataItem(final String text)
  {
    final Element label = new Element();
    label.setElementType(LabelType.INSTANCE);
    label.setName(text);
    label.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, text);
    label.getStyle().setStyleProperty(ElementStyleKeys.MIN_WIDTH, 100f);
    label.getStyle().setStyleProperty(ElementStyleKeys.MIN_HEIGHT, 20f);
    return label;
  }

  @Test
  public void testInlineReport() throws Exception
  {
    SubReport sr = new SubReport();
    sr.getReportHeader().addElement(createDataItem("Test"));

    MasterReport report = new MasterReport();
    report.getReportConfiguration().setConfigProperty(ClassicEngineCoreModule.COMPLEX_TEXT_CONFIG_OVERRIDE_KEY, "false");
    report.getReportHeader().getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    report.getReportHeader().addElement(sr);

    LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);

    RenderNode[] test = MatchFactory.findElementsByName(logicalPageBox, "Test");
    Assert.assertEquals(2, test.length);
    Assert.assertTrue(test[0] instanceof InlineRenderBox);
    Assert.assertTrue(test[1] instanceof RenderableText);
  }

  @Test
  public void testInlineReportWithNestedBandedSubReport() throws Exception
  {
    SubReport srInner = new SubReport();
    srInner.getReportHeader().addElement(createDataItem("Test"));

    SubReport sr = new SubReport();
    sr.getReportHeader().addSubReport(srInner);

    MasterReport report = new MasterReport();
    report.getReportConfiguration().setConfigProperty(ClassicEngineCoreModule.COMPLEX_TEXT_CONFIG_OVERRIDE_KEY, "false");
    report.getReportHeader().getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    report.getReportHeader().addElement(sr);

    LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);

    RenderNode[] test = MatchFactory.findElementsByName(logicalPageBox, "Test");
    Assert.assertEquals(2, test.length);
    Assert.assertTrue(test[0] instanceof InlineRenderBox);
    Assert.assertTrue(test[1] instanceof RenderableText);
  }

  @Test
  public void testBandedReport() throws Exception
  {
    // a subreport or inline subreport used in a banded context will always be considered a block element,
    // unless used in inline-context.
    //
    // If used in an inline context, the use of page- and repeated-header and footer is undefined, and there
    // is no telling on what will happen to the report. Most likely it will break badly.

    SubReport sr = new SubReport();
    sr.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    sr.getReportHeader().addElement(createDataItem("Test"));

    MasterReport report = new MasterReport();
    report.getReportConfiguration().setConfigProperty(ClassicEngineCoreModule.COMPLEX_TEXT_CONFIG_OVERRIDE_KEY, "false");
    report.getReportHeader().addSubReport(sr);

    LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);

    RenderNode[] test = MatchFactory.findElementsByName(logicalPageBox, "Test");
    Assert.assertEquals(2, test.length);
    Assert.assertTrue(test[0] instanceof BlockRenderBox);
    Assert.assertTrue(test[1] instanceof RenderableText);
  }

  @Test
  public void testBandedReportReportMarkedInline() throws Exception
  {
    // a subreport or inline subreport used in a banded context will always be considered a block element,
    // unless used in inline-context.
    //
    // If used in an inline context, the use of page- and repeated-header and footer is undefined, and there
    // is no telling on what will happen to the report. Most likely it will break badly.

    SubReport sr = new SubReport();
    sr.getReportHeader().addElement(createDataItem("Test"));

    MasterReport report = new MasterReport();
    report.getReportConfiguration().setConfigProperty(ClassicEngineCoreModule.COMPLEX_TEXT_CONFIG_OVERRIDE_KEY, "false");
    report.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    report.getReportHeader().addSubReport(sr);

    LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);

    RenderNode[] test = MatchFactory.findElementsByName(logicalPageBox, "Test");
    Assert.assertEquals(2, test.length);
    Assert.assertTrue(test[0] instanceof BlockRenderBox);
    Assert.assertTrue(test[1] instanceof RenderableText);
  }

  @Test
  @Ignore
  public void testBandedReportInInlineContext() throws Exception
  {
    SubReport sr = new SubReport();
    sr.getReportHeader().addElement(createDataItem("Test"));

    MasterReport report = new MasterReport();
    report.getReportConfiguration().setConfigProperty(ClassicEngineCoreModule.COMPLEX_TEXT_CONFIG_OVERRIDE_KEY, "false");
    final RelationalGroup rg = (RelationalGroup) report.getRootGroup();
    rg.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    rg.getHeader().addSubReport(sr);

    LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);

    RenderNode[] test = MatchFactory.findElementsByName(logicalPageBox, "Test");
    Assert.assertEquals(2, test.length);
    Assert.assertTrue(test[0] instanceof InlineRenderBox);
    Assert.assertTrue(test[0] instanceof RenderableText);
  }
}
