package com.jslib.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.jslib.api.csv.CsvColumn;
import com.jslib.api.csv.CsvDescriptor;
import com.jslib.api.csv.CsvFormat;
import com.jslib.api.csv.CsvWriter;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.converter.Converter;
import com.jslib.converter.ConverterRegistry;
import com.jslib.util.Classes;
import com.jslib.util.Params;

public class CsvWriterImpl<T> implements CsvWriter<T>
{
  private static final Log log = LogFactory.getLog(CsvWriterImpl.class);

  private final Converter converter;
  private final Writer writer;
  private final CsvDescriptor<T> descriptor;
  private final CsvFormat format;
  /** Value escape is enabled if all open quote, close quote and escape characters are not null. */
  private final boolean escaped;

  private boolean headerProcessed;

  /**
   * Create CSV writer using character encoding configured in the CSV format from given CSV descriptor.
   * 
   * @param descriptor CSV descriptor,
   * @param stream bytes output stream.
   */
  public CsvWriterImpl(CsvDescriptor<T> descriptor, OutputStream stream)
  {
    this(descriptor, new BufferedWriter(new OutputStreamWriter(stream, descriptor.format().charset())));
    log.trace("CsvWriterImpl(CsvDescriptor<T>,OutputStream)");
  }

  /**
   * Create CSV writer using platform default character set.
   * 
   * @param descriptor CSV descriptor,
   * @param writer destination characters stream with default encoding.
   */
  public CsvWriterImpl(CsvDescriptor<T> descriptor, Writer writer)
  {
    log.trace("CsvWriterImpl(CsvDescriptor<T>,Writer)");
    this.converter = ConverterRegistry.getConverter();
    this.writer = writer;
    this.descriptor = descriptor;
    this.format = descriptor.format();
    this.escaped = this.format.openQuote() != '\0' && this.format.closeQuote() != '\0' && this.format.escape() != '\0';
  }

  @Override
  public void write(T object) throws IOException
  {
    Params.notNull(object, "Object argument");

    // header processing
    if(format.header() && !headerProcessed) {
      headerProcessed = true;
      for(int i = 0; i < descriptor.columns().size(); ++i) {
        if(i > 0) {
          writer.write(format.delimiter());
        }
        writeValue(descriptor.columns().get(i).fieldName().toUpperCase());
      }
      writer.write("\r\n");
    }

    writeValue(getFieldValue(object, 0));
    for(int i = 1; i < descriptor.columns().size(); ++i) {
      writer.write(format.delimiter());
      writeValue(getFieldValue(object, i));
    }
    writer.write("\r\n");
  }

  @Override
  public void flush() throws IOException
  {
    writer.flush();
  }

  @Override
  public void close() throws IOException
  {
    writer.close();
  }

  // ----------------------------------------------------------------------------------------------

  /**
   * Get object field string value described by value descriptor with given column index.
   * 
   * @param object source object to get field value from,
   * @param columnIndex column index.
   * @return object field string value.
   */
  private String getFieldValue(T object, int columnIndex)
  {
    CsvColumn column = descriptor.columns().get(columnIndex);
    Object value = Classes.getFieldValue(object, column.fieldName());
    if(value == null) {
      return format.nullValue();
    }
    if(column.formatter() != null) {
      return column.formatter().format(value);
    }
    return converter.asString(value);
  }

  /**
   * Write value to output stream. If escape is enabled surround value with {@link CsvFormat#openQuote()}, respective
   * {@link CsvFormat#closeQuote()} and escape close quote.
   * 
   * @param value CSV value to write.
   * @throws IOException if writing to output CSV stream fails.
   */
  private void writeValue(String value) throws IOException
  {
    if(!escaped) {
      writer.write(value);
      return;
    }

    writer.write(format.openQuote());

    for(int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i);
      if(c == format.escape() || c == format.closeQuote()) {
        writer.write(format.escape());
      }
      writer.write(c);
    }

    writer.write(format.closeQuote());
  }
}
