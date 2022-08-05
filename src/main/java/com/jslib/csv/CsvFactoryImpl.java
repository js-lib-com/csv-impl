package com.jslib.csv;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.jslib.api.csv.CsvDescriptor;
import com.jslib.api.csv.CsvFactory;
import com.jslib.api.csv.CsvReader;
import com.jslib.api.csv.CsvWriter;
import com.jslib.lang.Config;
import com.jslib.lang.ConfigException;

public class CsvFactoryImpl implements CsvFactory
{
  @Override
  public <T> CsvDescriptor<T> getDescriptor(Class<T> type)
  {
    return new CsvDescriptorImpl<T>(type);
  }

  @Override
  public <T> CsvDescriptor<T> getDescriptor(Class<T> type, Config config) throws ConfigException
  {
    return new CsvDescriptorImpl<T>(type, config);
  }

  @Override
  public <T> CsvDescriptor<T> getDescriptor(Config config) throws ConfigException
  {
    return new CsvDescriptorImpl<T>(config);
  }

  @Override
  public <T> CsvReader<T> getReader(CsvDescriptor<T> descriptor, Reader reader)
  {
    return new CsvReaderImpl<T>(descriptor, reader);
  }

  @Override
  public <T> CsvReader<T> getReader(CsvDescriptor<T> descriptor, InputStream stream)
  {
    return new CsvReaderImpl<T>(descriptor, stream);
  }

  @Override
  public <T> CsvWriter<T> getWriter(CsvDescriptor<T> descriptor, Writer writer)
  {
    return new CsvWriterImpl<T>(descriptor, writer);
  }

  @Override
  public <T> CsvWriter<T> getWriter(CsvDescriptor<T> descriptor, OutputStream stream)
  {
    return new CsvWriterImpl<T>(descriptor, stream);
  }
}
