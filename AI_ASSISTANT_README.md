# BBS Mod AI Assistant Integration

## Overview

The BBS mod now includes a hybrid AI assistant that can help with animation creation, keyframe optimization, creative suggestions, and technical support. The AI system supports both cloud-based (OpenAI) and local AI providers (Ollama, LM Studio) with automatic fallback.

## Features

### AI Assistant Capabilities
- **Natural Language Animation Help**: Describe what you want to animate and get step-by-step guidance
- **Keyframe Optimization**: Get suggestions for improving animation timing and smoothness
- **Creative Assistance**: Ideas for animation sequences, camera work, and scene composition
- **Technical Support**: Troubleshooting help and performance optimization tips
- **Context-Aware Responses**: AI understands what you're currently working on in BBS

### Hybrid AI System
- **OpenAI API Support**: Use GPT-3.5-turbo or GPT-4 for high-quality responses
- **Local AI Support**: Use Ollama or LM Studio for privacy and offline usage
- **Automatic Fallback**: If one provider fails, automatically tries the next available
- **Secure Configuration**: API keys stored securely in BBS settings

## Setup Instructions

### Option 1: OpenAI API (Cloud-based)

1. **Get an OpenAI API Key**:
   - Go to [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)
   - Create an account or sign in
   - Generate a new API key
   - Copy the key (starts with `sk-`)

2. **Configure in BBS**:
   - Open BBS mod in Minecraft
   - Press `0` to open the dashboard
   - Click the AI Assistant panel (processor icon)
   - Click "AI Settings"
   - Enable "Enable AI Assistant" and "Enable OpenAI"
   - Paste your API key in the "API Key" field
   - Set model to `gpt-3.5-turbo` (or `gpt-4` if you have access)
   - Click "Test AI Providers" to verify

### Option 2: Local AI with Ollama (Offline)

