package js.csv.impl.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import js.csv.CsvComment;
import js.csv.CsvFormat;
import js.csv.CsvQuote;
import js.csv.impl.CsvFormatImpl;

public class CsvFormatTest
{
  @Test
  public void constructor()
  {
    CsvFormat format = new CsvFormatImpl();
    assertThat(format, isDefaultFormat());
  }

  @Test
  public void quote()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.quote(':');
    assertThat(format.openQuote(), equalTo(':'));
    assertThat(format.closeQuote(), equalTo(':'));
    assertThat(format.escape(), equalTo('"'));
  }

  @Test
  public void quote_Enumeration()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.quote(CsvQuote.SQUARE_BRACKETS);
    assertThat(format.openQuote(), equalTo('['));
    assertThat(format.closeQuote(), equalTo(']'));
    assertThat(format.escape(), equalTo('"'));
  }

  @Test
  public void charset()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.charset("ISO-8859-2");
    assertThat(format.charset(), notNullValue());
    assertThat(format.charset().name(), equalTo("ISO-8859-2"));
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void charset_Unsupported()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.charset("FAKE-CHARSET");
  }

  @Test
  public void comment()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.comment('/');
    assertThat(format.comment(), equalTo('/'));
  }

  @Test
  public void comment_Enumeration()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.comment(CsvComment.SLASH);
    assertThat(format.comment(), equalTo('/'));
  }

  @Test
  public void escape()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.escape('\\');
    assertThat(format.escape(), equalTo('\\'));
  }

  @Test
  public void nullValue()
  {
    CsvFormatImpl format = new CsvFormatImpl();
    format.nullValue("");
    assertThat(format.nullValue(), equalTo(""));
    format.nullValue(null);
    assertNull(format.nullValue());
  }

  // ----------------------------------------------------------------------------------------------

  private static Matcher<CsvFormat> isDefaultFormat()
  {
    return new IsDefaultFormat();
  }

  private static class IsDefaultFormat extends TypeSafeMatcher<CsvFormat>
  {
    @Override
    public void describeTo(Description description)
    {
      description.appendText("default CSV format");
    }

    @Override
    protected boolean matchesSafely(CsvFormat format)
    {
      if(format == null) return false;

      if(format.delimiter() != ',') return false;
      if(format.comment() != '#') return false;
      if(format.openQuote() != '"') return false;
      if(format.closeQuote() != '"') return false;
      if(format.escape() != '"') return false;
      if(format.header()) return false;
      if(format.emptyLines()) return false;
      if(!format.trim()) return false;
      if(!Charset.forName("UTF-8").equals(format.charset())) return false;
      if(format.nullValue() != "NULL") return false;
      if(format.strict()) return false;

      return true;
    }
  }
}
