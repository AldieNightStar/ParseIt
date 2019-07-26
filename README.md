# ParseIt
Easy miniframework, which consist of one class, which will help you to parse complicated `string` structures.

## Usage
```java
ParseIt p = ParseIt.parse("some func(a b c)");
String funcInit = p.readUntilWithoutSkip("(").string;
String whatIsInTheBrackets = p.readBetween("(", ")").string;
```

```java
public class Main {
    public static void main(String[] args){
      String s = "func add(int a, short b) { return a + b; }";
      ParseIt p = ParseIt.parse(s);
      String opName = p.readUntil(" ").string;
      String funcName = p.readUntilWithoutSkipping("(").string;
      String[] args = (p.readBetween("(", ")").string).split(",");
      String body = p.readBetween("{", "}").string;
      assertEquals("func", opName);
      assertEquals("add", funcName);
      assertEquals("int a", args[0].trim());
      assertEquals("short b", args[1].trim());
      assertEquals("return a + b;", body.trim());
    }
}
```

## Install
`pom.xml`
* Add `jitpack.io` repository to list if there is no such
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.AldieNightStar</groupId>
    <artifactId>ParseIt</artifactId>
    <version>33dc2ee033</version>
</dependency>
```