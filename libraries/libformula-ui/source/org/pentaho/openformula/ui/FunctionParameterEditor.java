package org.pentaho.openformula.ui;

import java.awt.Component;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.09.2010
 * Time: 17:13:06
 *
 * @author Thomas Morgner.
 */
public interface FunctionParameterEditor
{
  public void addParameterUpdateListener(ParameterUpdateListener parameterUpdateListener);

  public void removeParameterUpdateListener(ParameterUpdateListener parameterUpdateListener);

  public Component getEditorComponent();

  public void setFields(FieldDefinition[] fieldDefinitions);

  public void clearSelectedFunction();

  public void setSelectedFunction(FunctionParameterContext context);
}
