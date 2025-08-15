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
 * OpenAI API provider for AI functionality
 */
public class OpenAIProvider extends BaseAIProvider
{
    private static final String API_BASE_URL = "https://api.openai.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIProvider()
    {
        super("openai", "OpenAI", 100);
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
            
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected boolean isConfigured()
    {
        String apiKey = AISettings.openaiApiKey != null ? AISettings.openaiApiKey.get() : null;
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public CompletableFuture<AIResponse> sendRequest(AIRequest aiRequest)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String apiKey = AISettings.openaiApiKey.get();
                String model = AISettings.openaiModel != null ? AISettings.openaiModel.get() : "gpt-3.5-turbo";
                
                if (apiKey == null || apiKey.trim().isEmpty())
                {
                    return handleError("OpenAI API key not configured");
                }

                ObjectNode requestJson = objectMapper.createObjectNode();
                requestJson.put("model", model);
                requestJson.put("max_tokens", 1000);
                requestJson.put("temperature", 0.7);

                ArrayNode messages = requestJson.putArray("messages");
                
                // Add system message if we have context
                String systemPrompt = buildSystemPrompt(aiRequest);
                if (systemPrompt != null && !systemPrompt.trim().isEmpty())
                {
                    ObjectNode systemMessage = messages.addObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", systemPrompt);
                }

                // Add user message
                ObjectNode userMessage = messages.addObject();
                userMessage.put("role", "user");
                userMessage.put("content", aiRequest.getMessage());

                RequestBody body = RequestBody.create(objectMapper.writeValueAsString(requestJson), JSON);
                
                Request request = new Request.Builder()
                    .url(API_BASE_URL + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute())
                {
                    if (!response.isSuccessful())
                    {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        return handleError("OpenAI API error: " + response.code() + " - " + errorBody);
                    }

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
                    
                    return handleError("Invalid response format from OpenAI");
                }
            }
            catch (Exception e)
            {
                return handleError("OpenAI request failed: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> testConnection()
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String apiKey = AISettings.openaiApiKey != null ? AISettings.openaiApiKey.get() : null;
                if (apiKey == null || apiKey.trim().isEmpty())
                {
                    return false;
                }

                Request request = new Request.Builder()
                    .url(API_BASE_URL + "/models")
                    .addHeader("Authorization", "Bearer " + apiKey)
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
        });
    }

    @Override
    public String[] getRequiredConfig()
    {
        return new String[]{"openai_api_key", "openai_model"};
    }
}