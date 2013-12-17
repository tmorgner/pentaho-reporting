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
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.datasources.xpath;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import javax.swing.table.TableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.states.datarow.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.TableModelInfo;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class XPathQueryTest extends TestCase
{
  public static final String QUERY_1 = "/*/*";

  public XPathQueryTest()
  {
  }

  public XPathQueryTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testFromBundle() throws Exception
  {

    final ResourceManager manager = new ResourceManager();
    manager.registerDefaults();
    final Resource res = manager.createDirectly(XPathQueryTest.class.getResource("xpath-bundle-test.prpt"), MasterReport.class);
    final MasterReport report = (MasterReport) res.getResource();

    final CompoundDataFactory dataFactory = (CompoundDataFactory) report.getDataFactory();
    final XPathDataFactory xpathDataFactory = (XPathDataFactory) dataFactory.getReference(0);
    xpathDataFactory.initialize(report.getConfiguration(), report.getResourceManager(),
        report.getContentBase(), MasterReport.computeAndInitResourceBundleFactory
            (report.getResourceBundleFactory(), report.getReportEnvironment()));
    xpathDataFactory.open();
    xpathDataFactory.queryData("default", new StaticDataRow());
    xpathDataFactory.close();
  }

  public void testQuery8() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_1, ps);
    compareLineByLine("query1-results.txt", sw.toString());
  }

  private void compareLineByLine(final String sourceFile, final String resultText) throws IOException
  {
    final BufferedReader resultReader = new BufferedReader(new StringReader(resultText));
    final BufferedReader compareReader = new BufferedReader(new InputStreamReader
        (ObjectUtilities.getResourceRelativeAsStream(sourceFile, XPathQueryTest.class)));
    try
    {
      int line = 1;
      String lineResult = resultReader.readLine();
      String lineSource = compareReader.readLine();
      while (lineResult != null && lineSource != null)
      {
        assertEquals("Failure in line " + line, lineSource, lineResult);
        line += 1;
        lineResult = resultReader.readLine();
        lineSource = compareReader.readLine();
      }

      assertNull("Extra lines encountered in live-result " + line, lineResult);
      assertNull("Extra lines encountered in recorded result " + line, lineSource);
    }
    finally
    {
      resultReader.close();
      compareReader.close();
    }
  }

  private void performQueryTest(final String query,
                                final PrintStream out) throws SQLException, ReportDataFactoryException
  {
    final XPathDataFactory dataFactory = new XPathDataFactory();
    dataFactory.setXqueryDataFile("test/org/pentaho/reporting/engine/classic/extensions/datasources/xpath/customer.xml");
    try
    {
      dataFactory.open();
      dataFactory.setQuery("default", query);
      final TableModel tableModel = dataFactory.queryData("default", new ParameterDataRow());
      TableModelInfo.printTableModel(tableModel, out);
      TableModelInfo.printTableModelContents(tableModel, out);
    }
    finally
    {

      dataFactory.close();
    }
  }

  public static void main(final String[] args) throws Exception
  {
    final XPathQueryTest hellTest = new XPathQueryTest();
    hellTest.setUp();
    hellTest.performQueryTest(QUERY_1, System.out);
  }
}
