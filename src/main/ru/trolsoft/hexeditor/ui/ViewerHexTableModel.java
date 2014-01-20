package ru.trolsoft.hexeditor.ui;

import ru.trolsoft.hexeditor.data.FileByteBuffer;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;

/**
 * The table model used by the <code>JTable</code> in the hex viewer/editor.
 */
public class ViewerHexTableModel extends AbstractTableModel {

    private static final char[] HEX_CHAR_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String[] STRING_OF_ZERO = {"", "0", "00", "000", "0000", "00000", "000000", "0000000", "00000000", "000000000", "0000000000"};

    protected static final String[] HEX_BYTE_STRINGS = new String[256];

    protected final boolean[] VISIBLE_SYMBOLS = new boolean[256];
    protected long fileSize;

    private int hexDataColumns = 16*2;
    protected final FileByteBuffer buffer;




    public ViewerHexTableModel(String fileName, String fileMode) {
        buffer = new FileByteBuffer(fileName, fileMode);
        for (int i = 0; i < VISIBLE_SYMBOLS.length; i++) {
            VISIBLE_SYMBOLS[i] = i > 0;
        }
    }

    public void load() throws IOException {
        buffer.load();
        fileSize = buffer.getFileSize();
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
            String result = Long.toHexString(getRowOffset(rowIndex));
            return STRING_OF_ZERO[8-result.length()] + result;
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
                return byteToHex(buffer.getByte(fileOffset));
            } catch (IOException e) {
                e.printStackTrace();
                return "xx";
            }
        }
    }

    protected static String byteToHex(byte b) {
        int v = b & 0xFF;
        String result = HEX_BYTE_STRINGS[v];
        if (result == null) {
            result = Character.toString(HEX_CHAR_ARRAY[v >>> 4]) + HEX_CHAR_ARRAY[v & 0x0f];
            HEX_BYTE_STRINGS[v] = result;
        }
        return result;
    }


    protected static String bytesToHex(byte[] bytes, int offset, int size) {
        char[] hexChars = new char[size * 2];
        for (int i = offset; i < offset + size; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHAR_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    protected long getRowOffset(int row) {
        return row * hexDataColumns;
    }

    private String getAsciiDump(long fileOffset) throws IOException {
        StringBuilder sb = new StringBuilder();
        final int bufferOffset = (int)(fileOffset - buffer.getOffset());
        for (int i = 0; i <  hexDataColumns; i++) {
            int b = buffer.getByte(fileOffset + i) & 0xff;// getData()[i + bufferOffset] & 0xff;
            char ch = (char)b;
            if (!VISIBLE_SYMBOLS[b]) {
                ch = ' ';
            }
            sb.append(ch);
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
