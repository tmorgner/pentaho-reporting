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
 * Copyright (c) 2008 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package demo;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.IOException;
import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.CrosstabColumnGroup;
import org.pentaho.reporting.engine.classic.core.CrosstabColumnGroupBody;
import org.pentaho.reporting.engine.classic.core.CrosstabGroup;
import org.pentaho.reporting.engine.classic.core.CrosstabRowGroup;
import org.pentaho.reporting.engine.classic.core.CrosstabRowGroupBody;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ElementAlignment;
import org.pentaho.reporting.engine.classic.core.GroupDataBody;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.elementfactory.LabelElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewFrame;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingUtil;
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.TableModelInfo;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.util.PageFormatFactory;
import org.pentaho.reporting.engine.classic.core.util.PageSize;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.DenormalizedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.DriverConnectionProvider;
import org.pentaho.reporting.libraries.base.util.FloatDimension;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;

/**
 * Todo: Document me!
 *
 * @author : Thomas Morgner
 */
public class Olap4JSimpleDemo
{
  public Olap4JSimpleDemo()
  {
  }

  /**
   * Returns the display name of the demo.
   *
   * @return the name.
   */
  public String getDemoName()
  {
    return "Automatic-Report Demo";
  }

  /**
   * Creates the report. For XML reports, this will most likely call the ReportGenerator, while API reports may use this
   * function to build and return a new, fully initialized report object.
   *
   * @return the fully initialized JFreeReport object.
   */
  public MasterReport createReport()
  {
    final DriverConnectionProvider dcp = new DriverConnectionProvider();
    dcp.setDriver("mondrian.olap4j.MondrianOlap4jDriver");
    dcp.setProperty("Catalog", "demo/steelwheels.mondrian.xml");
    dcp.setProperty("JdbcUser", "sa");
    dcp.setProperty("JdbcPassword", "");
    dcp.setProperty("Jdbc", "jdbc:hsqldb:./sql/sampledata");
    dcp.setProperty("JdbcDrivers", "org.hsqldb.jdbcDriver");
    dcp.setUrl("jdbc:mondrian:");

    final DataFactory dataFactory;

    final DenormalizedMDXDataFactory mondrianDataFactory = new DenormalizedMDXDataFactory(dcp);
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
    dataFactory = mondrianDataFactory;

    final PageFormatFactory pageFormatFactory = PageFormatFactory.getInstance();
    final Paper paper = pageFormatFactory.createPaper(PageSize.A4);
    final PageFormat pageFormat = pageFormatFactory.createPageFormat(paper, PageFormat.LANDSCAPE);

    final MasterReport report = new MasterReport();
    report.setDataFactory(dataFactory);
    report.setQuery("default");
    report.setPageDefinition(new SimplePageDefinition(pageFormat));

    final ItemBand detailsBand = new ItemBand();

    final LabelElementFactory labelFactory = new LabelElementFactory();
    labelFactory.setAbsolutePosition(new Point2D.Float(0, 0));
    labelFactory.setMinimumSize(new FloatDimension(76, 12));
    labelFactory.setHorizontalAlignment(ElementAlignment.LEFT);
    labelFactory.setVerticalAlignment(ElementAlignment.MIDDLE);

    final TextFieldElementFactory factory = new TextFieldElementFactory();
    factory.setAbsolutePosition(new Point2D.Float(0, 0));
    factory.setMinimumSize(new FloatDimension(76, 12));
    factory.setHorizontalAlignment(ElementAlignment.LEFT);
    factory.setVerticalAlignment(ElementAlignment.MIDDLE);
    factory.setNullString("");
    factory.setFieldname("[Measures].[Quantity]");
    detailsBand.addElement(factory.createElement());

    final GroupDataBody body = new GroupDataBody();
    body.setItemBand(detailsBand);

    final CrosstabColumnGroup marketsGroup = new CrosstabColumnGroup();
    marketsGroup.setField("[Markets].[Territory]");
    factory.setFieldname("[Markets].[Territory]");
    marketsGroup.getHeader().addElement(factory.createElement());
    marketsGroup.getHeader().getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, Color.YELLOW);
    labelFactory.setText("Territory");
    marketsGroup.getTitleHeader().addElement(labelFactory.createElement());
    marketsGroup.setBody(body);

    final CrosstabColumnGroup territoryGroup = new CrosstabColumnGroup();
    territoryGroup.setField("[Markets].[(All)]");
    factory.setFieldname("[Markets].[(All)]");
    territoryGroup.getHeader().addElement(factory.createElement());
    territoryGroup.getHeader().getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, Color.ORANGE);
    labelFactory.setText("Marktes");
    territoryGroup.getTitleHeader().addElement(labelFactory.createElement());
    territoryGroup.setBody(new CrosstabColumnGroupBody(marketsGroup));

    final CrosstabRowGroup quartersGroup = new CrosstabRowGroup();
    quartersGroup.setField("[Time].[Quarters]");
    factory.setFieldname("[Time].[Quarters]");
    quartersGroup.getHeader().addElement(factory.createElement());
    quartersGroup.getHeader().getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, Color.LIGHT_GRAY);
    labelFactory.setText("Quarters");
    quartersGroup.getTitleHeader().addElement(labelFactory.createElement());
    quartersGroup.setBody(new CrosstabColumnGroupBody(territoryGroup));

    final CrosstabRowGroup yearsGroup = new CrosstabRowGroup();
    yearsGroup.setField("[Time].[Years]");
    factory.setFieldname("[Time].[Years]");
    yearsGroup.getHeader().addElement(factory.createElement());
    yearsGroup.getHeader().getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, Color.GRAY);
    labelFactory.setText("Years");
    yearsGroup.getTitleHeader().addElement(labelFactory.createElement());
    yearsGroup.setBody(new CrosstabRowGroupBody(quartersGroup));

    final CrosstabRowGroup plineGroup = new CrosstabRowGroup();
    plineGroup.setField("[Product].[Line]");
    plineGroup.setBody(new CrosstabRowGroupBody(yearsGroup));
    factory.setFieldname("[Product].[Line]");
    plineGroup.getHeader().addElement(factory.createElement());
    plineGroup.getHeader().getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, Color.MAGENTA);
    labelFactory.setText("Product Line");
    plineGroup.getTitleHeader().addElement(labelFactory.createElement());

    final CrosstabGroup group = new CrosstabGroup();
    group.setBody(new CrosstabRowGroupBody(plineGroup));
    report.setRootGroup(group);
    return report;
  }

  public void writeBundle(final String path)
      throws IOException, ContentIOException, BundleWriterException
  {
    BundleWriter.writeReportToZipFile(createReport(), new File(path));
  }

  public static void main(String[] args) throws ReportProcessingException, IOException, ContentIOException, BundleWriterException, ResourceKeyCreationException, ResourceCreationException, ResourceLoadingException
  {
    ClassicEngineBoot.getInstance().start();

    final Olap4JSimpleDemo handler = new Olap4JSimpleDemo();
    handler.writeBundle("/tmp/auto-table.prpt");

    ResourceManager rmg = new ResourceManager();
    rmg.registerDefaults();
    rmg.createDirectly("file:///tmp/auto-table.prpt", MasterReport.class);
//    final PreviewFrame frame = new PreviewFrame(handler.createReport());
//    frame.pack();
//    SwingUtil.centerFrameOnScreen(frame);
//    frame.setVisible(true);
//    ExcelReportUtil.createXLS(handler.createReport(), "/tmp/report.xls");
//    PdfReportUtil.createPDF(handler.createReport(), "/tmp/report.pdf");

  }
}