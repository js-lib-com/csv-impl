package js.csv.impl.unit;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import js.csv.CsvColumn;
import js.csv.CsvDescriptor;
import js.csv.impl.CsvDescriptorImpl;
import js.csv.impl.fixture.NameFormat;
import js.csv.impl.fixture.Person;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.util.Classes;

public class CsvDescriptorTest
{
  @Test
  public void constructor() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("default-config.xml");
    assertThat(descriptor.columns(), isValidColumns());
  }

  @Test
  public void config_Delimiter() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("config-delimiter.xml");
    assertThat(descriptor.format().delimiter(), equalTo(';'));
  }

  @Test
  public void config_Comment() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("config-comment.xml");
    assertThat(descriptor.format().comment(), equalTo('/'));
  }

  @Test
  public void config_Quote() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("config-quote.xml");
    assertThat(descriptor.format().openQuote(), equalTo('\''));
    assertThat(descriptor.format().closeQuote(), equalTo('\''));
  }

  @Test
  public void config_QuotePair() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("config-quote-pair.xml");
    assertThat(descriptor.format().openQuote(), equalTo('['));
    assertThat(descriptor.format().closeQuote(), equalTo(']'));
  }

  @Test(expected = ConfigException.class)
  public void config_MissingCloseQuote() throws ConfigException
  {
    descriptor("config-missing-close-quote.xml");
  }

  @Test
  public void config_Escape() throws ConfigException
  {
    CsvDescriptor<Person> descriptor = descriptor("config-escape.xml");
    assertThat(descriptor.format().escape(), equalTo('\\'));
  }

  @Test
  public void config_header() throws ConfigException
  {
    CsvDescriptor<Person> descriptor =  descriptor("config-header.xml");
    assertTrue(descriptor.format().header());
  }

  @Test
  public void config_EmptyLines() throws ConfigException
  {
    CsvDescriptor<Person> descriptor =   descriptor("config-empty-lines.xml");
    assertFalse(descriptor.format().emptyLines());
  }

  @Test
  public void config_Trim() throws ConfigException
  {
    CsvDescriptor<Person> descriptor =    descriptor("config-trim.xml");
    assertFalse(descriptor.format().trim());
  }

  @Test
  public void config_Charset() throws ConfigException
  {
    CsvDescriptor<Person> descriptor =    descriptor("config-charset.xml");
    assertThat(descriptor.format().charset(), equalTo(Charset.forName("ISO-8859-2")));
  }

  @Test
  public void config_Strict() throws ConfigException
  {
    CsvDescriptor<Person> descriptor =     descriptor("config-strict.xml");
    assertTrue(descriptor.format().strict());
  }

  @Test(expected = ConfigException.class)
  public void config_NoBoundClass() throws ConfigException
  {
    descriptor("config-no-bound-class.xml");
  }

  @Test(expected = ConfigException.class)
  public void config_NoFieldName() throws ConfigException
  {
    descriptor("config-no-field-name.xml");
  }

  @Test(expected = ConfigException.class)
  public void config_NoFormatterClass() throws ConfigException
  {
    descriptor("config-no-formatter-class.xml");
  }

  @Test(expected = ConfigException.class)
  public void config_AbstractFormatterClass() throws ConfigException
  {
    descriptor("config-abstract-formatter-class.xml");
  }

  @Test
  public void columns_Names()
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.columns("name", "postalAddress");

    assertThat(descriptor.columns(), notNullValue());
    assertThat(descriptor.columns(), not(empty()));
    assertThat(descriptor.columns(), hasSize(2));
    assertThat(descriptor.columns().get(0).fieldName(), equalTo("name"));
    assertThat(descriptor.columns().get(0).formatter(), Matchers.nullValue());
    assertThat(descriptor.columns().get(1).fieldName(), equalTo("postalAddress"));
    assertThat(descriptor.columns().get(1).formatter(), Matchers.nullValue());
  }

  @Test
  public void columns_Enumeration()
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.columns(Columns.class);

    assertThat(descriptor.columns(), notNullValue());
    assertThat(descriptor.columns(), not(empty()));
    assertThat(descriptor.columns(), hasSize(2));
    assertThat(descriptor.columns().get(0).fieldName(), equalTo("name"));
    assertThat(descriptor.columns().get(0).formatter(), Matchers.nullValue());
    assertThat(descriptor.columns().get(1).fieldName(), equalTo("postalAddress"));
    assertThat(descriptor.columns().get(1).formatter(), Matchers.nullValue());
  }

  // ----------------------------------------------------------------------------------------------

  private CsvDescriptor<Person> descriptor(String resourceName) throws ConfigException
  {
    ConfigBuilder config = new ConfigBuilder(Classes.getResourceAsStream(resourceName));
    return new CsvDescriptorImpl<>(config.build());
  }

  private static Matcher<List<CsvColumn>> isValidColumns()
  {
    return new IsValidColumns();
  }

  private static class IsValidColumns extends TypeSafeMatcher<List<CsvColumn>>
  {
    @Override
    public void describeTo(Description description)
    {
      description.appendText("valid CSV columns");
    }

    @Override
    protected boolean matchesSafely(List<CsvColumn> columns)
    {
      if(columns == null) return false;

      if(columns.size() != 2) return false;
      if(!"name".equals(columns.get(0).fieldName())) return false;
      if(columns.get(0).formatter() == null) return false;
      if(!(columns.get(0).formatter() instanceof NameFormat)) return false;
      if(!"address".equals(columns.get(1).fieldName())) return false;
      if(columns.get(1).formatter() != null) return false;

      return true;
    }
  }

  private enum Columns
  {
    NAME, POSTAL_ADDRESS
  }
}
