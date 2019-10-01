package js.csv.impl.fixture;

import java.text.ParseException;

import js.format.Format;

public class NameFormat implements Format
{
  @Override
  public String format(Object object)
  {
    return object.toString().toUpperCase();
  }

  @Override
  public Object parse(String value) throws ParseException
  {
    return value.toUpperCase();
  }
}