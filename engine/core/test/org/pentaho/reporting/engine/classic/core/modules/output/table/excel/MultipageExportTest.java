package org.pentaho.reporting.engine.classic.core.modules.output.table.excel;

import java.net.URL;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.bugs.Pre422Test;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 12.09.2009
 * Time: 14:59:50
 *
 * @author Thomas Morgner.
 */
public class MultipageExportTest extends TestCase
{
  public MultipageExportTest()
  {
  }

  public MultipageExportTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testSecondPageFormatting() throws Exception
  {
    final URL target = MultipageExportTest.class.getResource("multipage.prpt");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();

 //   DebugReportRunner.createXLS(report);
  }
}
