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
 * Copyright (c) 2008 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package demo;

import java.io.File;
import java.io.IOException;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewFrame;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingUtil;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.wizard.RelationalAutoGeneratorPreProcessor;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DenormalizedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DriverDataSourceProvider;
import org.pentaho.reporting.libraries.repository.ContentIOException;

/**
 * Todo: Document me!
 *
 * @author : Thomas Morgner
 */
public class SimpleDemo
{
  public SimpleDemo()
  {
  }

  /**
   * Creates the report. For XML reports, this will most likely call the ReportGenerator, while API reports may use this
   * function to build and return a new, fully initialized report object.
   *
   * @return the fully initialized JFreeReport object.
   */
  public MasterReport createReport()
  {
    if (false)
    {
      final DenormalizedMDXDataFactory mondrianDataFactory = new DenormalizedMDXDataFactory();
      final DriverDataSourceProvider provider = new DriverDataSourceProvider();
      provider.setDriver("org.hsqldb.jdbcDriver");
      provider.setUrl("jdbc:hsqldb:./sql/sampledata");
      mondrianDataFactory.setCubeFileProvider(new DefaultCubeFileProvider("demo/steelwheels.mondrian.xml"));
      mondrianDataFactory.setDataSourceProvider(provider);
      mondrianDataFactory.setJdbcUser("sa");
      mondrianDataFactory.setJdbcPassword("");

      mondrianDataFactory.setQuery("default",
          "select {[Markets].[All Markets].[APAC], [Markets].[All Markets].[EMEA], " +
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
              "from [SteelWheelsSales]\n");

      final MasterReport report = new MasterReport();
      report.setDataFactory(mondrianDataFactory);
      report.setQuery("default");
      report.addPreProcessor(new RelationalAutoGeneratorPreProcessor());
      return report;
    }
    else
    {
      final BandedMDXDataFactory mondrianDataFactory = new BandedMDXDataFactory();
      final DriverDataSourceProvider provider = new DriverDataSourceProvider();
      provider.setDriver("org.hsqldb.jdbcDriver");
      provider.setUrl("jdbc:hsqldb:./sql/sampledata");
      mondrianDataFactory.setCubeFileProvider(new DefaultCubeFileProvider("demo/steelwheels.mondrian.xml"));
      mondrianDataFactory.setDataSourceProvider(provider);
      mondrianDataFactory.setJdbcUser("sa");
      mondrianDataFactory.setJdbcPassword("");

      mondrianDataFactory.setQuery("default",
          "select {[Markets].[All Markets].[APAC], [Markets].[All Markets].[EMEA], " +
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
              "from [SteelWheelsSales]\n");

      final MasterReport report = new MasterReport();
      report.setDataFactory(mondrianDataFactory);
      report.setQuery("default");
      report.addPreProcessor(new RelationalAutoGeneratorPreProcessor());
      return report;
    }
  }

  public void writeBundle(final String path)
      throws IOException, ContentIOException, BundleWriterException
  {
    BundleWriter.writeReportToZipFile(createReport(), new File(path));
  }

  public static void main(String[] args) throws ReportProcessingException, IOException, ContentIOException, BundleWriterException
  {
    ClassicEngineBoot.getInstance().start();

    final SimpleDemo handler = new SimpleDemo();
    //   handler.writeBundle("/tmp/auto-table.prc");

    final PreviewFrame frame = new PreviewFrame(handler.createReport());
    frame.pack();
    SwingUtil.centerFrameOnScreen(frame);
    frame.setVisible(true);
//    ExcelReportUtil.createXLS(handler.createReport(), "/tmp/report.xls");
//    PdfReportUtil.createPDF(handler.createReport(), "/tmp/report.pdf");

  }
}
