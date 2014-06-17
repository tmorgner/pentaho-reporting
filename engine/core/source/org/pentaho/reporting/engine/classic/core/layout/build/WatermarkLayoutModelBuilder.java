/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.reporting.engine.classic.core.layout.build;

import java.util.ArrayList;

import org.pentaho.reporting.engine.classic.core.ReportAttributeMap;
import org.pentaho.reporting.engine.classic.core.ReportDefinition;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.SubReport;
import org.pentaho.reporting.engine.classic.core.filter.types.AutoLayoutBoxType;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.InlineSubreportMarker;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderLength;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableReplacedContentBox;
import org.pentaho.reporting.engine.classic.core.layout.model.SectionRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.context.BoxDefinition;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessorFeature;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.layout.style.SectionKeepTogetherStyleSheet;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.StyleSheet;
import org.pentaho.reporting.engine.classic.core.util.InstanceID;

public class WatermarkLayoutModelBuilder extends LayoutModelBuilderWrapper
{
  private static class WatermarkRenderNodeFactory implements RenderNodeFactory
  {
    private RenderNodeFactory backend;
    private BoxDefinition watermarkBoxDefinition;

    private WatermarkRenderNodeFactory(final RenderNodeFactory backend)
    {
      final BoxDefinition boxDefinition = new BoxDefinition();
      boxDefinition.setPreferredHeight(RenderLength.createPercentage(100));
      this.watermarkBoxDefinition = boxDefinition.lock();
      this.backend = backend;
    }

    public LogicalPageBox createPage(final ReportDefinition report, final StyleSheet style)
    {
      return backend.createPage(report, style);
    }

    public RenderBox produceSubReportPlaceholder(final ReportElement element,
                                                 final StyleSheet style,
                                                 final ReportStateKey stateKey,
                                                 final boolean inlineContext)
    {
      return backend.produceSubReportPlaceholder(element, style, stateKey, inlineContext);
    }

    public RenderBox produceSectionBox(final String layoutType, final ReportStateKey stateKey)
    {
      final StyleSheet styleSheet = new SectionKeepTogetherStyleSheet(false);
      return new SectionRenderBox(styleSheet, new InstanceID(), watermarkBoxDefinition, AutoLayoutBoxType.INSTANCE,
          ReportAttributeMap.emptyMap(), stateKey);
    }

    public RenderBox produceRenderBox(final ReportElement band,
                                      final StyleSheet style,
                                      final String layoutType,
                                      final ReportStateKey stateKey)
    {
      return backend.produceRenderBox(band, style, layoutType, stateKey);
    }

    @Deprecated
    public RenderBox createAutoParagraph(final ReportStateKey stateKey)
    {
      return backend.createAutoParagraph(stateKey);
    }

    public RenderBox createAutoParagraph(final ReportElement band,
                                         final StyleSheet bandStyle,
                                         final ReportStateKey stateKey)
    {
      return backend.createAutoParagraph(band, bandStyle, stateKey);
    }

    public StyleSheet createAutoGeneratedSectionStyleSheet(final StyleSheet style)
    {
      return backend.createAutoGeneratedSectionStyleSheet(style);
    }

    public BoxDefinition getBoxDefinition(final StyleSheet style)
    {
      return backend.getBoxDefinition(style);
    }

    public RenderBox createPageBreakIndicatorBox(final ReportStateKey stateKey,
                                                 final long range)
    {
      return backend.createPageBreakIndicatorBox(stateKey, range);
    }

    public RenderableReplacedContentBox createReplacedContent(final ReportElement element,
                                                              final StyleSheet style,
                                                              final Object value,
                                                              final Object rawValue,
                                                              final ReportStateKey stateKey)
    {
      return backend.createReplacedContent(element, style, value, rawValue, stateKey);
    }

    public StyleSheet createStyle(final StyleSheet style)
    {
      return backend.createStyle(style);
    }

    public void close()
    {
      backend.close();
    }

    public void initialize(final OutputProcessorMetaData outputProcessorMetaData)
    {
      backend.initialize(outputProcessorMetaData);
    }
  }

