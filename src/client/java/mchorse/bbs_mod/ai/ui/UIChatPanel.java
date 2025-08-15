package mchorse.bbs_mod.ai.ui;

import mchorse.bbs_mod.ai.AIContext;
import mchorse.bbs_mod.ai.AIManager;
import mchorse.bbs_mod.ai.AIRequest;
import mchorse.bbs_mod.ai.AIResponse;
import mchorse.bbs_mod.ai.config.AISettings;
import mchorse.bbs_mod.ai.context.BBSContextDetector;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.ScrollDirection;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Colors;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Chat interface panel for interacting with the AI assistant
 */
public class UIChatPanel extends UIElement
{
    public UIScrollView chatHistory;
    public UIScrollView quickPrompts;
    public UITextbox messageInput;
    public UIButton sendButton;
    public UIButton clearButton;
    public UILabel contextLabel;
    
    private List<ChatMessage> messages;
    private boolean isProcessing = false;

    public UIChatPanel()
    {
        this.messages = new ArrayList<>();
        
        this.chatHistory = new UIScrollView(ScrollDirection.VERTICAL);
        this.chatHistory.scroll.scrollSpeed = 51;
        
        this.quickPrompts = new UIScrollView(ScrollDirection.HORIZONTAL);
        this.quickPrompts.scroll.scrollSpeed = 51;
        this.quickPrompts.scroll.cancelScrolling().noScrollbar();
        
        this.messageInput = new UITextbox(1000, this::sendMessage);
        this.messageInput.placeholder = "Ask the AI assistant for help with your animation...";
        
        this.sendButton = new UIButton(IKey.raw("Send"), this::sendCurrentMessage);
        this.clearButton = new UIButton(IKey.raw("Clear"), this::clearChat);
        
        this.contextLabel = UI.label(IKey.raw("Context: " + BBSContextDetector.getContextDescription()));
        this.contextLabel.color(0xFF888888);
        
        // Layout
        this.contextLabel.relative(this).x(10).y(5).w(1F, -20).h(12);
        this.quickPrompts.relative(this).x(10).y(20).w(1F, -20).h(25);
        this.chatHistory.relative(this).x(10).y(50).w(1F, -20).h(1F, -100);
        this.messageInput.relative(this).x(10).y(1F, -45).w(1F, -120).h(20);
        this.sendButton.relative(this).x(1F, -100).y(1F, -45).w(40).h(20);
        this.clearButton.relative(this).x(1F, -55).y(1F, -45).w(40).h(20);
        
        this.add(this.contextLabel, this.quickPrompts, this.chatHistory, this.messageInput, this.sendButton, this.clearButton);
        
        this.setupQuickPrompts();
        
        // Add welcome message
        this.addMessage(new ChatMessage(
            "AI Assistant", 
            "Hello! I'm your BBS animation assistant. I can help you with:\n\n" +
            "• Creating animations with natural language\n" +
            "• Optimizing keyframes and motion\n" +
            "• Animation ideas and creative suggestions\n" +
            "• Technical support and troubleshooting\n" +
            "• Camera work and scene composition\n\n" +
            "What would you like help with today?",
            ChatMessage.Type.ASSISTANT
        ));
        
        this.updateChatDisplay();
    }

    private void setupQuickPrompts()
    {
        String[] prompts = BBSContextDetector.getContextualQuickPrompts();
        
        this.quickPrompts.removeAll();
        
        for (String prompt : prompts)
        {
            if (prompt.length() > 50)
            {
                prompt = prompt.substring(0, 47) + "...";
            }
            
            UIButton promptButton = new UIButton(IKey.raw(prompt), (button) -> {
                this.messageInput.setText(prompt);
                this.sendCurrentMessage(button);
            });
            promptButton.h(20).background(false).textColor(0xFF4A90E2, true);
            
            this.quickPrompts.add(promptButton);
        }
        
        this.quickPrompts.row(5).resize();
    }

    private void sendMessage(String text)
    {
        if (!text.trim().isEmpty())
        {
            sendCurrentMessage(null);
        }
    }

