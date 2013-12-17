package org.pentaho.reporting.engine.classic.core.function.formula;

import java.util.TimeZone;

import org.pentaho.reporting.engine.classic.core.testsupport.FormulaTestBase;

/**
 * Todo: Document me!
 * <p/>
 * Date: 11.05.2010
 * Time: 17:03:56
 *
 * @author Thomas Morgner.
 */
public class ParameterTextFunctionTest extends FormulaTestBase
{
  public ParameterTextFunctionTest()
  {
  }

  public ParameterTextFunctionTest(final String s)
  {
    super(s);
  }

  protected Object[][] createDataTest()
  {
    return new Object[][]{
        {"PARAMETERTEXT(DATE(2009;10;10))", "2009-10-10T00%3A00%3A00.000%2B0000"},
        {"PARAMETERTEXT(100000)", "100000"},
        {"PARAMETERTEXT(1000.001)", "1000.001"},
        {"PARAMETERTEXT(\"AAAA\"; TRUE())", "AAAA"},
        {"PARAMETERTEXT(\"&:;\"; FALSE())", "&:;"},
        {"PARAMETERTEXT(\"&:;\"; TRUE())", "%26%3A%3B"},

    };
  }

  public void testDefault() throws Exception
  {
    runDefaultTest();
  }
}
