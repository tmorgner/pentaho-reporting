package org.pentaho.reporting.libraries.pensol.vfs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * Todo: Document me!
 * <p/>
 * Date: 10.02.2010
 * Time: 18:16:44
 *
 * @author Thomas Morgner.
 */
public class WebSolutionFileObject extends AbstractFileObject
{
  private WebSolutionFileSystem fs;

  public WebSolutionFileObject(final FileName name,
                               final WebSolutionFileSystem fs)
  {
    super(name, fs);
    this.fs = fs;
  }

  /**
   * Attaches this file object to its file resource.  This method is called
   * before any of the doBlah() or onBlah() methods.  Sub-classes can use
   * this method to perform lazy initialisation.
   * <p/>
   * This implementation does nothing.
   */
  protected void doAttach() throws Exception
  {
    super.doAttach();
  }

  /**
   * Determines the type of this file.  Must not return null.  The return
   * value of this method is cached, so the implementation can be expensive.
   */
  protected FileType doGetType() throws Exception
  {
    if (getName().getDepth() < 2)
    {
      return FileType.FOLDER;
    }
    if (fs.getLocalFileModel().exists(getName()) == false)
    {
      return FileType.IMAGINARY;
    }
    if (fs.getLocalFileModel().isDirectory(getName()))
    {
      return FileType.FOLDER;
    }
    return FileType.FILE;
  }

  /**
   * Lists the children of this file.  Is only called if {@link #doGetType}
   * returns {@link org.apache.commons.vfs.FileType#FOLDER}.  The return value of this method
   * is cached, so the implementation can be expensive.
   */
  protected String[] doListChildren() throws Exception
  {
    return fs.getLocalFileModel().getChilds(getName());
  }

  /**
   * Returns the size of the file content (in bytes).  Is only called if
   * {@link #doGetType} returns {@link org.apache.commons.vfs.FileType#FILE}.
   */
  protected long doGetContentSize() throws Exception
  {
    return 0;
  }

  /**
   * Determines if this file is hidden.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs.FileType#IMAGINARY}.
   * <p/>
   * This implementation always returns false.
   */
  protected boolean doIsHidden() throws Exception
  {
    return fs.getLocalFileModel().isVisible(getName()) == false;
  }

  /**
   * Returns the last modified time of this file.  Is only called if
   * {@link #doGetType} does not return {@link org.apache.commons.vfs.FileType#IMAGINARY}.
   * <p/>
   * This implementation throws an exception.
   */
  protected long doGetLastModifiedTime() throws Exception
  {
    return fs.getLocalFileModel().getLastModifiedDate(getName());
  }

  /**
   * Returns the attributes of this file.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs.FileType#IMAGINARY}.
   * <p/>
   * This implementation always returns an empty map.
   */
  protected Map doGetAttributes() throws Exception
  {
    final String description = fs.getLocalFileModel().getDescription(getName());
    final String localizedName = fs.getLocalFileModel().getLocalizedName(getName());
    final String paramServiceUrl = fs.getLocalFileModel().getParamServiceUrl(getName());
    final String url = fs.getLocalFileModel().getUrl(getName());

    final HashMap map = new HashMap();
    map.put("description", description);
    map.put("localized-name", localizedName);
    map.put("param-service-url", paramServiceUrl);
    map.put("url", url);
    return map;
  }

  /**
   * Sets an attribute of this file.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs.FileType#IMAGINARY}.
   * <p/>
   * This implementation throws an exception.
   */
  protected void doSetAttribute(final String atttrName, final Object value) throws Exception
  {
    if ("description".equals(atttrName))
    {
      if (value instanceof String)
      {
        fs.getLocalFileModel().setDescription(getName(), String.valueOf(value));
      }
      else
      {
        fs.getLocalFileModel().setDescription(getName(), null);
      }
    }
  }

  /**
   * Creates an input stream to read the file content from.  Is only called
   * if {@link #doGetType} returns {@link org.apache.commons.vfs.FileType#FILE}.
   * <p/>
   * <p>It is guaranteed that there are no open output streams for this file
   * when this method is called.
   * <p/>
   * <p>The returned stream does not have to be buffered.
   */
  protected InputStream doGetInputStream() throws Exception
  {
    return new ByteArrayInputStream(new byte[0]);
  }

  /**
   * Creates this file as a folder.  Is only called when:
   * <ul>
   * <li>{@link #doGetType} returns {@link org.apache.commons.vfs.FileType#IMAGINARY}.
   * <li>The parent folder exists and is writeable, or this file is the
   * root of the file system.
   * </ul>
   * <p/>
   * This implementation throws an exception.
   */
  protected void doCreateFolder() throws Exception
  {
    fs.getLocalFileModel().createFolder(getName());
  }

  public String getDescription() throws FileSystemException
  {
    return fs.getLocalFileModel().getDescriptionEntries().get(getName());
  }

  public void setDescription(final String description)
  {
    fs.getLocalFileModel().getDescriptionEntries().put(getName(), description);
  }
}
