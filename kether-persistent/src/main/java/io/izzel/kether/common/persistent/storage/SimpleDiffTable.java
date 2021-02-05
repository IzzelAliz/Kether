package io.izzel.kether.common.persistent.storage;

import io.izzel.kether.common.api.ActionProperties;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.persistent.QuestTableStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntStack;

import java.util.function.IntSupplier;

class SimpleDiffTable implements QuestTableStore.QuestTable.BlockTable {

    private final IntSupplier nextIdSupplier;
    private final IntList oldData;
    private final IntList newData;
    private final Int2IntMap redirects = new Int2IntOpenHashMap();
    private final IntStack stack = new IntArrayList();
    private int index = 0;

    SimpleDiffTable(IntSupplier nextIdSupplier, IntList oldData) {
        this.nextIdSupplier = nextIdSupplier;
        this.oldData = oldData;
        this.newData = new IntArrayList(oldData.size());
    }

    protected int nextId() {
        return nextIdSupplier.getAsInt();
    }

    @Override
    public void pushAction() {
        stack.push(index);
    }

    @Override
    public void transferTo(ParsedAction<?> before, ParsedAction<?> after) {

    }

    @Override
    public void drop(ParsedAction<?> action) {

    }

    @Override
    public void insert(ParsedAction<?> action) {

    }

    @Override
    public void assign(ParsedAction<?> action) {
        action.set(ActionProperties.ADDRESS, currentAddress());
    }

    private int currentAddress() {
        IntArrayList list = new IntArrayList();
        if (index < oldData.size()) {
            return oldData.get(index++);
        } else {
            return -1;
        }
    }
}
