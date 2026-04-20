package icu.iseenu.agent.tool.assistant.tools;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import icu.iseenu.agent.tool.assistant.StockAssistant;

/**
 * RAG测试工具
 */
public class Test {
//    public static void main(String[] args) {
//
//
//        List<Document> documents =
//                FileSystemDocumentLoader.loadDocuments("D:\\codes\\autoCodeWorkspace\\data\\rag\\");
//
//        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
//
//

//
//        Assistant assistant = AiServices.builder(Assistant.class)
//                .chatModel(chatModel)
//                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
//                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
//                .build();
//
//        System.out.println(assistant.chat(
//                "这是一个中国节假日的数据，里面存储的对应月份所放假的日期，例如：\"1\":[1,2,3] 代表1月份的1,2,3三天放假"
//                , "分析一下文档里面的数据今年假期共有多少天"));
//    }

    public static void main(String[] args) {

        ChatModel chatModel = OpenAiChatModel.builder().baseUrl("https://api.deepseek.com/v1").apiKey("sk-cacdd0e062dc4a51bbd087ba3d613615").modelName("deepseek-chat").build();

        StockAssistant assistant = AiServices.builder(StockAssistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(new StockTools())
                .build();

//        Stock stock = assistant.extractStockFrom("添加股票代码为600262，持仓价格为13.26，持仓数量为300");
//        String stock = assistant.getStockTableWithProfit();
//        System.out.println(stock);
    }
}
