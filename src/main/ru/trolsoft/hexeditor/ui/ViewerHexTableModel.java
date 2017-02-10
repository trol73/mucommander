package ru.trolsoft.hexeditor.ui;

import ru.trolsoft.hexeditor.data.AbstractByteBuffer;
import ru.trolsoft.utils.StrUtils;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;

/**
 * The table model used by the <code>JTable</code> in the hex viewer/editor.
 */
public class ViewerHexTableModel extends AbstractTableModel {

    protected final boolean[] VISIBLE_SYMBOLS = new boolean[256];
    protected long fileSize;

    private final int hexDataColumns;
    protected final AbstractByteBuffer buffer;




    public ViewerHexTableModel(AbstractByteBuffer byteBuffer, int columns) {
        this.buffer = byteBuffer;
        for (int i = 0; i < VISIBLE_SYMBOLS.length; i++) {
            VISIBLE_SYMBOLS[i] = i > 0;
        }
        this.hexDataColumns = columns;
    }

    public ViewerHexTableModel(AbstractByteBuffer byteBuffer) {
        this(byteBuffer, 32);
    }


    public void load() throws IOException {
        this.fileSize = buffer.getFileSize();
        if (fileSize > 0) {
            buffer.getByte(0);
        }
    }


    /**
     * Get file size
     * @return
     */
    public long getSize() {
        return fileSize;
    }


    @Override
    public int getRowCount() {
        long fs = getSize();
        int rows = (int)(fs / hexDataColumns);
        if (fs % hexDataColumns > 0) {
            rows++;
        }
        return rows;
    }

    @Override
    public int getColumnCount() {
        return hexDataColumns + 2;  // offset, data columns, ASCII dump
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            // offset
            return StrUtils.dwordToHexStr(getRowOffset(rowIndex));
        } else if (columnIndex == hexDataColumns + 1) {
            // dump
            try {
                return getAsciiDump(rowIndex * hexDataColumns);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            long fileOffset = rowIndex * hexDataColumns + columnIndex - 1;
            if (fileOffset >= fileSize) {
                return "";
            }
            try {
                return StrUtils.byteToHexStr(buffer.getByte(fileOffset));
            } catch (IOException e) {
                e.printStackTrace();
                return "xx";
            }
        }
    }


    protected long getRowOffset(int row) {
        return row * hexDataColumns;
    }

    private String getAsciiDump(long fileOffset) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <  hexDataColumns; i++) {
            long offset = fileOffset + i;
            if (offset >= fileSize) {
                sb.append(' ');
            } else {
                int b = buffer.getByte(offset) & 0xff;// getData()[i + bufferOffset] & 0xff;
                char ch = (char)b;
                if (!VISIBLE_SYMBOLS[b]) {
                    ch = ' ';
                }
                sb.append(ch);
            }
        }

        return sb.toString();
    }


    public int getNumberOfHexColumns() {
        return hexDataColumns;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Offset";
        } else if (column == hexDataColumns + 1) {
            return "ASCII dump";
        }
        return "+" + Integer.toHexString(column-1).toUpperCase();
    }

    void setAsciiCharVisible(char ch, boolean visible) {
        VISIBLE_SYMBOLS[ch] = visible;
    }

}
