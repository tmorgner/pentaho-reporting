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

package org.pentaho.reporting.libraries.resourceloader.cache;

import java.util.WeakHashMap;

import org.pentaho.reporting.libraries.resourceloader.ResourceBundleData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Disables caching. It simply returns null on every request and ignores
 * the put requests. You certainly want to use one of the other cache
 * providers in real world applications.
 *
 * This cache uses a weak-hashmap to keep the loaded resources alive until
 * the resource-key is no longer used. This allows the cache to hang on to
 * the bundle object for as long as the parsing happens.
 *
 * @author Thomas Morgner
 */
public class NullResourceBundleDataCache implements ResourceBundleDataCache
{
  private WeakHashMap<ResourceKey, ResourceBundleDataCacheEntry> keys;

  public NullResourceBundleDataCache()
  {
    keys = new WeakHashMap<ResourceKey, ResourceBundleDataCacheEntry>();
  }

  public synchronized ResourceBundleData put(final ResourceManager caller, final ResourceBundleData data)  throws ResourceLoadingException
  {
    final ResourceBundleData retval = CachingResourceBundleData.createCached(data);
    keys.put(retval.getBundleKey(), new DefaultResourceBundleDataCacheEntry(retval, caller));
    return retval;
  }

  public synchronized ResourceBundleDataCacheEntry get(final ResourceKey key)
  {
    return keys.get(key);
  }

  public synchronized void remove(final ResourceBundleData data)
  {
    keys.remove(data.getBundleKey());
  }

  public synchronized void clear()
  {
    keys.clear();
  }

  public synchronized void shutdown()
  {
    keys.clear();
  }
}