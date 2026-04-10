package com.example.gitsage.settings

import com.example.gitsage.ai.ModelInfo
import com.example.gitsage.ai.ModelListFetcher
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

class GitSageConfigurable : Configurable {
    private val logger = Logger.getInstance(GitSageConfigurable::class.java)
    private var mainPanel: JPanel? = null
    private val settings: GitSageSettings by lazy { GitSageSettings.getInstance() }

    private var currentConvention: CommitConvention = CommitConvention.CONVENTIONAL_COMMITS
    private var currentLanguage: GenerationLanguage = GenerationLanguage.AUTO

    private lateinit var providerTypeCombo: JComboBox<ProviderType>
    private lateinit var baseUrlField: JBTextField
    private lateinit var apiKeyField: JBPasswordField
    private lateinit var modelCombo: ComboBox<String>
    private lateinit var temperatureField: JBTextField
    private lateinit var maxTokensField: JBTextField
    private lateinit var conventionCombo: JComboBox<CommitConvention>
    private lateinit var languageCombo: JComboBox<GenerationLanguage>
    private lateinit var baseUrlRow: JPanel
    private lateinit var apiKeyRow: JPanel
    private val availableModels = mutableListOf<ModelInfo>()
    private val allModelOptions = mutableListOf<String>()
    private var suppressModelFiltering = false

    override fun getDisplayName(): String = "GitSage"

    override fun createComponent(): JComponent? {
        mainPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)

            val contentPanel = JPanel(GridBagLayout()).apply {
                val gbc = GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    insets = JBUI.insets(5)
                    gridx = 0
                    gridy = 0
                    weightx = 1.0
                }

                add(createGroupLabel("Provider Settings"), gbc)
                gbc.gridy++

                add(createLabeledRow("Provider Type:", createProviderTypeSelector()), gbc)
                gbc.gridy++

                baseUrlField = JBTextField()
                baseUrlRow = createLabeledRow("Base URL:", baseUrlField)
                add(baseUrlRow, gbc)
                gbc.gridy++

                apiKeyField = JBPasswordField()
                apiKeyRow = createLabeledRow("API Key:", apiKeyField)
                add(apiKeyRow, gbc)
                gbc.gridy++

                add(createLabeledRow("Model:", createModelSelector()), gbc)
                gbc.gridy++

                add(createButtonRow(), gbc)
                gbc.gridy++

                temperatureField = JBTextField("0.7")
                add(createLabeledRow("Temperature:", temperatureField), gbc)
                gbc.gridy++

                maxTokensField = JBTextField("500")
                add(createLabeledRow("Max Tokens:", maxTokensField), gbc)
                gbc.gridy++

                gbc.insets = JBUI.insets(15, 5, 5, 5)
                add(createGroupLabel("Generation Settings"), gbc)
                gbc.insets = JBUI.insets(5)
                gbc.gridy++

                conventionCombo = ComboBox(CommitConvention.values())
                add(createLabeledRow("Commit Convention:", conventionCombo), gbc)
                gbc.gridy++

                languageCombo = ComboBox(GenerationLanguage.values())
                add(createLabeledRow("Language:", languageCombo), gbc)
                gbc.gridy++

