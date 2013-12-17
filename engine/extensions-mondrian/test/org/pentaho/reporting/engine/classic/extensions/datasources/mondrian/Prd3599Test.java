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
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.datasources.mondrian;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class Prd3599Test extends TestCase
{
  public Prd3599Test()
  {
  }

  public Prd3599Test(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testEmptySecurityPropertiesAreNull()
      throws IOException, ContentIOException, BundleWriterException, ResourceException
  {
    final BandedMDXDataFactory mondrianDataFactory = new BandedMDXDataFactory();
    final DriverDataSourceProvider provider = new DriverDataSourceProvider();
    provider.setDriver("org.hsqldb.jdbcDriver");
    provider.setUrl("jdbc:hsqldb:./sql/sampledata");
    mondrianDataFactory.setCubeFileProvider(new DefaultCubeFileProvider
        ("test/org/pentaho/reporting/engine/classic/extensions/datasources/mondrian/steelwheels.mondrian.xml"));
    mondrianDataFactory.setDataSourceProvider(provider);
    mondrianDataFactory.setJdbcPassword("");
    mondrianDataFactory.setJdbcPasswordField("");
    mondrianDataFactory.setJdbcUser("");
    mondrianDataFactory.setJdbcUserField("");
    mondrianDataFactory.setRole("");
    mondrianDataFactory.setRoleField("");

    final MasterReport report = new MasterReport();
    report.setDataFactory(mondrianDataFactory);

    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BundleWriter.writeReportToZipStream(report, byteArrayOutputStream);

    final byte[] data = byteArrayOutputStream.toByteArray();
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final Resource directly = resourceManager.createDirectly(data, MasterReport.class);
    final MasterReport parsedReport = (MasterReport) directly.getResource();
    assertNotNull(parsedReport);

    final DataFactory dataFactory = parsedReport.getDataFactory();
    assertTrue(dataFactory instanceof BandedMDXDataFactory);
    final BandedMDXDataFactory bdf = (BandedMDXDataFactory) dataFactory;
    assertNull(bdf.getJdbcPassword());
    assertNull(bdf.getJdbcPasswordField());
    assertNull(bdf.getJdbcUser());
    assertNull(bdf.getJdbcUserField());
    assertNull(bdf.getRole());
    assertNull(bdf.getRoleField());
  }
}
