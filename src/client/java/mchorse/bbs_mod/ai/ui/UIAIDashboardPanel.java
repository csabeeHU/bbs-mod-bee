package mchorse.bbs_mod.ai.ui;

import mchorse.bbs_mod.ai.ui.UIChatPanel;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.dashboard.panels.UIDashboardPanel;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlay;

/**
 * AI Assistant dashboard panel
 */
public class UIAIDashboardPanel extends UIDashboardPanel
{
    public UIChatPanel chatPanel;
    public UIButton settingsButton;

    public UIAIDashboardPanel(UIDashboard dashboard)
    {
        super(dashboard);
        
        this.chatPanel = new UIChatPanel();
        this.settingsButton = new UIButton(IKey.raw("AI Settings"), this::openSettings);
        
        this.chatPanel.relative(this).x(10).y(10).w(1F, -20).h(1F, -50);
        this.settingsButton.relative(this).x(10).y(1F, -35).w(80).h(20);
        
        this.add(this.chatPanel, this.settingsButton);
    }

    private void openSettings(UIButton button)
    {
        UIOverlay.addOverlay(this.getContext(), new UIAISettingsOverlayPanel(), 400, 600);
    }

    @Override
    public boolean canToggleVisibility()
    {
        return true;
    }
}