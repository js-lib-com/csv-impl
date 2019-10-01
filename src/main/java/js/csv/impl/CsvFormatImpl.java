package js.csv.impl;

import java.nio.charset.Charset;

import js.csv.CsvComment;
import js.csv.CsvDelimiter;
import js.csv.CsvEscape;
import js.csv.CsvFormat;
import js.csv.CsvQuote;

public class CsvFormatImpl implements CsvFormat
{
  private static final char DEF_DELIMITER_CHAR = ',';
  private static final char DEF_COMMENT_CHAR = '#';
  private static final char DEF_OPEN_QUOTE_CHAR = '"';
  private static final char DEF_CLOSE_QUOTE_CHAR = '"';
  private static final char DEF_ESCAPE_CHAR = '"';
  private static final boolean DEF_HEADER = false;
  private static final boolean DEF_EMPTY_LINES = false;
  private static final boolean DEF_TRIM = true;
  private static final Charset DEF_CHARSET = Charset.forName("UTF-8");
  private static final String DEF_NULL_VALUE = "NULL";
  private static final boolean DEF_STRICT = false;

  private char delimiterChar = DEF_DELIMITER_CHAR;
  private char commentChar = DEF_COMMENT_CHAR;
  private char openQuoteChar = DEF_OPEN_QUOTE_CHAR;
  private char closeQuoteChar = DEF_CLOSE_QUOTE_CHAR;
  private char escapeChar = DEF_ESCAPE_CHAR;
  private boolean header = DEF_HEADER;
  private boolean emptyLines = DEF_EMPTY_LINES;
  private boolean trim = DEF_TRIM;
  private Charset charset = DEF_CHARSET;
  private String nullValue = DEF_NULL_VALUE;
  private boolean strict = DEF_STRICT;

  /**
   * Create CSV format with default properties.
   */
  public CsvFormatImpl()
  {
  }

  /**
   * Set the character used to separate record values. This method just delegates {@link #delimiter(char)}.
   * 
   * @param delimiter values separator.
   * @return this pointer.
   */
  public CsvFormatImpl delimiter(CsvDelimiter delimiter)
  {
    return delimiter(delimiter.value());
  }

  /**
   * Set the character used to separate record values, both simple and complex. Given delimiter character should not be
   * present in value itself. If value contains delimiter it should be quoted.
   * 
   * @param delimiter character used to separate record values.
   * @return this pointer.
   */
  public CsvFormatImpl delimiter(char delimiter)
  {
    this.delimiterChar = delimiter;
    return this;
  }

  @Override
  public char delimiter()
  {
    return delimiterChar;
  }

  public CsvFormatImpl comment(char commentChar)
  {
    this.commentChar = commentChar;
    return this;
  }

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

  public CsvFormatImpl quote(char quoteChar)
  {
    quote(quoteChar, quoteChar);
    return this;
  }

  public CsvFormatImpl quote(CsvQuote quote)
  {
    quote(quote.open(), quote.close());
    return this;
  }

  public CsvFormatImpl quote(char openQuoteChar, char closeQuoteChar)
  {
    this.openQuoteChar = openQuoteChar;
    this.closeQuoteChar = closeQuoteChar;
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

  public CsvFormatImpl escape(char escapeChar)
  {
    this.escapeChar = escapeChar;
    return this;
  }

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
