/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.trolsoft.calculator

import com.mucommander.cache.TextHistory
import com.mucommander.ui.dialog.FocusDialog
import com.mucommander.ui.helper.MnemonicHelper
import com.mucommander.ui.layout.XAlignedComponentPanel
import com.mucommander.ui.layout.XBoxPanel
import com.mucommander.ui.layout.YBoxPanel
import de.congrace.exp4j.CustomOperator
import de.congrace.exp4j.ExpressionBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.trolsoft.utils.StrUtils
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.text.DecimalFormat
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Created on 04/06/14.
 * @author Oleg Trifonov
 */
class CalculatorDialog(
    owner: Frame
) : FocusDialog(owner, i18n("calculator.calculator"), null), ActionListener, KeyListener {
    private val cbExpression: HistoryComboBox
    private val edtDec: JTextField
    private val edtHex: JTextField
    private val edtBin: JTextField
    private val edtOct: JTextField
    private val edtExp: JTextField
    private val btnDec: JButton
    private val btnHex: JButton
    private val btnBin: JButton
    private val btnOct: JButton
    private val btnExp: JButton
    private val btnClose: JButton
    private val lblError: JLabel

    init {
        val contentPane = getContentPane()

        val yPanel = YBoxPanel(10)

        // Text fields panel
        val compPanel: XAlignedComponentPanel = object : XAlignedComponentPanel() {
            override fun add(comp: Component, constraints: Any) {
                (constraints as GridBagConstraints).fill = GridBagConstraints.HORIZONTAL
                super.add(comp, constraints)
            }
        }

        val calcHistory = TextHistory.getInstance().getList(TextHistory.Type.CALCULATOR)
        cbExpression = HistoryComboBox(this, calcHistory).apply {
            addActionListener(this@CalculatorDialog)
            getEditor().editorComponent.addKeyListener(this@CalculatorDialog)
        }
        compPanel.addRow(i18n("calculator.expression") + ":", cbExpression, 5)

        lblError = JLabel()
        compPanel.addRow("", lblError, 10)

        val buttonFont = Font.getFont(Font.MONOSPACED)

        btnDec = JButton("DEC").apply {
            addActionListener(this@CalculatorDialog)
            setFont(buttonFont)
        }
        edtDec = JTextField().apply {
            isEditable = false
        }

        compPanel.addRow(btnDec, edtDec, 0)

        btnHex = JButton("HEX").apply {
            addActionListener(this@CalculatorDialog)
            setFont(buttonFont)
        }
        edtHex = JTextField().apply {
            isEditable = false
        }
        compPanel.addRow(btnHex, edtHex, 0)

        btnBin = JButton("BIN").apply {
            addActionListener(this@CalculatorDialog)
            setFont(buttonFont)
        }
        edtBin = JTextField().apply {
            isEditable = false
        }
        compPanel.addRow(btnBin, edtBin, 0)

        btnOct = JButton("OCT").apply {
            addActionListener(this@CalculatorDialog)
            setFont(buttonFont)
        }
        edtOct = JTextField().apply {
            isEditable = false
        }
        compPanel.addRow(btnOct, edtOct, 0)

        btnExp = JButton("EXP").apply {
            addActionListener(this@CalculatorDialog)
            setFont(buttonFont)
        }
        edtExp = JTextField().apply {
            isEditable = false
        }
        compPanel.addRow(btnExp, edtExp, 0)

        val mnemonicHelper = MnemonicHelper()

        val buttonsPanel = XBoxPanel()
        val buttonGroupPanel = JPanel(FlowLayout(FlowLayout.RIGHT))

        btnClose = JButton(i18n("close")).apply {
            addActionListener(this@CalculatorDialog)
            setMnemonic(mnemonicHelper.getMnemonic(this))
        }
        buttonGroupPanel.add(btnClose)

        buttonsPanel.add(buttonGroupPanel)

        contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        contentPane.add(yPanel, BorderLayout.NORTH)

        yPanel.add(compPanel)

        minimumSize = MIN_DIMENSION
        setModal(false)

        fixHeight()
    }

    private fun calculateAndShow(): Boolean {
        val expression = this.getExpression() ?: return false
        var success: Boolean
        try {
            val res = evaluate(expression)
            TextHistory.getInstance().add(TextHistory.Type.CALCULATOR, expression, false)
            cbExpression.addToHistory(expression)
            showResult(res)
            success = true
        } catch (e: Exception) {
            log.error("Calculation failed", e)
            clearResultFields()
            success = false
        }
        enableControls(success)
        lblError.setText(if (success) "" else i18n("calculator.error"))
        return success
    }

    private fun showResult(res: Double) {
        val valLong = res.roundToLong()
        val isDecimal = valLong.toDouble() == res
        edtDec.text = if (isDecimal) valLong.toString() else FORMAT_DEC.format(res).replace(',', '.')
        edtHex.text = java.lang.Long.toHexString(valLong)
        edtOct.text = java.lang.Long.toOctalString(valLong)
        edtBin.text = java.lang.Long.toBinaryString(valLong)
        edtExp.text = formatExp(res)
    }

    private fun getExpression(): String? {
        val selectedItem = cbExpression.selectedItem ?: return null
        val result = selectedItem.toString().trim()
        return StrUtils.removeUtfMarker(result).trim()
    }


    private fun enableControls(enable: Boolean) {
        edtDec.setEnabled(enable)
        edtHex.setEnabled(enable)
        edtOct.setEnabled(enable)
        edtBin.setEnabled(enable)
        edtOct.setEnabled(enable)
        edtExp.setEnabled(enable)
        btnDec.setEnabled(enable)
        btnHex.setEnabled(enable)
        btnOct.setEnabled(enable)
        btnBin.setEnabled(enable)
        btnOct.setEnabled(enable)
        btnExp.setEnabled(enable)
    }

    private fun clearResultFields() {
        edtDec.text = ""
        edtHex.text = ""
        edtOct.text = ""
        edtBin.text = ""
        edtExp.text = ""
    }

    @Throws(Exception::class)
    private fun evaluate(expression: String): Double {
        if (expression.trim { it <= ' ' }.isEmpty()) {
            return 0.0
        }
        return ExpressionBuilder(expression).apply {
            withOperations(OPERATORS)
            withVariable("pi", Math.PI)
            withVariable("e", Math.E)
        }.build().calculate()
    }

    private fun formatExp(`val`: Double): String {
        var result = FORMAT_EXP.format(`val`).uppercase(Locale.getDefault())
        val index = result.indexOf('E')
        if (index > 0 && result[index + 1] != '-') {
            result = result.substring(0, index) + '+' + result.substring(index)
        }
        return result
    }

    override fun actionPerformed(e: ActionEvent) {
        val src = e.getSource()
        if (src === cbExpression) {
            calculateAndShow()
        } else if (src === btnClose) {
            cancel()
        } else if (src === btnDec) {
            toClipboard(edtDec.getText())
        } else if (src === btnHex) {
            toClipboard(edtHex.getText())
        } else if (src === btnBin) {
            toClipboard(edtBin.getText())
        } else if (src === btnOct) {
            toClipboard(edtOct.getText())
        } else if (src === btnExp) {
            toClipboard(edtExp.getText())
        }
    }

    private fun toClipboard(s: String?) {
        val data = StringSelection(s)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(data, data)
    }


    override fun saveState() {
        super.saveState()
        TextHistory.getInstance().save(TextHistory.Type.CALCULATOR)
    }

    override fun keyTyped(e: KeyEvent) {}

    override fun keyPressed(e: KeyEvent) {}

    override fun keyReleased(e: KeyEvent) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && (e.modifiersEx and (KeyEvent.CTRL_DOWN_MASK or KeyEvent.META_DOWN_MASK)) != 0) {
            if (calculateAndShow()) {
                cbExpression.setSelectedItem(edtDec.getText())
            }
        }
    }

    companion object {
        private val MIN_DIMENSION = Dimension(520, 300)
        private val log: Logger = LoggerFactory.getLogger(CalculatorDialog::class.java)

        private fun opLL(values: DoubleArray, operation: (Long, Long) -> Long): Double =
            operation(values[0].roundToLong(), values[1].roundToLong()).toDouble()
        private fun opLI(values: DoubleArray, operation: (Long, Int) -> Long): Double =
            operation(values[0].roundToLong(), values[1].roundToInt()).toDouble()

        private val OP_SHL = object : CustomOperator("<<", true, 10, 2) {
            protected override fun applyOperation(values: DoubleArray) =
                opLI(values) { a: Long, b: Int -> a shl b }
        }

        private val OP_SHR = object : CustomOperator(">>", true, 11, 2) {
            protected override fun applyOperation(values: DoubleArray) =
                opLI(values) { a: Long, b: Int -> a shr b }
        }

        private val OP_AND = object : CustomOperator("&", true, 8, 2) {
            protected override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a and b }
        }

        private val OP_OR = object : CustomOperator("|", true, 6, 2) {
            protected override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a or b }
        }

        private val OP_NOT = object : CustomOperator("~", true, 15, 1) {
            protected override fun applyOperation(values: DoubleArray) =
                values[0].roundToLong().inv().toDouble()
        }

        private val OP_XOR = object : CustomOperator("^^", true, 7, 2) {
            protected override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a xor b }
        }

        private val FORMAT_DEC = DecimalFormat("#.##################")
        private val FORMAT_EXP = DecimalFormat("0.00000000000000E0000")

        private val OPERATORS = listOf(
            OP_SHL, OP_SHR, OP_AND, OP_OR, OP_NOT, OP_XOR
        )
    }
}
