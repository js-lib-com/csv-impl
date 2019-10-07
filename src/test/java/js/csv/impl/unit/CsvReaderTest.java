package js.csv.impl.unit;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import js.csv.CsvDelimiter;
import js.csv.CsvDescriptor;
import js.csv.CsvEscape;
import js.csv.CsvException;
import js.csv.CsvFormat;
import js.csv.CsvQuote;
import js.csv.CsvReader;
import js.csv.impl.CsvDescriptorImpl;
import js.csv.impl.CsvFormatImpl;
import js.csv.impl.CsvReaderImpl;
import js.csv.impl.fixture.Employee;
import js.csv.impl.fixture.ExceptionalFormat;
import js.csv.impl.fixture.NameFormat;
import js.csv.impl.fixture.Person;
import js.util.Classes;

public class CsvReaderTest
{
  private CsvFormat format;

  @Before
  public void beforeTest()
  {
    format = new CsvFormatImpl();
  }

  /** Standard conformance test. */
  @Test
  public void conformance() throws IOException
  {
    List<Person> persons = exercise("/conformance.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  @Test
  public void comaptibility() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().header(true);
    descriptor.columns("name", "address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, getClass().getResourceAsStream("/compatibility-test.csv"));
    List<Person> persons = new ArrayList<>();
    for(Person person : reader) {
      persons.add(person);
    }
    reader.close();

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(6));

    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("United States"));
    assertThat(persons.get(1).name, equalTo("Jane Doe"));
    assertThat(persons.get(1).address, equalTo("France"));
    assertThat(persons.get(2).name, equalTo(""));
    assertThat(persons.get(2).address, equalTo("Germany"));
    assertThat(persons.get(3).name, equalTo("Baby Doe"));
    assertThat(persons.get(3).address, equalTo(""));
    assertThat(persons.get(4).name, equalTo("John Doe, Sr."));
    assertThat(persons.get(4).address, equalTo("Romania"));
    assertThat(persons.get(5).name, equalTo("Lion, \"The Little Cat\""));
    assertThat(persons.get(5).address, equalTo("Russia"));
  }

  @Test
  public void emptyCsvStream() throws IOException
  {
    List<Person> persons = exercise("/empty.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, empty());
  }

  @Test
  public void lastFieldEmpty() throws IOException
  {
    List<Person> persons = exercise("/last-field-empty.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo(""));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  @Test
  public void eol_CRLF() throws IOException
  {
    eol("John Doe,Romania\r\nBaby Doe,United Kingdom\r\n");
  }

  @Test
  public void eol_LF() throws IOException
  {
    eol("John Doe,Romania\nBaby Doe,United Kingdom\n");
  }

  @Test
  public void eol_CR() throws IOException
  {
    eol("John Doe,Romania\rBaby Doe,United Kingdom\r");
  }

  private void eol(String CSV) throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.columns("name", "address");
    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, new StringReader(CSV));

    List<Person> persons = new ArrayList<>();
    for(Person person : reader) {
      persons.add(person);
    }
    reader.close();

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  @Test
  public void charset_UTF8() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().charset("UTF-8");
    descriptor.columns("name", "address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, getClass().getResourceAsStream("/charset-utf8.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, equalTo("Ghiță Mureșan"));
    assertThat(person.address, equalTo("România"));
  }

  @Test
  public void charset_Cp1250() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().charset("Cp1250");
    descriptor.columns("name", "address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, getClass().getResourceAsStream("/charset-cp-1250.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, equalTo("Ghiţă Mureşan"));
    assertThat(person.address, equalTo("România"));
  }

