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

package org.pentaho.reporting.engine.classic.extensions.datasources.pmd;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DefaultResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.MetaAttributeNames;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.TableModelInfo;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.engine.classic.core.wizard.ConceptQueryMapper;
import org.pentaho.reporting.engine.classic.core.wizard.DataAttributes;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchema;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaCompiler;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultConceptQueryMapper;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataAttributeContext;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataSchemaDefinition;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class PentahoMetaDataTest extends TestCase
{
  private static final String QUERY =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                  "<mql>" +
                  "  <domain_type>relational</domain_type>" +
                  "  <domain_id>steel-wheels</domain_id>" +
                  "  <model_id>BV_HUMAN_RESOURCES</model_id>" +
                  "  <model_name>Human Resources</model_name>" +
                  "  <options>" +
                  "     <disable_distinct>true</disable_distinct>" +
                  "  </options>" +
                  "  <selections>" +
                  "    <selection>" +
                  "       <view>BC_EMPLOYEES_</view>" +
                  "       <column>BC_EMPLOYEES_FIRSTNAME</column>" +
                  "    </selection>" +
                  "    <selection>" +
                  "       <view>BC_EMPLOYEES_</view>" +
                  "       <column>BC_EMPLOYEES_LASTNAME</column>" +
                  "    </selection>" +
                  "    <selection>" +
                  "      <view>BC_EMPLOYEES_</view>" +
                  "      <column>BC_EMPLOYEES_EMPLOYEENUMBER</column>" +
                  "    </selection>" +
                  "    <selection>" +
                  "      <view>BC_EMPLOYEES_</view>" +
                  "      <column>BC_EMPLOYEES_EMAIL</column>" +
                  "    </selection>" +
                  "  </selections>" +
                  "  <constraints/>" +
                  "  <orders>" +
                  "    <order>" +
                  "      <direction>asc</direction>" +
                  "      <view_id>BC_OFFICES_</view_id>" +
                  "      <column_id>BC_OFFICES_COUNTRY</column_id>" +
                  "    </order>" +
                  "    <order>" +
                  "      <direction>asc</direction>" +
                  "      <view_id>BC_OFFICES_</view_id>" +
                  "      <column_id>BC_OFFICES_STATE</column_id>" +
                  "    </order>" +
                  "  </orders>" +
                  "</mql>";

  private static final String PARAMETRIZED_QUERY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<mql>" +
      "<domain_id>steel-wheels</domain_id>" +
      "<model_id>BV_ORDERS</model_id>" +
      "<options>" +
      "<disable_distinct>false</disable_distinct>" +
      "</options>" +
      "<parameters>" +
      "<parameter defaultValue=\"Shipped\" name=\"oStatus\" type=\"STRING\"/>" +
      "</parameters>" +
      "<selections>" +
      "<selection>" +
      "<view>BC_CUSTOMER_W_TER_</view>" +
      "<column>BC_CUSTOMER_W_TER_CUSTOMERNUMBER</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>BC_CUSTOMER_W_TER_</view>" +
      "<column>BC_CUSTOMER_W_TER_CUSTOMERNAME</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERS_ORDERNUMBER</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERDETAILS_TOTAL</column>" +
      "<aggregation>SUM</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERS_STATUS</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERS_COMMENTS</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "</selections>" +
      "<constraints>" +
      "<constraint>" +
      "<operator/>" +
      "<condition>[CAT_ORDERS.BC_ORDERS_STATUS] = [param:oStatus]</condition>" +
      "</constraint>" +
      "</constraints>" +
      "<orders/>" +
      "</mql>";
    
  private static final String MULTIPLE_AGG_QUERY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<mql>" +
      "<domain_id>steel-wheels</domain_id>" +
      "<model_id>BV_ORDERS</model_id>" +
      "<options>" +
      "<disable_distinct>false</disable_distinct>" +
      "</options>" +
      "<selections>" +
      "<selection>" +
      "<view>BC_CUSTOMER_W_TER_</view>" +
      "<column>BC_CUSTOMER_W_TER_TERRITORY</column>" +
      "<aggregation>NONE</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERDETAILS_QUANTITYORDERED</column>" +
      "<aggregation>SUM</aggregation>" +
      "</selection>" +
      "<selection>" +
      "<view>CAT_ORDERS</view>" +
      "<column>BC_ORDERDETAILS_QUANTITYORDERED</column>" +
      "<aggregation>AVERAGE</aggregation>" +
      "</selection>" +
      "</selections>" +
      "<constraints>" +
      "</constraints>" +
      "<orders/>" +
      "</mql>";
  
  public PentahoMetaDataTest()
  {
  }

  public PentahoMetaDataTest(final String s)
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
    performQueryTest(QUERY, ps);
    System.out.println(sw);
    compareLineByLine("query-results.txt", sw.toString());
  }

  private void compareLineByLine(final String sourceFile, final String resultText) throws IOException
  {
    final BufferedReader resultReader = new BufferedReader(new StringReader(resultText));
    final BufferedReader compareReader = new BufferedReader(new InputStreamReader
            (ObjectUtilities.getResourceRelativeAsStream(sourceFile, PentahoMetaDataTest.class)));
    try
    {
      int line = 1;
      String lineResult = resultReader.readLine();
      String lineSource = compareReader.readLine();
      while (lineResult != null && lineSource != null)
      {
        assertEqualsFixPmd(line, lineSource, lineResult);
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

  private void assertEqualsFixPmd(final int lineNumber, final String line1, final String line2)
  {
    final int firstAtSign = line1.indexOf('@');
    if (firstAtSign == -1)
    {
      assertEquals("Failure in line " + lineNumber, line1, line2);
      return;
    }
    if (firstAtSign == line2.indexOf('@'))
    {
      // strip out the identifier ..
      assertEquals("Failure in line " + lineNumber, line1.substring(0, firstAtSign), line2.substring(0, firstAtSign));
      return;
    }
    assertEquals("Failure in line " + lineNumber, line1, line2);
  }

  private void performQueryTest(final String query,
                                final PrintStream out) throws SQLException, ReportDataFactoryException
  {
    final PmdDataFactory pmdDataFactory = new PmdDataFactory();
    pmdDataFactory.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory.setDomainId("steel-wheels");
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    pmdDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);

    try
    {
      pmdDataFactory.setQuery("default", query, null, null);
      final CloseableTableModel tableModel = (CloseableTableModel) pmdDataFactory.queryData("default", new ParameterDataRow());
      try
      {
        TableModelInfo.printTableModel(tableModel, out);
        TableModelInfo.printTableModelContents(tableModel, out);
        TableModelInfo.printTableMetaData(tableModel, out);
        TableModelInfo.printTableCellAttributes(tableModel, out);
      }
      finally
      {
        tableModel.close();
      }
    }
    finally
    {

      pmdDataFactory.close();
    }
  }

  public void testDataSchemaCompiler() throws Exception
  {

    final PmdDataFactory pmdDataFactory = new PmdDataFactory();
    pmdDataFactory.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory.setDomainId("steel-wheels");
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    pmdDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);

    try
    {
      pmdDataFactory.setQuery("default", QUERY, null, null);
      final CloseableTableModel tableModel = (CloseableTableModel) pmdDataFactory.queryData("default", new ParameterDataRow());
      try
      {
        final DefaultDataSchemaDefinition def = new DefaultDataSchemaDefinition();
        final DataSchemaCompiler compiler = new DataSchemaCompiler(def, new DefaultDataAttributeContext());
        final DataSchema dataSchema = compiler.compile(tableModel);
        final String[] names = dataSchema.getNames();
        assertEquals(4, names.length);
        assertEquals("BC_EMPLOYEES_FIRSTNAME", names[0]);
        assertEquals("BC_EMPLOYEES_LASTNAME", names[1]);
        assertEquals("BC_EMPLOYEES_EMPLOYEENUMBER", names[2]);
        assertEquals("BC_EMPLOYEES_EMAIL", names[3]);

        final DataAttributes attributes = dataSchema.getAttributes(names[2]);
        attributes.toString();
        // assert that formatting-label is not a default mapper
        final ConceptQueryMapper mapper = attributes.getMetaAttributeMapper(MetaAttributeNames.Formatting.NAMESPACE,
                MetaAttributeNames.Formatting.LABEL);
        if (mapper instanceof DefaultConceptQueryMapper)
        {
          fail("Formatting::label should be a LocalizedString instead of a default-mapper");
        }

        final Object value = attributes.getMetaAttribute(MetaAttributeNames.Formatting.NAMESPACE,
                MetaAttributeNames.Formatting.LABEL, null, new DefaultDataAttributeContext());
        if (value instanceof LocalizedString == false)
        {
          fail("Formatting::label should be a LocalizedString");
        }

        final Object label = attributes.getMetaAttribute(MetaAttributeNames.Formatting.NAMESPACE,
                MetaAttributeNames.Formatting.LABEL, String.class, new DefaultDataAttributeContext(Locale.US));
        if (label instanceof String == false)
        {
          fail("Formatting::label should be a String");
        }

        final Object elementAlignment = attributes.getMetaAttribute(MetaAttributeNames.Style.NAMESPACE,
                MetaAttributeNames.Style.HORIZONTAL_ALIGNMENT, null, new DefaultDataAttributeContext(Locale.US));
        if ("right".equals(elementAlignment) == false)
        {
          fail("Style::horizontal-alignment should be a String of value 'right'");
        }

        final DataAttributes attributes2 = dataSchema.getAttributes(names[0]);
        final Object elementAlignment2 = attributes2.getMetaAttribute(MetaAttributeNames.Style.NAMESPACE,
                MetaAttributeNames.Style.HORIZONTAL_ALIGNMENT, null, new DefaultDataAttributeContext(Locale.US));
        if ("left".equals(elementAlignment2) == false)
        {
          fail("Style::horizontal-alignment should be a String of value 'right'");
        }

      }
      finally
      {
        tableModel.close();
      }
    }
    finally
    {

      pmdDataFactory.close();
    }
  }


  public void testMetaData() throws ReportDataFactoryException
  {
    final PmdDataFactory pmdDataFactory = new PmdDataFactory();
    pmdDataFactory.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory.setDomainId("steel-wheels");
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    pmdDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    pmdDataFactory.setQuery("default", PARAMETRIZED_QUERY, null, null);

    final DataFactoryMetaData metaData = DataFactoryRegistry.getInstance().getMetaData(PmdDataFactory.class.getName());
    final Object queryHash = metaData.getQueryHash(pmdDataFactory, "default", new StaticDataRow());
    assertNotNull(queryHash);

    final PmdDataFactory pmdDataFactory2 = new PmdDataFactory();
    pmdDataFactory2.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory2.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory2.setDomainId("steel-wheels");
    pmdDataFactory2.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    pmdDataFactory2.setQuery("default", QUERY, null, null);
    pmdDataFactory2.setQuery("default2", PARAMETRIZED_QUERY, null, null);

    assertNotEquals("Physical Query is not the same", queryHash, metaData.getQueryHash(pmdDataFactory2, "default", new StaticDataRow()));
    assertEquals("Physical Query is the same", queryHash, metaData.getQueryHash(pmdDataFactory2, "default2", new StaticDataRow()));

    final PmdDataFactory pmdDataFactory3 = new PmdDataFactory();
    pmdDataFactory3.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory3.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory3.setDomainId("steel-wheels2");
    pmdDataFactory3.setQuery("default", QUERY, null, null);
    pmdDataFactory3.setQuery("default2", PARAMETRIZED_QUERY, null, null);

    assertNotEquals("Physical Connection is not the same", queryHash, metaData.getQueryHash(pmdDataFactory3, "default", new StaticDataRow()));
    assertNotEquals("Physical Connection is the same", queryHash, metaData.getQueryHash(pmdDataFactory3, "default2", new StaticDataRow()));

    final PmdDataFactory pmdDataFactory4 = new PmdDataFactory();
    pmdDataFactory4.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory4.setXmiFile("devresource/metadata/metadata2.xmi");
    pmdDataFactory4.setDomainId("steel-wheels");
    pmdDataFactory4.setQuery("default", QUERY, null, null);
    pmdDataFactory4.setQuery("default2", PARAMETRIZED_QUERY, null, null);

    assertNotEquals("Physical Connection is not the same", queryHash, metaData.getQueryHash(pmdDataFactory4, "default", new StaticDataRow()));
    assertNotEquals("Physical Connection is the same", queryHash, metaData.getQueryHash(pmdDataFactory4, "default2", new StaticDataRow()));
  }

  public void testMultipleAggregations() throws Exception {
    final PmdDataFactory pmdDataFactory = new PmdDataFactory();
    PmdConnectionProvider provider = new PmdConnectionProvider() {
      private static final long serialVersionUID = 2672461111722673121L;
      public IMetadataDomainRepository getMetadataDomainRepository(final String domainId,
          final ResourceManager resourceManager,
          final ResourceKey contextKey,
          final String xmiFile) throws ReportDataFactoryException {
        try
        {
          final InputStream stream = createStream(resourceManager, contextKey, xmiFile);
          try
          {
            final InMemoryMetadataDomainRepository repo = new InMemoryMetadataDomainRepository();
            final XmiParser parser = new XmiParser();
            final Domain domain = parser.parseXmi(stream);
            // add a couple of agg types to the quantity ordered physical column
            IPhysicalTable table = ((SqlPhysicalModel)domain.getPhysicalModels().get(0)).getPhysicalTables().get(7);
            IPhysicalColumn col = table.getPhysicalColumns().get(3);
            List<AggregationType> list = new ArrayList<AggregationType>();
            list.add(AggregationType.SUM);
            list.add(AggregationType.AVERAGE);
            col.setAggregationList(list);
            domain.setId(domainId);
            repo.storeDomain(domain, true);
            return repo;
          }
          finally
          {
            stream.close();
          }
        }
        catch (Exception e)
        {
          throw new ReportDataFactoryException("The Specified XMI File is invalid: " + xmiFile, e);
        }
      }

    };
    pmdDataFactory.setConnectionProvider(provider);
    pmdDataFactory.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory.setDomainId("steel-wheels");
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    pmdDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    try {
      pmdDataFactory.setQuery("default", MULTIPLE_AGG_QUERY, null, null);
      
      final CloseableTableModel tableModel = (CloseableTableModel) pmdDataFactory.queryData("default", new ParameterDataRow());
      try
      {
        final DefaultDataSchemaDefinition def = new DefaultDataSchemaDefinition();
        final DataSchemaCompiler compiler = new DataSchemaCompiler(def, new DefaultDataAttributeContext());
        final DataSchema dataSchema = compiler.compile(tableModel);
        final String[] names = dataSchema.getNames();
        assertEquals(3, names.length);
        assertEquals("BC_CUSTOMER_W_TER_TERRITORY", names[0]);
        assertEquals("BC_ORDERDETAILS_QUANTITYORDERED", names[1]);
        assertEquals("BC_ORDERDETAILS_QUANTITYORDERED:AVERAGE", names[2]);
        final ByteArrayOutputStream sw = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(sw);

        TableModelInfo.printTableModel(tableModel, out);
        TableModelInfo.printTableModelContents(tableModel, out);
        TableModelInfo.printTableMetaData(tableModel, out);
        TableModelInfo.printTableCellAttributes(tableModel, out);
        
        compareLineByLine("agg-query-results.txt", sw.toString());
      }
      finally
      {
        tableModel.close();
      }
    }
    finally
    {
      pmdDataFactory.close();
    }
  }
  
  public void testParameter() throws ReportDataFactoryException
  {
    final PmdDataFactory pmdDataFactory = new PmdDataFactory();
    pmdDataFactory.setConnectionProvider(new PmdConnectionProvider());
    pmdDataFactory.setXmiFile("devresource/metadata/metadata.xmi");
    pmdDataFactory.setDomainId("steel-wheels");
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final ResourceBundleFactory resourceBundleFactory = new DefaultResourceBundleFactory();
    pmdDataFactory.initialize
        (ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager, null, resourceBundleFactory);
    pmdDataFactory.setQuery("default", PARAMETRIZED_QUERY, null, null);
    pmdDataFactory.setQuery("default2", QUERY, null, null);

    final DataFactoryMetaData metaData = DataFactoryRegistry.getInstance().getMetaData(PmdDataFactory.class.getName());
    final String[] fields = metaData.getReferencedFields(pmdDataFactory, "default", new StaticDataRow());
    assertNotNull(fields);
    assertEquals(2, fields.length);
    assertEquals("oStatus", fields[0]);
    assertEquals(DataFactory.QUERY_LIMIT, fields[1]);

    final String[] fields2 = metaData.getReferencedFields(pmdDataFactory, "default2", new StaticDataRow());
    assertNotNull(fields2);
    assertEquals(1, fields2.length);
    assertEquals(DataFactory.QUERY_LIMIT, fields2[0]);
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