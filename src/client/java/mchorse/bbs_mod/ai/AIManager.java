package mchorse.bbs_mod.ai;

import mchorse.bbs_mod.ai.providers.IAIProvider;
import mchorse.bbs_mod.ai.providers.LocalAIProvider;
import mchorse.bbs_mod.ai.providers.OpenAIProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main AI manager that handles multiple providers and fallback logic
 */
public class AIManager
{
    private static AIManager instance;
    private final List<IAIProvider> providers;

    private AIManager()
    {
        this.providers = new ArrayList<>();
        
        // Register providers
        this.providers.add(new OpenAIProvider());
        this.providers.add(new LocalAIProvider());
        
        // Sort by priority (highest first)
        this.providers.sort(Comparator.comparingInt(IAIProvider::getPriority).reversed());
    }

    public static AIManager getInstance()
    {
        if (instance == null)
        {
            instance = new AIManager();
        }
        return instance;
    }

    /**
     * Send a request to available AI providers with fallback
     */
    public CompletableFuture<AIResponse> sendRequest(AIRequest request)
    {
        List<IAIProvider> availableProviders = getAvailableProviders();
        
        if (availableProviders.isEmpty())
        {
            return CompletableFuture.completedFuture(
                AIResponse.error("No AI providers available. Please configure an AI provider in settings.", "none")
            );
        }

        return sendRequestWithFallback(request, availableProviders, 0);
    }

    private CompletableFuture<AIResponse> sendRequestWithFallback(AIRequest request, List<IAIProvider> providers, int index)
    {
        if (index >= providers.size())
        {
            return CompletableFuture.completedFuture(
                AIResponse.error("All AI providers failed to respond", "fallback")
            );
        }

        IAIProvider provider = providers.get(index);
        
        return provider.sendRequest(request).thenCompose(response -> {
            if (response.isSuccess())
            {
                return CompletableFuture.completedFuture(response);
            }
            else
            {
                // Try next provider in fallback
                return sendRequestWithFallback(request, providers, index + 1);
            }
        }).exceptionally(throwable -> {
            // If this provider fails with exception, try next
            if (index + 1 < providers.size())
            {
                return sendRequestWithFallback(request, providers, index + 1).join();
            }
            else
            {
                return AIResponse.error("All AI providers failed: " + throwable.getMessage(), "fallback");
            }
        });
    }

    /**
     * Get all registered providers
     */
    public List<IAIProvider> getAllProviders()
    {
        return new ArrayList<>(providers);
    }

    /**
     * Get only available providers (configured and enabled)
     */
    public List<IAIProvider> getAvailableProviders()
    {
        return providers.stream()
            .filter(IAIProvider::isAvailable)
            .toList();
    }

    /**
     * Get provider by ID
     */
    public IAIProvider getProvider(String id)
    {
        return providers.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Test all providers and return their status
     */
    public CompletableFuture<List<ProviderStatus>> testAllProviders()
    {
        List<CompletableFuture<ProviderStatus>> futures = providers.stream()
            .map(provider -> {
                if (!provider.isAvailable())
                {
                    return CompletableFuture.completedFuture(
                        new ProviderStatus(provider.getId(), provider.getDisplayName(), false, "Not configured")
                    );
                }
                
                return provider.testConnection().thenApply(success -> 
                    new ProviderStatus(provider.getId(), provider.getDisplayName(), success, 
                        success ? "Connected" : "Connection failed")
                ).exceptionally(throwable ->
                    new ProviderStatus(provider.getId(), provider.getDisplayName(), false, 
                        "Error: " + throwable.getMessage())
                );
            })
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    /**
     * Status information for a provider
     */
    public static class ProviderStatus
    {
        private final String id;
        private final String displayName;
        private final boolean available;
        private final String status;

        public ProviderStatus(String id, String displayName, boolean available, String status)
        {
            this.id = id;
            this.displayName = displayName;
            this.available = available;
            this.status = status;
        }

        public String getId()
        {
            return id;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public boolean isAvailable()
        {
            return available;
        }

        public String getStatus()
        {
            return status;
        }
    }
}