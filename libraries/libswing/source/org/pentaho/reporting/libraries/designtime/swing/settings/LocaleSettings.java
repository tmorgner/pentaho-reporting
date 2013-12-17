package org.pentaho.reporting.libraries.designtime.swing.settings;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Todo: Document me!
 * <p/>
 * Date: 14.05.2010
 * Time: 16:06:12
 *
 * @author Thomas Morgner.
 */
public interface LocaleSettings
{
  public String getDateFormatPattern();
  public String getTimeFormatPattern();
  public String getDatetimeFormatPattern();

  public Locale getLocale();
  public TimeZone getTimeZone();
}
