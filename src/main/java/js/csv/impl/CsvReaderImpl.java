package js.csv.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import js.converter.Converter;
import js.converter.ConverterException;
import js.converter.ConverterRegistry;
import js.csv.CsvColumn;
import js.csv.CsvDescriptor;
import js.csv.CsvException;
import js.csv.CsvFormat;
import js.csv.CsvReader;
import js.format.Format;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Params;
import js.util.Strings;

public class CsvReaderImpl<T> implements CsvReader<T>
{
  private static final Log log = LogFactory.getLog(CsvReaderImpl.class);

  private final Converter converter;
  private final Reader reader;
  private final CsvDescriptor<T> descriptor;
  private final CsvFormat format;

  /** Values for a single CSV row. Values are updated by {@link #parseRecord()} and consumed by {@link #next()}. */
  private List<String> values;

  private boolean headerProcessed;

  /**
   * Create CSV reader using character encoding configured in the CSV format from given CSV descriptor.
   * 
   * @param descriptor CSV descriptor,
   * @param stream bytes input stream.
   */
  public CsvReaderImpl(CsvDescriptor<T> descriptor, InputStream stream)
  {
    this(descriptor, reader(stream, descriptor));
    log.trace("CsvReaderImpl(CsvDescriptor<T>,InputStream)");
  }

  private static Reader reader(InputStream stream, CsvDescriptor<?> descriptor)
  {
    Params.notNull(descriptor, "CSV descriptor");
    Params.notNull(descriptor.format(), "CSV descriptor format");
    return new BufferedReader(new InputStreamReader(stream, descriptor.format().charset()));
  }

