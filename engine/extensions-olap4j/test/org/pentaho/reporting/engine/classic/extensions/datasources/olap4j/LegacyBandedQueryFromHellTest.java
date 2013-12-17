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

package org.pentaho.reporting.engine.classic.extensions.datasources.olap4j;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.SQLException;

import junit.framework.TestCase;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.PreparedOlapStatement;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.TableModelInfo;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.DriverConnectionProvider;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class LegacyBandedQueryFromHellTest extends TestCase
{
  /**
   * A zero-dimensional query resulting a single-cell result-set, having no row and no column-dimensions. This can be
   * displayed by having the "field" property of column and row-groups set to &lt;null&gt;
   */
  protected static final String QUERY_1 = "select from [SteelWheelsSales]";
  /**
   * A one-dimensional query. Results in a table with one dimension and a measure.
   */
  protected static final String QUERY_2 = "select [Product].Children on 0 from [SteelWheelsSales]";
  /**
   * A two-dimensional query, where one axis is empty. The result-set has no measures. (this ends up empty because the
   * parent of 'all' is null, and null members are implicitly filtered)
   */
  protected static final String QUERY_3 = "select [Product].parent on 0, [Time].Children on 1 from [SteelWheelsSales]";
  protected static final String QUERY_3A = "select [Time].Children on 0, [Product].parent on 1 from [SteelWheelsSales]";

  /**
   * A two-dimensional query, where one axis is empty. The result-set has no measures. (this ends up empty because the
   * parent of 'all' is null, and null members are implicitly filtered)
   */
  protected static final String QUERY_4 = "select crossjoin([Markets].Children, {[Measures].[Quantity], [Measures].[Sales]}) on 0, crossjoin([Product].Children, [Time].Children) on 1 from [SteelWheelsSales]";

  /**
   * Same as query4, but measures are not right above the cell set (i.e. the last dimension on the columns axis)
   */
  protected static final String QUERY_5 = "select crossjoin({[Measures].[Quantity], [Measures].[Sales]}, [Markets].Children) on 0, crossjoin([Product].Children, [Time].Children) on 1 from [SteelWheelsSales]";

  /**
   * Same as query4 but with measures on the columns
   */
  protected static final String QUERY_6 = "select crossjoin([Product].Children, [Markets].Children) on 0, crossjoin({[Measures].[Quantity], [Measures].[Sales]}, [Time].Children) on 1 from [SteelWheelsSales]";

  /**
   * Cells with properties.
   */
  protected static final String QUERY_7 = "with member [Measures].[Foo] as  ' [Measures].[Sales] / 2 ',\n" +
      "   format_string = '$#,###',\n" +
      "   back_color = 'yellow',  \n" +
      "   my_property = iif([Measures].CurrentMember > 10, \"foo\", \"bar\")\n" +
      "select {[Measures].[Foo], [Measures].[Sales]} on 0,\n" +
      " [Product].Children on 1\n" +
      "from [SteelWheelsSales]";

  /**
   * A query with a ragged hierarchy.
   */
  protected static final String QUERY_8 = "select {[Markets].[All Markets].[APAC], [Markets].[All Markets].[EMEA], " +
      "[Markets].[All Markets].[Japan], [Markets].[All Markets], " +
      "[Markets].[All Markets].[NA]} ON COLUMNS,\n" +
      "  Hierarchize(Union(Union(Union(Union(Union(Union(Crossjoin({[Product].[All Products].[Classic Cars]}, " +
      "{[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]}), " +
      "Crossjoin({[Product].[All Products].[Motorcycles]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Union(Crossjoin({[Product].[All Products].[Planes]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]}), Crossjoin({[Product].[All Products].[Planes]}, " +
      "[Time].[All Years].[2004].Children))), Crossjoin({[Product].[All Products].[Ships]}, " +
      "{[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Crossjoin({[Product].[All Products].[Trains]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]})), Crossjoin({[Product].[All Products].[Trucks " +
      "and Buses]}, {[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Crossjoin({[Product].[All Products].[Vintage Cars]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]}))) ON ROWS\n" +
      "from [SteelWheelsSales]\n";

  /**
   * A query with a ragged hierarchy (flipped).
   */
  protected static final String QUERY_9 = "select " +
      "  Hierarchize(Union(Union(Union(Union(Union(Union(Crossjoin({[Product].[All Products].[Classic Cars]}, " +
      "{[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]}), " +
      "Crossjoin({[Product].[All Products].[Motorcycles]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Union(Crossjoin({[Product].[All Products].[Planes]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]}), Crossjoin({[Product].[All Products].[Planes]}, " +
      "[Time].[All Years].[2004].Children))), Crossjoin({[Product].[All Products].[Ships]}, " +
      "{[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Crossjoin({[Product].[All Products].[Trains]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]})), Crossjoin({[Product].[All Products].[Trucks " +
      "and Buses]}, {[Time].[All Years].[2003], [Time].[All Years].[2004], [Time].[All Years].[2005]})), " +
      "Crossjoin({[Product].[All Products].[Vintage Cars]}, {[Time].[All Years].[2003], " +
      "[Time].[All Years].[2004], [Time].[All Years].[2005]}))) " +
      "ON COLUMNS,\n" +
      "{[Markets].[All Markets].[APAC], [Markets].[All Markets].[EMEA], " +
      "[Markets].[All Markets].[Japan], [Markets].[All Markets], " +
      "[Markets].[All Markets].[NA]} " +
      "ON ROWS\n" +
      "from [SteelWheelsSales]\n";

  public LegacyBandedQueryFromHellTest()
  {
  }

  public LegacyBandedQueryFromHellTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testQuery1() throws Exception
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_1, ps);
    compareLineByLine("query1-legacy-results.txt", sw.toString());
  }

  public void testQuery2() throws Exception
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_2, ps);
    compareLineByLine("query2-legacy-results.txt", sw.toString());
  }

  public void testQuery3() throws Exception
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_3, ps);
    compareLineByLine("query3-legacy-results.txt", sw.toString());
  }

  public void testQuery3a() throws Exception
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_3A, ps);
    compareLineByLine("query3a-legacy-results.txt", sw.toString());
  }

  public void testQuery4() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_4, ps);
    compareLineByLine("query4-legacy-results.txt", sw.toString());
  }

  public void testQuery5() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_5, ps);
    compareLineByLine("query5-legacy-results.txt", sw.toString());
  }

  public void testQuery6() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_6, ps);
    compareLineByLine("query6-legacy-results.txt", sw.toString());
  }

  public void testQuery7() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_7, ps);
    compareLineByLine("query7-legacy-results.txt", sw.toString());
  }

  public void testQuery8() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_8, ps);
    compareLineByLine("query8-legacy-results.txt", sw.toString());
  }

  public void testQuery9() throws SQLException, IOException, ReportDataFactoryException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY_9, ps);
    compareLineByLine("query9-legacy-results.txt", sw.toString());
  }

  private void compareLineByLine(final String sourceFile, final String resultText) throws IOException
  {
    final BufferedReader resultReader = new BufferedReader(new StringReader(resultText));
    final BufferedReader compareReader = new BufferedReader(new InputStreamReader
        (ObjectUtilities.getResourceRelativeAsStream(sourceFile, BandedQueryFromHellTest.class)));
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

  protected void performQueryTest(final String query,
                                final PrintStream out) throws SQLException, ReportDataFactoryException
  {
    final DriverConnectionProvider dcp = createProvider();
    final OlapConnection wrapper = dcp.createConnection(null, null);
    try
    {
      final OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
      final PreparedOlapStatement statement = olapConnection.prepareOlapStatement(query);
      final CellSet cellSet = statement.executeQuery();
      final LegacyBandedMDXTableModel tableModel = new LegacyBandedMDXTableModel(new QueryResultWrapper(statement, cellSet), -1);
      try
      {
        TableModelInfo.printTableModel(tableModel, out);
        TableModelInfo.printTableModelContents(tableModel, out);
      }
      finally
      {
        tableModel.close();
      }
    }
    finally
    {

      wrapper.close();
    }
  }

  private DriverConnectionProvider createProvider()
  {
    final DriverConnectionProvider dcp = new DriverConnectionProvider();
    dcp.setDriver("mondrian.olap4j.MondrianOlap4jDriver");
    dcp.setProperty("Catalog",
        "test/org/pentaho/reporting/engine/classic/extensions/datasources/olap4j/steelwheels.mondrian.xml");
    dcp.setProperty("JdbcUser", "sa");
    dcp.setProperty("JdbcPassword", "");
    dcp.setProperty("Jdbc", "jdbc:hsqldb:./sql/sampledata");
    dcp.setProperty("JdbcDrivers", "org.hsqldb.jdbcDriver");
    dcp.setUrl("jdbc:mondrian:");
    return dcp;
  }
}