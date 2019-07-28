package haxidenti.parseit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Escaped {
    public String string;
    public Map<Integer, String> map;

    public Escaped(String escaped) {
        string = escaped;
        map = new HashMap<>();
    }

    public String unescape() {
        String[] str = new String[]{string};
        map.forEach((k, v) -> {
            str[0] = str[0].replace("$$(" + k + ")$$", v);
        });
        return str[0];
    }

    public String unescapeWithQuotes(String code, String quote) {
        String[] str = new String[]{code};
        map.forEach((k, v) -> {
            str[0] = str[0].replace("$$(" + k + ")$$", quote + v + quote);
        });
        return str[0];
    }

    public String unescapeWithQuotes(String quote) {
        return unescapeWithQuotes(this.string, quote);
    }

    public String unescape(String s) {
        Escaped escaped = new Escaped(s);
        escaped.map = map;
        return escaped.unescape();
    }

    public boolean isEmpty() {
        return string == null || string.isEmpty();
    }

    public List<String> getEscapedList() {
        return map.entrySet().stream()
                .map(elem -> elem.getValue())
                .collect(Collectors.toList());
    }

    public String getFirstEscaped() {
        try {
            return getEscapedList().get(0);
        } catch (Exception e) {
            return "";
        }
    }
}
