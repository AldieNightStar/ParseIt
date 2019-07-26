package haxidenti.parseit;

public class Result {
    public String string, skipped;
    public Exception exception;

    public Result(String string) {
        this.string = nonNull(string);
    }

    public Result(String string, String skipped) {
        this(nonNull(string));
        this.skipped = skipped;
    }

    public Result(Exception eception) {
        this("", "");
        this.exception = eception;
    }

    public boolean hasError() {
        return exception != null;
    }

    private static String nonNull(String s) {
        return (s != null) ? s : "";
    }
}
