package com.mucommander.ui.main.table;

import java.awt.*;

/**
 * Created on 26/01/17.
 */
public class CellTransparentLabel extends CellLabel {

    public CellTransparentLabel() {
        super();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcAtop.derive(0.3f));
        super.paint(g2);
        g2.dispose();
    }
}
