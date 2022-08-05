package com.jslib.csv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.jslib.api.csv.CsvDelimiter;
import com.jslib.api.csv.CsvQuote;
import com.jslib.api.csv.CsvWriter;
import com.jslib.csv.fixture.NameFormat;
import com.jslib.csv.fixture.Person;

public class CsvWriterTest
{
  private CsvFormatImpl format;

  @Before
  public void beforeTest()
  {
    format = new CsvFormatImpl();
  }

  @Test
  public void conformance() throws IOException
  {
    String result = exercise();
    assertThat(result, equalTo("\"John Doe\",\"Romania\"\r\n\"Baby Doe\",\"United States\"\r\n"));
  }

  @Test
  public void delimiter() throws IOException
  {
    format.delimiter(CsvDelimiter.COLON);
    String result = exercise();
    assertThat(result, equalTo("\"John Doe\":\"Romania\"\r\n\"Baby Doe\":\"United States\"\r\n"));
  }

  @Test
  public void quote_SquareBrackets() throws IOException
  {
    format.quote(CsvQuote.SQUARE_BRACKETS);
    String result = exercise();
    assertThat(result, equalTo("[John Doe],[Romania]\r\n[Baby Doe],[United States]\r\n"));
  }

  @Test
  public void quote_Disabled() throws IOException
  {
    format.quote('\0');
    String result = exercise();
    assertThat(result, equalTo("John Doe,Romania\r\nBaby Doe,United States\r\n"));
  }

  private String exercise() throws IOException
  {
    CsvDescriptorImpl<Person> descriptor = new CsvDescriptorImpl<>(format, Person.class);
    descriptor.columns("name", "address");

    StringWriter buffer = new StringWriter();

    CsvWriter<Person> writer = new CsvWriterImpl<>(descriptor, buffer);
    writer.write(new Person("John Doe", "Romania"));
    writer.write(new Person("Baby Doe", "United States"));
    writer.close();

    return buffer.toString();
  }

  @Test
  public void escape() throws IOException
  {
    CsvDescriptorImpl<Person> descriptor = new CsvDescriptorImpl<>(format, Person.class);
    descriptor.columns("name", "address");

    StringWriter buffer = new StringWriter();

    CsvWriter<Person> writer = new CsvWriterImpl<>(descriptor, buffer);
    writer.write(new Person("John Doe", "Romania, \"Jassy\""));
    writer.write(new Person("Baby\r\nDoe", "United States"));
    writer.close();

    assertThat(buffer.toString(), equalTo("\"John Doe\",\"Romania, \"\"Jassy\"\"\"\r\n\"Baby\r\nDoe\",\"United States\"\r\n"));
  }

  @Test
  public void nullValue() throws IOException
  {
    CsvDescriptorImpl<Person> descriptor = new CsvDescriptorImpl<>(format, Person.class);
    descriptor.columns("name", "address");

    StringWriter buffer = new StringWriter();

    CsvWriter<Person> writer = new CsvWriterImpl<>(descriptor, buffer);
    writer.write(new Person());
    writer.write(new Person());
    writer.close();

    assertThat(buffer.toString(), equalTo("\"NULL\",\"NULL\"\r\n\"NULL\",\"NULL\"\r\n"));
  }

  @Test
  public void formatter() throws IOException
  {
    CsvDescriptorImpl<Person> descriptor = new CsvDescriptorImpl<>(format, Person.class);
    descriptor.column("name", new NameFormat());
    descriptor.column("address");

    StringWriter buffer = new StringWriter();

    CsvWriter<Person> writer = new CsvWriterImpl<>(descriptor, buffer);
    writer.write(new Person("John Doe", "Romania"));
    writer.write(new Person("Baby Doe", "United States"));
    writer.close();

    assertThat(buffer.toString(), equalTo("\"JOHN DOE\",\"Romania\"\r\n\"BABY DOE\",\"United States\"\r\n"));
  }

  @Test
  public void byteStream() throws IOException
  {
    CsvDescriptorImpl<Person> descriptor = new CsvDescriptorImpl<>(format, Person.class);
    descriptor.column("name", new NameFormat());
    descriptor.column("address");

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    CsvWriter<Person> writer = new CsvWriterImpl<>(descriptor, buffer);
    writer.write(new Person("John Doe", "Romania"));
    writer.write(new Person("Baby Doe", "United States"));
    writer.close();

    assertThat(buffer.toString(), equalTo("\"JOHN DOE\",\"Romania\"\r\n\"BABY DOE\",\"United States\"\r\n"));
  }
}
