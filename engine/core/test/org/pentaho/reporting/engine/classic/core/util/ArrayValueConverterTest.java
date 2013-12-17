package org.pentaho.reporting.engine.classic.core.util;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.util.beans.ArrayValueConverter;
import org.pentaho.reporting.engine.classic.core.util.beans.BeanException;
import org.pentaho.reporting.engine.classic.core.util.beans.StringValueConverter;

/**
 * Todo: Document me!
 * <p/>
 * Date: 15.04.2010
 * Time: 15:35:55
 *
 * @author Thomas Morgner.
 */
public class ArrayValueConverterTest extends TestCase
{
  public ArrayValueConverterTest()
  {
  }

  public ArrayValueConverterTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testArrayConversion() throws BeanException
  {
    final String[] array = { " "};
    final ArrayValueConverter c = new ArrayValueConverter(String.class, new StringValueConverter());
    final String s = c.toAttributeValue(array);
    final Object o = c.toPropertyValue(s);
    assertTrue(o instanceof String[]);
    final String[] strings = (String[]) o;
    assertEquals(strings.length, 1);
    assertEquals(strings[0], " ");
  }
}
