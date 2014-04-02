package ru.trolsoft.hexeditor.ui;


import com.mucommander.profiler.Profiler;
import ru.trolsoft.hexeditor.events.OnOffsetChangeListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * The table displaying the hex data
 */
public class HexTable extends JTable {

    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);

    /**
     * ASCII characters. Used to prevent strings creation on repainting cell
     */
    private static final String[] CHARACTERS = new String[256];

    static {
        for (char i = 0; i < CHARACTERS.length; i++) {
            CHARACTERS[i] = Character.toString(i);
        }
    }


    private final ViewerHexTableModel model;
    private final CellRenderer cellRenderer = new CellRenderer();
    private final Rectangle repaintRect = new Rectangle();


    private int widthOfW;
    private int fontHeight;
    private int fontAscent;

    private Color alternateCellColor;
    private Color offsetColor;
    private Color asciiDumpColor;
    private Color highlightSelectionInAsciiDumpColor;
    private boolean alternateRowBackground;
    private boolean alternateColumnBackground;
    private long leadSelectionIndex;
    private long anchorSelectionIndex;

    private OnOffsetChangeListener onOffsetChangeListener;

    public HexTable(ViewerHexTableModel model) {
        super(model);
        this.model = model;
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);



        setShowGrid(false);
        setIntercellSpacing(ZERO_DIMENSION);
        alternateCellColor = getBackground();
        offsetColor = getForeground();
        asciiDumpColor = getForeground();

        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setDefaultEditor(Object.class, cellEditor);
        setDefaultRenderer(Object.class, cellRenderer);
        getTableHeader().setReorderingAllowed(false);
        setShowGrid(false);

        setFont(getFont());
    }


    /**
     * Changes the selected byte range.
     *
     * @param row
     * @param col
     * @param toggle
     * @param extend if true, extend the current selection
     * @see #changeSelectionByOffset(long, boolean)
     * @see #setSelectedRows(int, int)
     * @see #setSelectionByOffsets(long, long)
     */
    @Override
    public void changeSelection(int row, int col, boolean toggle, boolean extend) {
        // remind previous selection range
        //long prevSmallest = getSmallestSelectionIndex();
        //long prevLargest = getLargestSelectionIndex();

        // Don't allow the user to select the "ascii dump" or any
        // empty cells in the last row of the table.
        col = adjustColumn(row, col);
        if (row < 0) {
            row = 0;
        }

        final long prevSelectionIndexFrom = anchorSelectionIndex;
        final long prevSelectionIndexTo = leadSelectionIndex;

        if (extend) {
            leadSelectionIndex = cellToOffset(row, col);
        } else {
            anchorSelectionIndex = leadSelectionIndex = cellToOffset(row, col);
        }
        if (onOffsetChangeListener != null) {
            onOffsetChangeListener.onChange(anchorSelectionIndex);
        }

        // Scroll after changing the selection as blit scrolling is
        // immediate, so that if we cause the repaint after the scroll we
        // end up painting everything!
        if (getAutoscrolls()) {
            ensureCellIsVisible(row, col);
        }

        // Draw the new selection.
        repaintSelection(prevSelectionIndexFrom, prevSelectionIndexTo);

//        fireSelectionChangedEvent(prevSmallest, prevLargest);
    }

    private static final int min(int x1, int x2, int x3, int x4) {
        int min = x1 < x2 ? x1 : x2;
        if (x3 < min) {
            min = x3;
        }
        if (x4 < min) {
            min = x4;
        }
        return min;
    }

    private static final int max(int x1, int x2, int x3, int x4) {
        int max = x1 > x2 ? x1 : x2;
        if (x3 > max) {
            max = x3;
        }
        if (x4 > max) {
            max = x4;
        }
        return max;
    }


    @Override
    public boolean isCellEditable(int row, int col) {
        return false;//cellToOffset(row, col) >- 1;
    }

    @Override
    public boolean isCellSelected(int row, int col) {
        // Offset and ASCII dump
        if (col == 0 || col == model.getColumnCount()-1) {
            return false;
        }
        long offset = cellToOffset(row, col);
        final long start = getSmallestSelectionIndex();
        final long end = getLargestSelectionIndex();
        return offset >= start && offset <= end;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        calculateSizes(font);
        if (cellRenderer != null) { // check for NPE prevent, because constructor of JTable calls setFont()
            cellRenderer.setFont(font);
        }
    }

    /**
     * Calculates table column sizes after change of font
     */
    private void calculateSizes(Font font) {
        if (model == null) {
            return;
        }
        FontMetrics fm = getFontMetrics(font);
        FontMetrics fmHeader = getFontMetrics(getTableHeader().getFont());

        widthOfW = fm.charWidth('W');
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();

        final int hexColumns = model.getNumberOfHexColumns();
        int w = Math.max(widthOfW*3, fmHeader.stringWidth("+999"));
//System.out.println(">> " + widthOfW*3 + "   " + fmHeader.stringWidth("+999"));
        for (int i = 1; i <= hexColumns; i++) {
            setColumnWidth(i, w);
        }
        // Offset
        setColumnWidth(0, widthOfW * 10);

        w = widthOfW * (hexColumns + 1);
        // ASCII dump
        setColumnWidth(hexColumns + 1, w);

        model.setAsciiCharVisible((char) 0, false);
        for (char ch = 1; ch <= 0xff; ch++) {
            model.setAsciiCharVisible(ch, fm.charWidth(ch) <= widthOfW);
        }

        setPreferredScrollableViewportSize(new Dimension(w * 16 + 200, 25 * getRowHeight()));    // todo

    }

    /**
     * Set preferred, minimum and maximum width of column
     * @param index
     * @param width
     */
    private void setColumnWidth(int index, int width) {
        TableColumn column = getColumnModel().getColumn(index);
        column.setPreferredWidth(width);
        column.setMinWidth(width);
        column.setMaxWidth(width);
    }

    /**
     * Returns the column for the cell containing data that is the closest
     * to the specified cell.  This is used when, for example, the user clicks
     * on an "empty" cell in the last row of the table.
     *
     * @param row The row of the cell clicked on.
     * @param col The column of the cell clicked on.
     * @return The column of the closest cell containing data.
     */
    private int adjustColumn(int row, int col) {
        if (col < 0) {
            return 0;
        }
        // last row
        if (row == getRowCount() - 1) {
            final long fileSize = model.getSize();
            final int hexColumns = model.getNumberOfHexColumns();
            int lastRowCount = (int)(fileSize % hexColumns);
            if (lastRowCount > 0) {
                return Math.min(col, lastRowCount);
            }
        }
        return Math.min(col, getColumnCount()-2);
    }


    /**
     * Returns the cell representing the specified offset into the hex document.
     *
     * @param offset The offset into the document.
     * @return The cell, in the form <code>(row, col)</code>.  If the specified offset is invalid, <code>(-1, -1)</code> is returned.
     * @see #cellToOffset(int, int)
     */
    public Point offsetToCell(long offset) {
        if (offset < 0 || offset >= model.getSize()) {
            return new Point(-1, -1);
        }
        final int hexColumns = model.getNumberOfHexColumns();
        int row = (int)(offset / hexColumns);
        int col = (int)(offset % hexColumns);
        return new Point(row, col);
    }


    /**
     * Returns the offset into the bytes being edited represented at the
     * specified cell in the table, if any.
     *
     * @param row The row in the table.
     * @param col The column in the table.
     * @return The offset into the byte array, or <code>-1</code> if the
     *         cell does not represent part of the byte array (such as the
     *         tailing "ascii dump" column's cells).
     * @see #offsetToCell(long)
     */
    public long cellToOffset(int row, int col) {
        // Check row and column individually to prevent them being invalid
        // values but still pointing to a valid offset in the buffer.
        final int hexColumns = model.getNumberOfHexColumns();
        // Don't include last column (ASCII dump)
        if (row < 0 || row >= getRowCount() || col < 1 || col > hexColumns) {
            return -1;
        }
        int offs = row*hexColumns + col - 1;
        return (offs >= 0 && offs < model.getSize()) ? offs : -1;
    }


    /**
     * Returns the largest selection index.
     *
     * @return The largest selection index.
     * @see #getSmallestSelectionIndex()
     */
    public long getLargestSelectionIndex() {
        long index = Math.max(leadSelectionIndex, anchorSelectionIndex);
        return index < 0 ? 0 : index; // Don't return -1 if table is empty
    }


    /**
     * Returns the smallest selection index.
     *
     * @return The smallest selection index.
     * @see #getLargestSelectionIndex()
     */
    public long getSmallestSelectionIndex() {
        long index = Math.min(leadSelectionIndex, anchorSelectionIndex);
        return index < 0 ? 0 : index; // Don't return -1 if table is empty
    }



    /**
     * Changes the selection by an offset into the bytes being edited.
     *
     * @param offset
     * @param extend
     * @see #changeSelection(int, int, boolean, boolean)
     * @see #setSelectedRows(int, int)
     * @see #setSelectionByOffsets(long, long)
     */
    public void changeSelectionByOffset(long offset, boolean extend) {
        final long fileSize = model.getSize();
        if (offset < 0) {
            offset = 0;
        } else if (offset >= fileSize) {
            offset = fileSize - 1;
        }
        final int hexColumns = model.getNumberOfHexColumns();
        int row = (int)(offset / hexColumns);
        int col = (int)(offset % hexColumns) + 1;
        changeSelection(row, col, false, extend);
    }


    /**
     * Clears the selection.  The "lead" of the selection is set back to the
     * position of the "anchor."
     */
    @Override
    public void clearSelection() {
        final long prevSelectionIndexFrom = anchorSelectionIndex;
        final long prevSelectionIndexTo = leadSelectionIndex;

        if (anchorSelectionIndex >= 0) { // Always true unless an error
            leadSelectionIndex = anchorSelectionIndex;
        } else {
            anchorSelectionIndex = leadSelectionIndex = 0;
        }
        repaintSelection(prevSelectionIndexFrom, prevSelectionIndexTo);
    }


    public void setSelectedRows(int min, int max) {
        if (min < 0 || min >= getRowCount() || max < 0 || max >= getRowCount()) {
            throw new IllegalArgumentException();
        }
        final int hexColumns = model.getNumberOfHexColumns();
        int startOffs = min * hexColumns;
        int endOffs = max*hexColumns + hexColumns-1;
        // TODO: Have a single call to change selection by a range.
        changeSelectionByOffset(startOffs, false);
        changeSelectionByOffset(endOffs, true);
    }


    /**
     * Selects the specified range of bytes in the table.
     *
     * @param startOffs The "anchor" byte of the selection.
     * @param endOffs The "lead" byte of the selection.
     * @see #changeSelection(int, int, boolean, boolean)
     * @see #changeSelectionByOffset(long, boolean)
     */
    public void setSelectionByOffsets(long startOffs, long endOffs) {
        final long prevSelectionIndexFrom = anchorSelectionIndex;
        final long prevSelectionIndexTo = leadSelectionIndex;

        if (startOffs < 0) {
            startOffs = 0;
        } else if (startOffs >= model.getSize()) {
            startOffs = model.getSize()-1;
        }

        // Clear the old selection (may not be necessary).
        //repaintSelection();

        anchorSelectionIndex = startOffs;
        leadSelectionIndex = endOffs;

        // Scroll after changing the selection as blit scrolling is
        // immediate, so that if we cause the repaint after the scroll we
        // end up painting everything!
        if (getAutoscrolls()) {
            final int hexColumns = model.getNumberOfHexColumns();
            int endRow = (int)(endOffs / hexColumns);
            int endCol = (int)(endOffs % hexColumns);
            // Don't allow the user to select the "ascii dump" or any empty cells in the last row of the table.
            endCol = adjustColumn(endRow, endCol);
            if (endRow < 0) {
                endRow = 0;
            }
            ensureCellIsVisible(endRow, endCol);
        }

        // Draw the new selection.
        repaintSelection(prevSelectionIndexFrom, prevSelectionIndexTo);
    }


    /**
     * Ensures the specified cell is visible.
     *
     * @param row The row of the cell.
     * @param col The column of the cell.
     */
    private void ensureCellIsVisible(int row, int col) {
        Rectangle cellRect = getCellRect(row, col, false);
        scrollRectToVisible(cellRect);
    }

    private void repaintSelection(long indexFromBefore, long indexFromAfter) {
        if (model == null) {
            return;
        }
        // calculate area for repainting
        final int hexColumns = model.getNumberOfHexColumns();
        final int row1 = (int)(indexFromBefore / hexColumns);
        final int row2 = (int)(indexFromAfter / hexColumns);
        final int row3 = (int)(anchorSelectionIndex / hexColumns);
        final int row4 = (int)(leadSelectionIndex / hexColumns);
        final int rowFrom = min(row1, row2, row3, row4);
        final int rowTo = max(row1, row2, row3, row4);

        int colFrom, colTo;
        if (rowFrom == rowTo) {
            final int col1 = (int)(indexFromBefore % hexColumns);
            final int col2 = (int)(indexFromAfter % hexColumns);
            final int col3 = (int)(anchorSelectionIndex % hexColumns);
            final int col4 = (int)(leadSelectionIndex % hexColumns);

            colFrom = min(col1, col2, col3, col4);
            colTo = max(col1, col2, col3, col4);
        } else {
            colFrom = 0;
            colTo = hexColumns-1;
        }

        //System.out.println("> " + colFrom + ", " + rowFrom + " -> " + colTo + ", " + rowTo);
        // repaint hex columns
        final int rowHeight = getRowHeight();
        final TableColumnModel cm = getColumnModel();
        final int offsetColumnWidth = cm.getColumn(0).getWidth();
        final int hexColumnWidth = cm.getColumn(1).getWidth();
        final int dumpColumnWidth = cm.getColumn(cm.getColumnCount()-1).getWidth();
        //repaintRect.setBounds(offsetColumnWidth, rowHeight*rowFrom, getWidth()-offsetColumnWidth, (rowTo-rowFrom+1)*rowHeight);
        //repaint(repaintRect);
        repaintRect.setBounds(offsetColumnWidth + colFrom*hexColumnWidth, rowHeight*rowFrom, (colTo-colFrom+1)*hexColumnWidth, (rowTo-rowFrom+1)*rowHeight);
        repaint(repaintRect);
        repaintRect.setBounds(getWidth() - dumpColumnWidth, rowHeight*rowFrom, dumpColumnWidth, (rowTo-rowFrom+1)*rowHeight);
        repaint(repaintRect);
    }

    /**
     * Set alternate background color
     * @param color alternate background color
     */
    public void setAlternateBackground(Color color) {
        this.alternateCellColor = color;
    }

    /**
     * Get alternate background color
     * @return alternate background color
     */
    public Color getAlternateBackground() {
        return alternateCellColor;
    }

    /**
     * Set color to render the first offset column
     * @param color
     */
    public void setOffsetColomnColor(Color color) {
        this.offsetColor = color;
    }

    /**
     * Get color to render the first offset column
     * @return
     */
    public Color getOffsetColomnColor() {
        return offsetColor;
    }

    /**
     * Set color to render the last ASCII dump column
     * @param color color for ASCII dump text
     */
    public void setAsciiColumnColor(Color color) {
        this.asciiDumpColor = color;
    }

    /**
     * Get color to render the last ASCII dump column
     * @return
     */
    public Color getAsciiColumnColor() {
        return asciiDumpColor;
    }

    /**
     *
     * @param color
     */
    public void setHighlightSelectionInAsciiDumpColor(Color color) {
        this.highlightSelectionInAsciiDumpColor = color;
    }

    /**
     *
     * @return
     */
    public Color getHighlightSelectionInAsciiDumpColor() {
        return highlightSelectionInAsciiDumpColor;
    }


    /**
     *
     * @param enable
     */
    public void setAlternateRowBackground(boolean enable) {
        this.alternateRowBackground = enable;
    }

    /**
     *
     * @return
     */
    public boolean isAlternateRowBackground() {
        return alternateRowBackground;
    }

    /**
     *
     * @param enable
     */
    public void setAlternateColumnBackground(boolean enable) {
        this.alternateColumnBackground = enable;
    }

    /**
     *
     * @return
     */
    public boolean isAlternateColumnBackground() {
        return alternateColumnBackground;
    }


    /**
     * Returns the rendering hints for text that will most accurately reflect
     * those of the native windowing system.
     *
     * @return The rendering hints, or <code>null</code> if they cannot be
     *         determined.
     */
    private Map getDesktopAntiAliasHints() {
        return (Map)getToolkit().getDesktopProperty("awt.font.desktophints");
    }

    @Override
    protected void processKeyEvent (KeyEvent e) {
        // TODO: Convert into Actions and put into InputMap/ActionMap?
        final int hexColumns = model.getNumberOfHexColumns();
        final long lastOffset = model.getSize() - 1;
        final boolean extend = e.isShiftDown();
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    long offs = leadSelectionIndex > 0 ? leadSelectionIndex-1 : 0;
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_RIGHT:
                    offs = Math.min(leadSelectionIndex+1, lastOffset);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_UP:
                    offs = Math.max(leadSelectionIndex - hexColumns, 0);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_DOWN:
                    offs = Math.min(leadSelectionIndex + hexColumns, lastOffset);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_PAGE_DOWN:
                    int visibleRowCount = getVisibleRect().height/getRowHeight();
                    offs = Math.min(leadSelectionIndex + visibleRowCount * hexColumns, lastOffset);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    visibleRowCount = getVisibleRect().height / getRowHeight();
                    offs = Math.max(leadSelectionIndex - visibleRowCount*hexColumns, 0);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_HOME:
                    offs = (leadSelectionIndex/hexColumns)*hexColumns;
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_END:
                    offs = (leadSelectionIndex/hexColumns)*hexColumns + hexColumns-1;
                    offs = Math.min(offs, lastOffset);
                    changeSelectionByOffset(offs, extend);
                    e.consume();
                    return;
                case KeyEvent.VK_BACK_SPACE:
                    System.out.println();
                    Profiler.print();
                    //System.out.println(Profiler.getTime());
                    return;
            }
        }
        super.processKeyEvent(e);
    }


    private class CellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        private final Point highlight;
        private final Map desktopAAHints;
        private boolean hasSeparatorLine;

        public CellRenderer() {
            highlight = new Point();
            desktopAAHints = getDesktopAntiAliasHints();
            setFont(HexTable.this.getFont());
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setValue(value);

            highlight.setLocation(-1, -1);
            hasSeparatorLine = column > 1 && column % 4 == 1;
            // ASCII dump
            if (column == table.getColumnCount() - 1) {
                long selStart = getSmallestSelectionIndex();
                long selEnd = getLargestSelectionIndex();

                final int hexColumns = model.getNumberOfHexColumns();
                int b1 = row * hexColumns;
                int b2 = b1 + hexColumns - 1;
                if (selStart <= b2 && selEnd >= b1) {
                    long start = Math.max(selStart, b1) - b1;
                    long end = Math.min(selEnd, b2) - b1;
                    highlight.setLocation(start, end);
                }
                boolean alternateColor = alternateRowBackground && (row & 1) > 0;
                setBackground(alternateColor ? alternateCellColor : table.getBackground());
                setForeground(asciiDumpColor);
                hasSeparatorLine = false;
            }  else {
                if (!isSelected) {
                    if ((alternateRowBackground && (row & 1) > 0) ^ (alternateColumnBackground && (column & 1)>0)) {
                        setBackground(alternateCellColor);
                    } else {
                        setBackground(table.getBackground());
                    }
                } else {
                    setBackground(table.getSelectionBackground());
                }
                // Offset column
                if (column == 0) {
                    setForeground(offsetColor);
                } else {
                    setForeground(table.getForeground());
                }
            }

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            final String text = getText();
            final int len = text.length();
            int x = (getWidth() - widthOfW*len)/2;
            int y = (getHeight() - fontHeight)/2 + fontAscent;

            if (highlight.x >= 0) {
                g.setColor(highlightSelectionInAsciiDumpColor);
                g.fillRect(x + highlight.x * widthOfW, 0, (highlight.y - highlight.x + 1) * widthOfW, getRowHeight());
            }

            Graphics2D g2d = (Graphics2D)g;
            Object oldHints = null;
            if (desktopAAHints != null) {
                oldHints = g2d.getRenderingHints();
                g2d.addRenderingHints(desktopAAHints);
            }

            g.setColor(getForeground());
            // not padding low bytes, and this one is in range 00-0f.
            if (len == 1) {
                x += widthOfW;
            }
            for (int i = 0; i < len; i++) {
                char ch = text.charAt(i);
                if (ch != ' ') {
                    g.drawString(CHARACTERS[ch], x, y);
                }
                x += widthOfW;
            }
            //g.drawString(text, x, y);

            // Restore rendering hints appropriately.
            if (desktopAAHints != null) {
                g2d.addRenderingHints((Map)oldHints);
            }

            if (hasSeparatorLine) {
                g.setColor(Color.GRAY);
                g.drawLine(0, 0, 0, getHeight());
            }
        }
    }



    public OnOffsetChangeListener getOnOffsetChangeListener() {
        return onOffsetChangeListener;
    }

    public void setOnOffsetChangeListener(OnOffsetChangeListener onOffsetChangeListener) {
        this.onOffsetChangeListener = onOffsetChangeListener;
    }

}
