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

package org.pentaho.reporting.libraries.fonts.pfm;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.util.HashMap;

import org.pentaho.reporting.libraries.fonts.registry.AbstractFontFileRegistry;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontFamily;
import org.pentaho.reporting.libraries.fonts.registry.FontFamily;
import org.pentaho.reporting.libraries.fonts.registry.FontMetricsFactory;
import org.pentaho.reporting.libraries.fonts.cache.FontCache;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creation-Date: 21.07.2007, 16:58:06
 *
 * @author Thomas Morgner
 */
public class PfmFontRegistry extends AbstractFontFileRegistry
{
  private static final Log logger = LogFactory.getLog(PfmFontRegistry.class);

  /**
   * The font path filter is used to collect font files and directories during
   * the font path registration.
   */
  private static class FontPathFilter implements FileFilter
  {
    /** Default Constructor. */
    protected FontPathFilter()
    {
    }

    /**
     * Tests whether or not the specified abstract pathname should be included
     * in a pathname list.
     *
     * @param pathname The abstract pathname to be tested
     * @return <code>true</code> if and only if <code>pathname</code> should be
     *         included
     */
    public boolean accept(final File pathname)
    {
      if (pathname.canRead() == false)
      {
        return false;
      }
      if (pathname.isDirectory())
      {
        return true;
      }
      final String name = pathname.getName();
      return StringUtils.endsWithIgnoreCase(name, ".pfm");
    }

  }

  /** The singleton instance of the font path filter. */
  private static final FontPathFilter FONTPATHFILTER = new FontPathFilter();
  /** Fonts stored by name. */

  private HashMap fontFamilies;
  private HashMap alternateFamilyNames;
  private HashMap fullFontNames;
  private boolean itextCompatibleChecks;

  public PfmFontRegistry()
  {
    this.fontFamilies = new HashMap();
    this.alternateFamilyNames = new HashMap();
    this.fullFontNames = new HashMap();
    this.itextCompatibleChecks = true;
  }

  public boolean isItextCompatibleChecks()
  {
    return itextCompatibleChecks;
  }

  public void setItextCompatibleChecks(final boolean itextCompatibleChecks)
  {
    this.itextCompatibleChecks = itextCompatibleChecks;
  }

  protected FileFilter getFileFilter()
  {
    return FONTPATHFILTER;
  }

  public FontMetricsFactory createMetricsFactory()
  {
    // this is a todo - for now we rely on itext
    throw new UnsupportedOperationException();
  }

  public FontCache getSecondLevelCache()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds the fontname by creating the basefont object. This method tries to
   * load the fonts as embeddable fonts, if this fails, it repeats the loading
   * with the embedded-flag set to false.
   *
   * @param font     the font file name.
   * @param encoding the encoding.
   * @throws java.io.IOException       if the base font file could not be read.
   */
  public boolean addFont(final File font, final String encoding) throws IOException
  {
    final String fileName = font.getCanonicalPath();
    final String filePfbName = fileName.substring(0, fileName.length() - 3) + "pfb";
    final File filePfb = new File(filePfbName);
    boolean embedded = true;
    if (filePfb.exists() == false ||
        filePfb.isFile() == false ||
        filePfb.canRead() == false)
    {
      logger.warn("Cannot embedd font: " + filePfb + " is missing for " + font);
      embedded = false;
    }

    final PfmFont pfmFont = new PfmFont(font, embedded);
    if (itextCompatibleChecks)
    {
      if (pfmFont.isItextCompatible() == false)
      {
        logger.warn("Cannot embedd font: pfb-file for " + font + " is not valid (according to iText).");
      }
    }
    registerFont (pfmFont);
    pfmFont.dispose();
    return true;
  }

  private void registerFont(final PfmFont font) throws IOException
  {
    final String windowsName = font.getFamilyName();
    final String postscriptName = font.getFontName();

    final DefaultFontFamily fontFamily = createFamily(windowsName);
    this.alternateFamilyNames.put(windowsName, fontFamily);
    this.alternateFamilyNames.put(postscriptName, fontFamily);

    this.fullFontNames.put(windowsName, fontFamily);
    this.fullFontNames.put(postscriptName, fontFamily);

    final PfmFontRecord record = new PfmFontRecord(font, fontFamily);
    fontFamily.addFontRecord(record);

  }

