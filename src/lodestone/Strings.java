package lodestone;

import java.util.Arrays;

public class Strings {

    public static String capitalize(String in) {
        String[] parts = in.split(" ");
        return Arrays.stream(parts)
                .map( current -> Character.toUpperCase(current.charAt(0)) + current.substring(1).toLowerCase() )
                .reduce("", (s1, s2) -> s1 + " " + s2).trim();
    }

}
