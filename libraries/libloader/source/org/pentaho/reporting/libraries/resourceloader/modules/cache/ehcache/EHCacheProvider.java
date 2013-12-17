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

package org.pentaho.reporting.libraries.resourceloader.modules.cache.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceBundleDataCache;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceBundleDataCacheProvider;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceDataCache;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceDataCacheProvider;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceFactoryCache;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceFactoryCacheProvider;

public class EHCacheProvider implements ResourceDataCacheProvider,
        ResourceFactoryCacheProvider, ResourceBundleDataCacheProvider
{
  private static CacheManager cacheManager;
  private static final Log logger = LogFactory.getLog(EHCacheProvider.class);

  public static synchronized CacheManager getCacheManager() throws CacheException
  {
    if (cacheManager == null)
    {
      cacheManager = CacheManager.create();
    }
    return cacheManager;
  }

  public EHCacheProvider()
  {
  }

  public ResourceDataCache createDataCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-data") == false)
        {
          manager.addCache("libloader-data");
        }
        return new EHResourceDataCache(manager.getCache("libloader-data"));
      }
    }
    catch (CacheException e)
    {
      logger.debug("Failed to create EHCache libloader-data cache", e);
      return null;
    }
  }

  public ResourceBundleDataCache createBundleDataCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-bundles") == false)
        {
          manager.addCache("libloader-bundles");
        }
        return new EHResourceBundleDataCache(manager.getCache("libloader-bundles"));
      }
    }
    catch (CacheException e)
    {
      logger.debug("Failed to create EHCache libloader-bundles cache", e);
      return null;
    }
  }

  public ResourceFactoryCache createFactoryCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-factory") == false)
        {
          manager.addCache("libloader-factory");
        }
        return new EHResourceFactoryCache(manager.getCache("libloader-factory"));
      }
    }
    catch (CacheException e)
    {
      logger.debug("Failed to create EHCache libloader-factory cache", e);
      return null;
    }
  }
}
