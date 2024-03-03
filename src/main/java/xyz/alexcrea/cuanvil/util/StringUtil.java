package xyz.alexcrea.cuanvil.util;

public class StringUtil {

    //we assume snake_cased_string is in snake case
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
