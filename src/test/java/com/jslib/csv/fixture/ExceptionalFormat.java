package com.jslib.csv.fixture;

import java.text.ParseException;

import com.jslib.format.Format;

public class ExceptionalFormat implements Format
{
  @Override
  public String format(Object object)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object parse(String value) throws ParseException
  {
    throw new ParseException("Simulated exception.", 0);
  }
}