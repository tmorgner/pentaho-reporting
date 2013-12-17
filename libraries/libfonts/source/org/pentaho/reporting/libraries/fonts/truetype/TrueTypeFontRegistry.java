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

package org.pentaho.reporting.libraries.fonts.truetype;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.fonts.FontException;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.fonts.cache.FontCache;
import org.pentaho.reporting.libraries.fonts.registry.AbstractFontFileRegistry;
import org.pentaho.reporting.libraries.fonts.registry.DefaultFontFamily;
import org.pentaho.reporting.libraries.fonts.registry.FontFamily;
import org.pentaho.reporting.libraries.fonts.registry.FontMetricsFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * Creation-Date: 07.11.2005, 19:05:46
 *
 * @author Thomas Morgner
 */
public class TrueTypeFontRegistry extends AbstractFontFileRegistry
{
  private static FontCache secondLevelCache;

  protected static synchronized FontCache internalGetSecondLevelCache()
  {
    if (secondLevelCache == null)
    {
      secondLevelCache = LibFontBoot.getInstance().createDefaultCache();
    }
    return secondLevelCache;
  }

  private static final Log logger = LogFactory.getLog(TrueTypeFontRegistry.class);

  /**
   * The font path filter is used to collect font files and directories during the font path registration.
   */
  private static class FontPathFilter implements FileFilter, Serializable
  {
    /**
     * Default Constructor.
     */
    protected FontPathFilter()
    {
    }

    /**
     * Tests whether or not the specified abstract pathname should be included in a pathname list.
     *
     * @param pathname The abstract pathname to be tested
     * @return <code>true</code> if and only if <code>pathname</code> should be included
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
      if (StringUtils.endsWithIgnoreCase(name, ".ttf"))
      {
        return true;
      }
      if (StringUtils.endsWithIgnoreCase(name, ".ttc"))
      {
        return true;
      }
      if (StringUtils.endsWithIgnoreCase(name, ".otf"))
      {
        return true;
      }
      return false;
    }

  }

  /**
   * The singleton instance of the font path filter.
   */
  private static final FontPathFilter FONTPATHFILTER = new FontPathFilter();

  // canonical filename -> font-file record
  private HashMap fontFamilies;
  private HashMap alternateFamilyNames;
  private HashMap fullFontNames;

  public TrueTypeFontRegistry()
  {
    this.fontFamilies = new HashMap();
    this.alternateFamilyNames = new HashMap();
    this.fullFontNames = new HashMap();
  }

  public FontCache getSecondLevelCache()
  {
    return internalGetSecondLevelCache();
  }

  protected FileFilter getFileFilter()
  {
    return FONTPATHFILTER;
  }

  protected boolean addFont(final File file, final String encoding) throws IOException
  {
    try
    {
      if (StringUtils.endsWithIgnoreCase(file.getName(), ".ttc"))
      {
        final TrueTypeCollection ttc = new TrueTypeCollection(file);
        for (int i = 0; i < ttc.getNumFonts(); i++)
        {
          TrueTypeFont font = null;
          try
          {
            font = ttc.getFont(i);
            registerTrueTypeFont(font);
          }
          finally
          {
            if (font != null)
            {
              font.dispose();
            }
          }
        }
      }
      else
      {
        TrueTypeFont font = null;
        try
        {
          font = new TrueTypeFont(file);
          registerTrueTypeFont(font);
        }
        finally
        {
          if (font != null)
          {
            font.dispose();
          }
        }
      }
      return true;
    }
    catch (Exception e)
    {
      logger.info("Unable to register font file " + file, e);
      // An error must not stop us on our holy mission to find and register
      // all fonts :)
      return false;
    }
  }

  private void registerTrueTypeFont(final TrueTypeFont font)
      throws IOException
  {
    final NameTable table = (NameTable) font.getTable(NameTable.TABLE_ID);
    if (table == null)
    {
      throw new IOException(
          "The NameTable is required for all conforming fonts.");
    }

    final String familyName = table.getPrimaryName(NameTable.NAME_FAMILY);
    final DefaultFontFamily fontFamily = createFamily(familyName);
    try
    {
      final TrueTypeFontRecord record = new TrueTypeFontRecord(font, fontFamily);
      fontFamily.addFontRecord(record);
    }
    catch (FontException e)
    {
      logger.info("The font '" + font.getFilename() + "' is invalid.", e);
      return;
    }

    fontFamilies.put(familyName, fontFamily);
    alternateFamilyNames.put(familyName, fontFamily);

    final String[] allNames = table.getAllNames(NameTable.NAME_FAMILY);
    final int nameCount = allNames.length;
    for (int i = 0; i < nameCount; i++)
    {
      final String name = allNames[i];
      fontFamily.addName(name);
      alternateFamilyNames.put(name, fontFamily);
    }

    final String[] allFullNames = table.getAllNames(NameTable.NAME_FULLNAME);
    final int allNameCount = allFullNames.length;
    for (int i = 0; i < allNameCount; i++)
    {
      final String name = allFullNames[i];
      this.fullFontNames.put(name, fontFamily);
    }

  }

  private DefaultFontFamily createFamily(final String name)
  {
    final DefaultFontFamily fontFamily = (DefaultFontFamily) this.fontFamilies.get(name);
    if (fontFamily != null)
    {
      return fontFamily;
    }

    return new DefaultFontFamily(name);
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

  /**
   * Creates a new font metrics factory. That factory is specific to a certain font registry and is not required to
   * handle font records from foreign font registries.
   * <p/>
   * A font metrics factory should never be used on its own. It should be embedded into and used by a FontStorage
   * implementation.
   *
   * @return a new FontMetricsFactory instance
   */
  public FontMetricsFactory createMetricsFactory()
  {
    return new TrueTypeFontMetricsFactory();
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
    final File ttfCache = new File(location, "ttf-fontcache.ser");
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
        this.getSeenFiles().putAll(cachedSeenFiles);
        this.fontFamilies.putAll(cachedFontFamilies);
        this.fullFontNames.putAll(cachedFullFontNames);
        this.alternateFamilyNames.putAll(cachedAlternateNames);
      }
    }
    catch (final ClassNotFoundException cnfe)
    {
      // ignore the exception.
      logger.debug("Failed to restore the cache: Cache was created by a different version of LibFonts");
    }
    catch (Exception e)
    {
      logger.debug("Non-Fatal: Failed to restore the cache. The cache will be rebuilt.", e);
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

    final File ttfCache = new File(location, "ttf-fontcache.ser");
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
