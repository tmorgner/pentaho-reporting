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

package demo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;
import org.olap4j.PreparedOlapStatement;
import org.olap4j.metadata.Measure;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.DenormalizedMDXTableModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.QueryResultWrapper;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.DriverConnectionProvider;

/**
 * Todo: Document me!
 *
 * @author Thomas Morgner
 */
public class DirectSample
{
  public static void main(String[] args) throws SQLException
  {
    final DriverConnectionProvider dcp = new DriverConnectionProvider();
    dcp.setDriver("mondrian.olap4j.MondrianOlap4jDriver");
    dcp.setProperty("Catalog", "demo/steelwheels.mondrian.xml");
    dcp.setProperty("JdbcUser", "sa");
    dcp.setProperty("JdbcPassword", "");
    dcp.setProperty("Jdbc", "jdbc:hsqldb:./sql/sampledata");
    dcp.setProperty("JdbcDrivers", "org.hsqldb.jdbcDriver");
    dcp.setUrl("jdbc:mondrian:");

    final List<Integer> pos = new ArrayList();
    pos.add(0);
    pos.add(0);

    OlapWrapper wrapper = dcp.createConnection(null, null);
    OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
//    final String mdxQuery = "select [Product].parent on 0, [Time].Children on 1 from [SteelWheelsSales]";
    final String mdxQuery = "select from [SteelWheelsSales]";
    PreparedOlapStatement statement = olapConnection.prepareOlapStatement(mdxQuery);
    final CellSet cellSet = statement.executeQuery();
    new DenormalizedMDXTableModel(new QueryResultWrapper(statement, cellSet));
    final List<Measure> measureList = cellSet.getMetaData().getCube().getMeasures();
    for (int i = 0; i < measureList.size(); i++)
    {
      Measure measure = measureList.get(i);

      System.out.println(measure.isVisible() + " " + measure.getUniqueName());
    }
    final Cell cell = cellSet.getCell(new ArrayList<Integer>());
    System.out.println(cell);


  }
}
