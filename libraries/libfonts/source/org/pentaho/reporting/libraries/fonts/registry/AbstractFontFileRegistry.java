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
 * Copyright (c) 2006 - 2009 Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.fonts.registry;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.fonts.encoding.EncodingRegistry;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.base.config.Configuration;

/**
 * Creation-Date: 21.07.2007, 17:01:15
 *
 * @author Thomas Morgner
 */
public abstract class AbstractFontFileRegistry implements FontRegistry
{
  private static final Log logger = LogFactory.getLog(AbstractFontFileRegistry.class);

  private HashMap seenFiles;

  protected AbstractFontFileRegistry()
  {
    seenFiles = new HashMap();
  }

  protected HashMap getSeenFiles()
  {
    return seenFiles;
  }

  protected abstract FileFilter getFileFilter();

  public void initialize()
  {
    registerDefaultFontPath();
    final Configuration configuration = LibFontBoot.getInstance().getGlobalConfig();
    final Iterator extraDirIt =
            configuration.findPropertyKeys("org.pentaho.reporting.libraries.fonts.extra-font-dirs.");
    while (extraDirIt.hasNext())
    {
      final String extraDirKey = (String) extraDirIt.next();
      final String extraDir = configuration.getConfigProperty(extraDirKey);
      final File extraDirFile = new File(extraDir);
      try
      {
        if (extraDirFile.isDirectory())
        {
          registerFontPath(extraDirFile, getDefaultEncoding());
        }
      }
      catch (Exception e)
      {
        logger.warn("Extra font path " + extraDir + " could not be fully registered.", e);
      }
    }
  }

  protected String getDefaultEncoding()
  {
    return LibFontBoot.getInstance().getGlobalConfig().getConfigProperty
        ("org.pentaho.reporting.libraries.fonts.itext.FontEncoding", EncodingRegistry.getPlatformDefaultEncoding());
  }

  /**
   * Register os-specific font paths to the PDF-FontFactory. For unix-like operating systems, X11 is searched in
   * /usr/X11R6 and the default truetype fontpath is added. For windows the system font path is added (%windir%/fonts)
   */
  public void registerDefaultFontPath()
  {
    final String encoding = getDefaultEncoding();
    loadFromCache(encoding);

    final String osname = safeSystemGetProperty("os.name", "<protected by system security>");
    final String jrepath = safeSystemGetProperty("java.home", ".");
    final String fs = safeSystemGetProperty("file.separator", File.separator);

    logger.debug("Running on operating system: " + osname);
    logger.debug("Character encoding used as default: " + encoding);

    if (safeSystemGetProperty("mrj.version", null) != null)
    {
      final String userhome = safeSystemGetProperty("user.home", ".");
      logger.debug("Detected MacOS (Property 'mrj.version' is present.");
      registerFontPath(new File(userhome + "/Library/Fonts"), encoding);
      registerFontPath(new File("/Library/Fonts"), encoding);
      registerFontPath(new File("/Network/Library/Fonts"), encoding);
      registerFontPath(new File("/System/Library/Fonts"), encoding);
    }
    else if (StringUtils.startsWithIgnoreCase(osname, "windows"))
    {
      registerWindowsFontPath(encoding);
    }
    else
    {
      logger.debug("Assuming unix like file structures");
      // Assume X11 is installed in the default location.
      registerFontPath(new File("/usr/X11R6/lib/X11/fonts"), encoding);
      registerFontPath(new File("/usr/share/fonts"), encoding);
    }
    registerFontPath(new File(jrepath, "lib" + fs + "fonts"), encoding);

    storeToCache(encoding);
    logger.info("Completed font registration.");
  }

  protected void storeToCache(final String encoding)
  {

  }

  protected void loadFromCache(String encoding)
  {
  }


