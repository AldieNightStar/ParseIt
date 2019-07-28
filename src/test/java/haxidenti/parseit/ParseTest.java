package haxidenti.parseit;

import org.junit.Test;

import java.util.regex.Pattern;

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
        ParseIt p = parse("aaa<zzz>");
        Result result = p.readBetween("<", ">");
        assertEquals("zzz", result.string);
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
    public void readBetweenQuotesTest04() {
        ParseIt p = parse("xxx");
        Result result = p.readBetweenQuotes("\"");
        assertTrue(result.hasError());
    }

    @Test
    public void readBetweenQuotesTest05() {
        ParseIt p = parse("xxx\"");
        Result result = p.readBetweenQuotes("\"");
        assertTrue(result.hasError());
    }

    @Test
    public void skipTest() {
        ParseIt p = parse("--minus");
        String s = p.skip(2);
        assertEquals("--", s);
        assertEquals("minus", p.readToEnd().string);
    }

    @Test
    public void fullTest01() {
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

    @Test
    public void fullTest02() throws Exception {
        String code = "register(\"a(123)\", \",,Irvin \\\"26\\\",,\", \"))))\");";
        Escaped escaped = ParseIt.escapeQuoted(code, "\"");
        code = escaped.string;

        ParseIt p = parse(code);
        String funcName = p.readUntilWithoutSkipping("(").string;
        String[] args = p.readBetween("(", ")").string.split(Pattern.quote(","));
        for (int i = 0; i < args.length; i++) {
            args[i] = escaped.unescape(args[i].trim());
        }
        String lastChar = p.readUntil(";").skipped;

        assertEquals("register", funcName);
        assertEquals("a(123)", args[0]);
        assertEquals(",,Irvin \"26\",,", args[1]);
        assertEquals("))))", args[2]);
        assertEquals(";", lastChar);
    }

    @Test
    public void escapeTest() {
        ParseIt p = parse("a123");
        Escaped escaped = p.escape("1", "2", "3");
        String unescaped = escaped.unescape();
        assertEquals("a123", unescaped);
    }

    @Test
    public void escapeInQuotes01() {
        ParseIt p = parse("t1: \"dsadasdas\" t2: \"zzzzzz\" t3: \"zx\"");
        Escaped escaped = p.escapeQuoted("\"");
        assertEquals("t1: $$(0)$$ t2: $$(1)$$ t3: $$(2)$$", escaped.string);
        assertEquals("t1: dsadasdas t2: zzzzzz t3: zx", escaped.unescape());
        assertEquals("zzzzzz", escaped.unescape("$$(1)$$"));
    }

    @Test
    public void escapeInQuotes02() {
        ParseIt p = parse("t1: \"123\\\"321\"");
        Escaped escaped = p.escapeQuoted("\"");
        assertEquals("123\"321", escaped.getFirstEscaped());
    }

    @Test
    public void escapeInQuotes03() {
        Escaped escaped = ParseIt.escapeQuoted("live !alone!", "!");
        assertEquals("he is !alone!", escaped.unescapeWithQuotes("he is $$(0)$$", "!"));
        assertEquals("he is noone", escaped.unescapeWithQuotes("he is noone", "!"));
    }

    @Test
    public void prefixTest01() {
        ParseIt p = parse("xxPREFIXpoe");
        assertTrue(p.skipPrefix("xx"));
        assertTrue(p.prefixOfNext("PREFIX"));
    }

    @Test
    public void prefixTest02() {
        ParseIt p = parse("abc123*&^002IhorFox-end");
        assertTrue(p.skipPrefix("abc"));
        assertTrue(p.skipPrefix("123"));
        assertTrue(p.skipPrefix("*&^"));
        assertFalse(p.skipPrefix("000"));
        assertFalse(p.skipPrefix("001"));
        assertTrue(p.skipPrefix("002"));
        assertTrue(p.skipPrefix("IhorFox"));
        assertFalse(p.skipPrefix("easter"));
        assertTrue(p.skipPrefix("-end"));
    }

    @Test
    public void prefixTest03() {
        ParseIt p = parse("zinto");
        assertTrue(p.skipPrefix("zin"));
        assertFalse(p.skipPrefix("tozzzz"));
        assertFalse(p.skipPrefix("VOID"));
    }

    @Test
    public void prefixTest04() {
        ParseIt p = parse("x");
        assertTrue(p.skipPrefix("x"));
        assertFalse(p.skipPrefix(""));
    }

    @Test
    public void validateTest01() {
        ParseIt p = parse("name = Fox and Aldie end");
        assertTrue(p.validate("name = * and * end", "*"));
    }

    @Test
    public void validateTest02() {
        ParseIt p = parse("name = Fox and Aldie end");
        assertFalse(p.validate("name = Ilie and * end", "*"));
    }

    @Test
    public void validateTest03() {
        ParseIt p = parse("call aaa(bbb, ccc);");
        assertTrue(p.validate("call *(*);", "*"));
    }

    @Test
    public void validateTest04() {
        ParseIt p = parse(" a(zen que) biz");
        p.skip(1);
        assertTrue(p.validate("*(*)", "*"));
        assertFalse(p.validate("*=*", "*"));
        p.setString("dirt = Heller");
        assertFalse(p.validate("*(*)", "*"));
        assertTrue(p.validate("*=*", "*"));
    }

    private static ParseIt parse(String text) {
        return ParseIt.parse(text);
    }
}
