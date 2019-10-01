package js.csv.impl;

import java.util.ArrayList;
import java.util.List;

import js.csv.CsvColumn;
import js.csv.CsvDescriptor;
import js.csv.CsvFormat;
import js.format.Format;
import js.lang.Config;
import js.lang.ConfigException;
import js.util.Classes;
import js.util.Params;
import js.util.Strings;

public class CsvDescriptorImpl<T> implements CsvDescriptor<T>
{
  private final CsvFormatImpl format;
  private final Class<T> type;
  private final List<CsvColumn> columns = new ArrayList<>();

  public CsvDescriptorImpl(Class<T> type)
  {
    Params.notNull(type, "Descriptor type");
    this.format = new CsvFormatImpl();
    this.type = type;
  }

  /**
   * Create CSV format with default properties.
   */
  public CsvDescriptorImpl(CsvFormatImpl format, Class<T> type)
  {
    Params.notNull(format, "CSV format");
    Params.notNull(type, "Descriptor type");
    this.format = format;
    this.type = type;
  }

  @SuppressWarnings("unchecked")
  public CsvDescriptorImpl(Config config) throws ConfigException
  {
    Params.notNull(config, "Descriptor configuration");
    this.format = new CsvFormatImpl();
    this.type = config.getAttribute("class", Class.class);
    if(this.type == null) {
      throw new ConfigException("Invalid CSV descriptor configuration. Missing <class> attribute.");
    }
    config(config);
  }

  @SuppressWarnings("unchecked")
  public CsvDescriptorImpl(Class<T> type, Config config) throws ConfigException
  {
    Params.notNull(type, "Descriptor type");
    Params.notNull(config, "Descriptor configuration");

    this.format = new CsvFormatImpl();
    this.type = config.getAttribute("class", Class.class);
    if(this.type == null) {
      throw new ConfigException("Invalid CSV descriptor configuration. Missing <class> attribute.");
    }
    if(!this.type.equals(type)) {
      throw new ConfigException("Invalid CSV descriptor configuration. Configured <class> attribute does not match descriptor type.");
    }
    config(config);
  }

  private void config(Config config) throws ConfigException
  {
    if(!Classes.isInstantiable(this.type)) {
      throw new ConfigException("Invalid CSV descriptor configuration. Not instantiable bound class.");
    }

    if(config.hasAttribute("delimiter")) {
      format.delimiter(config.getAttribute("delimiter", char.class));
    }
    if(config.hasAttribute("comment")) {
      format.comment(config.getAttribute("comment", char.class));
    }

    if(config.hasAttribute("quote")) {
      format.quote(config.getAttribute("quote", char.class));
    }
    if(config.hasAttribute("open-quote")) {
      char openQuote = config.getAttribute("open-quote", char.class);
      Character closeQuote = config.getAttribute("close-quote", Character.class);
      if(closeQuote == null) {
        throw new ConfigException("Invalid CSV descriptor configuration. Missing <close-quote> attribute.");
      }
      format.quote(openQuote, closeQuote);
    }

    if(config.hasAttribute("escape")) {
      format.escape(config.getAttribute("escape", char.class));
    }
    if(config.hasAttribute("header")) {
      format.header(config.getAttribute("header", boolean.class));
    }
    if(config.hasAttribute("empty-lines")) {
      format.emptyLines(config.getAttribute("empty-lines", boolean.class));
    }
    if(config.hasAttribute("trim")) {
      format.trim(config.getAttribute("trim", boolean.class));
    }
    if(config.hasAttribute("charset")) {
      format.charset(config.getAttribute("charset"));
    }
    if(config.hasAttribute("null-value")) {
      format.nullValue(config.getAttribute("null-value"));
    }
    if(config.hasAttribute("strict")) {
      format.strict(config.getAttribute("strict", boolean.class));
    }

    for(Config column : config.getChildren()) {
      String fieldName = column.getAttribute("field");
      if(fieldName == null) {
        throw new ConfigException("Invalid CSV descriptor configuration. Missing field name attribute from <column> item.");
      }

      Format formatter = null;
      String formatterClassName = column.getAttribute("format");
      if(formatterClassName != null) {
        Class<? extends Format> formatterClass = Classes.forOptionalName(formatterClassName);
        if(formatterClass == null) {
          throw new ConfigException("Invalid CSV descriptor configuration. Missing format class |%s|.", formatterClassName);
        }

        try {
          formatter = Classes.newInstance(formatterClass);
        }
        catch(Throwable t) {
          String cause = t.getCause() != null ? t.getCause().getMessage() : t.getMessage();
          throw new ConfigException("Invalid CSV descriptor configuration. Cannot instantiate formatter |%s|. Root cause: %s", formatterClass, cause);
        }
      }

      columns.add(new CsvColumnImpl(fieldName, formatter));
    }
  }

  @Override
  public CsvFormat format()
  {
    return format;
  }

  @Override
  public Class<T> type()
  {
    return type;
  }

  @Override
  public void load(List<String> header)
  {
    for(String columnName : header) {
      column(Strings.toMemberName(columnName), null);
    }
  }

  @Override
  public CsvDescriptor<T> columns(String... fieldNames)
  {
    for(String fieldName : fieldNames) {
      columns.add(new CsvColumnImpl(fieldName, null));
    }
    return this;
  }

  @Override
  public CsvDescriptor<T> columns(Class<? extends Enum<?>> columnNames)
  {
    for(Enum<?> columnName : columnNames.getEnumConstants()) {
      columns.add(new CsvColumnImpl(Strings.toMemberName(columnName.name()), null));
    }
    return this;
  }

  @Override
  public CsvDescriptor<T> column(String fieldName)
  {
    columns.add(new CsvColumnImpl(fieldName, null));
    return this;
  }

  @Override
  public CsvDescriptor<T> column(String fieldName, Format formatter)
  {
    columns.add(new CsvColumnImpl(fieldName, formatter));
    return this;
  }

  @Override
  public List<CsvColumn> columns()
  {
    return columns;
  }

  // ----------------------------------------------------------------------------------------------

  private static final class CsvColumnImpl implements CsvColumn
  {
    private final String fieldName;
    private final Format formatter;

    public CsvColumnImpl(String fieldName, Format formatter)
    {
      this.fieldName = fieldName;
      this.formatter = formatter;
    }

    @Override
    public String fieldName()
    {
      return fieldName;
    }

    @Override
    public Format formatter()
    {
      return formatter;
    }
  }
}
