package js.csv.impl.fixture;

import js.util.Strings;

public class Person
{
  public String name;
  public String address;

  public Person()
  {
  }

  public Person(String name, String address)
  {
    this.name = name;
    this.address = address;
  }

  @Override
  public String toString()
  {
    return Strings.toString(name, address);
  }
}