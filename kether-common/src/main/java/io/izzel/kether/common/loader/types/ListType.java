package io.izzel.kether.common.loader.types;

import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.loader.QuestReader;
import io.izzel.kether.common.util.LocalizedException;

import java.util.ArrayList;
import java.util.List;

final class ListType<T> implements ArgType<List<T>> {

    private final ArgType<T> elementType;

    ListType(ArgType<T> elementType) {
        this.elementType = elementType;
    }

    @Override
    public List<T> read(QuestReader reader) throws LocalizedException {
        reader.expect("[");
        ArrayList<T> list = new ArrayList<>();
        while (reader.hasNext() && reader.peek() != ']') {
            list.add(reader.next(elementType));
        }
        list.trimToSize();
        reader.expect("]");
        return list;
    }
}
