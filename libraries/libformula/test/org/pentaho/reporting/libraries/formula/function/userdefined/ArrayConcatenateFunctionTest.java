package org.pentaho.reporting.libraries.formula.function.userdefined;

import java.math.BigDecimal;

import org.pentaho.reporting.libraries.formula.FormulaTestBase;

/**
 * Todo: Document me!
 * <p/>
 * Date: 27.08.2010
 * Time: 13:24:19
 *
 * @author Thomas Morgner.
 */
public class ArrayConcatenateFunctionTest extends FormulaTestBase
{
  public void testDefault() throws Exception
  {
    runDefaultTest();
  }

  public Object[][] createDataTest()
  {
    return new Object[][]
        {
            {"NORMALIZEARRAY(ARRAYCONCATENATE([.B3:.B8]; {1}))", new Object[]
                { "7", new BigDecimal(2), new BigDecimal(3), Boolean.TRUE, "Hello", new BigDecimal(1)}},
            {"NORMALIZEARRAY(ARRAYCONCATENATE([.B3:.B8]; 1))", new Object[]
                { "7", new BigDecimal(2), new BigDecimal(3), Boolean.TRUE, "Hello", new BigDecimal(1)}},
        };
  }

}
