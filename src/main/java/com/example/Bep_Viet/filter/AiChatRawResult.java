package com.example.Bep_Viet.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AiChatRawResult {

    @JsonProperty("reply_text")
    private String replyText;

    private String intent; // "recipe_suggestion" | "general_chat" | "clarify"

    private ChatFilters filters;
}