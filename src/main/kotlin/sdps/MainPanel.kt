/*
 * Copyright © 2021 antD97
 * Licensed under the MIT License https://antD.mit-license.org/
 */
package sdps

import java.awt.*
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableColumn

/** The main UI panel. */
class MainPanel(
    private val dpsTracker: DPSTracker,
    configData: ConfigManager.ConfigData,
    private val windowSidebarMinSize: Dimension,
    private val windowSmallMinSize: Dimension
) : JPanel(GridBagLayout()) {

    val ign: String
        get() { return nameField.text }

    val isSidebarEnabled: Boolean
        get() { return sidebar.isVisible }

    val isOnTopEnabled: Boolean
        get() { return onTopCheckBox.isSelected }

    val columnOrder: List<String>
        get() { return dpsTable.columnModel.columns.toList().map { it.headerValue as String } }

    val columnWidths: List<Int>
        get() { return dpsTable.columnModel.columns.toList().map { it.width } }

/* --------------------------------------- GUI Components --------------------------------------- */

    private val colMaxWidth = Int.MAX_VALUE
    private val colMinWidth = 15

    private val dpsTable = JTable(
        DefaultTableModel(arrayOf(), arrayOf("Time", "DPS", "Damage", "Σ Damage", "Mitigated", "Reason")))
        .apply { setDefaultEditor(Object::class.java, null) }
    private val dpsTableScrollPane = JScrollPane(dpsTable)
        .apply { preferredSize = Dimension(400, 300) }

    private val sidebar = JPanel(GridBagLayout())

    private val minimizeSidebarButton = JButton()
        .apply {
            preferredSize = Dimension(20, 10)
            addActionListener(::minimizeSidebarButtonClick)
            toolTipText = "Minimize the sidebar (Escape)"
        }

    private val onTopCheckBox = JCheckBox("Window always on top")
        .apply {
            addActionListener(::onTopCheckBoxClick)
            toolTipText = "Force this window to display on top of Smite and other windows (T)"
        }

    private val nameField = JTextField(13)
        .apply {
            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) { nameFieldUpdate() }
                override fun removeUpdate(e: DocumentEvent?) { nameFieldUpdate() }
                override fun changedUpdate(e: DocumentEvent?) { nameFieldUpdate() }
            })
        }

    private val combatLogField = JTextField(13)
        .apply {
            text = "no file"
            isEditable = false
        }

    private val resetTimerButton = JButton("Reset")
        .apply {
            addActionListener(::resetTimerButtonClick)
            toolTipText = "Makes your next tick of damage reset the DPS timer (R)"
        }
    private val clearTableButton = JButton("Clear Table")
        .apply {
            addActionListener(::clearTableButtonClick)
            toolTipText = "Clears the table of all rows and resets the DPS timer (C)"
        }

    private val minimizedBar = JPanel(GridBagLayout())
    private val maximizeSidebarButton = JButton()
        .apply {
            preferredSize = Dimension(10, 20)
            addActionListener(::maximizeSidebarButtonClick)
            toolTipText = "Maximize the sidebar (Escape)"
        }

    private val timeCheckBox = JCheckBox("Time")
        .apply {
            addActionListener(::timeCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the time column (1)"
        }
    private val dpsCheckBox = JCheckBox("DPS")
        .apply {
            addActionListener(::dpsCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the DPS column (2)"
        }
    private val damageCheckBox = JCheckBox("Damage")
        .apply {
            addActionListener(::damageCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the damage column (3)"
        }
    private val totalDamageCheckBox = JCheckBox("Total Damage")
        .apply {
            addActionListener(::totalDamageCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the total damage column (4)"
        }
    private val mitigatedCheckBox = JCheckBox("Mitigated")
        .apply {
            addActionListener(::mitigatedCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the mitigated column (5)"
        }
    private val reasonCheckBox = JCheckBox("Reason")
        .apply {
            addActionListener(::reasonCheckBoxClick)
            isSelected = true
            toolTipText = "Toggles the reason column (6)"
        }

/* ----------------------------------------- Constructor ---------------------------------------- */

    init {
        val c = GridBagConstraints()

        // main table
        c.gridx = 2
        c.weightx = 1.0; c.weighty = 1.0
        c.fill = GridBagConstraints.BOTH
        c.insets = Insets(10, 0, 10, 10)
        add(dpsTableScrollPane, c)

        // sidebar
        c.gridx = 1
        c.weightx = 0.0
        c.insets = Insets(10, 10, 10, 10)
        sidebar.apply {
            val c2 = GridBagConstraints()

            // minimize sidebar button
            c2.gridx = 0; c2.gridy = 0
            c2.weightx = 1.0; c2.weighty = 0.0
            c2.anchor = GridBagConstraints.LINE_START
            c2.insets = Insets(0, 0, 0, 0)
            add(minimizeSidebarButton, c2)

            // title
            c2.gridy++
            c2.anchor = GridBagConstraints.PAGE_START
            c2.insets = Insets(0, 0, 10, 0)
            add(JLabel("Smite Damage Tracker"), c2)

            // separator
            c2.gridy++
            c2.fill = GridBagConstraints.HORIZONTAL
            add(JSeparator(), c2)

            // on top checkbox
            c2.gridy++
            c2.fill = GridBagConstraints.NONE
            c2.anchor = GridBagConstraints.FIRST_LINE_START
            add(onTopCheckBox, c2)

            // in-game name input field
            c2.gridy++
            add(LabelPanel("In-game name", nameField), c2)

            // combat log file
            c2.gridy++
            add(LabelPanel("Combat log file", combatLogField), c2)

            // 2x button group
            c2.gridy++
            c2.fill = GridBagConstraints.HORIZONTAL
            JPanel(GridBagLayout()).apply {
                val c3 = GridBagConstraints()

                // reset timer button
                c3.gridx = 0; c3.gridy = 0
                add(resetTimerButton, c3)

                // clear table button
                c3.gridx++
                c3.insets = Insets(0, 15, 0, 0)
                add(clearTableButton, c3)
            }.also { add(it, c2) }

            // separator
            c2.gridy++
            add(JSeparator(), c2)

            // time checkbox
            c2.gridy++
            c2.fill = GridBagConstraints.NONE
            c2.anchor = GridBagConstraints.FIRST_LINE_START
            c2.insets = Insets(0, 0, 0, 0)
            add(timeCheckBox, c2)

            // dps checkbox
            c2.gridy++
            add(dpsCheckBox, c2)

            // damage checkbox
            c2.gridy++
            add(damageCheckBox, c2)

            // total damage checkbox
            c2.gridy++
            add(totalDamageCheckBox, c2)

            // mitigated checkbox
            c2.gridy++
            add(mitigatedCheckBox, c2)

            // reason checkbox
            c2.gridy++
            c2.weighty = 1.0
            c2.fill = GridBagConstraints.HORIZONTAL
            c2.anchor = GridBagConstraints.PAGE_START
            add(reasonCheckBox, c2)

            minimumSize = preferredSize

        }.also { add(it, c) }

        // minimized sidebar
        c.gridx = 0
        c.insets = Insets(10, 5, 5, 5)
        minimizedBar.apply {
            val c2 = GridBagConstraints()

            // maximize side bar button
            c2.weightx = 1.0; c2.weighty = 1.0
            c2.anchor = GridBagConstraints.PAGE_START
            add(maximizeSidebarButton, c2)

            minimumSize = preferredSize

        }.also { add(it, c) }

        // key binds
        isFocusable = true
        addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) { }
            override fun mousePressed(e: MouseEvent?) { requestFocus() }
            override fun mouseReleased(e: MouseEvent?) { }
            override fun mouseEntered(e: MouseEvent?) { }
            override fun mouseExited(e: MouseEvent?) { }
        })

        fun Component.onEachComponent(condition: (Component) -> Boolean, action: (Component) -> Unit) {
            if (condition(this)) {
                action(this)
                if (this is Container) {
                    for (comp in components) comp.onEachComponent(condition, action)
                }
            }
        }

        onEachComponent({ component: Component -> component !is JTextField },
            {
                it.addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) { }
                    override fun keyPressed(e: KeyEvent?) { shortcutButtonPress(e) }
                    override fun keyReleased(e: KeyEvent?) { }
                })
            })

        // load settings
        if (configData.ign != null) nameField.text = configData.ign

        sidebar.isVisible = configData.sidebar
        minimizedBar.isVisible = !configData.sidebar

        onTopCheckBox.isSelected = configData.onTop

        if (configData.columnOrder != null) {
            for (i in 0..configData.columnOrder!!.lastIndex) {
                val targetCol = configData.columnOrder!![i]
                val tableColumns = dpsTable.columnModel.columns.toList()

                for (j in i+1..tableColumns.lastIndex) {
                    if (tableColumns[j].headerValue as String == targetCol) {
                        // move the column to the correct spot
                        dpsTable.columnModel.moveColumn(j, i)
                    }
                }
            }
        }

        if (configData.columnWidths != null) {
            for (i in configData.columnWidths!!.indices) {

                val column = dpsTable.columnModel.getColumn(i)

                if (configData.columnWidths!![i] == 0) {
                    when (column.headerValue) {
                        "Time" -> timeCheckBox.isSelected = false
                        "DPS" -> dpsCheckBox.isSelected = false
                        "Damage" -> damageCheckBox.isSelected = false
                        "Σ Damage" -> totalDamageCheckBox.isSelected = false
                        "Mitigated" -> mitigatedCheckBox.isSelected = false
                        "Reason" -> reasonCheckBox.isSelected = false
                    }

                    column.minWidth = 0
                    column.maxWidth = 0

                } else column.preferredWidth = configData.columnWidths!![i]
            }
        }

        // update dps tracker
        dpsTracker.updateIGN(nameField.text.toLowerCase())

        dpsTracker.dpsTableModel = dpsTable.model as DefaultTableModel

        dpsTracker.addDPSTableListener(::dpsTableUpdate)
        dpsTracker.addCombatLogListener(::combatLogUpdate)
    }

