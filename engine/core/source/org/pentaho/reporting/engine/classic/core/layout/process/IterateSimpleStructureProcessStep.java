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
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout.process;

import java.io.Serializable;

import org.pentaho.reporting.engine.classic.core.PerformanceTags;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.libraries.base.performance.EmptyPerformanceLoggingStopWatch;
import org.pentaho.reporting.libraries.base.performance.PerformanceLoggingStopWatch;
import org.pentaho.reporting.libraries.base.performance.PerformanceMonitorContext;

public abstract class IterateSimpleStructureProcessStep implements Serializable
{
  private PerformanceLoggingStopWatch summaryWatch;
  private PerformanceLoggingStopWatch eventWatch;

  protected IterateSimpleStructureProcessStep()
  {
    summaryWatch = EmptyPerformanceLoggingStopWatch.INSTANCE;
    eventWatch = EmptyPerformanceLoggingStopWatch.INSTANCE;
  }

  public void initialize(PerformanceMonitorContext monitorContext)
  {
    summaryWatch.stop();
    eventWatch.stop();

    summaryWatch = monitorContext.createStopWatch
        (PerformanceTags.getSummaryTag(PerformanceTags.REPORT_LAYOUT_PROCESS_SUFFIX, getClass().getSimpleName()));
    eventWatch = monitorContext.createStopWatch
        (PerformanceTags.getDetailTag(PerformanceTags.REPORT_LAYOUT_PROCESS_SUFFIX, getClass().getSimpleName()));
  }

  public void close()
  {
    summaryWatch.close();
    eventWatch.close();
  }

  protected final void startProcessing(final RenderNode node)
  {
    final int nodeType = node.getNodeType();
    if (nodeType == LayoutNodeTypes.TYPE_BOX_LOGICALPAGE)
    {
      final LogicalPageBox box = (LogicalPageBox) node;
      if (startBox(box))
      {
        startProcessing(box.getWatermarkArea());
        startProcessing(box.getHeaderArea());
        processBoxChilds(box);
        startProcessing(box.getFooterArea());
        startProcessing(box.getRepeatFooterArea());
      }
      finishBox(box);
    }
    else if ((nodeType & LayoutNodeTypes.MASK_BOX) == LayoutNodeTypes.MASK_BOX)
    {
      final RenderBox box = (RenderBox) node;
      if (startBox(box))
      {
        processBoxChilds(box);
      }
      finishBox(box);
    }
    else
    {
      processOtherNode(node);
    }
  }

  protected void processBoxChilds(final RenderBox box)
  {
    RenderNode node = box.getFirstChild();
    while (node != null)
    {
      startProcessing(node);
      node = node.getNext();
    }
  }

  protected void processOtherNode(final RenderNode node)
  {
  }

  protected void finishBox(final RenderBox box)
  {
  }

  protected boolean startBox(final RenderBox box)
  {
    return true;
  }

}
