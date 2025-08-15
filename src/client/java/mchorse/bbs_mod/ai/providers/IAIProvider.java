package mchorse.bbs_mod.ai.providers;

import mchorse.bbs_mod.ai.AIRequest;
import mchorse.bbs_mod.ai.AIResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI providers (OpenAI, Local AI, etc.)
 */
public interface IAIProvider
{
    /**
     * Get the unique identifier for this provider
     */
    String getId();

    /**
     * Get the display name for this provider
     */
    String getDisplayName();

    /**
     * Check if this provider is available and configured
     */
    boolean isAvailable();

    /**
     * Send a request to the AI provider
     * @param request The AI request
     * @return A CompletableFuture containing the response
     */
    CompletableFuture<AIResponse> sendRequest(AIRequest request);

    /**
     * Test the connection to the provider
     */
    CompletableFuture<Boolean> testConnection();

    /**
     * Get configuration requirements for this provider
     */
    String[] getRequiredConfig();

    /**
     * Get the priority of this provider (higher = more preferred)
     */
    int getPriority();
}