package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ShuntingYard {

    public static void main(String[] args) {
//        String query = "select * from marks where pass == false or (id > 5 and name like 'B' or name == 'Faye') and mark > 87;";
        String query = "select * from marks where pass == false or ((pass == true and (name like 'B' or name == 'Faye')) and ((name like 'i' and mark > 50) or mark < 40));";

        QueryLexer lexer = new QueryLexer(query);
        lexer.setup();

        ShuntingYard shuntingYard = new ShuntingYard();
        List<String> conditionList = shuntingYard.parseConditionShunting(lexer.tokens, 5);

        System.out.println(conditionList);
    }

    public List<String> parseConditionShunting(List<String> query, int startIndex) {
        List<String> parsedConditions = new ArrayList<String>();
        Stack<String> operators = new Stack<String>();

        for (int i = startIndex; i < query.size(); i++) {
            String currentToken = query.get(i);

            if (currentToken.equals(")")) {
                while (!operators.empty() && !operators.peek().equals("(")) { // is empty() needed?
                    parsedConditions.add(operators.pop());
                }
                operators.pop();
            }
            if (currentToken.equals("(") || currentToken.equalsIgnoreCase("and")
                    || currentToken.equalsIgnoreCase("or")) {
                operators.push(currentToken);
            } else if (!currentToken.equals(")") && !currentToken.equals(";")) {
                parsedConditions.add(currentToken);
            }
        }
        while (!operators.empty()) {
            parsedConditions.add(operators.pop());
        }

        return parsedConditions;
    }


}
