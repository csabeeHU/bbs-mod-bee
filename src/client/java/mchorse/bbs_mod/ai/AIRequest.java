package mchorse.bbs_mod.ai;

/**
 * Represents an AI request with message content and context
 */
public class AIRequest
{
    private String message;
    private AIContext context;
    private String systemPrompt;

    public AIRequest(String message, AIContext context)
    {
        this(message, context, null);
    }

    public AIRequest(String message, AIContext context, String systemPrompt)
    {
        this.message = message;
        this.context = context;
        this.systemPrompt = systemPrompt;
    }

    public String getMessage()
    {
        return message;
    }

    public AIContext getContext()
    {
        return context;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }
}