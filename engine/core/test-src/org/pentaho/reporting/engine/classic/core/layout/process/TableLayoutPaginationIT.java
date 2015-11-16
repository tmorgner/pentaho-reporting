/*
 * This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2006 - 2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout.process;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableText;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.testsupport.selector.MatchFactory;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * These are several tests related to pagination for table layout. Each used file represent a separate case.<br/>
 * If nothing is commented explicitly, then these settings are used:
 * <ul>
 *   <li>Page setup - standard portrait-oriented {@linkplain
 * org.pentaho.reporting.engine.classic.core.util.PageSize#LETTER LETTER} with margins of 72; the overall free vertical
 * space is (792 - 72 x 2) = 648</li>
 *   <li>Each row's height is 200 and its name is 'detailRow'</li>
 *   <li>Table header's and footer's height are 150; their names are 'header' and 'footer' respectively</li>
 *   <li>Page header's and footer's height are either 50 (small) or 200 (large); their names are 'page-header' and
 *   'page-footer' respectively</li>
 *   <li>Report dataset contains 10 records, representing a number from 1 to 10</li>
 * </ul>
 *
 * @author Andrey Khayrutdinov
 */
public class TableLayoutPaginationIT {

  private static final String PAGE_HEADER_NAME = "page-header";
  private static final String PAGE_HEADER_VALUE = "Page-Header";

  private static final String PAGE_FOOTER_NAME = "page-footer";
  private static final String PAGE_FOOTER_VALUE = "Page-Footer";

  private static final String HEADER_NAME = "header";
  private static final int HEADER_HEIGHT = 150;
  private static final String HEADER_VALUE = "Header";

  private static final String FOOTER_NAME = "footer";
  private static final int FOOTER_HEIGHT = 150;
  private static final String FOOTER_VALUE = "Footer";

  private static final String DETAIL_ROW_NAME = "detailRow";
  private static final int ROW_HEIGHT = 200;


  @BeforeClass
  public static void init() throws Exception {
    ClassicEngineBoot.getInstance().start();
  }

  private static List<LogicalPageBox> loadPages( String file, int expectedPages ) throws Exception {
    ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    Resource resource = resourceManager
      .createDirectly( TableLayoutPaginationIT.class.getResource( "pagination/" + file ), MasterReport.class );
    MasterReport report = (MasterReport) resource.getResource();

    int[] pages = new int[ expectedPages ];
    for ( int i = 0; i < expectedPages; i++ ) {
      pages[ i ] = i;
    }
    return DebugReportRunner.layoutPagesStrict( report, expectedPages, pages );
  }

  /*
   * No header and no footer
   *
   * Expected layout:
   *  1: 1,2,3
   *  2: 4,5,6
   *  3: 7,8,9
   *  4: 10
   */
  @Test
  public void simple() throws Exception {
    String file = "table-simple.prpt";
    List<LogicalPageBox> pages = loadPages( file, 4 );
    PageValidator validator = validator();
    validator.validatePage( pages.get( 0 ), "1", "2", "3" );
    validator.validatePage( pages.get( 1 ), "4", "5", "6" );
    validator.validatePage( pages.get( 2 ), "7", "8", "9" );
    validator.validatePage( pages.get( 3 ), "10" );
  }

  /*
   * Expected layout:
   *  1: th,1,2
   *  ........
   *  5: th,9,10
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeader() throws Exception {
    String file = "table-header.prpt";
    List<LogicalPageBox> pages = loadPages( file, 5 );

    PageValidator validator = validator().checkTableHeader();
    validator.validatePage( pages.get( 0 ), "1", "2" );
    validator.validatePage( pages.get( 1 ), "3", "4" );
    validator.validatePage( pages.get( 2 ), "5", "6" );
    validator.validatePage( pages.get( 3 ), "7", "8" );
    validator.validatePage( pages.get( 4 ), "9", "10" );
  }

  /*
   * Expected layout:
   *  1: 1,2,3
   *  ........
   *  3: 7,8,9
   *  4: 10,tf
   */
  @Test
  public void withFooter() throws Exception {
    String file = "table-footer.prpt";
    List<LogicalPageBox> pages = loadPages( file, 4 );

    PageValidator validator = validator();
    validator.validatePage( pages.get( 0 ), "1", "2", "3" );
    validator.validatePage( pages.get( 1 ), "4", "5", "6" );
    validator.validatePage( pages.get( 2 ), "7", "8", "9" );
    validator.checkTableFooter().validatePage( pages.get( 3 ), "10" );
  }