  private ArrayList<RenderNode> slots;
  private int slotCounter;
  private RenderBox parentBox;
  private int inBoxDepth;
  private OutputProcessorMetaData metaData;

  public WatermarkLayoutModelBuilder(final LayoutModelBuilder backend)
  {
    super(backend);
    backend.setLimitedSubReports(true);
    backend.setCollapseProgressMarker(false);
    this.slots = new ArrayList<RenderNode>();
  }

  public void initialize(final ProcessingContext metaData,
                         final RenderBox parentBox,
                         final RenderNodeFactory renderNodeFactory)
  {
    this.parentBox = parentBox;
    getParent().initialize(metaData, parentBox, new WatermarkRenderNodeFactory(renderNodeFactory));
    this.metaData = metaData.getOutputProcessorMetaData();
  }

  public void setLimitedSubReports(final boolean limitedSubReports)
  {
  }

  public InstanceID startBox(final ReportElement element)
  {
    InstanceID instanceID = getParent().startBox(element);
    inBoxDepth += 1;
    return instanceID;
  }

  public void startSection(final ReportElement element, final int sectionSize)
  {
    throw new UnsupportedOperationException("Global sections cannot be started for page headers");
  }

  public InlineSubreportMarker processSubReport(final SubReport element)
  {
    throw new UnsupportedOperationException("SubReports cannot be started for page headers");
  }

  public boolean finishBox()
  {
    inBoxDepth -= 1;
    if (inBoxDepth == 0)
    {
      slotCounter += 1;
    }
    return super.finishBox();
  }

  public void endSubFlow()
  {
    throw new UnsupportedOperationException("SubReport sections cannot be started for page headers");
  }

  public void addProgressMarkerBox()
  {
    super.addProgressMarkerBox();
    slotCounter += 1;
  }

  public void addManualPageBreakBox(final long range)
  {
    throw new UnsupportedOperationException("PageBreak sections cannot be started for page headers");
  }

  public LayoutModelBuilder deriveForStorage(final RenderBox clonedContent)
  {
    final WatermarkLayoutModelBuilder clone = (WatermarkLayoutModelBuilder) super.deriveForStorage(clonedContent);
    clone.slots = (ArrayList<RenderNode>) slots.clone();
    clone.slots.clear();
    clone.parentBox = clonedContent;
    return clone;
  }

  public LayoutModelBuilder deriveForPageBreak()
  {
    final WatermarkLayoutModelBuilder clone = (WatermarkLayoutModelBuilder) super.deriveForPageBreak();
    clone.slots = (ArrayList<RenderNode>) slots.clone();
    clone.slots.clear();
    return clone;
  }

  public void startSection()
  {
    slots.clear();
    slotCounter = 0;
    // check what slots are filled and update the list

    final RenderNode lastChild = parentBox.getLastChild();
    if (lastChild instanceof RenderBox)
    {
      final RenderBox slottedContent = (RenderBox) lastChild;
      RenderNode box = slottedContent.getLastChild();
      while (box != null)
      {
        if (box.getStyleSheet().getBooleanStyleProperty(BandStyleKeys.STICKY))
        {
          slots.add(0, box);
        }
        box = box.getPrev();
      }
    }

    parentBox.clear();
    super.startSection();
  }

  public void endSection()
  {
    if (metaData.isFeatureSupported(OutputProcessorFeature.STRICT_COMPATIBILITY))
    {
      super.legacyFlagNotEmpty();
    }
    super.endSection();

    for (int i = slotCounter; i < slots.size(); i++)
    {
      final RenderNode node = slots.get(i);
      parentBox.addGeneratedChild(node.derive(true));
    }
  }

  public InstanceID createSubflowPlaceholder(final ReportElement element)
  {
    throw new UnsupportedOperationException("SubReport sections cannot be started for page headers");
  }

  public void startSubFlow(final InstanceID insertationPoint)
  {
    throw new UnsupportedOperationException("SubReport sections cannot be started for page headers");
  }

  public void startSubFlow(final ReportElement element)
  {
    throw new UnsupportedOperationException("SubReport sections cannot be started for page headers");
  }

  public void suspendSubFlow()
  {
    throw new UnsupportedOperationException("SubReport sections cannot be started for page headers");
  }
}