                gbc.weighty = 1.0
                gbc.fill = GridBagConstraints.BOTH
                add(Box.createVerticalGlue(), gbc)
            }

            add(JScrollPane(contentPanel), BorderLayout.CENTER)
        }

        loadSettings()

        return mainPanel
    }

    private fun createGroupLabel(text: String): JLabel {
        return JBLabel(text).apply {
            font = font.deriveFont(java.awt.Font.BOLD, font.size + 2f)
            border = JBUI.Borders.empty(0, 0, 5, 0)
        }
    }

    private fun createLabeledRow(label: String, component: JComponent): JPanel {
        return JPanel(BorderLayout(JBUI.scale(10), 0)).apply {
            val labelComp = JBLabel(label).apply {
                preferredSize = Dimension(JBUI.scale(120), preferredSize.height)
            }
            add(labelComp, BorderLayout.WEST)
            add(component, BorderLayout.CENTER)
        }
    }

    private fun createProviderTypeSelector(): JComponent {
        providerTypeCombo = ComboBox(ProviderType.values())
        providerTypeCombo.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                label.text = when (value as ProviderType) {
                    ProviderType.CUSTOM -> "OpenAI Compatible"
                    ProviderType.OPENCODE_GO -> "OpenCode Go (API Key Required)"
                    ProviderType.OPENCODE_ZEN -> "OpenCode Zen (API Key Required)"
                    ProviderType.OPENROUTER -> "OpenRouter"
                }
                return label
            }
        }
        providerTypeCombo.addActionListener {
            updateUIForProviderType()
        }
        return providerTypeCombo
    }

    private fun updateUIForProviderType() {
        val selectedType = providerTypeCombo.selectedItem as? ProviderType ?: ProviderType.CUSTOM

        when (selectedType) {
            ProviderType.CUSTOM -> {
                baseUrlRow.isVisible = true
                apiKeyRow.isVisible = true
                baseUrlField.isEnabled = true
                baseUrlField.text = if (baseUrlField.text == "https://opencode.ai" || baseUrlField.text == "https://openrouter.ai/api/v1") "https://api.openai.com/v1" else baseUrlField.text
            }
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> {
                baseUrlRow.isVisible = false
                apiKeyRow.isVisible = true
                baseUrlField.text = "https://opencode.ai"
            }
            ProviderType.OPENROUTER -> {
                baseUrlRow.isVisible = false
                apiKeyRow.isVisible = true
                baseUrlField.text = "https://openrouter.ai/api/v1"
            }
        }
        mainPanel?.revalidate()
        mainPanel?.repaint()
    }

    private fun createModelSelector(): JComponent {
        modelCombo = ComboBox()
        modelCombo.isEditable = true
        setupModelSearch()

        return JPanel(BorderLayout(JBUI.scale(5), 0)).apply {
            add(modelCombo, BorderLayout.CENTER)
        }
    }

    private fun createButtonRow(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(10), 0)).apply {
            add(JButton("Fetch Models").apply {
                addActionListener { fetchModels() }
            })
            add(JButton("Test Connection").apply {
                addActionListener { testConnection() }
            })
        }
    }

    private fun loadSettings() {
        currentConvention = settings.state.convention
        currentLanguage = settings.state.language

        conventionCombo.selectedItem = currentConvention
        languageCombo.selectedItem = currentLanguage

        val selectedProvider = settings.getSelectedProvider()
        if (selectedProvider != null) {
            loadProviderSettings(selectedProvider)
        } else {
            providerTypeCombo.selectedItem = ProviderType.CUSTOM
            baseUrlField.text = "https://api.openai.com/v1"
            setAllModelOptions(emptyList())
            updateUIForProviderType()
        }
    }

    private fun loadProviderSettings(provider: AIProviderConfig) {
        providerTypeCombo.selectedItem = provider.providerType
        baseUrlField.text = provider.getEffectiveBaseUrl()
        temperatureField.text = provider.temperature.toString()
        maxTokensField.text = provider.maxTokens.toString()

        val savedApiKey = CredentialsManager.getApiKey(provider.id)
        apiKeyField.text = savedApiKey ?: ""

        availableModels.clear()
        setAllModelOptions(provider.cachedModels)

        if (provider.model.isNotBlank()) {
            if (!provider.cachedModels.contains(provider.model)) {
                setAllModelOptions(provider.cachedModels + provider.model)
            }
            setModelSelection(provider.model)
        } else {
            setModelSelection("")
        }

        updateUIForProviderType()
    }

    private fun fetchModels() {
        logger.info("[GitSageConfigurable] fetchModels() called")
        
        val providerType = providerTypeCombo.selectedItem as? ProviderType ?: ProviderType.CUSTOM
        val baseUrl = when (providerType) {
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "https://opencode.ai"
            ProviderType.OPENROUTER -> "https://openrouter.ai/api"
            ProviderType.CUSTOM -> baseUrlField.text
        }
        val apiKey = String(apiKeyField.password)

        logger.info("[GitSageConfigurable] ProviderType: $providerType")
        logger.info("[GitSageConfigurable] BaseUrl: $baseUrl")
        logger.info("[GitSageConfigurable] API Key length: ${apiKey.length}")

        if (baseUrl.isBlank()) {
            showMessage("Please enter Base URL", "Validation Error", JOptionPane.WARNING_MESSAGE)
            return
        }

        if (apiKey.isBlank()) {
            showMessage("Please enter API Key", "Validation Error", JOptionPane.WARNING_MESSAGE)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            logger.info("[GitSageConfigurable] Starting coroutine to fetch models")
            try {
                logger.info("[GitSageConfigurable] Calling ModelListFetcher.fetchModels()")
                val models = ModelListFetcher.fetchModels(baseUrl, apiKey, providerType)
                logger.info("[GitSageConfigurable] Fetched ${models.size} models")
                withContext(Dispatchers.Main) {
                    availableModels.clear()
                    availableModels.addAll(models)

                    val modelIds = models.map { it.id }.toMutableList()
                    setAllModelOptions(modelIds)
                    updateCachedModels(modelIds)

                    if (models.isNotEmpty()) {
                        setModelSelection(models.first().id)
                        showMessage(
                            "Successfully fetched ${models.size} models",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } else {
                        showMessage(
                            "No models found. You can still manually enter a model name.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error("[GitSageConfigurable] Exception in fetchModels", e)
                withContext(Dispatchers.Main) {
                    val errorDetail = buildString {
                        appendLine("Error Type: ${e.javaClass.simpleName}")
                        appendLine()
                        appendLine("Message: ${e.message}")
                        appendLine()
                        appendLine("Stack Trace:")
                        e.stackTrace.take(15).forEach { appendLine(it.toString()) }
                    }
                    showDetailedError("Failed to Fetch Models", errorDetail)
                }
            }
        }
    }

    private fun testConnection() {
        logger.info("[GitSageConfigurable] testConnection() called")

        val providerType = providerTypeCombo.selectedItem as? ProviderType ?: ProviderType.CUSTOM
        val baseUrl = when (providerType) {
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "https://opencode.ai"
            ProviderType.OPENROUTER -> "https://openrouter.ai/api"
            ProviderType.CUSTOM -> baseUrlField.text
        }
        val apiKey = String(apiKeyField.password)
        val selectedModel = modelCombo.selectedItem?.toString()

        logger.info("[GitSageConfigurable] testConnection - ProviderType: $providerType")
        logger.info("[GitSageConfigurable] testConnection - BaseUrl: $baseUrl")
        logger.info("[GitSageConfigurable] testConnection - API Key length: ${apiKey.length}")
        logger.info("[GitSageConfigurable] testConnection - Selected Model: $selectedModel")

        if (baseUrl.isBlank()) {
            showMessage("Please enter Base URL", "Validation Error", JOptionPane.WARNING_MESSAGE)
            return
        }

        if (apiKey.isBlank()) {
            showMessage("Please enter API Key", "Validation Error", JOptionPane.WARNING_MESSAGE)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            logger.info("[GitSageConfigurable] testConnection - Starting coroutine")
            try {
                logger.info("[GitSageConfigurable] testConnection - Calling ModelListFetcher.testConnection()")
                val (success, message) = ModelListFetcher.testConnection(baseUrl, apiKey, providerType, selectedModel)
                logger.info("[GitSageConfigurable] testConnection - Result: success=$success")
                withContext(Dispatchers.Main) {
                    if (success) {
                        showMessage(message, "Success", JOptionPane.INFORMATION_MESSAGE)
                    } else {
                        showDetailedError("Connection Failed", message)
                    }
                }
            } catch (e: Exception) {
                logger.error("[GitSageConfigurable] testConnection - Exception occurred", e)
                withContext(Dispatchers.Main) {
                    val errorDetail = buildString {
                        appendLine("Error Type: ${e.javaClass.simpleName}")
                        appendLine()
                        appendLine("Message: ${e.message}")
                        appendLine()
                        appendLine("Stack Trace:")
                        e.stackTrace.take(15).forEach { appendLine(it.toString()) }
                    }
                    showDetailedError("Connection Failed", errorDetail)
                }
            }
        }
    }

    private fun showMessage(message: String, title: String, messageType: Int) {
        JOptionPane.showMessageDialog(mainPanel, message, title, messageType)
    }

    private fun showDetailedError(title: String, message: String) {
        val textArea = javax.swing.JTextArea(message).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            rows = 20
            columns = 60
            font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        }

        val scrollPane = javax.swing.JScrollPane(textArea).apply {
            preferredSize = java.awt.Dimension(600, 400)
        }

        JOptionPane.showMessageDialog(
            mainPanel,
            scrollPane,
            title,
            JOptionPane.ERROR_MESSAGE
        )
    }

    override fun isModified(): Boolean {
        val provider = settings.getSelectedProvider()
        return provider?.let {
            it.providerType != providerTypeCombo.selectedItem ||
            it.baseUrl != baseUrlField.text ||
            CredentialsManager.getApiKey(it.id) != String(apiKeyField.password) ||
            it.model != getModelEditorText() ||
            it.temperature.toString() != temperatureField.text ||
            it.maxTokens.toString() != maxTokensField.text ||
            currentConvention != conventionCombo.selectedItem ||
            currentLanguage != languageCombo.selectedItem
        } ?: true
    }

    override fun apply() {
        val providerType = providerTypeCombo.selectedItem as ProviderType

        val providerId = when (providerType) {
            ProviderType.CUSTOM -> "openai"
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "opencode"
            ProviderType.OPENROUTER -> "openrouter"
        }

        val providerName = when (providerType) {
            ProviderType.CUSTOM -> "OpenAI"
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "OpenCode"
            ProviderType.OPENROUTER -> "OpenRouter"
        }

        val provider = AIProviderConfig(
            id = providerId,
            name = providerName,
            providerType = providerType,
            baseUrl = baseUrlField.text,
            model = getModelEditorText(),
            temperature = temperatureField.text.toDoubleOrNull() ?: 0.7,
            maxTokens = maxTokensField.text.toIntOrNull() ?: 500,
            apiKey = String(apiKeyField.password),
            cachedModels = getCurrentCachedModels()
        )

        settings.saveProvider(provider)
        settings.setSelectedProvider(providerId)
        settings.setConvention(conventionCombo.selectedItem as CommitConvention)
        settings.setLanguage(languageCombo.selectedItem as GenerationLanguage)
    }

    private fun getCurrentCachedModels(): MutableList<String> {
        val models = allModelOptions.toMutableList()
        val selectedModel = getModelEditorText()
        if (selectedModel.isNotBlank() && !models.contains(selectedModel)) {
            models.add(selectedModel)
        }
        return models
    }

    private fun setupModelSearch() {
        val editorComponent = modelCombo.editor.editorComponent as? JTextComponent ?: return
        editorComponent.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = filterModelItems()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = filterModelItems()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = filterModelItems()
        })
    }

    private fun filterModelItems() {
        if (suppressModelFiltering) return

        val query = getModelEditorText()
        val sourceItems = getFilterSourceModels()
        val filteredItems = sourceItems
            .filter { it.contains(query, ignoreCase = true) }

        refreshModelComboItems(if (query.isBlank()) sourceItems else filteredItems)
        setModelSelection(query)

        if (filteredItems.isNotEmpty() && modelCombo.isDisplayable) {
            modelCombo.setPopupVisible(true)
        } else {
            modelCombo.setPopupVisible(false)
        }
    }

    private fun getFilterSourceModels(): List<String> {
        val fetchedModelIds = availableModels.map { it.id }
        return (fetchedModelIds + allModelOptions)
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    private fun setAllModelOptions(items: List<String>) {
        allModelOptions.clear()
        allModelOptions.addAll(items.filter { it.isNotBlank() }.distinct().sorted())
        refreshModelComboItems(allModelOptions)
    }

    private fun refreshModelComboItems(items: List<String>) {
        suppressModelFiltering = true
        try {
            modelCombo.removeAllItems()
            items.forEach { modelCombo.addItem(it) }
        } finally {
            suppressModelFiltering = false
        }
    }

    private fun setModelSelection(value: String) {
        suppressModelFiltering = true
        try {
            modelCombo.selectedItem = value
            val editorComponent = modelCombo.editor.editorComponent as? JTextComponent
            editorComponent?.text = value
        } finally {
            suppressModelFiltering = false
        }
    }

    private fun getModelEditorText(): String {
        val editorComponent = modelCombo.editor.editorComponent as? JTextComponent
        return editorComponent?.text?.trim().orEmpty()
    }

    private fun updateCachedModels(models: MutableList<String>) {
        val selectedProvider = settings.getSelectedProvider()
        selectedProvider?.let { provider ->
            val updatedProvider = provider.copy(cachedModels = models)
            settings.saveProvider(updatedProvider)
        }
    }

    override fun reset() {
        loadSettings()
    }
}
