package org.pentaho.reporting.libraries.pensol;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.pentaho.reporting.libraries.pensol.vfs.WebSolutionFileProvider;
import org.pentaho.reporting.libraries.pensol.vfs.WebSolutionFileSystem;

/**
 * Todo: Document me!
 * <p/>
 * Date: 11.02.2010
 * Time: 18:55:44
 *
 * @author Thomas Morgner.
 */
public class TestWebSolutionFileProvider extends WebSolutionFileProvider
{
  /**
   * Creates a {@link org.apache.commons.vfs.FileSystem}.  If the returned FileSystem implements
   * {@link org.apache.commons.vfs.provider.VfsComponent}, it will be initialised.
   *
   * @param rootName The name of the root file of the file system to create.
   */
  protected FileSystem doCreateFileSystem(final FileName rootName,
                                          final FileSystemOptions fileSystemOptions) throws FileSystemException
  {
    return new WebSolutionFileSystem(rootName, fileSystemOptions, new TestSolutionFileModel());
  }
}
