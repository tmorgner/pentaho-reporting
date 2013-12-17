package org.pentaho.reporting.engine.classic.core.modules.output.csv;

import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.elementfactory.ContentFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;

/**
 * Todo: Document me!
 * <p/>
 * Date: 30.10.2009
 * Time: 16:42:27
 *
 * @author Thomas Morgner.
 */
public class CSVExportTest extends TestCase
{
  public CSVExportTest()
  {
  }

  public CSVExportTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testExport() throws Exception
  {
    final MasterReport report = new MasterReport();
    final ItemBand itemBand = report.getItemBand();
    final TextFieldElementFactory cfef = new TextFieldElementFactory();
    cfef.setFieldname("field");
    cfef.setMinimumWidth(new Float(500));
    cfef.setMinimumHeight(new Float(200));
    itemBand.addElement(cfef.createElement());

    final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"field"}, 2000);
    for (int row = 0; row < tableModel.getRowCount(); row++)
    {
      tableModel.setValueAt("Value row = " + row, row, 0);
    }

    report.setDataFactory(new TableDataFactory("default", tableModel));

    DebugReportRunner.createDataCSV(report);
  }
}
