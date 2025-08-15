package mchorse.bbs_mod.ai.ui;

import mchorse.bbs_mod.ai.AIManager;
import mchorse.bbs_mod.ai.config.AISettings;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.ScrollDirection;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Colors;

/**
 * AI Settings overlay panel for configuring AI providers
 */
public class UIAISettingsOverlayPanel extends UIOverlayPanel
{
    public UIScrollView content;
    public UIButton testButton;
    public UILabel statusLabel;

    public UIAISettingsOverlayPanel()
    {
        super(IKey.raw("AI Assistant Settings"));
        
        this.content = new UIScrollView(ScrollDirection.VERTICAL);
        this.content.scroll.scrollSpeed = 51;
        
        this.testButton = new UIButton(IKey.raw("Test AI Providers"), this::testProviders);
        this.statusLabel = UI.label(IKey.raw(""));
        
        this.content.full(this.area);
        this.content.column().scroll().vertical().stretch().padding(10).height(20);
        
        this.testButton.relative(this.area).x(10).y(1F, -35).w(120).h(20);
        this.statusLabel.relative(this.area).x(140).y(1F, -31);
        
        this.add(this.content, this.testButton, this.statusLabel);
        
        this.setupControls();
    }

    private void setupControls()
    {
        // General AI Settings
        this.content.add(UI.label(IKey.raw("AI Assistant")).marginTop(10).color(Colors.ACTIVE));
        
        UIToggle aiEnabled = new UIToggle(IKey.raw("Enable AI Assistant"), (toggle) -> {
            AISettings.aiEnabled.set(toggle.getValue());
        });
        aiEnabled.setValue(AISettings.aiEnabled.get());
        this.content.add(aiEnabled.marginTop(5));
        
        UIToggle autoSuggestions = new UIToggle(IKey.raw("Auto Suggestions"), (toggle) -> {
            AISettings.aiAutoSuggestions.set(toggle.getValue());
        });
        autoSuggestions.setValue(AISettings.aiAutoSuggestions.get());
        this.content.add(autoSuggestions.marginTop(5));

        // OpenAI Settings
        this.content.add(UI.label(IKey.raw("OpenAI Configuration")).marginTop(20).color(Colors.ACTIVE));
        
        UIToggle openaiEnabled = new UIToggle(IKey.raw("Enable OpenAI"), (toggle) -> {
            AISettings.openaiEnabled.set(toggle.getValue());
        });
        openaiEnabled.setValue(AISettings.openaiEnabled.get());
        this.content.add(openaiEnabled.marginTop(5));
        
        UITextbox openaiApiKey = new UITextbox(100, (text) -> {
            AISettings.openaiApiKey.set(text);
        });
        openaiApiKey.setText(AISettings.openaiApiKey.get());
        openaiApiKey.placeholder = "Enter your OpenAI API key...";
        this.content.add(UI.label(IKey.raw("API Key:")).marginTop(10), openaiApiKey.marginTop(5));
        
        UITextbox openaiModel = new UITextbox(50, (text) -> {
            AISettings.openaiModel.set(text);
        });
        openaiModel.setText(AISettings.openaiModel.get());
        openaiModel.placeholder = "gpt-3.5-turbo";
        this.content.add(UI.label(IKey.raw("Model:")).marginTop(10), openaiModel.marginTop(5));

        // Local AI Settings
        this.content.add(UI.label(IKey.raw("Local AI Configuration")).marginTop(20).color(Colors.ACTIVE));
        
        UIToggle localEnabled = new UIToggle(IKey.raw("Enable Local AI"), (toggle) -> {
            AISettings.localAiEnabled.set(toggle.getValue());
        });
        localEnabled.setValue(AISettings.localAiEnabled.get());
        this.content.add(localEnabled.marginTop(5));
        
        UITextbox localEndpoint = new UITextbox(100, (text) -> {
            AISettings.localAiEndpoint.set(text);
        });
        localEndpoint.setText(AISettings.localAiEndpoint.get());
        localEndpoint.placeholder = "localhost:11434";
        this.content.add(UI.label(IKey.raw("Endpoint:")).marginTop(10), localEndpoint.marginTop(5));
        
        UITextbox localModel = new UITextbox(50, (text) -> {
            AISettings.localAiModel.set(text);
        });
        localModel.setText(AISettings.localAiModel.get());
        localModel.placeholder = "llama2";
        this.content.add(UI.label(IKey.raw("Model:")).marginTop(10), localModel.marginTop(5));

        // Help text
        this.content.add(UI.label(IKey.raw("Help:")).marginTop(20).color(Colors.ACTIVE));
        this.content.add(UI.label(IKey.raw("OpenAI: Requires API key from openai.com")).marginTop(5));
        this.content.add(UI.label(IKey.raw("Local AI: Supports Ollama (ollama.ai) and LM Studio")).marginTop(5));
        this.content.add(UI.label(IKey.raw("For Ollama: Install and run 'ollama run llama2'")).marginTop(5));
        this.content.add(UI.label(IKey.raw("For LM Studio: Start local server at localhost:1234")).marginTop(5));
    }

    private void testProviders(UIButton button)
    {
        this.statusLabel.label = IKey.raw("Testing providers...");
        this.statusLabel.color(0xFFFF9800); // Orange
        this.testButton.setEnabled(false);
        
        AIManager.getInstance().testAllProviders().thenAccept(statuses -> {
            StringBuilder result = new StringBuilder("Test Results: ");
            boolean anySuccess = false;
            
            for (AIManager.ProviderStatus status : statuses)
            {
                result.append(status.getDisplayName()).append(" - ");
                result.append(status.isAvailable() ? "✓" : "✗");
                result.append(" ");
                
                if (status.isAvailable())
                {
                    anySuccess = true;
                }
            }
            
            this.statusLabel.label = IKey.raw(result.toString());
            this.statusLabel.color(anySuccess ? 0xFF4CAF50 : 0xFFF44336); // Green or Red
            this.testButton.setEnabled(true);
        });
    }

    @Override
    protected void renderBackground(UIContext context)
    {
        this.area.render(context.batcher, Colors.A75, 0xcc000000);
    }
}