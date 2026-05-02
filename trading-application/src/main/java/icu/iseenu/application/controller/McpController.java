//package icu.iseenu.application.controller;
//
//
//import icu.iseenu.ai.mcp.McpAssistant;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/mcp")
//@Slf4j
//public class McpController {
//
//    @Autowired
//    private McpAssistant mcpAssistant;
//
//    /**
//     * 测试 MCP 服务
//     *
//     * @param question 用户问题
//     * @return MCP 助手返回的结果
//     */
//    @GetMapping("/chat")
//    public String chat(@RequestParam String question) {
//        log.info("收到 MCP 测试请求: {}", question);
//        try {
//            return mcpAssistant.chat(question);
//        } catch (Exception e) {
//            log.error("MCP 调用失败", e);
//            return "MCP 调用失败: " + e.getMessage();
//        }
//    }
//
//
//}
