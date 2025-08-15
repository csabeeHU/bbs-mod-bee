package mchorse.bbs_mod.ai.context;

import mchorse.bbs_mod.ai.AIContext;

/**
 * BBS-specific prompt manager that creates context-aware prompts for AI assistants
 */
public class BBSPromptManager
{
    /**
     * Create a system prompt for the BBS animation assistant
     */
    public static String createSystemPrompt(AIContext context)
    {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert AI assistant specialized in Minecraft animation creation using the BBS (Blender-like Block Studio) mod. ");
        prompt.append("You help users create cinematic animations, optimize their workflow, and solve technical issues.\n\n");
        
        prompt.append("BBS MOD CONTEXT:\n");
        prompt.append("- BBS is a Minecraft mod for creating professional animations and films\n");
        prompt.append("- It supports keyframe animation, camera movements, and complex scenes\n");
        prompt.append("- Users can animate entities, create camera clips, and manage timelines\n");
        prompt.append("- The mod supports various animation techniques like morphing and particle effects\n\n");
        
        if (context != null)
        {
            switch (context.getType())
            {
                case ANIMATION_EDITING:
                    prompt.append("CURRENT CONTEXT: Animation Editing\n");
                    addAnimationEditingContext(prompt, context);
                    break;
                case KEYFRAME_EDITING:
                    prompt.append("CURRENT CONTEXT: Keyframe Editing\n");
                    addKeyframeEditingContext(prompt, context);
                    break;
                case CAMERA_WORK:
                    prompt.append("CURRENT CONTEXT: Camera Work\n");
                    addCameraWorkContext(prompt, context);
                    break;
                case TECHNICAL_SUPPORT:
                    prompt.append("CURRENT CONTEXT: Technical Support\n");
                    addTechnicalSupportContext(prompt, context);
                    break;
                default:
                    prompt.append("CURRENT CONTEXT: General Assistant\n");
                    break;
            }
        }
        
        prompt.append("\nYOUR CAPABILITIES:\n");
        prompt.append("1. Animation Planning: Help design animation sequences and storyboards\n");
        prompt.append("2. Keyframe Optimization: Suggest timing, easing, and interpolation improvements\n");
        prompt.append("3. Camera Work: Advise on camera movements, angles, and cinematic techniques\n");
        prompt.append("4. Creative Suggestions: Propose animation ideas and creative solutions\n");
        prompt.append("5. Technical Help: Troubleshoot issues and optimize performance\n");
        prompt.append("6. Workflow Tips: Share best practices and efficient techniques\n\n");
        
        prompt.append("RESPONSE GUIDELINES:\n");
        prompt.append("- Provide practical, actionable advice specific to BBS mod\n");
        prompt.append("- Use clear step-by-step instructions when explaining procedures\n");
        prompt.append("- Suggest specific keyframe timings and easing when relevant\n");
        prompt.append("- Reference BBS mod features and UI elements accurately\n");
        prompt.append("- Offer creative alternatives when possible\n");
        prompt.append("- Keep responses concise but comprehensive\n");
        prompt.append("- Use animation and filmmaking terminology appropriately\n");
        
        return prompt.toString();
    }

    private static void addAnimationEditingContext(StringBuilder prompt, AIContext context)
    {
        String currentClip = context.getPropertyAsString("currentClip");
        Integer framePosition = context.getPropertyAsInt("framePosition");
        Integer totalFrames = context.getPropertyAsInt("totalFrames");
        
        if (currentClip != null)
        {
            prompt.append("- Currently editing clip: ").append(currentClip).append("\n");
        }
        if (framePosition != null && totalFrames != null)
        {
            prompt.append("- Timeline position: frame ").append(framePosition).append(" of ").append(totalFrames).append("\n");
        }
        prompt.append("- Focus on animation flow, timing, and clip transitions\n");
    }

    private static void addKeyframeEditingContext(StringBuilder prompt, AIContext context)
    {
        String selectedBone = context.getPropertyAsString("selectedBone");
        Integer keyframeCount = context.getPropertyAsInt("keyframeCount");
        
        if (selectedBone != null)
        {
            prompt.append("- Currently working on bone/part: ").append(selectedBone).append("\n");
        }
        if (keyframeCount != null)
        {
            prompt.append("- Number of keyframes: ").append(keyframeCount).append("\n");
        }
        prompt.append("- Focus on keyframe placement, interpolation, and animation curves\n");
    }

    private static void addCameraWorkContext(StringBuilder prompt, AIContext context)
    {
        String cameraType = context.getPropertyAsString("cameraType");
        
        if (cameraType != null)
        {
            prompt.append("- Camera type: ").append(cameraType).append("\n");
        }
        prompt.append("- Focus on cinematography, camera movements, and visual storytelling\n");
    }

    private static void addTechnicalSupportContext(StringBuilder prompt, AIContext context)
    {
        String issue = context.getPropertyAsString("issue");
        String errorMessage = context.getPropertyAsString("errorMessage");
        
        if (issue != null)
        {
            prompt.append("- Reported issue: ").append(issue).append("\n");
        }
        if (errorMessage != null)
        {
            prompt.append("- Error message: ").append(errorMessage).append("\n");
        }
        prompt.append("- Focus on troubleshooting, optimization, and technical solutions\n");
    }

    /**
     * Common animation prompts that users can quickly select
     */
    public static final String[] QUICK_PROMPTS = {
        "Help me create a walking animation",
        "How do I smooth out jerky camera movements?",
        "Suggest ideas for an action scene",
        "How do I optimize animation performance?",
        "Explain keyframe interpolation and easing",
        "Help me plan a cinematic sequence",
        "How do I create realistic character movements?",
        "Tips for dramatic camera angles",
        "How to synchronize animation with audio",
        "Best practices for scene composition"
    };

    /**
     * Get animation-specific tips based on context
     */
    public static String getContextualTip(AIContext.ContextType contextType)
    {
        switch (contextType)
        {
            case ANIMATION_EDITING:
                return "Tip: Use the timeline scrubber to preview your animation in real-time. Right-click on clips for additional options.";
            case KEYFRAME_EDITING:
                return "Tip: Hold Shift while dragging keyframes to maintain their relative timing. Use Ctrl+Z to undo changes.";
            case CAMERA_WORK:
                return "Tip: Try the rule of thirds for better composition. Use smooth camera movements for professional results.";
            case TECHNICAL_SUPPORT:
                return "Tip: Check the console for error messages. Save your work frequently to avoid data loss.";
            default:
                return "Tip: Use Ctrl+S to save your project regularly. The AI assistant can help with any animation questions!";
        }
    }
}