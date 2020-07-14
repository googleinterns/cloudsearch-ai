package com.google.cloudsearch.ai;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestCloudStorageHandler.class,
        TestAISkillDriver.class,
        TestAISkillSet.class,
        TestCategoryExtractionSkill.class
})

public class TestSuite {
}