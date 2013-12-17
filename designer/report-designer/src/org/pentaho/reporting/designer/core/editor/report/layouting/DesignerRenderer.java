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

package org.pentaho.reporting.designer.core.editor.report.layouting;

import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.DefaultLayoutBuilder;
import org.pentaho.reporting.engine.classic.core.layout.StreamingRenderer;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessorMetaData;

/**
 * Todo: Document me!
 *
 * @author Thomas Morgner
 */
public class DesignerRenderer extends StreamingRenderer
{
  private DesignerOutputProcessor outputProcessor;

  public DesignerRenderer(final DesignerOutputProcessor outputProcessor)
  {
    super(outputProcessor);
    this.outputProcessor = outputProcessor;
  }

  protected DefaultLayoutBuilder createNormalFlowLayoutBuilder(final OutputProcessorMetaData metaData)
  {
    return new DesignerLayoutBuilder(metaData);
  }

  protected void initializeRendererOnStartReport(final ProcessingContext processingContext)
  {
    super.initializeRendererOnStartReport(processingContext);
    outputProcessor.reset();
  }

  public LogicalPageBox getPageBox()
  {
    return super.getPageBox();
  }

  public void createRollbackInformation()
  {
    // intentionally a No-Op
  }

  public void applyRollbackInformation()
  {
    // intentionally a No-Op
  }

  public void rollback()
  {
    // intentionally a No-Op
  }
}
