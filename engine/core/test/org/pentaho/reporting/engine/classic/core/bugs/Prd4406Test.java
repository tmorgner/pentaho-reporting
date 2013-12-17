package org.pentaho.reporting.engine.classic.core.bugs;

import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.function.ItemSumFunction;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;

public class Prd4406Test extends TestCase
{
  public Prd4406Test()
  {
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testSharedConnections() throws ReportProcessingException
  {
    MasterReport report = new MasterReport();
    report.setDataFactory(new TableDataFactory("query", new DefaultTableModel(2,2)));
    report.setQuery("query");
    report.addExpression(new ItemSumFunction());

    report.setAttribute(AttributeNames.Internal.NAMESPACE, AttributeNames.Internal.SHARED_CONNECTIONS, Boolean.TRUE);

    // this will fail if the bug exists in 3.9
    DebugReportRunner.createPDF(report);

  }

  public void testPrivateConnections() throws ReportProcessingException
  {
    MasterReport report = new MasterReport();
    report.setDataFactory(new TableDataFactory("query", new DefaultTableModel(2,2)));
    report.setQuery("query");
    report.addExpression(new ItemSumFunction());

    report.setAttribute(AttributeNames.Internal.NAMESPACE, AttributeNames.Internal.SHARED_CONNECTIONS, Boolean.FALSE);

    // this will fail if the bug exists in 4.0
    DebugReportRunner.createPDF(report);

  }
}
