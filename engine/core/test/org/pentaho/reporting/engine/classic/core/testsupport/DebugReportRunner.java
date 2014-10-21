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
 * Copyright (c) 2000 - 2009 Pentaho Corporation, Simba Management Limited and Contributors...  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.testsupport;

import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.table.DefaultTableModel;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.EmptyReportException;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportParameterValidationException;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.Renderer;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.output.ContentProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.output.ReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.core.modules.output.csv.CSVDataReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.graphics.PrintReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.graphics.internal.PhysicalPageDrawable;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.plaintext.PlainTextReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.xml.XmlPageReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.PageableHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.SingleRepositoryURLRewriter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xml.XmlTableOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xml.XmlTableReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xml.internal.XmlTableOutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.modules.output.xml.XMLProcessor;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.font.LocalFontRegistry;
import org.pentaho.reporting.engine.classic.core.testsupport.gold.GoldTestBase;
import org.pentaho.reporting.engine.classic.core.util.NullOutputStream;
import org.pentaho.reporting.libraries.base.util.MemoryByteArrayOutputStream;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;
import org.pentaho.reporting.libraries.fonts.monospace.MonospaceFontRegistry;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontStorage;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.RepositoryUtilities;
import org.pentaho.reporting.libraries.repository.zipwriter.ZipRepository;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class DebugReportRunner
{
  private static final Log logger = LogFactory.getLog(DebugReportRunner.class);

  private DebugReportRunner()
  {
  }

  public static boolean createPlainText(final MasterReport report)
  {
    try
    {
      PlainTextReportUtil.createPlainText(report, new NullOutputStream(), 10, 15);
      return true;
    }
    catch (ReportParameterValidationException p)
    {
      return true;
    }
    catch (Exception rpe)
    {
      logger.debug("Failed to execute plain text: ", rpe);
      Assert.fail();
      return false;
    }
  }

  public static void createRTF(final MasterReport report)
      throws Exception
  {
    try
    {
      RTFReportUtil.createRTF(report, new NullOutputStream());
    }
    catch (IndexOutOfBoundsException ibe)
    {
      // this is a known iText bug that does not get fixed.
    }
    catch (ReportParameterValidationException p)
    {
      // reports that have mandatory parameters are ok to fail.
      Assert.fail();
    }
  }

  public static void createXmlFlow(final MasterReport report)
      throws Exception
  {
    XmlTableReportUtil.createFlowXML(report, new NullOutputStream());
  }

  public static void createXmlStream(final MasterReport report)
      throws Exception
  {
    XmlTableReportUtil.createStreamXML(report, new NullOutputStream());
  }

  public static byte[] createXmlTablePageable(final MasterReport report)
      throws IOException, ReportProcessingException
  {
    final MemoryByteArrayOutputStream outputStream = new MemoryByteArrayOutputStream();
    try
    {
      final LocalFontRegistry localFontRegistry = new LocalFontRegistry();
      localFontRegistry.initialize();
      final XmlTableOutputProcessor outputProcessor =
          new XmlTableOutputProcessor(outputStream, new XmlTableOutputProcessorMetaData(
              report.getConfiguration(),
              XmlTableOutputProcessorMetaData.PAGINATION_FULL, localFontRegistry));
      final ReportProcessor streamReportProcessor = new PageableReportProcessor(report, outputProcessor);
      try
      {
        streamReportProcessor.processReport();
      }
      finally
      {
        streamReportProcessor.close();
      }
    }
    finally
    {
      outputStream.close();
    }
    return (outputStream.toByteArray());
  }

  public static void createXmlPageable(final MasterReport report)
      throws Exception
  {
    XmlPageReportUtil.createXml(report, new NullOutputStream());
  }

  public static void createCSV(final MasterReport report)
      throws Exception
  {
    try
    {
      CSVReportUtil.createCSV(report, new NullOutputStream(), null);
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createDataCSV(final MasterReport report)
      throws Exception
  {
    try
    {
      CSVDataReportUtil.createCSV(report, new NullOutputStream(), "UTF-8");
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createDataXML(final MasterReport report)
      throws Exception
  {
    try
    {
      final XMLProcessor pr = new XMLProcessor(report);
      final Writer fout = new BufferedWriter(new OutputStreamWriter(new NullOutputStream(), "UTF-8"));
      pr.setWriter(fout);
      pr.processReport();
      fout.flush();
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createXLS(final MasterReport report)
      throws Exception
  {
    try
    {
      ExcelReportUtil.createXLS(report, new NullOutputStream());
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createStreamHTML(final MasterReport report)
      throws Exception
  {
    try
    {
      HtmlReportUtil.createStreamHTML(report, new NullOutputStream());
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createZIPHTML(final MasterReport report)
      throws Exception
  {
    try
    {
      HtmlReportUtil.createZIPHTML(report, new NullOutputStream(), "report.html");
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static void createPageableHTML(final MasterReport report)
      throws Exception
  {
    try
    {
      if (report == null)
      {
        throw new NullPointerException();
      }

      try
      {
        final ZipRepository zipRepository = new ZipRepository(new NullOutputStream());
        final ContentLocation root = zipRepository.getRoot();
        final ContentLocation data = RepositoryUtilities.createLocation
            (zipRepository, RepositoryUtilities.splitPath("data", "/"));

        final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());

        final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
        printer.setContentWriter(root, new DefaultNameGenerator(root, "report.html"));
        printer.setDataWriter(data, new DefaultNameGenerator(data, "content"));
        printer.setUrlRewriter(new SingleRepositoryURLRewriter());
        outputProcessor.setPrinter(printer);

        final PageableReportProcessor sp = new PageableReportProcessor(report, outputProcessor);
        sp.processReport();
        sp.close();
        zipRepository.close();
      }
      catch (IOException ioe)
      {
        throw ioe;
      }
      catch (ReportProcessingException re)
      {
        throw re;
      }
      catch (Exception re)
      {
        throw new ReportProcessingException("Failed to process the report", re);
      }
    }
    catch (ReportParameterValidationException e)
    {
      Assert.fail();
    }
  }

  public static boolean execGraphics2D(final MasterReport report)
  {
    try
    {
      final PrintReportProcessor proc = new PrintReportProcessor(report);
      final int nop = proc.getNumberOfPages();
      if (proc.isError())
      {
        if (proc.getErrorReason() instanceof ReportParameterValidationException)
        {
          return true;
        }
        logger.error("Failed to process report", proc.getErrorReason());
        Assert.fail();
        return false;
      }
      if (nop == 0)
      {
        return false;
      }
      for (int i = 0; i < nop; i++)
      {
        if (proc.getPageDrawable(i) == null)
        {
          Assert.fail();
          return false;
        }
      }
      proc.close();
      return true;
    }
    catch (ReportParameterValidationException p)
    {
      // reports that have mandatory parameters are ok to fail.
      return true;
    }
    catch (EmptyReportException ere)
    {
      return true;
    }
    catch (Exception e)
    {
      Assert.fail();
      logger.error("Generating Graphics2D failed.", e);
      return false;
    }
  }

  /**
   * Saves a report to PDF format.
   *
   * @param report the report.
   * @return true or false.
   */
  public static boolean createPDF(final MasterReport report)
  {
    OutputStream out = new NullOutputStream();
    try
    {
      final PdfOutputProcessor outputProcessor = new PdfOutputProcessor(report.getConfiguration(), out,
          report.getResourceManager());
      final PageableReportProcessor proc = new PageableReportProcessor(report, outputProcessor);
      proc.processReport();
      return true;
    }
    catch (ReportParameterValidationException e)
    {
      return true;
    }
    catch (Exception e)
    {
      logger.error("Writing PDF failed.", e);
      return false;
    }
  }

  public static void executeAll(final MasterReport report) throws Exception
  {
    logger.debug("   GRAPHICS2D ..");
    TestCase.assertTrue(DebugReportRunner.execGraphics2D(report));
    logger.debug("   PDF ..");
    TestCase.assertTrue(DebugReportRunner.createPDF(report));
    logger.debug("   CSV ..");
    DebugReportRunner.createCSV(report);
    logger.debug("   PLAIN_TEXT ..");
    TestCase.assertTrue(DebugReportRunner.createPlainText(report));
    logger.debug("   RTF ..");
    DebugReportRunner.createRTF(report);
    logger.debug("   STREAM_HTML ..");
    DebugReportRunner.createStreamHTML(report);
    logger.debug("   EXCEL ..");
    DebugReportRunner.createXLS(report);
    logger.debug("   ZIP_HTML ..");
    DebugReportRunner.createZIPHTML(report);
  }

  public static LogicalPageBox layoutPage(final MasterReport report, final int page) throws Exception
  {
    final PrintReportProcessor proc = new PrintReportProcessor(report);
    final int nop = proc.getNumberOfPages();
    if (proc.isError())
    {
      if (proc.getErrorReason() instanceof ReportParameterValidationException)
      {
        return null;
      }
      if (proc.getErrorReason() != null)
      {
        proc.getErrorReason().printStackTrace();
      }
      Assert.fail();
      return null;
    }

    if (nop == 0)
    {
      throw new EmptyReportException("Empty report");
    }

    final PhysicalPageDrawable pageDrawable = (PhysicalPageDrawable) proc.getPageDrawable(page);
    return pageDrawable.getPageDrawable().getLogicalPageBox();
  }

  public static LogicalPageBox layoutSingleBand(final MasterReport report,
                                                final Band reportHeader)
      throws ReportProcessingException, ContentProcessingException
  {
    return layoutSingleBand(report, reportHeader, true, false);
  }

  public static LogicalPageBox layoutSingleBand(final MasterReport originalReport,
                                                final Band reportHeader,
                                                final boolean monospaced,
                                                final boolean expectPageBreak)
      throws ReportProcessingException, ContentProcessingException
  {
    final ReportStateKey stateKey = new ReportStateKey();

    final DebugOutputProcessorMetaData metaData;
    if (monospaced)
    {
      metaData = new DebugOutputProcessorMetaData(originalReport.getConfiguration(),
          new DefaultFontStorage(new MonospaceFontRegistry(9, 18)));
    }
    else
    {
      metaData = new DebugOutputProcessorMetaData(originalReport.getConfiguration());
    }

    try
    {

      final MasterReport report = (MasterReport) originalReport.derive(true);

      final ProcessingContext processingContext = new DefaultProcessingContext(report, metaData);
      final DebugExpressionRuntime runtime = new DebugExpressionRuntime
          (new DefaultTableModel(), 0, processingContext);

      final DebugRenderer debugLayoutSystem = new DebugRenderer(metaData);
      debugLayoutSystem.setStateKey(stateKey);
      debugLayoutSystem.startReport(report, processingContext);
      debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);
      debugLayoutSystem.add(reportHeader, runtime, stateKey);
      debugLayoutSystem.endSection();
      if (expectPageBreak)
      {
        debugLayoutSystem.endReport();
        final Renderer.LayoutResult result = debugLayoutSystem.validatePages();
        Assert.assertEquals(Renderer.LayoutResult.LAYOUT_PAGEBREAK, result);
      }
      else
      {
        debugLayoutSystem.validatePages();
      }
      return debugLayoutSystem.getPageBox();
    }
    catch (CloneNotSupportedException cne)
    {
      throw new ReportProcessingException("Clone failed", cne);
    }
  }

  public static MasterReport parseGoldenSampleReport(final String name) throws ResourceException
  {
    final File file = GoldTestBase.locateGoldenSampleReport(name);
    if (file == null)
    {
      throw new ResourceException("Unable to locate report '" + name + "' in the golden samples.");
    }

    final ResourceManager mgr = new ResourceManager();
    mgr.registerDefaults();
    return (MasterReport) mgr.createDirectly(file, MasterReport.class).getResource();
  }

  public static void showDialog(final MasterReport report)
  {
    if (GraphicsEnvironment.isHeadless())
    {
      return;
    }

    final PreviewDialog dialog = new PreviewDialog(report);
    dialog.setModal(true);
    dialog.pack();
    LibSwingUtil.centerFrameOnScreen(dialog);
    dialog.setVisible(true);
  }
}
