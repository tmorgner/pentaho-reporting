package org.pentaho.reporting.engine.classic.extensions.datasources.kettle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.SQLException;
import javax.swing.table.TableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DefaultResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.TableModelInfo;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document me!
 * <p/>
 * Date: 27.01.11
 * Time: 16:29
 *
 * @author Thomas Morgner.
 */
public class KettleDataFactoryTest extends TestCase
{
  private static final String QUERY = "test-src/org/pentaho/reporting/engine/classic/extensions/datasources/kettle/row-gen.ktr";
  private static final String STEP = "Formula";

  public KettleDataFactoryTest()
  {
  }

  public void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testData() throws SQLException, ReportDataFactoryException, IOException
  {
    final ByteArrayOutputStream sw = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(sw);
    performQueryTest(QUERY, STEP, ps);
    System.out.println(sw.toString());
    compareLineByLine("query-results.txt", sw.toString());

  }

  private void compareLineByLine(final String sourceFile, final String resultText) throws IOException
  {
    final BufferedReader resultReader = new BufferedReader(new StringReader(resultText));
    final BufferedReader compareReader = new BufferedReader(new InputStreamReader
        (ObjectUtilities.getResourceRelativeAsStream(sourceFile, KettleDataFactoryTest.class)));
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

  private void performQueryTest(final String file, final String stepName,
                                final PrintStream out) throws SQLException, ReportDataFactoryException
  {
    final KettleDataFactory kettleDataFactory = new KettleDataFactory();
    try
    {
      final KettleTransFromFileProducer producer =
          new KettleTransFromFileProducer(file, stepName, new String[0], new ParameterMapping[0]);

      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
      kettleDataFactory.initialize
          (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
      kettleDataFactory.setQuery("default", producer);
      final TableModel tableModel = kettleDataFactory.queryData("default", new ParameterDataRow());
      TableModelInfo.printTableModel(tableModel, out);
      TableModelInfo.printTableModelContents(tableModel, out);
    }
    finally
    {

      kettleDataFactory.close();
    }
  }

  public void testMetaData()
  {
    final KettleDataFactory kettleDataFactory = new KettleDataFactory();
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    kettleDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    kettleDataFactory.setQuery("default",
        new KettleTransFromFileProducer(QUERY, STEP, new String[0], new ParameterMapping[0]));

    final DataFactoryMetaData metaData = DataFactoryRegistry.getInstance().getMetaData(KettleDataFactory.class.getName());
    final Object queryHash = metaData.getQueryHash(kettleDataFactory, "default", new StaticDataRow());
    assertNotNull(queryHash);

    final KettleDataFactory kettleDataFactory2 = new KettleDataFactory();
    kettleDataFactory2.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    kettleDataFactory2.setQuery("default",
        new KettleTransFromFileProducer(QUERY + "2", STEP, new String[0], new ParameterMapping[0]));
    kettleDataFactory2.setQuery("default2",
        new KettleTransFromFileProducer(QUERY, STEP, new String[0], new ParameterMapping[0]));

    assertNotEquals("Physical Query is not the same", queryHash, metaData.getQueryHash(kettleDataFactory2, "default", new StaticDataRow()));
    assertEquals("Physical Query is the same", queryHash, metaData.getQueryHash(kettleDataFactory2, "default2", new StaticDataRow()));
  }

  public void testParameter()
  {
    final KettleDataFactory kettleDataFactory = new KettleDataFactory();
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    kettleDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    final ParameterMapping[] parameterMappings = {
        new ParameterMapping("name", "kettle-name"),
        new ParameterMapping("name2", "k3"),
        new ParameterMapping("name", "k2")
    };
    final String[] argumentNames = {"arg0"};
    kettleDataFactory.setQuery("default",
        new KettleTransFromFileProducer(QUERY, STEP, argumentNames, parameterMappings));

    final DataFactoryMetaData metaData = DataFactoryRegistry.getInstance().getMetaData(KettleDataFactory.class.getName());
    final String[] fields = metaData.getReferencedFields(kettleDataFactory, "default", new StaticDataRow());
    assertNotNull(fields);
    assertEquals(4, fields.length);
    assertEquals("arg0", fields[0]);
    assertEquals("name", fields[1]);
    assertEquals("name2", fields[2]);
    assertEquals(DataFactory.QUERY_LIMIT, fields[3]);
  }

  private static void assertNotEquals(final String message, final Object o1, final Object o2)
  {
    if (o1 == o2)
    {
      fail(message);
    }
    if (o1 != null && o2 == null)
    {
      return;
    }
    if (o1 == null)
    {
      return;
    }
    if (o1.equals(o2))
    {
      fail(message);
    }
  }
}
