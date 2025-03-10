package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;

// need to modify this to take any query & parse correctly

public class QueryLexer{

    String query;
    String[] specialCharacters = {"(", ")", ",", ";", "=", "!", ">", "<"};
//    String[] terms = {"use", "database", "table", "into", "from", "update", "join", "and", "on", "add", "drop"};
    ArrayList<String> tokens = new ArrayList<String>();

    public QueryLexer(String line) {
        query = line;
    }

    public static void main(String args[]) {

        String newQuery = "update hello set age = 1, the = 'this' where ther > 1;";
//        String newQuery = "SELECT * FROM STUDENTS WHERE AGE=10 AND MARK>70;";
        QueryLexer lexer = new QueryLexer(newQuery);
        lexer.setup();;
        for (String token : lexer.tokens) {
            System.out.println(token);
        }

    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public void setup()
    {
        // Split the query on single quotes (to separate out query text from string literals)
        String[] fragments = query.split("'");
        for (int i = 0; i < fragments.length; i++) {
            // Every other fragment is a string literal, so just add it straight to "result" token list
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
                // If it's not a string literal, it must be query text (which needs further processing)
            else {
                // Tokenise the fragment into an array of strings - this is the "clever" bit !
                String[] nextBatchOfTokens = tokenise(fragments[i]);
                // Then copy all the tokens into the "result" list (needs a bit of conversion)
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        combineOperators();
    }

    public String[] tokenise(String input)
    {
        // Add in some extra padding spaces either side of the "special characters"...
        // so we can be SURE that they are separated by AT LEAST one space (possibly more)
        for(int i = 0; i < specialCharacters.length; i++) {
            input = input.replace(specialCharacters[i], " " + specialCharacters[i] + " ");
//            String regexString = "\\b" + "\\\\" + specialCharacters[i] + "\\b";
//            input = input.replaceAll(regexString, " " + specialCharacters[i] + " ");

        }
        // Remove any double spaces (the previous padding activity might have introduced some of these)
        while (input.contains("  ")) input = input.replace("  ", " "); // Replace two spaces by one
        // Remove any whitespace from the beginning and the end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a SINGLE space between tokens)
        return input.split(" ");
    }

    public void combineOperators() {
        for (int index = 0; index < tokens.size() - 1; index++) {
            if (tokens.get(index).equals("=") && tokens.get(index + 1).equals("=")) {
                tokens.set(index, "==");
                tokens.remove(index + 1);
            } else if (tokens.get(index).equals("!") && tokens.get(index + 1).equals("=")) {
                tokens.set(index, "!=");
                tokens.remove(index + 1);
            } else if (tokens.get(index).equals(">") && tokens.get(index + 1).equals("=")) {
                tokens.set(index, ">=");
                tokens.remove(index + 1);
            } else if (tokens.get(index).equals("<") && tokens.get(index + 1).equals("=")) {
                tokens.set(index, "<=");
                tokens.remove(index + 1);
            }
        }

    }


}



