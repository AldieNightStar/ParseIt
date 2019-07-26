package haxidenti.parseit;

import java.util.Objects;

public class Position {
    public int index, length, mark;
    public String chunk;

    public Position(int pos, int length) {
        this.index = pos;
        this.length = length;
        this.chunk = "";
    }

    public Position(int index, int length, int mark) {
        this(index, length);
        this.mark = mark;
    }

    public Position(int index, int length, int mark, String chunk) {
        this(index, length, mark);
        this.chunk = chunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position posSize = (Position) o;
        return index == posSize.index &&
                mark == posSize.mark;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, mark);
    }
}
