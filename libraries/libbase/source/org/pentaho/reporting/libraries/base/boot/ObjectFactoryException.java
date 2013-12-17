/*
 *
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2006 - 2009 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.reporting.libraries.base.boot;

public class ObjectFactoryException extends RuntimeException
{
  private String expectedType;
  private String configredClass;

  public ObjectFactoryException()
  {
  }

  public ObjectFactoryException(final String message)
  {
    super(message);
  }

  public ObjectFactoryException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public ObjectFactoryException(final Throwable cause)
  {
    super(cause);
  }

  public ObjectFactoryException(final String expectedType, final String configuredClass, final Throwable cause)
  {
    super(String.format("Unable to create object of type '%s' with configured class '%s'",
        expectedType, configuredClass), cause);
    this.expectedType = expectedType;
    this.configredClass = configuredClass;
  }

  public ObjectFactoryException(final String expectedType, final String configuredClass)
  {
    super(String.format("Unable to create object of type '%s' with configured class '%s'",
        expectedType, configuredClass));
    this.expectedType = expectedType;
    this.configredClass = configuredClass;
  }
}
