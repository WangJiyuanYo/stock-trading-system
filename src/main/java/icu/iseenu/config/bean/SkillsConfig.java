package icu.iseenu.config.bean;

import dev.langchain4j.skills.ClassPathSkillLoader;
import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class SkillsConfig {

    @Bean
    public Skills skills() {
        List<FileSystemSkill> skillList = ClassPathSkillLoader.loadSkills("skills");
        log.info("加载到 {} 个 Skills", skillList.size());
        skillList.forEach(skill -> log.info("- Skill: {}", skill.name()));
        return Skills.from(skillList);
    }
}
