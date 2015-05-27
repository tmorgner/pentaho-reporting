/*
 * This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2006 - 2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.libraries.fonts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.reporting.libraries.fonts.itext.ITextFontRegistry;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontContext;
import org.pentaho.reporting.libraries.fonts.registry.FontContext;
import org.pentaho.reporting.libraries.fonts.registry.FontFamily;
import org.pentaho.reporting.libraries.fonts.registry.FontMetrics;
import org.pentaho.reporting.libraries.fonts.registry.FontMetricsFactory;
import org.pentaho.reporting.libraries.fonts.registry.FontRecord;

public class Prd5446Test
{
  @Before
  public void setUp() throws Exception
  {
    LibFontBoot.getInstance().start();
  }

  @Test
  public void testFontMetrics()
  {
    ITextFontRegistry reg = new ITextFontRegistry();
    FontMetricsFactory metricsFactory = reg.createMetricsFactory();
    FontContext fc = new DefaultFontContext(36, false, true, false, "UTF-8");
    FontFamily helvetica = reg.getFontFamily("Helvetica");
    FontRecord fontRecord = helvetica.getFontRecord(false, false);

    FontMetrics metrics = metricsFactory.createMetrics(fontRecord.getIdentifier(), fc);
    Assert.assertEquals(0, metrics.getCharWidth(0xa0));
    Assert.assertEquals(10008, metrics.getCharWidth(' '));
  }

}