/* ------------------------------------------ Listeners ----------------------------------------- */

    /** Handles shortcut key actions */
    private fun shortcutButtonPress(e: KeyEvent?) {
        if (e != null) {
            when (e.keyCode) {
                KeyEvent.VK_ESCAPE -> {
                    if (sidebar.isVisible) minimizeSidebarButtonClick(null)
                    else maximizeSidebarButtonClick(null)
                }
                KeyEvent.VK_T -> {
                    onTopCheckBox.isSelected = !onTopCheckBox.isSelected
                    onTopCheckBoxClick(null)
                }
                KeyEvent.VK_R -> { resetTimerButtonClick(null) }
                KeyEvent.VK_C -> { clearTableButtonClick(null) }
                KeyEvent.VK_1 -> {
                    timeCheckBox.isSelected = !timeCheckBox.isSelected
                    timeCheckBoxClick(null)
                }
                KeyEvent.VK_2 -> {
                    dpsCheckBox.isSelected = !dpsCheckBox.isSelected
                    dpsCheckBoxClick(null)
                }
                KeyEvent.VK_3 -> {
                    damageCheckBox.isSelected = !damageCheckBox.isSelected
                    damageCheckBoxClick(null)
                }
                KeyEvent.VK_4 -> {
                    totalDamageCheckBox.isSelected = !totalDamageCheckBox.isSelected
                    totalDamageCheckBoxClick(null)
                }
                KeyEvent.VK_5 -> {
                    mitigatedCheckBox.isSelected = !mitigatedCheckBox.isSelected
                    mitigatedCheckBoxClick(null)
                }
                KeyEvent.VK_6 -> {
                    reasonCheckBox.isSelected = !reasonCheckBox.isSelected
                    reasonCheckBoxClick(null)
                }
            }
        }
    }

    /** Minimizes the sidebar. */
    @Suppress("UNUSED_PARAMETER")
    private fun minimizeSidebarButtonClick(e: ActionEvent?) {
        topLevelAncestor.minimumSize = windowSmallMinSize
        sidebar.isVisible = false
        minimizedBar.isVisible = true
    }

    /** Toggles having the window on top of all other windows. */
    @Suppress("UNUSED_PARAMETER")
    private fun onTopCheckBoxClick(e: ActionEvent?) {
        (topLevelAncestor as JFrame).isAlwaysOnTop = onTopCheckBox.isSelected
    }

    /** Updates the in-game name used by the DPS tracker. */
    private fun nameFieldUpdate() { dpsTracker.updateIGN(nameField.text.toLowerCase()) }

    /** Resets the timer used by the DPS tracker. */
    @Suppress("UNUSED_PARAMETER")
    private fun resetTimerButtonClick(e: ActionEvent?) { dpsTracker.resetTimer() }

    /** Resets the time and clears the dps table of rows. */
    @Suppress("UNUSED_PARAMETER")
    private fun clearTableButtonClick(e: ActionEvent?) {
        dpsTracker.clearTable()
        dpsTracker.resetTimer()
    }

    /** Reveals the sidebar. */
    @Suppress("UNUSED_PARAMETER")
    private fun maximizeSidebarButtonClick(e: ActionEvent?) {
        sidebar.isVisible = true
        minimizedBar.isVisible = false
        topLevelAncestor.minimumSize = windowSidebarMinSize
    }

    /** Moves the table scroll to the bottom. */
    private fun dpsTableUpdate() {
        val scrollBar = dpsTableScrollPane.verticalScrollBar
        SwingUtilities.invokeLater {
            dpsTable.revalidate()
            scrollBar.value = scrollBar.maximum
        }
    }

    /** Updates the combat log field with the new combat log file name. */
    private fun combatLogUpdate(combatLog: File?) {
        combatLogField.text = if (combatLog == null) "no file" else combatLog.name
    }

    /** Toggles the DPS table time column. */
    @Suppress("UNUSED_PARAMETER")
    private fun timeCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("Time", timeCheckBox.isSelected)
    }

    /** Toggles the DPS table DPS column. */
    @Suppress("UNUSED_PARAMETER")
    private fun dpsCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("DPS", dpsCheckBox.isSelected)
    }

    /** Toggles the DPS table damage column. */
    @Suppress("UNUSED_PARAMETER")
    private fun damageCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("Damage", damageCheckBox.isSelected)
    }

    /** Toggles the DPS table total damage column. */
    @Suppress("UNUSED_PARAMETER")
    private fun totalDamageCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("Σ Damage", totalDamageCheckBox.isSelected)
    }

    /** Toggles the DPS table mitigated column. */
    @Suppress("UNUSED_PARAMETER")
    private fun mitigatedCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("Mitigated", mitigatedCheckBox.isSelected)
    }

    /** Toggles the DPS table reason column. */
    @Suppress("UNUSED_PARAMETER")
    private fun reasonCheckBoxClick(actionEvent: ActionEvent?) {
        setColumnVisible("Reason", reasonCheckBox.isSelected)
    }

/* -------------------------------------------- Util -------------------------------------------- */

    /** Shows or hides the specified column.  */
    private fun setColumnVisible(header: String, isVisible: Boolean) {
        var column: TableColumn? = null
        for (c in dpsTable.columnModel.columns)
            if (c.headerValue as String == header) column = c

        if (column != null) {
            // visible
            if (isVisible) {
                column.maxWidth = colMaxWidth
                column.minWidth = colMinWidth
                column.preferredWidth = 66
            }
            // hidden
            else {
                column.minWidth = 0
                column.maxWidth = 0
            }
        }
    }

    /** Creates a JPanel with a JLabel followed by the specified JComponents. */
    private class LabelPanel(label: String, vararg components: JComponent)
        : JPanel(GridBagLayout()) {

        init {
            val c = GridBagConstraints()

            // label
            c.gridx = 0; c.gridy = 0
            add(JLabel(label), c)

            // components
            c.insets = Insets(0, 5, 0, 0)
            for (component in components) {
                c.gridx++
                add(component, c)
            }
        }
    }
}
