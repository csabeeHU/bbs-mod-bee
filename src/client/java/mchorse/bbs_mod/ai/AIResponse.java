package mchorse.bbs_mod.ai;

/**
 * Represents an AI response with content and metadata
 */
public class AIResponse
{
    private String content;
    private boolean success;
    private String error;
    private String providerId;
    private long timestamp;

    public AIResponse(String content, boolean success, String providerId)
    {
        this.content = content;
        this.success = success;
        this.providerId = providerId;
        this.timestamp = System.currentTimeMillis();
    }

    public static AIResponse success(String content, String providerId)
    {
        return new AIResponse(content, true, providerId);
    }

    public static AIResponse error(String error, String providerId)
    {
        AIResponse response = new AIResponse(null, false, providerId);
        response.error = error;
        return response;
    }

    public String getContent()
    {
        return content;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getError()
    {
        return error;
    }

    public String getProviderId()
    {
        return providerId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}