package mchorse.bbs_mod.ai.config;

import mchorse.bbs_mod.settings.SettingsBuilder;
import mchorse.bbs_mod.settings.values.ValueBoolean;
import mchorse.bbs_mod.settings.values.ValueString;

/**
 * AI-specific settings for configuration
 */
public class AISettings
{
    // OpenAI settings
    public static ValueString openaiApiKey;
    public static ValueString openaiModel;
    public static ValueBoolean openaiEnabled;

    // Local AI settings  
    public static ValueString localAiEndpoint;
    public static ValueString localAiModel;
    public static ValueBoolean localAiEnabled;

    // General AI settings
    public static ValueBoolean aiEnabled;
    public static ValueBoolean aiAutoSuggestions;
    public static ValueString aiDefaultContext;

    public static void register(SettingsBuilder builder)
    {
        // General AI settings
        builder.category("ai");
        aiEnabled = builder.getBoolean("enabled", true);
        aiAutoSuggestions = builder.getBoolean("auto_suggestions", true);
        aiDefaultContext = builder.getString("default_context", "general");

        // OpenAI settings
        builder.category("ai_openai");
        openaiEnabled = builder.getBoolean("enabled", true);
        openaiApiKey = builder.getString("api_key", "");
        openaiModel = builder.getString("model", "gpt-3.5-turbo");

        // Local AI settings
        builder.category("ai_local");
        localAiEnabled = builder.getBoolean("enabled", true);
        localAiEndpoint = builder.getString("endpoint", "localhost:11434");
        localAiModel = builder.getString("model", "llama2");
    }
}