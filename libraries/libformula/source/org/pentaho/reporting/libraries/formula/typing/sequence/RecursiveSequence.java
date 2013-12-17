package org.pentaho.reporting.libraries.formula.typing.sequence;

import java.util.Collection;
import java.util.LinkedList;

import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.lvalues.LValue;
import org.pentaho.reporting.libraries.formula.typing.ArrayCallback;
import org.pentaho.reporting.libraries.formula.typing.Sequence;

/**
 * Todo: Document me!
 * <p/>
 * Date: 29.09.2010
 * Time: 16:47:11
 *
 * @author Thomas Morgner.
 */
public class RecursiveSequence implements Sequence
{
  private LinkedList<Object> stack;
  private FormulaContext context;

  public RecursiveSequence(final Object object,
                           final FormulaContext context)
  {
    this.context = context;
    this.stack = new LinkedList<Object>();
    this.stack.add(0, object);
  }

  public boolean hasNext() throws EvaluationException
  {
    if (stack.isEmpty())
    {
      return false;
    }
    final Object o = stack.remove();
    if (o instanceof Object[])
    {
      final Object[] array = (Object[]) o;
      final RawArraySequence s = new RawArraySequence(array);
      stack.add(0, s);
      return hasNext();
    }
    else if (o instanceof Collection)
    {
      final Collection array = (Collection) o;
      final RawArraySequence s = new RawArraySequence(array);
      stack.add(0, s);
      return hasNext();
    }
    else if (o instanceof ArrayCallback)
    {
      final ArrayCallback array = (ArrayCallback) o;
      final AnySequence s = new AnySequence(array, context);
      stack.add(0, s);
      return hasNext();
    }
    else if (o instanceof Sequence)
    {
      final Sequence s = (Sequence) o;
      if (s.hasNext())
      {
        final Object object = s.next();
        stack.add(0, s);
        stack.add(0, object);
      }
      return hasNext();
    }

    stack.add(0, o);
    return true;
  }

  public Object next() throws EvaluationException
  {
    final Object o = stack.remove();
    if (o instanceof Sequence)
    {
      final Sequence sequence = (Sequence) o;
      return sequence.next();
    }
    return (o);
  }

  public LValue nextRawValue() throws EvaluationException
  {
    throw new EvaluationException(LibFormulaErrorValue.ERROR_UNEXPECTED_VALUE);
  }
}
