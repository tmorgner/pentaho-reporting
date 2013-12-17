package org.pentaho.reporting.libraries.xmlns.common;

import junit.framework.TestCase;
import org.pentaho.reporting.libraries.xmlns.LibXmlBoot;

/**
 * Todo: Document me!
 * <p/>
 * Date: 29.03.2010
 * Time: 20:32:07
 *
 * @author Thomas Morgner.
 */
public class AttributeMapTest extends TestCase
{
  public AttributeMapTest()
  {
    super();
  }

  public AttributeMapTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    LibXmlBoot.getInstance().start();
  }

  public void testBasicAdd()
  {
    final AttributeMap map = new AttributeMap();
   
  }
}
