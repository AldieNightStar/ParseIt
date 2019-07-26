package haxidenti.parseit;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParseTest {

    @Test
    public void readToEndTest01() {
        String expected = "func a(int b) {}";
        ParseIt p = ParseIt.parse(expected);
        Result result = p.readToEnd();
        assertEquals(expected, result.string);
    }

    @Test
    public void readToEndTest02() {
        String text = "aaa bbb ccc";
        ParseIt p = ParseIt.parse(text);
        p.readUntil(" ");
        Result result = p.readToEnd();
        assertEquals("bbb ccc", result.string);
    }

    @Test
    public void readUntilTest01() {
        ParseIt p = parse("ccc|ddd");
        Result result = p.readUntil("|");
        assertEquals("ccc", result.string);
        assertEquals("|", result.skipped);
    }

    @Test
    public void readUntilTest02() {
        ParseIt p = parse("ccc");
        Result result = p.readUntil("|");
        assertTrue(result.hasError());
    }

    @Test
    public void readUntilTest03() {
        ParseIt p = parse("xxx*&");
        Result result = p.readUntil("&", "*");
        assertEquals("xxx", result.string);
        assertEquals("*", result.skipped);
    }

    @Test
    public void readBetweenTest01() {
        String text = "aaa<zzz>";
        ParseIt p = ParseIt.parse(text);
        String expected = "zzz";
        Result result = p.readBetween("<", ">");
        assertEquals(expected, result.string);
    }

    @Test
    public void readBetweenTest02() {
        Result result = parse("aaa((some Text))")
                .readBetween("((", "))");
        assertEquals("some Text", result.string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readBetweenTest03() {
        parse("\"z\"").readBetween("\"", "\"");
    }

    @Test
    public void readBetweenTest04() {
        Result result = parse("aaa ( zzzz").readBetween("(", ")");
        assertTrue(result.hasError());
    }

    @Test
    public void readBetweenTest05() {
        Result result = parse("aaa ) zzz")
                .readBetween("(", ")");
        assertTrue(result.hasError());
    }

    @Test
    public void readBetweenTest06() {
        Result result = parse("aaa )( readFromTest01")
                .readBetween("(", ")");
        assertTrue(result.hasError());
    }

    @Test
    public void readBetweenTest07() {
        Result result = parse("aaa")
                .readBetween("<", ">");
        assertTrue(result.hasError());
    }

    @Test
    public void readFromTest01() {
        Result result = parse("xxx*uinet***").readFrom("*");
        assertEquals("uinet***", result.string);
        assertEquals("*", result.skipped);
    }

    @Test
    public void readFromTest02() {
        Result result = parse("xxx#$uinet***").readFrom("$", "#");
        assertEquals("$uinet***", result.string);
        assertEquals("#", result.skipped);
    }

    @Test
    public void readBetweenQuotesTest01() {
        Result result = parse("astie \"zig zag\" zzz").readBetweenQuotes("\"");
        assertEquals("zig zag", result.string);
        assertEquals("\"", result.skipped);
    }

    @Test
    public void readBetweenQuotesTest02() {
        ParseIt p = parse("astie \"zig zag\" zzz \"Monku\"");
        Result result = p.readBetweenQuotes("\"");
        assertEquals("zig zag", result.string);
        result = p.readBetweenQuotes("\"");
        assertEquals("Monku", result.string);
        assertEquals("\"", result.skipped);
    }

    @Test
    public void readBetweenQuotesTest03() {
        ParseIt p = parse("xxx\"123\"zinto");
        Result result = p.readBetweenQuotes("\"");
        assertEquals("123", result.string);
        result = p.readToEnd();
        assertEquals("zinto", result.string);
    }

    @Test
    public void skiptTest01() {
        ParseIt p = parse("--minus");
        String s = p.skip(2);
        assertEquals("--", s);
        assertEquals("minus", p.readToEnd().string);
    }

    @Test
    public void fullTest() {
        String s = "func add(int a, short b) { return a + b; }";
        ParseIt p = parse(s);
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

    private static ParseIt parse(String text) {
        return ParseIt.parse(text);
    }
}
