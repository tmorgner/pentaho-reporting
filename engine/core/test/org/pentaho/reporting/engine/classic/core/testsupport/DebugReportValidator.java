package org.pentaho.reporting.engine.classic.core.testsupport;

import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.output.LogicalPageKey;

/**
 * Todo: Document me!
 * <p/>
 * Date: 06.02.11
 * Time: 18:46
 *
 * @author Thomas Morgner.
 */
public interface DebugReportValidator
{
  public void processPageContent(final LogicalPageKey logicalPageKey, final LogicalPageBox logicalPage);

}
