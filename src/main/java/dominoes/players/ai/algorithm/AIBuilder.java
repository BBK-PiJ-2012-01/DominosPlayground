package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.components.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder class for AIContoller objects.
 *
 * @author Sam Wright
 */
public class AIBuilder {
    private static final List<String> validAINames;
    private static final String builderMethodPrefix = "create";

    static {
        List<String> tempValidAINames = new ArrayList<String>();
        for (Method method : AIBuilder.class.getDeclaredMethods())
            if (method.getName().startsWith(builderMethodPrefix) && method.getGenericParameterTypes().length == 0)
                tempValidAINames.add(method.getName().substring(builderMethodPrefix.length()));
        validAINames = Collections.unmodifiableList(tempValidAINames);
    }


    private static AIController createSlowProbabilisticAI() {
        return new ProbabilisticAI(
                new LinearPlyManager(),
                new RouteSelectorImpl(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator());
    }

    private static AIController createShortSightedAI() {
        return new ShortSightedAIController();
    }

    private static AIController createProbabilisticAI() {
        return new ProbabilisticAI(
                new LinearPlyManager(),
                new FastRouteSelector(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator());
    }

    private static AIController createAIWithValueAddedPerChoice(int value) {
        return new ProbabilisticAI(
                new LinearPlyManager(),
                new FastRouteSelector(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator(value));
    }

    private static AIController createRandomAI() {
        return new RandomAIController();
    }

    public static List<String> getValidAINames() {
        return validAINames;
    }

    @SuppressWarnings("all")
    public static AIController createAI(String aiString) {
        if (!validAINames.contains(aiString))
            throw new IllegalArgumentException("Cannot find builder method for " + aiString);

        Method builderMethod;
        AIController createdAI;

        try {
            builderMethod = AIBuilder.class.getDeclaredMethod(builderMethodPrefix + aiString);
            createdAI = (AIController) builderMethod.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Reflection failed in AIBuilder", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Reflection failed in AIBuilder", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Reflection failed in AIBuilder", e);
        }

        return createdAI;
    }
}
