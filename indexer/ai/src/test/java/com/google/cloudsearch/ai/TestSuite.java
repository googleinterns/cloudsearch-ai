package com.google.cloudsearch.ai;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestCloudStorageHandler.class,
        TestAISkillDriver.class,
        TestAISkillSet.class,
        TestCategoryExtractionSkill.class,
        TestEntityExtractionSkill.class,
        TestSentimentExtractionSkill.class,
        TestBaseAISkill.class,
        TestCustomSkill.class
})

public class TestSuite {
}