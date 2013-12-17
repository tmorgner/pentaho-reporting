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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.testsupport;

import org.pentaho.reporting.engine.classic.core.layout.output.AbstractOutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessorFeature;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.fonts.registry.FontStorage;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class DebugOutputProcessorMetaData extends AbstractOutputProcessorMetaData
{
  public DebugOutputProcessorMetaData(final Configuration configuration)
  {
    super(configuration);
    addFeature(OutputProcessorFeature.PAGE_SECTIONS);
    addFeature(OutputProcessorFeature.PAGEBREAKS);
  }

  public DebugOutputProcessorMetaData(final Configuration configuration,
                                      final FontStorage fontStorage)
  {
    super(configuration, fontStorage);
    addFeature(OutputProcessorFeature.PAGE_SECTIONS);
    addFeature(OutputProcessorFeature.PAGEBREAKS);
    removeFeature(OutputProcessorFeature.LEGACY_LINEHEIGHT_CALC);
  }

  /**
   * The export descriptor is a string that describes the output characteristics. For libLayout outputs, it should start
   * with the output class (one of 'pageable', 'flow' or 'stream'), followed by '/liblayout/' and finally followed by
   * the output type (ie. PDF, Print, etc).
   *
   * @return the export descriptor.
   */
  public String getExportDescriptor()
  {
    return "pageable/debug";
  }
}