  /**
   * Create CSV reader using platform default character set.
   * 
   * @param descriptor CSV descriptor,
   * @param reader source characters stream with default encoding.
   */
  public CsvReaderImpl(CsvDescriptor<T> descriptor, Reader reader)
  {
    log.trace("CsvReaderImpl(CsvDescriptor<T>,Reader)");
    this.converter = ConverterRegistry.getConverter();
    this.reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader);
    this.descriptor = descriptor;
    this.format = descriptor.format();
  }

  @Override
  public boolean hasNext()
  {
    values = parseRecord();
    return values != null;
  }

  @Override
  public T next()
  {
    if(values == null) {
      // values can be null if hasNext() was not called before this next()
      // this may be the case of a bad iterator usage but can happen
      // attempt to parse next line from characters stream and return null for EOS
      values = parseRecord();
      if(values == null) {
        throw new NoSuchElementException();
      }
    }

    T instance = Classes.newInstance(descriptor.type());

    if(values.size() != descriptor.columns().size()) {
      String message = String.format("CSV values count |%d| does not match columns count |%d|.", values.size(), descriptor.columns().size());
      log.warn(message);
      if(format.strict()) {
        throw new CsvException(message);
      }
    }

    int columnsCount = Math.min(descriptor.columns().size(), values.size());
    for(int i = 0; i < columnsCount; ++i) {
      CsvColumn column = descriptor.columns().get(i);

      // seems like field lookup performance on recent JREs does not justify caching class fields
      // benchmarks did not reveal any differences when using local cache
      Field field = Classes.getOptionalFieldEx(descriptor.type(), column.fieldName());
      if(field == null) {
        String message = String.format("Missing field |%s#%s|", descriptor.type(), column.fieldName());
        log.warn(message);
        if(format.strict()) {
          throw new CsvException(message);
        }
        continue;
      }

      final String columnValue = values.get(i);
      if(columnValue.equalsIgnoreCase(format.nullValue())) {
        // if column value is recognized as null leave field with the default initialized by JVM at instance creation
        continue;
      }

      Object fieldValue = null;
      Format formatter = column.formatter();
      if(formatter != null) {
        try {
          fieldValue = formatter.parse(columnValue);
        }
        catch(ParseException e) {
          String message = String.format("Could not set field value |%s|. Root cause: %s", field, e.getMessage());
          log.error(message);
          if(format.strict()) {
            throw new CsvException(message);
          }
        }
      }
      else {
        try {
          fieldValue = converter.asObject(columnValue, field.getType());
        }
        catch(ConverterException e) {
          String message = String.format("Could not set field value |%s|. Root cause: %s", field, e.getCause() != null ? e.getCause() : e);
          log.error(message);
          if(format.strict()) {
            throw new CsvException(message);
          }
        }
      }

      if(fieldValue != null) {
        Classes.setFieldValue(instance, field, fieldValue);
      }
    }

    // take care to mark values as consumed
    values = null;
    return instance;

  }

  private List<String> parseRecord()
  {
    try {
      for(;;) {
        List<String> values = _parseRecord();

        // header processing
        if(format.header() && !headerProcessed) {
          headerProcessed = true;
          if(values == null) {
            log.info("Empty CSV stream for type |%s|.", descriptor.type());
            return null;
          }
          descriptor.load(values);
          // after header processed continue CSV stream parsing
          continue;
        }

        // at this point null values means EOS
        if(values == null) {
          return null;
        }
        if(!values.isEmpty()) {
          return values;
        }
        // at this point values list is empty; if empty lines are accepted return this empty list
        // otherwise continue reading and parsing source CSV stream
        if(format.emptyLines()) {
          return values;
        }

        // continue CSV stream parsing if values list, that is, CSV record is empty
      }
    }
    catch(IOException e) {
      log.error(e);
      return null;
    }
  }

  private List<String> _parseRecord() throws IOException
  {
    List<String> values = new ArrayList<>();
    StringBuilder valueBuilder = new StringBuilder();
    State state = State.RECORD_START;
    boolean escapeDetected = false;
    boolean closeQuoteDetected = false;

    RECORD: for(;;) {
      int i = reader.read();
      if(i == -1) {
        if(values.isEmpty() && valueBuilder.length() == 0) {
          // if encounter EOS when value builder is empty and there are no values on current record return null, to mark
          // end of record parsing

          // the idea is to consider EOS only if comes on an empty line
          // if current record has some values consider EOS as EOL, in order to process the value just before EOS
          return null;
        }
        // here we have EOS but we have some values on row; handle EOS as EOL
        addValue(values, valueBuilder);
        return values;
      }
      final char c = (char)i;

      switch(state) {
      case RECORD_START:
        if(c == format.comment()) {
          state = State.COMMENT;
          break;
        }

        if(isEOL(c)) {
          // if current row is empty just break row parsing loop
          break RECORD;
        }
        // fall through next value start case

      case VALUE_START:
        if(isEOL(c)) {
          addValue(values, valueBuilder);
          break RECORD;
        }

        if(Character.isWhitespace(c)) {
          // collect white spaces while waiting to decide if complex value
          valueBuilder.append(c);
          break;
        }

        if(c == format.openQuote()) {
          escapeDetected = false;
          closeQuoteDetected = false;
          state = State.COMPLEX_VALUE_READING;
          break;
        }
        else {
          state = State.VALUE_READING;
          // fall through READ_VALUE case
        }

      case VALUE_READING:
        if(isEOL(c)) {
          addValue(values, valueBuilder);
          break RECORD;
        }

        if(c != format.delimiter()) {
          valueBuilder.append(c);
        }
        else {
          addValue(values, valueBuilder);
          valueBuilder.setLength(0);
          state = State.VALUE_START;
        }
        break;

      case COMPLEX_VALUE_READING:
        if(isCloseQuote(c)) {
          closeQuoteDetected = true;
          continue;
        }
        if(!escapeDetected && c == format.escape()) {
          escapeDetected = true;
          continue;
        }
        if(closeQuoteDetected) {
          if(isEOL(c)) {
            addValue(values, valueBuilder);
            break RECORD;
          }
          if(c == format.delimiter()) {
            addValue(values, valueBuilder);
            valueBuilder.setLength(0);
            state = State.VALUE_START;
            continue;
          }
        }
        escapeDetected = false;
        valueBuilder.append(c);
        break;

      // on comment processing just wait for EOL
      case COMMENT:
        if(isEOL(c)) {
          state = State.RECORD_START;
        }
        break;
      }
    }

    return values;
  }

  public Iterator<T> iterator()
  {
    return this;
  }

  public void close() throws IOException
  {
    reader.close();
  }

  private void addValue(List<String> values, StringBuilder builder)
  {
    if(format.trim()) {
      values.add(Strings.trim(builder.toString()));
    }
    else {
      values.add(builder.toString());
    }
  }

  private boolean isCloseQuote(char c) throws IOException
  {
    if(c != format.closeQuote()) {
      return false;
    }
    reader.mark(1);
    int i = reader.read();
    if(i == -1) {
      return true;
    }
    boolean b = (char)i == format.delimiter() || Character.isWhitespace(i);
    reader.reset();
    return b;
  }

  private boolean isEOL(char c) throws IOException
  {
    if(c == '\n') {
      return true;
    }
    if(c != '\r') {
      return false;
    }
    // here we have CR; need to check if followed by LF
    reader.mark(1);
    int i = reader.read();
    if(i == -1) {
      // if EOS consider row complete
      return true;
    }
    if((char)i != '\n') {
      reader.reset();
    }
    return true;
  }

  /**
   * State machine for CSV parser.
   * 
   * @author Iulian Rotaru
   */
  private enum State
  {
    RECORD_START, VALUE_START, VALUE_READING, COMPLEX_VALUE_READING, COMMENT
  }
}
