package js.csv.impl;

import static js.util.Params.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.csv.CharEnum;
import js.csv.CsvColumn;
import js.csv.CsvComment;
import js.csv.CsvDelimiter;
import js.csv.CsvDescriptor;
import js.csv.CsvEscape;
import js.csv.CsvException;
import js.csv.CsvFormat;
import js.csv.CsvQuote;
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
    notNull(type, "Descriptor type");
    this.format = new CsvFormatImpl();
    this.type = type;
  }

  /**
   * Create CSV format with default properties.
   */
  public CsvDescriptorImpl(CsvFormatImpl format, Class<T> type)
  {
    notNull(format, "CSV format");
    notNull(type, "Descriptor type");
    this.format = format;
    this.type = type;
  }

  public CsvDescriptorImpl(Config config) throws ConfigException
  {
    notNull(config, "Descriptor configuration");
    this.format = new CsvFormatImpl();
    this.type = getType(config);
    config(config);
  }

  public CsvDescriptorImpl(Class<T> type, Config config) throws ConfigException
  {
    notNull(type, "Descriptor type");
    notNull(config, "Descriptor configuration");

    this.format = new CsvFormatImpl();
    this.type = getType(config);
    if(!this.type.equals(type)) {
      throw new ConfigException("Invalid CSV descriptor configuration. Configured <class> attribute does not match descriptor type.");
    }
    config(config);
  }

  private void config(Config config) throws ConfigException
  {
    if(config.hasAttribute("delimiter")) {
      format.delimiter(charEnum(config, "delimiter", CsvDelimiter.class).value());
    }
    if(config.hasAttribute("comment")) {
      format.comment(charEnum(config, "comment", CsvComment.class).value());
    }

    if(config.hasAttribute("quote")) {
      CharEnum charEnum = charEnum(config, "quote", CsvQuote.class);
      format.quote(charEnum.value(0), charEnum.value(1));
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
      format.escape(charEnum(config, "escape", CsvEscape.class).value());
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
    if(!columns.isEmpty()) {
      return;
    }

    String columns = Strings.join(header, ',');
    NameConverter converter = JavaName.accept(columns) ? new JavaName() : new NonJavaName();

    for(String columnName : header) {
      String fieldName = converter.fieldName(columnName);
      if(Classes.getOptionalFieldEx(type, fieldName) == null) {
        throw new CsvException("Field |%s| not found on type |%s|.", fieldName, type);
      }
      column(fieldName, null);
    }
  }

  @Override
  public CsvDescriptor<T> columns(String... fieldNames)
  {
    columns.clear();
    for(String fieldName : fieldNames) {
      if(format.strict() && Classes.getOptionalFieldEx(type, fieldName) == null) {
        throw new CsvException("Field |%s| not found on type |%s|.", fieldName, type);
      }
      columns.add(new CsvColumnImpl(fieldName, null));
    }
    return this;
  }

  @Override
  public CsvDescriptor<T> columns(Class<? extends Enum<?>> columnNames)
  {
    Params.notNull(columnNames, "Column names");
    NameConverter converter = new NonJavaName();

    columns.clear();
    for(Enum<?> columnName : columnNames.getEnumConstants()) {
      String fieldName = converter.fieldName(columnName.name());
      if(format.strict() && Classes.getOptionalFieldEx(type, fieldName) == null) {
        throw new CsvException("Field |%s| not found on type |%s|.", fieldName, type);
      }
      columns.add(new CsvColumnImpl(fieldName, null));
    }
    return this;
  }

  @Override
  public CsvDescriptor<T> column(String fieldName)
  {
    Params.notNullOrEmpty(fieldName, "Field name");
    if(format.strict() && Classes.getOptionalFieldEx(type, fieldName) == null) {
      throw new CsvException("Field |%s| not found on type |%s|.", fieldName, type);
    }
    columns.add(new CsvColumnImpl(fieldName, null));
    return this;
  }

  @Override
  public CsvDescriptor<T> column(String fieldName, Format formatter)
  {
    Params.notNullOrEmpty(fieldName, "Field name");
    if(format.strict() && Classes.getOptionalFieldEx(type, fieldName) == null) {
      throw new CsvException("Field |%s| not found on type |%s|.", fieldName, type);
    }
    columns.add(new CsvColumnImpl(fieldName, formatter));
    return this;
  }

  @Override
  public List<CsvColumn> columns()
  {
    return columns;
  }

  // ----------------------------------------------------------------------------------------------

  private static <T> Class<T> getType(Config config) throws ConfigException
  {
    String className = config.getAttribute("class");
    if(className == null) {
      throw new ConfigException("Invalid CSV descriptor configuration. Missing <class> attribute.");
    }

    Class<T> type;
    try {
      type = Classes.forNameEx(className);
    }
    catch(ClassNotFoundException e) {
      throw new ConfigException("Invalid CSV descriptor configuration. Class |%s| not found.", className);
    }

    if(!Classes.isInstantiable(type)) {
      throw new ConfigException("Invalid CSV descriptor configuration. Not instantiable class |%s|.", type);
    }
    return type;
  }

  private static <E extends Enum<E>> CharEnum charEnum(Config config, String attr, Class<E> enumType) throws ConfigException
  {
    class Char implements CharEnum
    {
      private char c;

      public Char(char c)
      {
        this.c = c;
      }

      @Override
      public char value(int... index)
      {
        return c;
      }
    }

    String value = config.getAttribute(attr);
    if(value.length() == 1) {
      return new Char(value.charAt(0));
    }
    try {
      return (CharEnum)Enum.valueOf(enumType, value);
    }
    catch(IllegalArgumentException e) {
      throw new ConfigException(e.getMessage());
    }
  }

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

  private interface NameConverter
  {
    String fieldName(String name);
  }

  private static class JavaName implements NameConverter
  {
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_,]+$");

    public static boolean accept(String columns)
    {
      Matcher matcher = PATTERN.matcher(columns);
      return matcher.find();
    }

    @Override
    public String fieldName(String fieldName)
    {
      return fieldName;
    }
  }

  private static class NonJavaName implements NameConverter
  {
    @Override
    public String fieldName(String columnName)
    {
      List<String> words = Strings.split(columnName, '-', '_', ' ');
      StringBuilder sb = new StringBuilder();

      boolean first = true;
      for(String word : words) {
        if(word.isEmpty()) {
          continue;
        }
        if(first) {
          first = false;
          sb.append(word.toLowerCase());
          continue;
        }

        sb.append(Character.toUpperCase(word.charAt(0)));
        sb.append(word.substring(1).toLowerCase());
      }
      return sb.toString();
    }
  }
}
