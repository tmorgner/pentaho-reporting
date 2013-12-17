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

import java.io.Serializable;

/**
 * Creation-Date: 02.11.2006, 09:32:21
 *
 * @author Thomas Morgner
 */
public interface Type extends Serializable
{
  public static final String NUMERIC_UNIT = "unit.numeric";
  public static final String NUMERIC_TYPE = "type.numeric";
  public static final String TEXT_TYPE = "type.text";
  public static final String LOGICAL_TYPE = "type.logical";
  public static final String SCALAR_TYPE = "type.scalar";
  public static final String ANY_TYPE = "type.any";
  public static final String ERROR_TYPE = "type.error";
  public static final String DATE_TYPE = "type.date";
  public static final String TIME_TYPE = "type.time";
  public static final String DATETIME_TYPE = "type.datetime";
  public static final String ARRAY_TYPE = "type.array";
  public static final String SEQUENCE_TYPE = "type.sequence";

  public static final String NUMERIC_SEQUENCE_TYPE = "type.numeric.sequence";

  public boolean isFlagSet (String name);
  public Object getProperty (String name);
}
