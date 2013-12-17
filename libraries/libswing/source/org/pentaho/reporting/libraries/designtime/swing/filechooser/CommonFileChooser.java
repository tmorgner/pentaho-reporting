package org.pentaho.reporting.libraries.designtime.swing.filechooser;

import java.awt.Component;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Todo: Document me!
 * <p/>
 * Date: 19.07.2010
 * Time: 17:12:00
 *
 * @author Thomas Morgner.
 */
public interface CommonFileChooser
{
  public String getFileType();

  public FileFilter[] getFilters();

  public void setFilters(FileFilter[] filter);

  public File getSelectedFile();

  public void setSelectedFile(File file);

  public boolean isAllowMultiSelection();

  public void setAllowMultiSelection(final boolean allowMultiSelection);

  public File[] getSelectedFiles();

  public void setSelectedFiles(File[] file);

  public boolean showDialog(final Component parent, final int mode);
}
