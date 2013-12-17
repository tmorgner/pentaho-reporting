package org.pentaho.reporting.engine.classic.core.bugs;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.ReportHeader;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document me!
 * <p/>
 * Date: 07.03.11
 * Time: 14:36
 *
 * @author Thomas Morgner.
 */
public class Prd3284Test extends TestCase
{
  public Prd3284Test()
  {
  }

  public Prd3284Test(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testParse() throws ResourceException
  {
    ResourceManager mgr = new ResourceManager();
    mgr.registerDefaults();
    final Resource resource = mgr.createDirectly(Prd3284Test.class.getResource("Prd-3284.xml"), MasterReport.class);
    final MasterReport report = (MasterReport) resource.getResource();
    final ReportHeader reportHeader = report.getReportHeader();
    final ReportElement error = reportHeader.getElement(1);
    final ReportElement sane = reportHeader.getElement(2);
    assertEquals(-100.0f, error.getStyle().getStyleProperty(ElementStyleKeys.MIN_WIDTH));
        DebugLog.logHere();
  }
}
