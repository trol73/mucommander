/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.tasks;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * @author Oleg Trifonov
 * Created on 08/12/14.
 */
public class TaskPanel extends JPanel {

    public TaskPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }


    public void addTask(TaskWidget taskWidget) {
        add(taskWidget);
        validate();
        repaint();
        getParent().revalidate();
        getParent().repaint();
    }

    public void removeWidget(TaskWidget taskWidget) {
        remove(taskWidget);
        validate();
        repaint();
        getParent().revalidate();
        getParent().repaint();
    }
}