  private DefaultFontFamily createFamily(final String name)
  {
    final DefaultFontFamily fontFamily = (DefaultFontFamily)
            this.fontFamilies.get(name);
    if (fontFamily != null)
    {
      return fontFamily;
    }

    final DefaultFontFamily createdFamily = new DefaultFontFamily(name);
    this.fontFamilies.put(name, createdFamily);
    return createdFamily;
  }

  public String[] getRegisteredFamilies()
  {
    return (String[]) fontFamilies.keySet().toArray
            (new String[fontFamilies.size()]);
  }

  public String[] getAllRegisteredFamilies()
  {
    return (String[]) alternateFamilyNames.keySet().toArray
            (new String[alternateFamilyNames.size()]);
  }

  public FontFamily getFontFamily(final String name)
  {
    final FontFamily primary = (FontFamily) this.fontFamilies.get(name);
    if (primary != null)
    {
      return primary;
    }
    final FontFamily secondary = (FontFamily)
            this.alternateFamilyNames.get(name);
    if (secondary != null)
    {
      return secondary;
    }
    return (FontFamily) this.fullFontNames.get(name);
  }


  protected void loadFromCache(final String encoding)
  {
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();

    final File location = createStorageLocation();
    if (location == null)
    {
      return;
    }
    final File ttfCache = new File(location, "afm-fontcache.ser");
    try
    {
      final ResourceKey resourceKey = resourceManager.createKey(ttfCache);
      final ResourceData data = resourceManager.load(resourceKey);
      final InputStream stream = data.getResourceAsStream(resourceManager);

      final HashMap cachedSeenFiles;
      final HashMap cachedFontFamilies;
      final HashMap cachedFullFontNames;
      final HashMap cachedAlternateNames;

      try
      {
        final ObjectInputStream oin = new ObjectInputStream(stream);
        final Object[] cache = (Object[]) oin.readObject();
        if (cache.length != 5)
        {
          return;
        }
        if (ObjectUtilities.equal(encoding, cache[0]) == false)
        {
          return;
        }
        cachedSeenFiles = (HashMap) cache[1];
        cachedFontFamilies = (HashMap) cache[2];
        cachedFullFontNames = (HashMap) cache[3];
        cachedAlternateNames = (HashMap) cache[4];
      }
      finally
      {
        stream.close();
      }

      // next; check the font-cache for validity. We cannot cleanly remove
      // entries from the cache once they become invalid, so we have to rebuild
      // the cache from scratch, if it is invalid.
      //
      // This should not matter that much, as font installations do not happen
      // every day.
      if (isCacheValid(cachedSeenFiles))
      {
        getSeenFiles().putAll(cachedSeenFiles);
        this.fontFamilies.putAll(cachedFontFamilies);
        this.fullFontNames.putAll(cachedFullFontNames);
        this.alternateFamilyNames.putAll(cachedAlternateNames);
      }
    }
    catch (Exception e)
    {
      logger.debug("Failed to restore the cache:", e);
    }
  }

  protected void storeToCache(final String encoding)
  {
    final File location = createStorageLocation();
    if (location == null)
    {
      return;
    }
    location.mkdirs();
    if (location.exists() == false || location.isDirectory() == false)
    {
      return;
    }

    final File ttfCache = new File(location, "afm-fontcache.ser");
    try
    {
      final FileOutputStream fout = new FileOutputStream(ttfCache);
      try
      {
        final Object[] map = new Object[5];
        map[0] = encoding;
        map[1] = getSeenFiles();
        map[2] = fontFamilies;
        map[3] = fullFontNames;
        map[4] = alternateFamilyNames;

        final ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(fout));
        objectOut.writeObject(map);
        objectOut.close();
      }
      finally
      {
        try
        {
          fout.close();
        }
        catch (IOException e)
        {
          // ignore ..
          logger.debug("Failed to store cached font data", e);
        }
      }
    }
    catch (IOException e)
    {
      // should not happen
      logger.debug("Failed to store cached font data", e);
    }
  }
}
