package haxidenti.parseit;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseIt {
    private String str;
    private int pos;
    private String escapeOperator;

    private ParseIt() {
        escapeOperator = "\\";
    }

    public static ParseIt parse(String s) {
        ParseIt p = new ParseIt();
        p.str = s;
        p.pos = 0;
        return p;
    }

    public void setEscapeOperator(String escapeOperator) {
        this.escapeOperator = escapeOperator;
    }

    public void decrementPos(int num) {
        pos -= num;
        if (pos < 0) pos = 0;
    }

    public void setString(String str) {
        this.str = str;
        this.pos = 0;
    }

    public Result readToEnd() {
        return new Result(str.substring(pos));
    }

    public Result readUntil(String... strings) {
        Set<Position> posesSet = new HashSet<>();
        for (String currentString : strings) {
            int index = str.indexOf(currentString, pos);
            if (index < pos) continue;
            posesSet.add(new Position(index, currentString.length()));
        }
        Position closest = posesSet.stream()
                .min(Comparator.comparingInt(p -> p.index))
                .orElse(null);
        if (closest == null) return new Result(new Exception("No such symbols"));
        String result = str.substring(pos, closest.index);
        pos = closest.index + closest.length;
        String skipped = str.substring(pos - closest.length, pos);
        return new Result(result, skipped);
    }

    public Result readUntilWithoutSkipping(String... strings) {
        Result result = readUntil(strings);
        this.pos -= result.skipped.length();
        result.skipped = "";
        return result;
    }

    public String skip(int quantity) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            try {
                builder.append(str.charAt(pos++));
            } catch (Exception e) {
                break;
            }
        }
        return builder.toString();
    }

    public Result readFrom(String... strings) {
        Set<Position> posesSet = new HashSet<>();
        for (String currentString : strings) {
            int index = str.indexOf(currentString, pos);
            if (index < pos) continue;
            posesSet.add(new Position(index, currentString.length(), 0, currentString));
        }
        Position closest = posesSet.stream().min(Comparator.comparingInt(p -> p.index))
                .orElse(null);
        if (closest == null) return null;
        pos = closest.index + closest.length;
        return new Result(str.substring(pos), closest.chunk);
    }

    /**
     * Read between str1 and str2 inside parsed string<br>
     * <b>Note:</b> str1 must not be equal to str2. Use {@link #readBetweenQuotes(String)} instead.
     *
     * @param str1
     * @param str2
     * @return
     */
    public Result readBetween(String str1, String str2) {
        Set<Position> poses = getPoses(str, str1, 1, pos);
        if (str1 != null && str1.equals(str2)) throw new IllegalArgumentException("str1 can't be equal to str2");
        poses.addAll(getPoses(str, str2, 2, pos));
        int deepness = 0;
        int pos1 = 0;
        int pos2 = 0;
        boolean firstPosAlreadySet = false;
        if (!contains(str, str1, str2)) return new Result(new Exception("no str1 or str2 found in parsing string"));
        List<Position> allPositions = poses.stream()
                .sorted(Comparator.comparingInt(p -> p.index))
                .collect(Collectors.toList());
        for (Position pos : allPositions) {
            if (!firstPosAlreadySet) {
                pos1 = pos.index + pos.length;
                firstPosAlreadySet = true;
                continue;
            }
            if (pos.mark == 1) {
                deepness++;
                continue;
            }
            if (pos.mark == 2) {
                if (deepness > 0) {
                    deepness--;
                    continue;
                } else {
                    pos2 = pos.index;
                    this.pos = pos2 + pos.length;
                    break;
                }
            }
        }
        try {
            return new Result(str.substring(pos1, pos2));
        } catch (Exception e) {
            return new Result(e);
        }
    }

    public Result readBetweenQuotes(String quote) {
        List<Position> poses = getPoses(this.str, quote, 0, this.pos).stream()
                .sorted(Comparator.comparingInt(p -> p.index))
                .collect(Collectors.toList());
        boolean willBeFirst = true;
        Position pos1 = null;
        Position pos2 = null;
        for (Position pos : poses) {
            if (willBeFirst) {
                pos1 = pos;
                willBeFirst = false;
            } else {
                pos2 = pos;
                break;
            }
        }
        if (pos1 == null || pos2 == null) return new Result(new RuntimeException("There are no quotes"));
        Result result = new Result(this.str.substring(pos1.index + pos1.length, pos2.index), quote);
        this.pos = pos2.index + pos2.length;
        return result;
    }

    public Escaped escape(String... strings) {
        Escaped escaped = new Escaped(str.substring(pos));
        int i = 0;
        for (String s : strings) {
            escaped.string = escaped.string.replace(s, "$$(" + i + ")$$");
            escaped.map.put(i, s);
            i++;
        }
        return escaped;
    }

    public Escaped escapeQuoted(String quote) {
        AtomicBoolean escapeOperatorPresent = new AtomicBoolean(false);
        List<Position> poses = getPoses(str, quote, 0, pos).stream()
                .sorted(Comparator.comparingInt(p -> p.index))
                .filter(p -> {
                    if (substr(str, p.index - escapeOperator.length(), p.index).equals(escapeOperator)) {
                        escapeOperatorPresent.set(true);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        boolean setStartPos = true;
        Position startPos = null;
        Position endPos = null;
        String textBetween = "";
        int cnt = 0;
        Escaped escaped = new Escaped(str);
        for (Position pos : poses) {
            if (setStartPos) {
                startPos = pos;
            } else {
                endPos = pos;
                textBetween = str.substring(startPos.index + startPos.length, endPos.index);
                escaped.map.put(cnt, textBetween);
                escaped.string = escaped.string.replace(quote + textBetween + quote, "$$(" + cnt + ")$$");
                if (escapeOperatorPresent.get()) escaped.map.put(cnt, escaped.map.get(cnt).replace(escapeOperator, ""));
                cnt++;
            }
            setStartPos = !setStartPos;
        }
        return escaped;
    }

    public boolean validate(String string, String delimiter) {
        int oldPos = pos;
        String[] arr = (string).split(Pattern.quote(delimiter));
        for (String s : arr) {
            Result result = readUntil(s);
            if (result.hasError()) return false;
        }
        pos = oldPos;
        return true;
    } // p.validate("call *(*);")

    public boolean prefixOfNext(String prefix) {
        if (prefix == null || prefix.isEmpty()) return false;
        String next = substr(str, pos, pos + prefix.length());
        return next.startsWith(prefix);
    }

    public boolean skipPrefix(String prefix) {
        if (prefixOfNext(prefix)) {
            pos += prefix.length();
            return true;
        }
        return false;
    }

    public static Escaped escapeQuoted(String code, String quote) {
        return ParseIt.parse(code).escapeQuoted(quote);
    }

    private static String replaceSubstring(String str, int start, int end, String dest) {
        return str.substring(0, start) + dest + str.substring(end);
    }

    private static Set<Position> getPoses(String str, String dest, int mark, int startPos) {
        Set<Position> poses = new HashSet<>();
        int destLength = dest.length();
        int index = startPos;
        while (true) {
            index = str.indexOf(dest, index);
            if (index < 0) break;
            poses.add(new Position(index, destLength, mark, dest));
            index += destLength;
        }
        return poses;
    }

    private static boolean contains(String line, String... str) {
        for (String s : str) {
            if (!line.contains(s)) return false;
        }
        return true;
    }

    private static String substr(String str, int startPos, int endPos) {
        try {
            return str.substring(startPos, endPos);
        } catch (Exception e) {
            return "";
        }
    }

    private static int getLessNumberInList(List<Integer> ints, int number) {
        List<Integer> list = ints.stream()
                .filter(i -> i < number)
                .sorted(Comparator.comparingInt(i -> i))
                .collect(Collectors.toList());
        if (list.isEmpty()) return 0;
        return list.get(0);
    }

    private static <T> List<Integer> mapToIntList(List<T> list, Function<T, Integer> func) {
        return list.stream().map(func).collect(Collectors.toList());
    }

}
