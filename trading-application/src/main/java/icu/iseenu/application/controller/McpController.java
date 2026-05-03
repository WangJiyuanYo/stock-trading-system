package icu.iseenu.application.controller;

import icu.iseenu.ai.mcp.McpAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp")
@ConditionalOnBean(McpAssistant.class)
@Slf4j
public class McpController {

    private final McpAssistant mcpAssistant;

    public McpController(McpAssistant mcpAssistant) {
        this.mcpAssistant = mcpAssistant;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String question) {
        log.info("收到 MCP 请求: {}", question);
        try {
            return mcpAssistant.chat(question);
        } catch (Exception e) {
            log.error("MCP 调用失败", e);
            return "MCP 调用失败: " + e.getMessage();
        }
    }
}
