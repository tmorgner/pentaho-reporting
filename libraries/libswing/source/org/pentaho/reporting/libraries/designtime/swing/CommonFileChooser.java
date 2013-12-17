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

package org.pentaho.reporting.libraries.designtime.swing;

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @deprecated Since PRD-3.7: Do not use anymore, use the FileChooserService instead.
 */
public class CommonFileChooser
{
  private static final String LOCATION_PREFS = "org/pentaho/reporting/libraries/designtime/swing/CommonFileChooser/locations";
  private static final String STATIC_PREFS = "org/pentaho/reporting/libraries/designtime/swing/CommonFileChooser/staticprefs";

  private Preferences resourceLocationMappings;
  private JFileChooser fileChooser;
  private static CommonFileChooser commonFileChooser;
  private Preferences staticLocationMappings;

  private CommonFileChooser()
  {
    resourceLocationMappings = Preferences.userRoot().node(LOCATION_PREFS);
    staticLocationMappings = Preferences.userRoot().node(STATIC_PREFS);

    fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public int showDialog(final Component parent,
                        final FileFilter[] filters,
                        final int mode,
                        final String key)
  {
    fileChooser.setSelectedFile(getCurrentLocation(key));
    fileChooser.updateUI();
    fileChooser.resetChoosableFileFilters();
    for (int i = 0; i < filters.length; i++)
    {
      fileChooser.addChoosableFileFilter(filters[i]);
    }
    final int retval;
    if (mode == JFileChooser.OPEN_DIALOG)
    {
      retval = fileChooser.showOpenDialog(parent);
    }
    else
    {
      retval = fileChooser.showSaveDialog(parent);
    }
    
    if (retval != JFileChooser.CANCEL_OPTION)
    {
      resourceLocationMappings.put(key, fileChooser.getSelectedFile().getAbsolutePath());
    }
    return retval;
  }

  private File getCurrentLocation(final String aKey)
  {
    final String theLocationValue = resourceLocationMappings.get(aKey, null);
    if (theLocationValue != null)
    {
      return new File(theLocationValue);
    }
    final String staticLocationValue = staticLocationMappings.get(aKey, null);
    if (staticLocationValue != null)
    {
      return new File(staticLocationValue);
    }
    return null;
  }

  public File getStaticLocation(final String aKey)
  {
    final String staticLocationValue = staticLocationMappings.get(aKey, null);
    if (staticLocationMappings != null)
    {
      return new File(staticLocationValue);
    }
    return null;
  }

  public void setStaticLocation(final String aKey, final File file)
  {
    staticLocationMappings.put(aKey, file.getAbsolutePath());
  }

  public File getSelectedFile()
  {
    return fileChooser.getSelectedFile();
  }

  public void setSelectedFile(final File file)
  {
    fileChooser.setSelectedFile(file);
  }

  public static synchronized CommonFileChooser getInstance()
  {
    if (commonFileChooser == null)
    {
      commonFileChooser = new CommonFileChooser();
    }
    return commonFileChooser;
  }
}