  /*
   * Expected layout:
   *  1: th,1,2
   *  ........
   *  5: th,9,10
   *  6: th,tf
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndFooter() throws Exception {
    String file = "table-header-footer.prpt";
    List<LogicalPageBox> pages = loadPages( file, 6 );

    PageValidator validator = validator().checkTableHeader();
    validator.validatePage( pages.get( 0 ), "1", "2" );
    validator.validatePage( pages.get( 1 ), "3", "4" );
    validator.validatePage( pages.get( 2 ), "5", "6" );
    validator.validatePage( pages.get( 3 ), "7", "8" );
    validator.validatePage( pages.get( 4 ), "9", "10" );
    validator.checkTableFooter().validatePage( pages.get( 5 ) );
  }

  /*
   * A special test with 100 records. It is needed to reveal possible mistakes
   * which amass slowly and are not discernible with 3-4 pages.
   *
   * Expected layout:
   *  01: th,1,2
   *  ............
   *  50: th,99,100
   *  51: th,tf
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndFooter_Long() throws Exception {
    String file = "table-header-footer-100-records.prpt";
    List<LogicalPageBox> pages = loadPages( file, 51 );

    PageValidator validator = validator().checkTableHeader();
    int recordCounter = 1;
    for ( int i = 0; i < 50; i++ ) {
      String firstRow = Integer.toString( recordCounter++ );
      String secondRow = Integer.toString( recordCounter++ );
      validator.validatePage( pages.get( i ), firstRow, secondRow );
    }
    validator.checkTableFooter().validatePage( pages.get( 50 ) );
  }

  /*
   * A test with a small page header (50). Since it suits 98 gap, the expected layout should be similar to
   * the case with no page header
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndSmallPageHeader() throws Exception {
    String file = "table-page-header-small.prpt";
    List<LogicalPageBox> pages = loadPages( file, 5 );

    PageValidator validator = validator().checkPageHeader( 50 ).checkTableHeader();
    validator.validatePage( pages.get( 0 ), "1", "2" );
    validator.validatePage( pages.get( 1 ), "3", "4" );
    validator.validatePage( pages.get( 2 ), "5", "6" );
    validator.validatePage( pages.get( 3 ), "7", "8" );
    validator.validatePage( pages.get( 4 ), "9", "10" );
  }

  /*
   * A test with a large page header (200). It does not suit 98 gap
   *
   * Expected layout:
   *  01: ph,th,1
   *  ............
   *  10: ph,th,10
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndLargePageHeader() throws Exception {
    String file = "table-page-header-large.prpt";
    List<LogicalPageBox> pages = loadPages( file, 10 );

    PageValidator validator = validator().checkPageHeader( 200 ).checkTableHeader();
    for ( int i = 0; i < 10; i++ ) {
      validator.validatePage( pages.get( i ), Integer.toString( i + 1 ) );
    }
  }

  /*
   * A test with a small page header (50). Since it suits 98 gap, the expected layout should be similar to
   * the case with no page footer
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndSmallPageFooter() throws Exception {
    String file = "table-page-footer-small.prpt";
    List<LogicalPageBox> pages = loadPages( file, 5 );

    PageValidator validator = validator().checkTableHeader().checkPageFooter( 50 );
    validator.validatePage( pages.get( 0 ), "1", "2" );
    validator.validatePage( pages.get( 1 ), "3", "4" );
    validator.validatePage( pages.get( 2 ), "5", "6" );
    validator.validatePage( pages.get( 3 ), "7", "8" );
    validator.validatePage( pages.get( 4 ), "9", "10" );
  }

  /*
   * A test with a large page footer (200). It does not suit 98 gap
   *
   * Expected layout:
   *  01: th,1,pf
   *  ............
   *  10: th,10,pf
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndLargePageFooter() throws Exception {
    String file = "table-page-footer-large.prpt";
    List<LogicalPageBox> pages = loadPages( file, 10 );

    PageValidator validator = validator().checkTableHeader().checkPageFooter( 200 );
    for ( int i = 0; i < 10; i++ ) {
      validator.validatePage( pages.get( i ), Integer.toString( i + 1 ) );
    }
  }

  /*
   * Expected layout:
   *  01: ph,th,1,pf
   *  ............
   *  10: ph,th,10,pf
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndSmallPageHeaderAndFooter() throws Exception {
    String file = "table-page-header-footer-small.prpt";
    List<LogicalPageBox> pages = loadPages( file, 10 );

    PageValidator validator = validator()
      .checkPageHeader( 50 )
      .checkTableHeader()
      .checkPageFooter( 50 );

    for ( int i = 0; i < 10; i++ ) {
      validator.validatePage( pages.get( i ), Integer.toString( i + 1 ) );
    }
  }

  /*
   * Expected layout:
   *  01: ph,th,1,pf
   *  ............
   *  09: ph,th,9,pf
   *  10: ph,th,10,tf,pf
   */
  @Test
  @Ignore( "Should be fixed with next commits within PRD-5547 ticket" )
  public void withHeaderAndFooterAndSmallPageHeaderAndFooter() throws Exception {
    String file = "table-header-footer-page-header-footer-small.prpt";
    List<LogicalPageBox> pages = loadPages( file, 10 );

    PageValidator validator = validator()
      .checkPageHeader( 50 )
      .checkTableHeader()
      .checkPageFooter( 50 );

    for ( int i = 0; i < 9; i++ ) {
      validator.validatePage( pages.get( i ), Integer.toString( i + 1 ) );
    }

    validator.checkTableFooter().validatePage( pages.get( 9 ), Integer.toString( 10 ) );
  }


