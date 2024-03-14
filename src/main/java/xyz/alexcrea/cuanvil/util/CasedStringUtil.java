package xyz.alexcrea.cuanvil.util;

/**
 * An incomplete cased string util
 */
public class CasedStringUtil {

    /**
     * Transform a snake cased string to an upper-cased spaced string.
     * <p>
     * for example: if we use  "hello_world" as an input this function will return "Hello World".
     * @param snake_cased_string The input string.
     * This argument NEED to be a snake cased string, or it will not work
     * @return The input as an upper-cased string with space separator.
     */
    public static String snakeToUpperSpacedCase(String snake_cased_string){
        if(snake_cased_string.contentEquals("")) return "";
        StringBuilder result = new StringBuilder();

        for (String word : snake_cased_string.split("_")) {
            result.append(" ");
            if(word.isEmpty()) continue;
            char firstChar = word.charAt(0);

            result.append(Character.toUpperCase(firstChar));
            result.append(word.substring(1));
        }
        return result.substring(1);
    }

}