    private void sendCurrentMessage(UIButton button)
    {
        String message = this.messageInput.getText().trim();
        if (message.isEmpty() || isProcessing)
        {
            return;
        }

        // Check if AI is enabled
        if (!AISettings.aiEnabled.get())
        {
            this.addMessage(new ChatMessage(
                "System", 
                "AI assistant is disabled. Please enable it in the settings.",
                ChatMessage.Type.ERROR
            ));
            this.updateChatDisplay();
            return;
        }

        // Add user message
        this.addMessage(new ChatMessage("You", message, ChatMessage.Type.USER));
        this.messageInput.setText("");
        
        // Show processing
        isProcessing = true;
        this.sendButton.label = IKey.raw("Sending...");
        this.sendButton.setEnabled(false);
        
        ChatMessage processingMessage = new ChatMessage("AI Assistant", "Thinking...", ChatMessage.Type.PROCESSING);
        this.addMessage(processingMessage);
        this.updateChatDisplay();

        // Create AI request with current context
        AIContext context = getCurrentContext();
        AIRequest request = new AIRequest(message, context);

        // Send request to AI manager
        AIManager.getInstance().sendRequest(request).thenAccept(response -> {
            // Remove processing message
            this.messages.remove(processingMessage);
            
            // Add response
            if (response.isSuccess())
            {
                this.addMessage(new ChatMessage(
                    "AI Assistant (" + response.getProviderId() + ")", 
                    response.getContent(),
                    ChatMessage.Type.ASSISTANT
                ));
            }
            else
            {
                this.addMessage(new ChatMessage(
                    "AI Error", 
                    response.getError(),
                    ChatMessage.Type.ERROR
                ));
            }
            
            // Reset UI state
            isProcessing = false;
            this.sendButton.label = IKey.raw("Send");
            this.sendButton.setEnabled(true);
            this.updateChatDisplay();
        });
    }

    private void clearChat(UIButton button)
    {
        this.messages.clear();
        this.updateChatDisplay();
    }

    private void addMessage(ChatMessage message)
    {
        this.messages.add(message);
    }

    private void updateChatDisplay()
    {
        this.chatHistory.removeAll();
        
        for (ChatMessage message : this.messages)
        {
            UIElement messageElement = createMessageElement(message);
            this.chatHistory.add(messageElement);
        }
        
        // Auto-scroll to bottom
        this.chatHistory.scroll.scrollTo(0, Integer.MAX_VALUE);
        this.chatHistory.resize();
    }

    private UIElement createMessageElement(ChatMessage message)
    {
        UIElement container = new UIElement();
        container.w(1F).h(20);
        
        UILabel senderLabel = UI.label(IKey.raw(message.getSender() + ":"));
        UILabel contentLabel = UI.label(IKey.raw(message.getContent()));
        
        // Set colors based on message type
        switch (message.getType())
        {
            case USER:
                senderLabel.color(Colors.ACTIVE);
                break;
            case ASSISTANT:
                senderLabel.color(0xFF4CAF50); // Green
                break;
            case ERROR:
                senderLabel.color(0xFFF44336); // Red
                contentLabel.color(0xFFF44336);
                break;
            case PROCESSING:
                senderLabel.color(0xFFFF9800); // Orange
                contentLabel.color(0xFFFF9800);
                break;
        }
        
        senderLabel.relative(container).x(5).y(2);
        contentLabel.relative(container).x(5).y(16);
        
        // Calculate height based on content
        int lines = message.getContent().split("\n").length;
        container.h(Math.max(35, 20 + lines * 12));
        
        container.add(senderLabel, contentLabel);
        return container;
    }

    private AIContext getCurrentContext()
    {
        // Use the context detector to get current BBS state
        return BBSContextDetector.detectCurrentContext();
    }

    @Override
    public void resize()
    {
        super.resize();
        
        // Update context label when resizing (in case context changed)
        this.contextLabel.label = IKey.raw("Context: " + BBSContextDetector.getContextDescription());
        
        // Refresh quick prompts
        this.setupQuickPrompts();
    }

    /**
     * Chat message data structure
     */
    public static class ChatMessage
    {
        public enum Type
        {
            USER, ASSISTANT, ERROR, PROCESSING
        }

        private final String sender;
        private final String content;
        private final Type type;
        private final long timestamp;

        public ChatMessage(String sender, String content, Type type)
        {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getSender()
        {
            return sender;
        }

        public String getContent()
        {
            return content;
        }

        public Type getType()
        {
            return type;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }
}