//package icu.iseenu.config;
//
//import dev.langchain4j.mcp.McpToolProvider;
//import dev.langchain4j.mcp.client.DefaultMcpClient;
//import dev.langchain4j.mcp.client.McpClient;
//import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
//import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
//import dev.langchain4j.mcp.registryclient.model.McpTransport;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Duration;
//import java.util.List;
//
//@Configuration
//@Slf4j
//public class McpConfig {
//
//    @Bean
//    public McpClient mcpClient() {
//        log.info("正在初始化 MCP 客户端...");
//        StdioMcpTransport transport = StdioMcpTransport.builder()
//                .command(List.of(
//                        "cmd.exe",
//                        "/c",
//                        "npx", "-y", "@modelcontextprotocol/server-filesystem", "/tmp"))
//                .logEvents(true)
//                .build();
//
//        return DefaultMcpClient.builder()
//                .key("mcp-echo-server")
//                .transport(transport)
//                .initializationTimeout(Duration.ofSeconds(60))
//                .build();
//    }
//
//    @Bean
//    public McpToolProvider mcpToolProvider(List<McpClient> mcpClients) {
//        log.info("正在创建 MCP ToolProvider...");
//        return McpToolProvider.builder()
//                .mcpClients(mcpClients)
//                .build();
//    }
//
//
//}
