package dominoes.players.ai.algorithm;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 27/03/2013
 * Time: 15:15
 */
public class AIBuilderTest {

    @Test
    public void testGetValidAINames() throws Exception {
        assertTrue(AIBuilder.getValidAINames().size() > 0);
    }

    @Test
    public void testCreateAI() throws Exception {
        for (String aiName : AIBuilder.getValidAINames()) {
            AIController ai = AIBuilder.createAI(aiName);
            assertNotNull(ai);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testBadName() throws Exception {
        AIBuilder.createAI("Not an AI!");

    }
}
