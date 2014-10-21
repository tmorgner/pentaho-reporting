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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.modules.output.pageable.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.ReportDefinition;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.AbstractRenderer;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.PageBreakPositionList;
import org.pentaho.reporting.engine.classic.core.layout.output.ContentProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.LayoutPagebreakHandler;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessor;
import org.pentaho.reporting.engine.classic.core.layout.process.CleanPaginatedBoxesStep;
import org.pentaho.reporting.engine.classic.core.layout.process.FillPhysicalPagesStep;
import org.pentaho.reporting.engine.classic.core.layout.process.OrphanStep;
import org.pentaho.reporting.engine.classic.core.layout.process.PaginationResult;
import org.pentaho.reporting.engine.classic.core.layout.process.PaginationStep;
import org.pentaho.reporting.engine.classic.core.layout.process.WidowStep;

/**
 * Creation-Date: 08.04.2007, 15:08:48
 *
 * @author Thomas Morgner
 */
public class PageableRenderer extends AbstractRenderer
{
  private static final Log logger = LogFactory.getLog(PageableRenderer.class);

  private PaginationStep paginationStep;
  private FillPhysicalPagesStep fillPhysicalPagesStep;
  private CleanPaginatedBoxesStep cleanPaginatedBoxesStep;
  private OrphanStep orphanStep;
  private WidowStep widowStep;
  private int pageCount;
  private long lastPageAge;
  private boolean pageStartPending;
  private boolean widowsEnabled;

  public PageableRenderer(final OutputProcessor outputProcessor)
  {
    super(outputProcessor);
    this.paginationStep = new PaginationStep();
    this.fillPhysicalPagesStep = new FillPhysicalPagesStep();
    this.cleanPaginatedBoxesStep = new CleanPaginatedBoxesStep();
    this.lastPageAge = System.currentTimeMillis();
    this.orphanStep = new OrphanStep();
    this.widowStep = new WidowStep();
  }

  public void startReport(final ReportDefinition report, final ProcessingContext processingContext)
  {
    final long prePageAge = System.currentTimeMillis();
    PageableRenderer.logger.debug("Time to pagination " + (prePageAge - lastPageAge));
    this.lastPageAge = prePageAge;

    widowsEnabled = !ClassicEngineBoot.isEnforceCompatibilityFor(processingContext.getCompatibilityLevel(), 3, 8);
    super.startReport(report, processingContext);
    pageCount = 0;
  }

  protected void debugPrint(final LogicalPageBox pageBox)
  {
  }

  protected boolean preparePagination(final LogicalPageBox pageBox)
  {
    if (widowsEnabled == false)
    {
      return true;
    }
    if (isWidowOrphanDefinitionsEncountered() == false)
    {
      return true;
    }

    if (orphanStep.processOrphanAnnotation(pageBox))
    {
//      logger.info("Orphans unlayoutable.");
      return false;
    }
    if (widowStep.processWidowAnnotation(pageBox))
    {
//      logger.info("Widows unlayoutable.");
      return false;
    }
    return true;
  }

  protected boolean isPageFinished()
  {
    final LogicalPageBox pageBox = getPageBox();
//    final long sizeBeforePagination = pageBox.getHeight();
//    final LogicalPageBox clone = (LogicalPageBox) pageBox.derive(true);
    final PaginationResult pageBreak = paginationStep.performPagebreak(pageBox);
    if (pageBreak.isOverflow() || pageBox.isOpen() == false)
    {
      setLastStateKey(pageBreak.getLastVisibleState());
      return true;
    }
    return false;
  }

