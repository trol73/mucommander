package ru.trolsoft.hexeditor.events;

public interface SelectionChangeListener {
    void onSelectionChanged(long fromAddress, long toAddress);
}