  private PageValidator validator() {
    return new PageValidator();
  }

  private static class PageValidator {
    private final int pageHeaderHeight;
    private final int tableHeaderHeight;
    private final int tableFooterHeight;
    private final int pageFooterHeight;

    public PageValidator() {
      this( 0, 0, 0, 0 );
    }

    public PageValidator( int pageHeaderHeight, int tableHeaderHeight, int tableFooterHeight, int pageFooterHeight ) {
      this.pageHeaderHeight = pageHeaderHeight;
      this.tableHeaderHeight = tableHeaderHeight;
      this.tableFooterHeight = tableFooterHeight;
      this.pageFooterHeight = pageFooterHeight;
    }

    public PageValidator checkTableHeader() {
      return new PageValidator( pageHeaderHeight, HEADER_HEIGHT, tableFooterHeight, pageFooterHeight );
    }

    public PageValidator checkTableFooter() {
      return new PageValidator( pageHeaderHeight, tableHeaderHeight, FOOTER_HEIGHT, pageFooterHeight );
    }

    public PageValidator checkPageHeader( int headerHeight ) {
      return new PageValidator( headerHeight, tableHeaderHeight, tableFooterHeight, pageFooterHeight );
    }

    public PageValidator checkPageFooter( int footerHeight ) {
      return new PageValidator( pageHeaderHeight, tableHeaderHeight, tableFooterHeight, footerHeight );
    }


    public void validatePage( LogicalPageBox page, String... rows ) {
      int shift = 0;
      if ( pageHeaderHeight > 0 ) {
        assertPageHeader( page, shift );
        shift += pageHeaderHeight;
      }

      if ( tableHeaderHeight > 0 ) {
        assertTableHeader( page, shift );
        shift += tableHeaderHeight;
      }

      if ( !ArrayUtils.isEmpty( rows ) ) {
        assertRowsOnPage( page, rows, shift );
        shift += rows.length * ROW_HEIGHT;
      }

      if ( tableFooterHeight > 0 ) {
        assertTableFooter( page, shift );
      }

      if ( pageFooterHeight > 0 ) {
        assertPageFooter( page );
      }
    }

    private void assertPageHeader( LogicalPageBox page, int shift ) {
      RenderNode p = findParagraph( page, PAGE_HEADER_NAME, PAGE_HEADER_VALUE );
      assertEquals( StrictGeomUtility.toInternalValue( shift ), p.getY() );
    }

    private void assertTableHeader( LogicalPageBox page, int shift ) {
      RenderNode p = findParagraph( page, HEADER_NAME, HEADER_VALUE );
      assertEquals( StrictGeomUtility.toInternalValue( shift ), p.getY() );
    }

    private void assertRowsOnPage( LogicalPageBox page, String[] rows, long shift ) {
      // returns paragraphs and text nodes
      RenderNode[] rowsNodes = MatchFactory.findElementsByName( page, DETAIL_ROW_NAME );
      assertEquals( String.format( "Expected for find all these rows: %s\n, but actually found: %s",
          Arrays.toString( rows ), Arrays.toString( rowsNodes ) ),
        rows.length * 2, rowsNodes.length );

      for ( int i = 0; i < rows.length; i++ ) {
        String expectedRow = rows[ i ];
        RenderableText found = (RenderableText) rowsNodes[ i * 2 + 1 ];
        assertEquals( expectedRow, found.getRawText() );

        RenderNode paragraphNode = rowsNodes[ i * 2 ];
        long expectedY = StrictGeomUtility.toInternalValue( shift + i * ROW_HEIGHT );
        assertEquals( expectedY, paragraphNode.getY() );
      }
    }

    private void assertTableFooter( LogicalPageBox page, int shift ) {
      RenderNode p = findParagraph( page, FOOTER_NAME, FOOTER_VALUE );
      assertEquals( StrictGeomUtility.toInternalValue( shift ), p.getY() );
    }

    private void assertPageFooter( LogicalPageBox page ) {
      RenderNode p = findParagraph( page, PAGE_FOOTER_NAME, PAGE_FOOTER_VALUE );
      assertEquals( StrictGeomUtility.toInternalValue( pageFooterHeight ), p.getHeight() );
      assertEquals( page.getPageEnd(), p.getY2() );
    }


    private RenderNode findParagraph( LogicalPageBox page, String nodeName, String nodeText ) {
      RenderNode[] nodes = MatchFactory.findElementsByName( page, nodeName );
      assertEquals( "Name lookup returned the paragraph and renderable text", 2, nodes.length );
      assertEquals( nodeText, ( (RenderableText) nodes[ 1 ] ).getRawText() );
      return nodes[ 0 ];
    }
  }
}
