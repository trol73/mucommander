package ru.trolsoft.calculator;

import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.JComboBox;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Created by trol on 06/06/14.
 */
public class HistoryComboBox extends JComboBox<String> {
    private final FocusDialog parent;

    public HistoryComboBox(FocusDialog parent, List<String> values) {
        super(values.toArray(new String[values.size()]));
        this.parent = parent;
        setEditable(true);
        if (values.size() > 0) {
            String text = values.get(0);
            setSelectedItem(text);
            getEditor().selectAll();
        }

        getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (isPopupVisible()) {
                        hidePopup();
                        e.consume();
                    } else {
                        HistoryComboBox.this.parent.cancel();
                    }
                }
            }
        });
    }


    public void addToHistory(String s) {
        for (int i = 0; i < getItemCount(); i++) {
            String item = getItemAt(i);
            if (item.equalsIgnoreCase(s)) {
                removeItem(item);
                break;
            }
        }
        insertItemAt(s, 0);
        setSelectedIndex(0);
    }
}
