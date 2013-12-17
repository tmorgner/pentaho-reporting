package org.pentaho.reporting.engine.classic.core.layout.process;

import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;

public abstract class AbstractMajorAxisLayoutStep extends IterateVisualProcessStep
{
  // Set the maximum height to an incredibly high value. This is now 2^43 micropoints or more than
  // 3000 kilometers. Please call me directly at any time if you need more space for printing.
  protected static final long MAX_AUTO = StrictGeomUtility.MAX_AUTO;

  private boolean cacheClean;

  protected AbstractMajorAxisLayoutStep(final boolean secondPass)
  {
  }

  public void compute(final LogicalPageBox pageBox)
  {
    this.cacheClean = true;
    startProcessing(pageBox);
  }

  public void continueComputation (final RenderBox pageBox)
  {
    this.cacheClean = true;
    startProcessing(pageBox);
  }

  protected boolean checkCacheValid(final RenderNode node)
  {
    final int cacheState = node.getCacheState();
    if (cacheState == RenderNode.CACHE_DEEP_DIRTY)
    {
      cacheClean = false;
    }

    if (cacheClean && node.isCacheValid())
    {
      return true;
    }
    return false;
  }

}