  /**
   * Registers the default windows font path. Once a font was found in the old seenFiles map and confirmed, that this
   * font still exists, it gets copied into the confirmedFiles map.
   *
   * @param encoding the default font encoding.
   */
  private void registerWindowsFontPath(final String encoding)
  {
    logger.debug("Found 'Windows' in the OS name, assuming DOS/Win32 structures");
    // Assume windows
    // If you are not using windows, ignore this. This just checks if a windows system
    // directory exist and includes a font dir.

    String fontPath = null;
    final String windirs = safeSystemGetProperty("java.library.path", null);
    final String fs = safeSystemGetProperty("file.separator", File.separator);

    if (windirs != null)
    {
      final StringTokenizer strtok = new StringTokenizer
          (windirs, safeSystemGetProperty("path.separator", File.pathSeparator));
      while (strtok.hasMoreTokens())
      {
        final String token = strtok.nextToken();

        if (StringUtils.endsWithIgnoreCase(token, "System32"))
        {
          // found windows folder ;-)
          final int lastBackslash = token.lastIndexOf(fs);
          if (lastBackslash != -1)
          {
            fontPath = token.substring(0, lastBackslash) + fs + "Fonts";
            break;
          }
          // try with forward slashs. Some systems may use the unix-semantics instead.
          // (Windows accepts both characters as path-separators for historical reasons)
          final int lastSlash = token.lastIndexOf('/');
          if (lastSlash != -1)
          {
            fontPath = token.substring(0, lastSlash) + lastSlash + "Fonts";
            break;
          }
        }
      }
    }
    logger.debug("Fonts located in \"" + fontPath + '\"');
    if (fontPath != null)
    {
      final File file = new File(fontPath);
      registerFontPath(file, encoding);
    }
  }

  /**
   * Register all fonts (*.ttf files) in the given path.
   *
   * @param file     the directory that contains the font files.
   * @param encoding the encoding for the given font.
   */
  public void registerFontPath(final File file, final String encoding)
  {
    if (file.exists() && file.isDirectory() && file.canRead())
    {
      final File[] files = file.listFiles(getFileFilter());
      final int fileCount = files.length;
      for (int i = 0; i < fileCount; i++)
      {
        final File currentFile = files[i];
        if (currentFile.isDirectory())
        {
          registerFontPath(currentFile, encoding);
        }
        else
        {
          if (isCached(currentFile) == false)
          {
            registerFontFile(currentFile, encoding);
          }
        }
      }
    }
  }


  protected boolean isCached(final File file)
  {
    try
    {
      final FontFileRecord stored = (FontFileRecord) seenFiles.get(file.getCanonicalPath());
      if (stored == null)
      {
        return false;
      }

      final FontFileRecord rec = new FontFileRecord(file);
      if (stored.equals(rec) == false)
      {
        seenFiles.remove(rec);
        return false;
      }
      return true;
    }
    catch (IOException e)
    {
      return false;
    }
  }

  /**
   * Register the font (must end this *.ttf) to the FontFactory.
   *
   * @param filename the filename.
   * @param encoding the encoding.
   */
  public void registerFontFile(final String filename,
                                            final String encoding)
  {
    final File file = new File(filename);
    registerFontFile(file, encoding);
  }

  public synchronized void registerFontFile(final File file, final String encoding)
  {
    if (getFileFilter().accept(file) && file.exists() && file.isFile() && file.canRead())
    {
      try
      {
        if (addFont(file, encoding))
        {
          final FontFileRecord value = new FontFileRecord(file);
          seenFiles.put(file.getCanonicalPath(), value);
        }
      }
      catch (Exception e)
      {
        logger.warn("Font " + file + " is invalid. Message:" + e.getMessage(), e);
      }
    }
  }


  /**
   * Adds the fontname by creating the basefont object. This method tries to load the fonts as embeddable fonts, if this
   * fails, it repeats the loading with the embedded-flag set to false.
   *
   * @param font     the font file name.
   * @param encoding the encoding.
   * @throws java.io.IOException if the base font file could not be read.
   */
  protected abstract boolean addFont(final File font, final String encoding)
      throws IOException;


  protected String safeSystemGetProperty(final String name,
                                         final String defaultValue)
  {
    try
    {
      return System.getProperty(name, defaultValue);
    }
    catch (SecurityException se)
    {
      return defaultValue;
    }
  }


  protected boolean isCacheValid(final HashMap cachedSeenFiles)
  {
    final Iterator iterator = cachedSeenFiles.entrySet().iterator();
    while (iterator.hasNext())
    {
      final Map.Entry entry = (Map.Entry) iterator.next();
      final String fullFileName = (String) entry.getKey();
      final FontFileRecord fontFileRecord = (FontFileRecord) entry.getValue();
      final File fontFile = new File(fullFileName);
      if (fontFile.isFile() == false || fontFile.exists() == false)
      {
        return false;
      }
      if (fontFile.length() != fontFileRecord.getFileSize())
      {
        return false;
      }
      if (fontFile.lastModified() != fontFileRecord.getLastAccessTime())
      {
        return false;
      }
    }
    return true;
  }

  protected File createStorageLocation()
  {
    final String homeDirectory = safeSystemGetProperty("user.home", null);
    if (homeDirectory == null)
    {
      return null;
    }
    final File homeFile = new File(homeDirectory);
    if (homeFile.isDirectory() == false)
    {
      return null;
    }
    return new File(homeFile, ".pentaho/caches/libfonts");
  }
}

