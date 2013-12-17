package org.pentaho.reporting.libraries.pensol;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileSystemException;
import org.pentaho.reporting.libraries.pensol.vfs.FileInfo;
import org.pentaho.reporting.libraries.pensol.vfs.SolutionFileModel;

/**
 * Todo: Document me!
 * <p/>
 * Date: 11.02.2010
 * Time: 21:56:06
 *
 * @author Thomas Morgner.
 */
public class TestSolutionFileModel extends SolutionFileModel
{
  public TestSolutionFileModel()
  {
  }

  public void refresh() throws IOException
  {
    final InputStream stream = TestSolutionFileModel.class.getResourceAsStream
        ("/org/pentaho/reporting/libraries/pensol/SolutionRepositoryService.xml");
    try
    {
      setRoot(this.performParse(stream));
    }
    finally
    {
      stream.close();
    }
  }

  public FileInfo performParse(final InputStream postResult) throws IOException
  {
    return super.performParse(postResult);
  }

  protected byte[] getDataInternally(final FileInfo fileInfo) throws FileSystemException
  {
    return new byte[0];
  }

  protected void setDataInternally(final FileInfo fileInfo, final byte[] data) throws FileSystemException
  {

  }
}
