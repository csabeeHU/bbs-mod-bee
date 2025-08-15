package mchorse.bbs_mod.ai;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents the context for AI requests, containing information about
 * the current animation work, selected objects, timeline position, etc.
 */
public class AIContext
{
    public enum ContextType
    {
        GENERAL,
        ANIMATION_EDITING,
        KEYFRAME_EDITING,
        CAMERA_WORK,
        TECHNICAL_SUPPORT
    }

    private ContextType type;
    private Map<String, Object> properties;

    public AIContext(ContextType type)
    {
        this.type = type;
        this.properties = new HashMap<>();
    }

    public ContextType getType()
    {
        return type;
    }

    public void setProperty(String key, Object value)
    {
        properties.put(key, value);
    }

    public Object getProperty(String key)
    {
        return properties.get(key);
    }

    public String getPropertyAsString(String key)
    {
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getPropertyAsInt(String key)
    {
        Object value = properties.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }

    public Boolean getPropertyAsBoolean(String key)
    {
        Object value = properties.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    public Map<String, Object> getAllProperties()
    {
        return new HashMap<>(properties);
    }

    /**
     * Create a context for general chat/questions
     */
    public static AIContext general()
    {
        return new AIContext(ContextType.GENERAL);
    }

    /**
     * Create a context for animation editing with current clip information
     */
    public static AIContext animationEditing(String currentClip, int framePosition, int totalFrames)
    {
        AIContext context = new AIContext(ContextType.ANIMATION_EDITING);
        context.setProperty("currentClip", currentClip);
        context.setProperty("framePosition", framePosition);
        context.setProperty("totalFrames", totalFrames);
        return context;
    }

    /**
     * Create a context for keyframe editing work
     */
    public static AIContext keyframeEditing(String selectedBone, int keyframeCount)
    {
        AIContext context = new AIContext(ContextType.KEYFRAME_EDITING);
        context.setProperty("selectedBone", selectedBone);
        context.setProperty("keyframeCount", keyframeCount);
        return context;
    }

    /**
     * Create a context for camera work
     */
    public static AIContext cameraWork(String cameraType, double[] position, double[] rotation)
    {
        AIContext context = new AIContext(ContextType.CAMERA_WORK);
        context.setProperty("cameraType", cameraType);
        context.setProperty("position", position);
        context.setProperty("rotation", rotation);
        return context;
    }

    /**
     * Create a context for technical support
     */
    public static AIContext technicalSupport(String issue, String errorMessage)
    {
        AIContext context = new AIContext(ContextType.TECHNICAL_SUPPORT);
        context.setProperty("issue", issue);
        context.setProperty("errorMessage", errorMessage);
        return context;
    }
}