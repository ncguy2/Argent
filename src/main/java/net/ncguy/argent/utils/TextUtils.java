package net.ncguy.argent.utils;

/**
 * Created by Guy on 10/06/2016.
 */
public class TextUtils {

    public static String padBinary(int dec, int len) {
        return String.format("%"+len+"s", Integer.toBinaryString(dec)).replace(' ', '0');
    }

    public static boolean isInteger(String text) {
        char[] ch = text.toCharArray();
        for (char c : ch) if(!Character.isDigit(c)) return false;
        return true;
    }

    public static String camelCase(String text) {
        StringBuilder sb = new StringBuilder();
        boolean nextTitle = false;

        for(char c : text.toCharArray()) {
            if(Character.isSpaceChar(c))
                nextTitle = true;
            else if(nextTitle) {
                c = Character.toTitleCase(c);
                nextTitle = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String replaceLine(String needle, String replace, String target) {
        String[] lines = target.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            if(l.contains(needle))
                lines[i] = replace;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
            sb.append(line).append("\n");
        return sb.toString();
    }

}
