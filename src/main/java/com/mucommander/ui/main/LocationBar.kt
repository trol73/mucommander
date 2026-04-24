/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2026 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License* along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.main

import java.awt.*
import java.awt.event.*
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * A wrapper panel that sits in place of the location text field in each FolderPanel.
 * It hosts two cards in a [CardLayout]:
 *  - The regular [LocationTextField] (always visible by default).
 *  - A [BreadcrumbBar] that renders the current path as a row of clickable hyperlink-style labels, one per directory
 *  segment, separated by `›` glyphs.
 *
 * When the Ctrl key (or Meta on macOS) is held for {@value #BREADCRUMB_SHOW_DELAY_MS}ms *and* the location text field
 * does not have keyboard focus (i.e. the user is not actively editing the path), the breadcrumb card is shown.
 * The delayed appearance prevents the breadcrumb from flickering during quick keyboard shortcuts (Ctrl+C, Ctrl+V, etc.).
 *
 * To reduce visual noise, the breadcrumb is only shown for the panel where the mouse is currently hovering.
 * If the mouse is not over either folder panel, breadcrumbs are shownin both panels. The breadcrumb dynamically follows
 * the mouse - if the user moves the mouse from one panel to another while still holding Ctrl, the breadcrumb switches
 * accordingly.
 * The breadcrumb is immediately hidden when Ctrl is released, or if any other key is pressedor the mouse is clicked
 * before the delay expires.
 */
class LocationBar(private val folderPanel: FolderPanel, private val locationTextField: LocationTextField) : JPanel() {
    private val breadcrumbBar: BreadcrumbBar
    private val cardLayout: CardLayout = CardLayout()

    /** Timer that delays showing the breadcrumb to avoid noise from quick keyboard shortcuts  */
    private val showBreadcrumbTimer: Timer

    /** Tracks whether Ctrl/Meta is currently being held down  */
    private var modifierHeld = false

    init {
        setLayout(cardLayout)
        setOpaque(false)

        breadcrumbBar = BreadcrumbBar(folderPanel)

        add(locationTextField, CARD_TEXT_FIELD)
        add(breadcrumbBar, CARD_BREADCRUMB)

        // On macOS the menu shortcut key is Cmd (META); on all other platforms it is Ctrl.
        // We want Ctrl to always work, and Cmd to also work on macOS.
        val menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
        val isMac = menuShortcutMask == InputEvent.META_DOWN_MASK

        // Initialize the timer that delays showing the breadcrumb
        showBreadcrumbTimer = Timer(BREADCRUMB_SHOW_DELAY_MS) { _: ActionEvent? ->
            // Only show breadcrumb if:
            // 1. Text field is currently visible (not already showing breadcrumb)
            // 2. Mouse is over this panel OR mouse is over neither panel
            if (locationTextField.isShowing() && shouldShowBreadcrumbForMousePosition()) {
                breadcrumbBar.setFile(folderPanel.currentFolder)
                cardLayout.show(this@LocationBar, CARD_BREADCRUMB)
            }
        }
        showBreadcrumbTimer.isRepeats = false

        // Listen for mouse events globally
        Toolkit.getDefaultToolkit().addAWTEventListener({ event: AWTEvent? ->
            if (event is MouseEvent) {
                when (event.getID()) {
                    MouseEvent.MOUSE_PRESSED -> cancelBreadcrumbTimer()
                    MouseEvent.MOUSE_MOVED -> if (modifierHeld) {
                        updateBreadcrumbVisibility()
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK or AWTEvent.MOUSE_MOTION_EVENT_MASK)

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e: KeyEvent? ->
            val isCtrl = e!!.getKeyCode() == KeyEvent.VK_CONTROL
            val isMeta = isMac && e.getKeyCode() == KeyEvent.VK_META

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (isCtrl || isMeta) {
                    // Ctrl/Meta pressed - start the timer to show breadcrumb after delay
                    if (!locationTextField.hasFocus() && !showBreadcrumbTimer.isRunning) {
                        modifierHeld = true
                        showBreadcrumbTimer.restart()
                    }
                } else {
                    // Any other key pressed - cancel the timer to prevent breadcrumb from showing
                    // This filters out keyboard shortcuts like Ctrl+C, Ctrl+V, etc.
                    cancelBreadcrumbTimer()
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                if (isCtrl || isMeta) {
                    // Ctrl/Meta released - cancel timer and hide breadcrumb if showing
                    cancelBreadcrumbTimer()

                    // Hide breadcrumb only when neither trigger modifier remains held
                    val ctrlStillHeld = (e.modifiersEx and InputEvent.CTRL_DOWN_MASK) != 0
                    val metaStillHeld = isMac && (e.modifiersEx and InputEvent.META_DOWN_MASK) != 0
                    if (!ctrlStillHeld && !metaStillHeld) {
                        modifierHeld = false
                        SwingUtilities.invokeLater {
                            cardLayout.show(this@LocationBar, CARD_TEXT_FIELD)
                        }
                    }
                }
            }
            false // never consume — other Ctrl shortcuts must keep working
        }
    }

    /**
     * Cancels the breadcrumb show timer if it's running.
     */
    private fun cancelBreadcrumbTimer() {
        if (showBreadcrumbTimer.isRunning) {
            showBreadcrumbTimer.stop()
        }
    }

    /**
     * Updates the breadcrumb visibility based on current mouse position.
     * Shows breadcrumb if mouse is over this panel or neither panel.
     * Hides breadcrumb if mouse is over the other panel.
     * Called when mouse moves while Ctrl/Meta is held.
     */
    private fun updateBreadcrumbVisibility() {
        SwingUtilities.invokeLater {
            val shouldShow = shouldShowBreadcrumbForMousePosition()
            val isShowingBreadcrumb = breadcrumbBar.isShowing()
            if (shouldShow && !isShowingBreadcrumb) {
                // Mouse moved to this panel or neutral area - show breadcrumb
                breadcrumbBar.setFile(folderPanel.currentFolder)
                cardLayout.show(this@LocationBar, CARD_BREADCRUMB)
            } else if (!shouldShow && isShowingBreadcrumb) {
                // Mouse moved to other panel - hide breadcrumb
                cardLayout.show(this@LocationBar, CARD_TEXT_FIELD)
            }
        }
    }

    /**
     * Determines whether the breadcrumb should be shown based on the current mouse position.
     * Returns true if:
     * - Mouse is over this panel, OR
     * - Mouse is over neither panel (show in both)
     * Returns false if:
     * - Mouse is over the other panel (don't show noise in the non-hovered panel)
     */
    private fun shouldShowBreadcrumbForMousePosition(): Boolean {
        val pointerInfo = MouseInfo.getPointerInfo() ?: return true
        val mouseLocation = pointerInfo.location

        // Check if mouse is over this panel
        val thisPanel = folderPanel.panel
        if (isMouseOverComponent(thisPanel, mouseLocation)) {
            return true
        }

        // Check if mouse is over the other panel
        val mainFrame = folderPanel.getMainFrame()
        val leftPanel = mainFrame.leftPanel
        val rightPanel = mainFrame.rightPanel
        val otherPanel = if (folderPanel === leftPanel) rightPanel else leftPanel

        if (otherPanel != null) {
            val otherPanelComponent = otherPanel.panel
            if (isMouseOverComponent(otherPanelComponent, mouseLocation)) {
                return false // Mouse is over the other panel, don't show breadcrumb here
            }
        }

        return true // Mouse is over neither panel, show breadcrumb in both
    }

    /**
     * Checks if the mouse at the given screen location is over the specified component.
     */
    private fun isMouseOverComponent(component: Component?, screenLocation: Point): Boolean {
        if (component == null || !component.isShowing()) {
            return false
        }

        val componentLocation = Point(screenLocation)
        SwingUtilities.convertPointFromScreen(componentLocation, component)
        return component.contains(componentLocation)
    }

    /**
     * Always report the text field's preferred size so that switching cards does
     * not cause the location bar row to grow or shrink.
     */
    override fun getPreferredSize(): Dimension? {
        return locationTextField.getPreferredSize()
    }

    override fun getMinimumSize(): Dimension? {
        return locationTextField.getMinimumSize()
    }

    companion object {
        private const val CARD_TEXT_FIELD = "textField"
        private const val CARD_BREADCRUMB = "breadcrumb"

        /** Delay in milliseconds before showing breadcrumb when Ctrl/Meta is held  */
        private const val BREADCRUMB_SHOW_DELAY_MS = 50
    }
}