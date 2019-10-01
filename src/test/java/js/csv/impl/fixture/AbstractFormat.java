package js.csv.impl.fixture;

import java.text.ParseException;

import js.format.Format;

public abstract class AbstractFormat implements Format
{
  public abstract String format(Object object);

  @Override
  public Object parse(String value) throws ParseException
  {
    return value.toUpperCase();
  }
}