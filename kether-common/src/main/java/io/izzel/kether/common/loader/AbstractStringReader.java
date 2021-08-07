package io.izzel.kether.common.loader;

public abstract class AbstractStringReader {

    protected final char[] arr;
    protected int index = 0;
    protected int mark = 0;

    public AbstractStringReader(char[] arr) {
        this.arr = arr;
    }

    public char peek() {
        if (index < arr.length) {
            return arr[index];
        } else {
            throw LoadError.EOF.create();
        }
    }

    public char peek(int n) {
        if (index + n < arr.length) {
            return arr[index + n];
        } else {
            throw LoadError.EOF.create();
        }
    }

    public int getIndex() {
        return index;
    }

    public int getMark() {
        return mark;
    }

    public boolean hasNext() {
        skipBlank();
        return index < arr.length;
    }

    public void mark() {
        this.mark = index;
    }

    public void reset() {
        this.index = mark;
    }

    public String nextToken() {
        if (!hasNext()) {
            throw LoadError.EOF.create();
        }
        int begin = index;
        while (index < arr.length && !Character.isWhitespace(arr[index])) {
            index++;
        }
        return new String(arr, begin, index - begin);
    }

    protected void skip(int n) {
        index += n;
    }

    protected void skipBlank() {
        while (index < arr.length) {
            if (Character.isWhitespace(arr[index])) {
                index++;
            } else if (index + 1 < arr.length && arr[index] == '/' && arr[index + 1] == '/') {
                while (index < arr.length && arr[index] != '\n' && arr[index] != '\r') {
                    index++;
                }
            } else {
                break;
            }
        }
    }

    protected void expect(String value) {
        String element = nextToken();
        if (!element.equals(value)) {
            failExpect(value, element);
        }
    }

    protected void failExpect(String expect, String got) {
        throw LoadError.NOT_MATCH.create(expect, got);
    }
}
