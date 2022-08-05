package com.jslib.csv;

import java.nio.charset.Charset;

import com.jslib.api.csv.CsvComment;
import com.jslib.api.csv.CsvDelimiter;
import com.jslib.api.csv.CsvEscape;
import com.jslib.api.csv.CsvFormat;
import com.jslib.api.csv.CsvQuote;
import com.jslib.util.Params;

public class CsvFormatImpl implements CsvFormat
{
  private char delimiterChar = ',';
  private char commentChar = '#';
  private char openQuoteChar = '"';
  private char closeQuoteChar = '"';
  private char escapeChar = '"';
  private boolean header = false;
  private boolean emptyLines = false;
  private boolean trim = true;
  private String nullValue = "NULL";
  private Charset charset = Charset.forName("UTF-8");
  private boolean strict = false;

  /**
   * Create CSV format with default properties.
   */
  public CsvFormatImpl()
  {
  }

  @Override
  public CsvFormatImpl delimiter(char delimiter)
  {
    Params.notNull(delimiter, "Delimiter character");
    this.delimiterChar = delimiter;
    return this;
  }

  @Override
  public CsvFormatImpl delimiter(CsvDelimiter delimiter)
  {
    return delimiter(delimiter.value());
  }

  @Override
  public char delimiter()
  {
    return delimiterChar;
  }

  @Override
  public CsvFormatImpl comment(char commentChar)
  {
    this.commentChar = commentChar;
    return this;
  }

  @Override
  public CsvFormatImpl comment(CsvComment comment)
  {
    this.commentChar = comment.value();
    return this;
  }

  @Override
  public char comment()
  {
    return commentChar;
  }

  @Override
  public CsvFormatImpl quote(char quoteChar)
  {
    quote(quoteChar, quoteChar);
    return this;
  }

  @Override
  public CsvFormatImpl quote(char openQuoteChar, char closeQuoteChar)
  {
    this.openQuoteChar = openQuoteChar;
    this.closeQuoteChar = closeQuoteChar;
    return this;
  }

  @Override
  public CsvFormatImpl quote(CsvQuote quote)
  {
    quote(quote.value(0), quote.value(1));
    return this;
  }

  @Override
  public char openQuote()
  {
    return openQuoteChar;
  }

  @Override
  public char closeQuote()
  {
    return closeQuoteChar;
  }

  @Override
  public CsvFormatImpl escape(char escapeChar)
  {
    this.escapeChar = escapeChar;
    return this;
  }

  @Override
  public CsvFormatImpl escape(CsvEscape escape)
  {
    this.escapeChar = escape.value();
    return this;
  }

  @Override
  public char escape()
  {
    return escapeChar;
  }

  @Override
  public CsvFormatImpl header(boolean header)
  {
    this.header = header;
    return this;
  }

  @Override
  public boolean header()
  {
    return header;
  }

  @Override
  public CsvFormatImpl emptyLines(boolean emptyLines)
  {
    this.emptyLines = emptyLines;
    return this;
  }

  @Override
  public boolean emptyLines()
  {
    return emptyLines;
  }

  /**
   * Enable or disable value white space trimming. Note that if delimiter is a white space, e.g. tab character, it is
   * not considered for value trimming.
   * 
   * @param trim flag for value white space trimming.
   * @return this pointer.
   */
  @Override
  public CsvFormatImpl trim(boolean trim)
  {
    this.trim = trim;
    return this;
  }

  @Override
  public boolean trim()
  {
    return trim;
  }

  @Override
  public CsvFormatImpl charset(String charset)
  {
    this.charset = Charset.forName(charset);
    return this;
  }

  @Override
  public Charset charset()
  {
    return charset;
  }

  @Override
  public CsvFormatImpl nullValue(String nullValue)
  {
    this.nullValue = nullValue;
    return this;
  }

  @Override
  public String nullValue()
  {
    return nullValue;
  }

  @Override
  public CsvFormatImpl strict(boolean strict)
  {
    this.strict = strict;
    return this;
  }

  @Override
  public boolean strict()
  {
    return strict;
  }
}
