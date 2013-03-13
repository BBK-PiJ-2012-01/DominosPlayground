package dominoes.players.ai.algorithm.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * User: Sam Wright
 * Date: 07/03/2013
 * Time: 13:46
 */
public class ConsoleHelper {
    public static int askUserForInteger(String question) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(question);

            try {
                String input = br.readLine();
                return Integer.parseInt(input);
            } catch (IOException e) {
                System.out.print("Bad input - ");
            }
        }
    }
}
