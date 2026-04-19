package icu.iseenu.agent.tool.assistant;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;

public interface HiringWorkflow {

    @SequenceAgent(
        subAgents = {
          HolidayAssistant.class,
          WriteJsonFileAssistant.class
        }
    )
    String process(@V("info") String candidateInfo, @V("jd") String jobDescription);
}
