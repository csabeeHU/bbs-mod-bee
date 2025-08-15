package mchorse.bbs_mod.ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mchorse.bbs_mod.ai.AIRequest;
import mchorse.bbs_mod.ai.AIResponse;
import mchorse.bbs_mod.ai.config.AISettings;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Local AI provider for Ollama and LM Studio
 */
public class LocalAIProvider extends BaseAIProvider
{
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LocalAIProvider()
    {
        super("local", "Local AI (Ollama/LM Studio)", 50);
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // Local AI might be slower
            .build();
            
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected boolean isConfigured()
    {
        String endpoint = AISettings.localAiEndpoint != null ? AISettings.localAiEndpoint.get() : null;
        String model = AISettings.localAiModel != null ? AISettings.localAiModel.get() : null;
        return endpoint != null && !endpoint.trim().isEmpty() && 
               model != null && !model.trim().isEmpty();
    }

    @Override
    public CompletableFuture<AIResponse> sendRequest(AIRequest aiRequest)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String endpoint = AISettings.localAiEndpoint.get();
                String model = AISettings.localAiModel.get();
                
                if (endpoint == null || endpoint.trim().isEmpty())
                {
                    return handleError("Local AI endpoint not configured");
                }
                
                if (model == null || model.trim().isEmpty())
                {
                    return handleError("Local AI model not configured");
                }

                // Normalize endpoint URL
                if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://"))
                {
                    endpoint = "http://" + endpoint;
                }
                if (!endpoint.endsWith("/"))
                {
                    endpoint += "/";
                }

                ObjectNode requestJson = objectMapper.createObjectNode();
                requestJson.put("model", model);
                
                // Build the prompt
                StringBuilder fullPrompt = new StringBuilder();
                String systemPrompt = buildSystemPrompt(aiRequest);
                if (systemPrompt != null && !systemPrompt.trim().isEmpty())
                {
                    fullPrompt.append(systemPrompt).append("\n\n");
                }
                fullPrompt.append("User: ").append(aiRequest.getMessage());
                fullPrompt.append("\n\nAssistant: ");
                
                requestJson.put("prompt", fullPrompt.toString());
                requestJson.put("max_tokens", 1000);
                requestJson.put("temperature", 0.7);
                requestJson.put("stream", false);

                RequestBody body = RequestBody.create(objectMapper.writeValueAsString(requestJson), JSON);
                
                // Try Ollama format first
                String url = endpoint + "api/generate";
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute())
                {
                    if (response.isSuccessful())
                    {
                        String responseBody = response.body().string();
                        JsonNode responseJson = objectMapper.readTree(responseBody);
                        
                        // Ollama format
                        JsonNode responseText = responseJson.get("response");
                        if (responseText != null)
                        {
                            return handleSuccess(responseText.asText());
                        }
                    }
                }
                
                // Try LM Studio format (OpenAI-compatible)
                url = endpoint + "v1/chat/completions";
                ObjectNode lmStudioRequest = objectMapper.createObjectNode();
                lmStudioRequest.put("model", model);
                lmStudioRequest.put("max_tokens", 1000);
                lmStudioRequest.put("temperature", 0.7);

                ArrayNode messages = lmStudioRequest.putArray("messages");
                
                if (systemPrompt != null && !systemPrompt.trim().isEmpty())
                {
                    ObjectNode systemMessage = messages.addObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", systemPrompt);
                }

                ObjectNode userMessage = messages.addObject();
                userMessage.put("role", "user");
                userMessage.put("content", aiRequest.getMessage());

                RequestBody lmStudioBody = RequestBody.create(objectMapper.writeValueAsString(lmStudioRequest), JSON);
                
                Request lmStudioReq = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(lmStudioBody)
                    .build();

                try (Response response = httpClient.newCall(lmStudioReq).execute())
                {
                    if (response.isSuccessful())
                    {
                        String responseBody = response.body().string();
                        JsonNode responseJson = objectMapper.readTree(responseBody);
                        
                        JsonNode choices = responseJson.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0)
                        {
                            JsonNode firstChoice = choices.get(0);
                            JsonNode message = firstChoice.get("message");
                            if (message != null)
                            {
                                JsonNode content = message.get("content");
                                if (content != null)
                                {
                                    return handleSuccess(content.asText());
                                }
                            }
                        }
                    }
                    else
                    {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        return handleError("Local AI error: " + response.code() + " - " + errorBody);
                    }
                }
                
                return handleError("Local AI request failed - unable to connect to either Ollama or LM Studio format");
            }
            catch (Exception e)
            {
                return handleError("Local AI request failed: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> testConnection()
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String endpoint = AISettings.localAiEndpoint != null ? AISettings.localAiEndpoint.get() : null;
                if (endpoint == null || endpoint.trim().isEmpty())
                {
                    return false;
                }

                if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://"))
                {
                    endpoint = "http://" + endpoint;
                }
                if (!endpoint.endsWith("/"))
                {
                    endpoint += "/";
                }

                // Test Ollama endpoint
                try
                {
                    Request request = new Request.Builder()
                        .url(endpoint + "api/tags")
                        .get()
                        .build();

                    try (Response response = httpClient.newCall(request).execute())
                    {
                        if (response.isSuccessful())
                        {
                            return true;
                        }
                    }
                }
                catch (Exception e)
                {
                    // Continue to test LM Studio
                }

                // Test LM Studio endpoint
                try
                {
                    Request request = new Request.Builder()
                        .url(endpoint + "v1/models")
                        .get()
                        .build();

                    try (Response response = httpClient.newCall(request).execute())
                    {
                        return response.isSuccessful();
                    }
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            catch (Exception e)
            {
                return false;
            }
        });
    }

    @Override
    public String[] getRequiredConfig()
    {
        return new String[]{"local_ai_endpoint", "local_ai_model"};
    }
}