  @Test
  public void charset_ISO_8859_2() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().charset("ISO-8859-2");
    descriptor.columns("name", "address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, getClass().getResourceAsStream("/charset-iso8859-2.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, equalTo("Ghiţă Mureşan"));
    assertThat(person.address, equalTo("România"));
  }

  /** CSV with header. */
  @Test
  public void header() throws IOException
  {
    format.header(true);
    List<Person> persons = exercise("/header.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /** CSV with header and not configured columns. This prove that columns are loaded from header. */
  @Test
  public void header_noColumns() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().header(true);

    CsvReader<Person> reader = new CsvReaderImpl<Person>(descriptor, Classes.getResourceAsReader("/header.csv"));
    List<Person> persons = new ArrayList<>();
    for(Person person : reader) {
      persons.add(person);
    }
    reader.close();

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  @Test
  public void header_Empty() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().header(true);

    CsvReader<Person> reader = new CsvReaderImpl<Person>(descriptor, Classes.getResourceAsReader("/empty.csv"));
    assertFalse(reader.hasNext());
    reader.close();
  }

  @Test
  public void header_noColumns_Empty() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().header(true);

    CsvReader<Person> reader = new CsvReaderImpl<Person>(descriptor, Classes.getResourceAsReader("/header-empty.csv"));
    assertFalse(reader.hasNext());
    reader.close();
  }

  @Test(expected = CsvException.class)
  public void header_noColumns_MissingField_Strict() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().header(true);
    descriptor.format().strict(true);

    CsvReader<Person> reader = new CsvReaderImpl<Person>(descriptor, Classes.getResourceAsReader("/header-missing-field.csv"));
    reader.next();
    reader.close();
  }

  /** CSV with header but disabled on CSV format. */
  @Test
  public void header_disabled() throws IOException
  {
    List<Person> persons = exercise("/header.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(3));
    assertThat(persons.get(0).name, equalTo("name"));
    assertThat(persons.get(0).address, equalTo("address"));
    assertThat(persons.get(1).name, equalTo("John Doe"));
    assertThat(persons.get(1).address, equalTo("Romania"));
    assertThat(persons.get(2).name, equalTo("Baby Doe"));
    assertThat(persons.get(2).address, equalTo("United Kingdom"));
  }

  /** Source CSV stream uses tab delimiter and CSV format is configured correctly. */
  @Test
  public void tabDelimiter() throws IOException
  {
    format.delimiter('\t');
    List<Person> persons = exercise("/tab-delimiter.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /** Configure CSV format delimiter with enumeration constant from {@link CsvDelimiter}. */
  @Test
  public void delimiter_Enumeration() throws IOException
  {
    format.delimiter(CsvDelimiter.TAB);
    List<Person> persons = exercise("/tab-delimiter.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /**
   * Source CSV stream uses tab delimiter but CSv format is configured with default comma. As a result entire row is
   * considered as first field value and second field remain null.
   */
  @Test
  public void delimiter_BadConfiguration() throws IOException
  {
    List<Person> persons = exercise("/tab-delimiter.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe\tRomania"));
    assertThat(persons.get(0).address, nullValue());
    assertThat(persons.get(1).name, equalTo("Baby Doe\tUnited Kingdom"));
    assertThat(persons.get(1).address, nullValue());
  }

  /** Source CSV stream contains empty lines that are by default ignored. */
  @Test
  public void emptyLines() throws IOException
  {
    List<Person> persons = exercise("/empty-lines.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /**
   * Source CSV stream has empty lines and CSV format is configured to accept them. In parsed objects list there are
   * objects with default / null fields.
   */
  @Test
  public void emptyLines_Accepted() throws IOException
  {
    format.emptyLines(true);
    List<Person> persons = exercise("/empty-lines.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(4));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, nullValue());
    assertThat(persons.get(1).address, nullValue());
    assertThat(persons.get(2).name, equalTo("Baby Doe"));
    assertThat(persons.get(2).address, equalTo("United Kingdom"));
    assertThat(persons.get(3).name, nullValue());
    assertThat(persons.get(3).address, nullValue());
  }

  /**
   * Source CSV stream contains comments that are by default processed, that is, recognized as comments and ignored by
   * parser.
   */
  @Test
  public void comments() throws IOException
  {
    List<Person> persons = exercise("/comments.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  @Test
  public void comments_FolloweByEmptyLine() throws IOException
  {
    List<Person> persons = exercise("/comments-followed-by-empty-line.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /**
   * Source CSV stream contains comments and CSV format is configured to not handle them. As a consequence comments are
   * processed as normal value and included into generated objects list - as value for first field.
   */
  @Test
  public void comments_Disabled() throws IOException
  {
    format.comment('\0');
    List<Person> persons = exercise("/comments.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(4));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("# comment #1"));
    assertThat(persons.get(1).address, nullValue());
    assertThat(persons.get(2).name, equalTo("Baby Doe"));
    assertThat(persons.get(2).address, equalTo("United Kingdom"));
    assertThat(persons.get(3).name, equalTo("# comment #2"));
    assertThat(persons.get(3).address, nullValue());
  }

  /** By default trimming white spaces around values is enabled. White space inside values are not affected. */
  @Test
  public void trim() throws IOException
  {
    List<Person> persons = exercise("/trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United\tKingdom"));
  }

  /**
   * Disabling value trimming will preserve white spaces on resulting objects fields. White space inside values are not
   * affected.
   */
  @Test
  public void trim_Disabled() throws IOException
  {
    format.trim(false);
    List<Person> persons = exercise("/trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("\tJohn Doe\t"));
    assertThat(persons.get(0).address, equalTo("\tRomania\t"));
    assertThat(persons.get(1).name, equalTo(" Baby Doe "));
    assertThat(persons.get(1).address, equalTo(" United\tKingdom "));
  }

  /** Spaces around tab delimiter are correctly trimmed out from parsed values. */
  @Test
  public void trim_OnTabDelimiter() throws IOException
  {
    format.delimiter('\t');
    List<Person> persons = exercise("/trim-on-tab-delimiter.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(2));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Romania"));
    assertThat(persons.get(1).name, equalTo("Baby Doe"));
    assertThat(persons.get(1).address, equalTo("United Kingdom"));
  }

  /**
   * Source CSV stream contains complex values enclosed on quotation marks, the default quote character. Although value
   * has comma (',') that is used as separator parser left it on value since it is inside quoted value.
   */
  @Test
  public void complexValue() throws IOException
  {
    List<Person> persons = exercise("/complex-value.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  /** Replace default quote character with single quotation mark. */
  @Test
  public void complexValue_SingleQuote() throws IOException
  {
    format.quote('\'');
    List<Person> persons = exercise("/complex-value-single-quote.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  @Test
  public void complexValue_Trim() throws IOException
  {
    List<Person> persons = exercise("/complex-value-trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  @Test
  public void complexValue_Trim_Disabled() throws IOException
  {
    format.trim(false);
    List<Person> persons = exercise("/complex-value-trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("\tJohn Doe "));
    assertThat(persons.get(0).address, equalTo("\tJassy, RO "));
  }

  /**
   * Complex value with CRLF inside. Since value is surrounded by quote characters CRLF from value is preserved, that
   * is, is not interpreted as EOL.
   */
  @Test
  public void complexValue_WithCRLF() throws IOException
  {
    List<Person> persons = exercise("/complex-value-with-crlf.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe\r\nJane Doe\r\nBaby Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  /**
   * Ensure complex value enclosed by quote character is correctly processed even if last value character is equal to
   * quote character itself, of course escaped. That is, on CSV stream value is ending with three quote characters in
   * sequence. First is escape character, second is part of value and last is for value enclosing.
   */
  @Test
  public void complexValue_DoubleQuoteEnding() throws IOException
  {
    List<Person> persons = exercise("/complex-value-double-quote-ending.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, \"RO\""));
  }

  @Test
  public void complexValue_SquareBrackets() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    List<Person> persons = exercise("/complex-value-square-brackets.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  @Test
  public void complexValue_SquareBrackets_WithCRLF() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    format.escape(CsvEscape.BACKSLASH);
    List<Person> persons = exercise("/complex-value-square-brackets-with-crlf.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe\r\nJane Doe\r\nBaby Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  @Test
  public void complexValue_SquareBrackets_Trim() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    format.escape(CsvEscape.BACKSLASH);
    List<Person> persons = exercise("/complex-value-square-brackets-trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, RO"));
  }

  @Test
  public void complexValue_SquareBrackets_Trim_Disabled() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    format.escape(CsvEscape.BACKSLASH);
    format.trim(false);
    List<Person> persons = exercise("/complex-value-square-brackets-trim.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("\tJohn Doe "));
    assertThat(persons.get(0).address, equalTo("\tJassy, RO "));
  }

  @Test
  public void complexValue_SquareBrackets_Escape() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    format.escape(CsvEscape.BACKSLASH);
    List<Person> persons = exercise("/complex-value-square-brackets-escape.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    assertThat(persons.get(0).name, equalTo("John Doe"));
    assertThat(persons.get(0).address, equalTo("Jassy, \\ [RO]"));
  }

  /**
   * Read complex value with double quotation mark but configure CSV format with wrong quote character. On parsed object
   * there are double quotation marks from CSV stream and address is truncated at comma, that since is not inside
   * recognized quote character it is considered value delimiter.
   */
  @Test
  public void complexValue_BadQuote() throws IOException
  {
    format.quote('!');
    List<Person> persons = exercise("/complex-value.csv");

    assertThat(persons, notNullValue());
    assertThat(persons, not(empty()));
    assertThat(persons, hasSize(1));
    // double quotation marks are present on name since are not recognized as quote characters
    assertThat(persons.get(0).name, equalTo("\"John Doe\""));
    // ,RO is missing from address since comma is interpreted as value delimiter
    assertThat(persons.get(0).address, equalTo("\"Jassy"));
  }

  @Test
  public void valueFormatter() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.column("name", new NameFormat());
    descriptor.column("address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/conformance.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, equalTo("JOHN DOE"));
    assertThat(person.address, equalTo("Romania"));
  }

  @Test(expected = CsvException.class)
  public void valueFormatter_Exception_Strict() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().strict(true);
    descriptor.column("name", new ExceptionalFormat());
    descriptor.column("address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/conformance.csv"));
    reader.next();
    reader.close();
  }

  @Test
  public void valueFormatter_Exception_Relaxed() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.column("name", new ExceptionalFormat());
    descriptor.column("address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/conformance.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, nullValue());
    assertThat(person.address, equalTo("Romania"));
  }

  @Test
  public void inheritance() throws IOException
  {
    CsvDescriptor<Employee> descriptor = new CsvDescriptorImpl<>(Employee.class);
    descriptor.columns("name", "address", "wage");

    CsvReader<Employee> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/inheritance.csv"));
    List<Employee> employees = new ArrayList<>();
    for(Employee employee : reader) {
      employees.add(employee);
    }
    reader.close();

    assertThat(employees, notNullValue());
    assertThat(employees, not(empty()));
    assertThat(employees, hasSize(2));
    assertThat(employees.get(0).name, equalTo("John Doe"));
    assertThat(employees.get(0).address, equalTo("Romania"));
    assertThat(employees.get(0).wage, equalTo(3500.0));
    assertThat(employees.get(1).name, equalTo("Baby Doe"));
    assertThat(employees.get(1).address, equalTo("United Kingdom"));
    assertThat(employees.get(1).wage, equalTo(2000.0));
  }

  @Test(expected = CsvException.class)
  public void badValueType_Strict() throws IOException
  {
    CsvDescriptor<Employee> descriptor = new CsvDescriptorImpl<>(Employee.class);
    descriptor.format().strict(true);
    descriptor.columns("name", "address", "wage");

    CsvReader<Employee> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/bad-value-type.csv"));
    List<Employee> employees = new ArrayList<>();
    for(Employee employee : reader) {
      employees.add(employee);
    }
    reader.close();
  }

  @Test
  public void badValueType_Relaxed() throws IOException
  {
    CsvDescriptor<Employee> descriptor = new CsvDescriptorImpl<>(Employee.class);
    descriptor.columns("name", "address", "wage");

    CsvReader<Employee> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/bad-value-type.csv"));
    Employee employee = reader.next();
    reader.close();

    assertThat(employee, notNullValue());
    assertThat(employee.name, equalTo("John Doe"));
    assertThat(employee.address, equalTo("Romania"));
    assertThat(employee.wage, equalTo(0.0));
  }

  @Test(expected = CsvException.class)
  public void missingField_Strict() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.format().strict(true);
    descriptor.columns("name", "postal-address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/conformance.csv"));
    reader.next();
    reader.close();
  }

  @Test
  public void missingField_Relaxed() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.columns("name", "postalAddress");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/conformance.csv"));
    Person person = reader.next();
    reader.close();

    assertThat(person, notNullValue());
    assertThat(person.name, equalTo("John Doe"));
    assertThat(person.address, nullValue());
  }

  @Test(expected = CsvException.class)
  public void badColumnsCount_Strict() throws IOException
  {
    CsvDescriptor<Employee> descriptor = new CsvDescriptorImpl<>(Employee.class);
    descriptor.format().strict(true);
    descriptor.columns("name", "address");

    CsvReader<Employee> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/bad-value-type.csv"));
    reader.next();
    reader.close();
  }

  @Test(expected = NoSuchElementException.class)
  public void nullNext() throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<>(Person.class);
    descriptor.columns("name", "address");

    CsvReader<Person> reader = new CsvReaderImpl<>(descriptor, Classes.getResourceAsReader("/empty.csv"));
    reader.next();
    reader.close();
  }

  // ----------------------------------------------------------------------------------------------

  private List<Person> exercise(String resourceName) throws IOException
  {
    CsvDescriptor<Person> descriptor = new CsvDescriptorImpl<Person>((CsvFormatImpl)format, Person.class);
    descriptor.columns("name", "address");
    CsvReader<Person> reader = new CsvReaderImpl<Person>(descriptor, Classes.getResourceAsReader(resourceName));

    List<Person> persons = new ArrayList<>();
    for(Person person : reader) {
      persons.add(person);
    }
    reader.close();

    return persons;
  }
}
