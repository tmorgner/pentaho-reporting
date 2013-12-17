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
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.libraries.designtime.swing.background;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.pentaho.reporting.libraries.base.util.GenericObjectTable;

/**
 * @deprecated This is a rather inefficient way to display tables in the data-preview dialog. 
 */
public class DerivedTableModel extends AbstractTableModel
{
  private String[] colNames;
  private Class[] colTypes;
  private GenericObjectTable backend;

  public DerivedTableModel(final TableModel parent)
  {
    final int colcount = parent.getColumnCount();
    colNames = new String[colcount];
    colTypes = new Class[colcount];

    for (int col = 0; col < colcount; col += 1)
    {
      colNames[col] = parent.getColumnName(col);
      colTypes[col] = parent.getColumnClass(col);
    }

    final int rowcount = parent.getRowCount();
    backend = new GenericObjectTable(Math.max (1, rowcount), Math.max (1, colcount));
    for (int row = 0; row < rowcount; row += 1)
    {
      for (int col = 0; col < colcount; col += 1)
      {
        backend.setObject(row, col, parent.getValueAt(row, col));
      }
    }
  }

  public int getRowCount()
  {
    return backend.getRowCount();
  }

  public int getColumnCount()
  {
    return backend.getColumnCount();
  }

  public Object getValueAt(final int rowIndex, final int columnIndex)
  {

    return backend.getObject(rowIndex, columnIndex);
  }

  public String getColumnName(final int column)
  {
    return colNames[column];
  }

  public Class getColumnClass(final int columnIndex)
  {
    return colTypes[columnIndex];
  }
}
