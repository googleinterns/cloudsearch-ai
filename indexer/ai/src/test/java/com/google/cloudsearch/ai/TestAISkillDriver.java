package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.io.IOException;
import org.json.simple.parser.ParseException;

/**
 * Test AI Skill Driver
 */
public class TestAISkillDriver {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test the behaviour of the initialize() function using non-existing files or
     * Invalid file names.
     * @throws Exception
     */
    @Test
    public void testInitializeInvalidFileName() throws Exception {
        exception.expect(IOException.class);
        AISkillDriver.initialize("file1", "file2");
    }

    /**
     * Test the behaviour of the initialize() function for a non JSON file.
     * @throws Exception
     */
    @Test
    public void testInitializeInvalidFileType() throws Exception {
        exception.expect(ParseException.class);
        String file_path = "./src/test/java/com/google/cloudsearch/ai/testFiles/AISkillDriver/NonJSONFile.txt";
        AISkillDriver.initialize(file_path, file_path);
    }

    /**
     * Test the behaviour of initialize() function for Invalid JSON file.
     * @throws Exception
     */
    @Test
    public void testInitializeInvalidJSONFile() throws Exception {
        exception.expect(InvalidConfigException.class);
        String file_path = "./src/test/java/com/google/cloudsearch/ai/testFiles/AISkillDriver/SampleJSON.json";
        AISkillDriver.initialize(file_path, file_path);
    }

    /**
     * Test the behaviour of populateStructuredData() when the Driver initialize() is not
     * called before calling it.
     * @throws NullPointerException
     */
    @Test
    public void testPopulateStructuredData() throws NullPointerException{
        exception.expect(NullPointerException.class);
        Multimap<String, Object> obj = ArrayListMultimap.create();;
        AISkillDriver.populateStructuredData("content", obj);
    }

    /**
     * Test the behaviour of closeSkillDriver() when the initialize() function was not
     * called before calling it.
     * @throws NullPointerException
     */
    @Test
    public void testCloseSkillDriver() throws NullPointerException{
        exception.expect(NullPointerException.class);
        AISkillDriver.closeSkillDriver();
    }
}