1. **Install Ollama**:
   - Go to [https://ollama.ai](https://ollama.ai)
   - Download and install Ollama for your OS
   - Open terminal/command prompt

2. **Download a Model**:
   ```bash
   ollama pull llama2
   # or for better performance:
   ollama pull mistral
   ollama pull codellama
   ```

3. **Start Ollama**:
   ```bash
   ollama serve
   ```

4. **Configure in BBS**:
   - Open BBS AI Settings (as above)
   - Enable "Enable Local AI"
   - Set endpoint to `localhost:11434`
   - Set model to the one you downloaded (e.g., `llama2`)
   - Click "Test AI Providers" to verify

### Option 3: Local AI with LM Studio (Offline)

1. **Install LM Studio**:
   - Go to [https://lmstudio.ai](https://lmstudio.ai)
   - Download and install LM Studio
   - Download a model through LM Studio's interface

2. **Start Local Server**:
   - In LM Studio, go to "Local Server" tab
   - Select your downloaded model
   - Start the server (usually on port 1234)

3. **Configure in BBS**:
   - Open BBS AI Settings
   - Enable "Enable Local AI"
   - Set endpoint to `localhost:1234`
   - Set model to the name shown in LM Studio
   - Click "Test AI Providers" to verify

## Usage

### Accessing the AI Assistant

1. **Open BBS Dashboard**: Press `0` in-game
2. **Navigate to AI Panel**: Click the AI Assistant icon (processor symbol)
3. **Start Chatting**: Type your questions or click quick prompt buttons

### Quick Prompts

The AI assistant provides context-aware quick prompts based on what you're currently doing:
- When editing animations: Animation-specific suggestions
- When working with keyframes: Timing and interpolation help
- When setting up cameras: Cinematography assistance
- General usage: Comprehensive animation help

### Example Interactions

**Animation Creation**:
```
You: "Help me create a walking animation for a character"
AI: "I'll help you create a realistic walking animation! Here's a step-by-step approach:

1. **Planning the walk cycle** (typically 24-30 frames):
   - Frame 1: Contact pose (left foot down)
   - Frame 6: Recoil (weight shifts)
   - Frame 12: Passing pose (right leg passes left)
   - Frame 18: Contact pose (right foot down)
   - Frame 24: Return to start

2. **Key poses to animate**:
   - Foot placement and weight transfer
   - Hip movement (up/down and forward)
   - Arm swing (opposite to legs)
   - Head bob and spine curve

3. **BBS-specific steps**:
   - Create keyframes for each major pose
   - Use smooth interpolation between frames
   - Add secondary animation for natural movement
   - Test with loop playback

Would you like me to detail any specific part of this process?"
```

**Technical Help**:
```
You: "My animation looks choppy, how can I smooth it out?"
AI: "Choppy animation is usually caused by keyframe spacing or interpolation issues. Here's how to fix it in BBS:

1. **Check keyframe spacing**:
   - Ensure keyframes aren't too close together
   - Use the rule: major poses every 6-8 frames for character animation

2. **Adjust interpolation**:
   - Right-click keyframes in timeline
   - Change from linear to smooth/ease interpolation
   - Use anticipation and follow-through

3. **Add in-between frames**:
   - Add subtle keyframes between major poses
   - Focus on smooth arcs of motion

4. **Preview settings**:
   - Use higher preview quality in BBS settings
   - Enable motion blur if supported

Try these adjustments and let me know if you need help with specific movements!"
```

### Context Awareness

The AI assistant automatically detects what you're working on and provides relevant help:
- **Film Panel Active**: Animation and camera advice
- **Keyframe Editing**: Timing and interpolation suggestions  
- **Technical Issues**: Troubleshooting and optimization
- **General Usage**: Comprehensive BBS guidance

## Configuration Options

### AI Settings Panel

Access via AI Assistant panel → "AI Settings" button:

- **General Settings**:
  - Enable AI Assistant: Master on/off switch
  - Auto Suggestions: Show contextual tips automatically

- **OpenAI Configuration**:
  - Enable OpenAI: Use cloud-based AI
  - API Key: Your OpenAI API key
  - Model: Choose between gpt-3.5-turbo (faster/cheaper) or gpt-4 (better quality)

- **Local AI Configuration**:
  - Enable Local AI: Use local AI providers
  - Endpoint: Server address (localhost:11434 for Ollama, localhost:1234 for LM Studio)
  - Model: Name of the model to use

### Provider Priority

The system automatically tries providers in this order:
1. OpenAI (if enabled and configured)
2. Local AI (if enabled and configured)

If one fails, it automatically tries the next available provider.

## Troubleshooting

### "No AI providers available"
- Check that at least one provider is enabled in settings
- Verify API key is correct for OpenAI
- Ensure local AI server is running for local providers

### "OpenAI API error: 401"
- Invalid API key - check and re-enter your OpenAI API key
- Make sure you have credits in your OpenAI account

### "Local AI request failed"
- Check that Ollama/LM Studio is running
- Verify the endpoint address (usually localhost:11434 or localhost:1234)
- Confirm the model name matches what's available

### "All AI providers failed"
- Check internet connection for OpenAI
- Restart local AI servers
- Try "Test AI Providers" button to diagnose issues

## Privacy and Security

### Data Handling
- **OpenAI**: Messages sent to OpenAI servers (see their privacy policy)
- **Local AI**: All processing happens on your computer, no data sent externally
- **API Keys**: Stored locally in BBS settings, never transmitted except to authenticate

### Recommendations
- Use local AI (Ollama/LM Studio) for maximum privacy
- Be mindful of sensitive information when using cloud providers
- API keys are stored in BBS config files - keep these secure

## Advanced Usage

### Custom Prompts
You can ask the AI assistant about any animation topic:
- "Explain the 12 principles of animation in the context of BBS"
- "How do I create realistic facial expressions?"
- "What's the best way to animate multiple characters in a scene?"
- "Help me plan a dramatic action sequence"

### Integration with BBS Workflow
- Use AI suggestions while actively animating
- Get real-time feedback on your current work
- Ask for creative alternatives to your current approach
- Troubleshoot technical issues as they arise

The AI assistant is designed to enhance your BBS animation workflow, providing expert guidance whenever you need it!