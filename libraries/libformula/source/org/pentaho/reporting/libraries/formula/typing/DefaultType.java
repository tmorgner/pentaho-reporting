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

package org.pentaho.reporting.libraries.formula.typing;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Creation-Date: 02.11.2006, 09:37:54
 *
 * @author Thomas Morgner
 */
public abstract class DefaultType implements Type
{
  private HashSet flags;
  private HashMap properties;
  private boolean locked;
  private static final long serialVersionUID = -8206983276033867416L;

  protected DefaultType()
  {
  }

  public boolean isLocked()
  {
    return locked;
  }

  public void lock()
  {
    this.locked = true;
  }

  public void addFlag(final String name)
  {
    if (locked)
    {
      throw new IllegalStateException();
    }
    if (flags == null)
    {
      flags = new HashSet();
    }
    flags.add(name);
  }

  public boolean isFlagSet(final String name)
  {
    if (flags == null)
    {
      return false;
    }
    return flags.contains(name);
  }

  public void setProperty(final String name, final Object value)
  {
    if (locked)
    {
      throw new IllegalStateException();
    }
    if (properties == null)
    {
      properties = new HashMap();
    }
    properties.put(name, value);
  }

  public Object getProperty(final String name)
  {
    // The type system has no properties yet. This is done later, when we
    // deal with real meta-data
    if (properties == null)
    {
      return null;
    }
    return properties.get(name);
  }
}
