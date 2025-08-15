package mchorse.bbs_mod.ai.providers;

import mchorse.bbs_mod.ai.AIRequest;
import mchorse.bbs_mod.ai.AIResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for AI providers
 */
public abstract class BaseAIProvider implements IAIProvider
{
    protected final String id;
    protected final String displayName;
    protected final int priority;
    protected boolean enabled = true;

    public BaseAIProvider(String id, String displayName, int priority)
    {
        this.id = id;
        this.displayName = displayName;
        this.priority = priority;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean isAvailable()
    {
        return enabled && isConfigured();
    }

    /**
     * Check if this provider is properly configured
     */
    protected abstract boolean isConfigured();

    /**
     * Build the system prompt for BBS-specific context
     */
    protected String buildSystemPrompt(AIRequest request)
    {
        String systemPrompt = request.getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty())
        {
            return systemPrompt;
        }
        
        // Use BBS-specific prompt manager
        return mchorse.bbs_mod.ai.context.BBSPromptManager.createSystemPrompt(request.getContext());
    }

    /**
     * Handle errors in a consistent way
     */
    protected AIResponse handleError(String error)
    {
        return AIResponse.error(error, getId());
    }

    /**
     * Handle success response
     */
    protected AIResponse handleSuccess(String content)
    {
        return AIResponse.success(content, getId());
    }
}