package mchorse.bbs_mod.ai.context;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.ai.AIContext;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.dashboard.panels.UIDashboardPanel;
import mchorse.bbs_mod.ui.film.UIFilmPanel;

/**
 * Detects the current context in BBS to provide relevant AI assistance
 */
public class BBSContextDetector
{
    /**
     * Detect the current context based on BBS state
     */
    public static AIContext detectCurrentContext()
    {
        try
        {
            UIDashboard dashboard = BBSModClient.getDashboard();
            
            if (dashboard == null || dashboard.getPanels() == null)
            {
                return AIContext.general();
            }

            UIDashboardPanel currentPanel = dashboard.getPanels().panel;
            
            if (currentPanel instanceof UIFilmPanel filmPanel)
            {
                return detectFilmPanelContext(filmPanel);
            }
            
            // TODO: Add detection for other panel types (model blocks, morphing, etc.)
            
            return AIContext.general();
        }
        catch (Exception e)
        {
            // If context detection fails, fall back to general context
            return AIContext.general();
        }
    }

    private static AIContext detectFilmPanelContext(UIFilmPanel filmPanel)
    {
        try
        {
            // Try to get information about current animation work
            // This is a simplified version - in reality, we'd need to access the film panel's internal state
            
            AIContext context = AIContext.animationEditing("Unknown Clip", 0, 100);
            
            // Add additional context based on what we can detect
            context.setProperty("panelType", "film");
            context.setProperty("mode", "editing");
            
            return context;
        }
        catch (Exception e)
        {
            return AIContext.general();
        }
    }

    /**
     * Get user-friendly description of current context
     */
    public static String getContextDescription()
    {
        AIContext context = detectCurrentContext();
        
        switch (context.getType())
        {
            case ANIMATION_EDITING:
                return "Currently editing animations in the Film panel";
            case KEYFRAME_EDITING:
                return "Working with keyframes and timing";
            case CAMERA_WORK:
                return "Setting up camera movements and shots";
            case TECHNICAL_SUPPORT:
                return "Troubleshooting technical issues";
            default:
                return "General BBS assistance";
        }
    }

    /**
     * Get relevant quick prompts for the current context
     */
    public static String[] getContextualQuickPrompts()
    {
        AIContext context = detectCurrentContext();
        
        switch (context.getType())
        {
            case ANIMATION_EDITING:
                return new String[]{
                    "How do I create smooth character movements?",
                    "Help me with animation timing and pacing",
                    "Suggest improvements for my current animation",
                    "How do I synchronize multiple animated elements?"
                };
            case KEYFRAME_EDITING:
                return new String[]{
                    "Explain keyframe interpolation types",
                    "How do I fix jerky animations?",
                    "What's the best easing for natural movement?",
                    "How to create smooth transitions between poses?"
                };
            case CAMERA_WORK:
                return new String[]{
                    "Suggest cinematic camera angles",
                    "How do I create smooth camera movements?",
                    "Tips for dramatic shots and composition",
                    "How to plan a camera sequence?"
                };
            default:
                return BBSPromptManager.QUICK_PROMPTS;
        }
    }
}