  protected boolean performPagination(final LayoutPagebreakHandler layoutPagebreakHandler,
                                      final boolean performOutput)
      throws ContentProcessingException
  {
    // next: perform pagination.
    final LogicalPageBox pageBox = getPageBox();

    //    final long sizeBeforePagination = pageBox.getHeight();
    //    final LogicalPageBox clone = (LogicalPageBox) pageBox.derive(true);
    final PaginationResult pageBreak = paginationStep.performPagebreak(pageBox);
    if (pageBreak.isOverflow() || pageBox.isOpen() == false)
    {
//      final long sizeAfterPagination = pageBox.getHeight();
      setLastStateKey(pageBreak.getLastVisibleState());
      setPagebreaks(getPagebreaks() + 1);
      pageBox.setAllVerticalBreaks(pageBreak.getAllBreaks());

      pageCount += 1;
//      DebugLog.log("1: **** Start Printing Page: " + pageCount);
      debugPrint(pageBox);

      // A new page has been started. Recover the page-grid, then restart
      // everything from scratch. (We have to recompute, as the pages may
      // be different now, due to changed margins or page definitions)
      final OutputProcessor outputProcessor = getOutputProcessor();
      final long nextOffset = pageBreak.getLastPosition();
      final long pageOffset = pageBox.getPageOffset();

      if (performOutput)
      {
        if (outputProcessor.isNeedAlignedPage())
        {
          final LogicalPageBox box = fillPhysicalPagesStep.compute(pageBox, pageOffset, nextOffset);
          outputProcessor.processContent(box);
          // DebugLog.log("Processing contents for Page " + pageCount + " Page-Offset: " + pageOffset + " -> " + nextOffset);
        }
        else
        {
          // DebugLog.log("Processing fast contents for Page " + pageCount + " Page-Offset: " + pageOffset + " -> " + nextOffset);
          outputProcessor.processContent(pageBox);
        }
      }
      else
      {
        // todo: When recomputing the contents, we have to update the page cursor or the whole exercise is next to useless ..
        // DebugLog.log("Recomputing contents for Page " + pageCount + " Page-Offset: " + pageOffset + " -> " + nextOffset);
        outputProcessor.processRecomputedContent(pageBox);
      }

      // Now fire the pagebreak. This goes through all layers and informs all
      // components, that a pagebreak has been encountered and possibly a
      // new page has been set. It does not save the state or perform other
      // expensive operations. However, it updates the 'isPagebreakEncountered'
      // flag, which will be active until the input-feed received a new event.
      //      Log.debug ("PageTime " + (currentPageAge - lastPageAge));
      lastPageAge = System.currentTimeMillis();

      final boolean repeat = pageBox.isOpen() || (pageBox.getHeight() > nextOffset);
      if (repeat)
      {
        pageBox.setPageOffset(nextOffset);

        cleanPaginatedBoxesStep.compute(pageBox);
        pageBox.resetCacheState(true);

        if (pageBreak.isNextPageContainsContent())
        {
          if (layoutPagebreakHandler != null)
          {
            layoutPagebreakHandler.pageStarted();
          }
          return true;
        }
        // No need to try again, we know that the result will not change, as the next page is
        // empty. (We already tested it.)
        pageStartPending = true;
        return false;
      }
      else
      {
        pageBox.setPageOffset(nextOffset);
        outputProcessor.processingFinished();
        return false;
      }
    }
    return false;
  }

  public boolean clearPendingPageStart(final LayoutPagebreakHandler layoutPagebreakHandler)
  {
    if (pageStartPending == false)
    {
      return false;
    }

    if (layoutPagebreakHandler != null)
    {
      layoutPagebreakHandler.pageStarted();
    }
    pageStartPending = false;
    return true;
  }


  public int getPageCount()
  {
    return pageCount;
  }

  public boolean isCurrentPageEmpty()
  {
    // todo: Invent a test that checks whether the page is currently empty.
    final LogicalPageBox logicalPageBox = getPageBox();
    if (logicalPageBox == null)
    {
      throw new IllegalStateException("LogicalPageBox being null? You messed it up again!");
    }

    final PageBreakPositionList breakPositionList = logicalPageBox.getAllVerticalBreaks();
    final long masterBreak = breakPositionList.getLastMasterBreak();
    final boolean nextPageContainsContent = (logicalPageBox.getHeight() > masterBreak);
    return nextPageContainsContent == false;
  }


  public boolean isPageStartPending()
  {
    return pageStartPending;
  }

  public boolean isPendingPageHack()
  {
    return true;
  }
}
