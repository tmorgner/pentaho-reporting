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
 * Copyright (c) 2000 - 2011 Pentaho Corporation and Contributors...  
 * All rights reserved.
 */

package org.pentaho.reporting.engine.classic.testcases.gold;

import java.io.IOException;
import java.io.OutputStream;

import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.testsupport.gold.GoldTestBase;
import org.pentaho.reporting.libraries.base.util.MemoryByteArrayOutputStream;
import org.pentaho.reporting.libraries.docbundle.BundleUtilities;
import org.pentaho.reporting.libraries.docbundle.DocumentMetaData;
import org.pentaho.reporting.libraries.docbundle.MemoryDocumentBundle;
import org.pentaho.reporting.libraries.docbundle.WriteableDocumentMetaData;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class GoldSaveLoadTest extends GoldTestBase
{
  public GoldSaveLoadTest()
  {
  }

  protected MasterReport postProcess(final MasterReport originalReport) throws Exception
  {
    final DocumentMetaData originalMeta = originalReport.getBundle().getMetaData();
    final MemoryByteArrayOutputStream bout = new MemoryByteArrayOutputStream();
    writeReportToZipStream(originalReport, bout, originalMeta);
    assertTrue(bout.getLength() > 0);

    final ResourceManager mgr = new ResourceManager();
    mgr.registerDefaults();
    final Resource reportRes = mgr.createDirectly(bout.toByteArray(), MasterReport.class);
    return (MasterReport) reportRes.getResource();
  }

  public static void writeReportToZipStream(final MasterReport report,
                                            final OutputStream out,
                                            final DocumentMetaData metaData)
      throws IOException, BundleWriterException, ContentIOException
  {
    if (report == null)
    {
      throw new NullPointerException();
    }
    if (out == null)
    {
      throw new NullPointerException();
    }
    final MemoryDocumentBundle documentBundle = new MemoryDocumentBundle();
    final BundleWriter writer = new BundleWriter();
    writer.writeReport(documentBundle, report);

    // restore the metadata to match the metadata of the original bundle.
    final WriteableDocumentMetaData targetMetaData = (WriteableDocumentMetaData) documentBundle.getMetaData();
    for (final String namespace: metaData.getMetaDataNamespaces())
    {
      for (final String name: metaData.getMetaDataNames(namespace))
      {
        targetMetaData.setBundleAttribute(namespace, name, metaData.getBundleAttribute(namespace, name));
      }
    }

    BundleUtilities.writeAsZip(out, documentBundle);
  }

  @Test
  public void testExecuteReports() throws Exception
  {
    if ("false".equals(ClassicEngineBoot.getInstance().getGlobalConfig().getConfigProperty
        ("org.pentaho.reporting.engine.classic.test.ExecuteLongRunningTest")))
    {
      return;
    }
    runAllGoldReports();
  }
}
