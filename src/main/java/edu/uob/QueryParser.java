package edu.uob;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class QueryParser {

    String[] reservedWords = {"use", "create", "drop", "alter", "insert", "select", "update", "delete", "join"
                              , "database", "table", "into", "values", "from", "where", "set", "and", "or"
                              , "add", "true", "false", "null"};

    public boolean parseQuery(List<String> query, QueryHandler queryHandler) {
        if (!query.get(query.size() - 1).equalsIgnoreCase(";")) {
            queryHandler.setErrorLine("Missing semicolon at end of query.");
            return false;
        }
        boolean queryValid;
        switch (query.get(0).toLowerCase()) {
            case "use" ->  queryValid =  parseUse(queryHandler, query);
            case "create" ->  queryValid =  parseCreate(queryHandler, query);
            case "drop" ->  queryValid =  parseDrop(queryHandler, query);
            case "alter" ->  queryValid =  parseAlter(queryHandler, query);
            case "insert" ->  queryValid =  parseInsert(queryHandler, query);
            case "select" ->  queryValid =  parseSelect(queryHandler, query);
            case "update" ->  queryValid =  parseUpdate(queryHandler, query);
            case "delete" ->  queryValid =  parseDelete(queryHandler, query);
            case "join" ->  queryValid =  parseJoin(queryHandler, query);
            default -> {
                queryHandler.setErrorLine("First word is not a valid query type.");
                queryValid =  false;
            }
        }
        return queryValid;
    }

    public boolean parseUse(QueryHandler queryHandler, List<String> query) {
        if (query.size() != 3 || !checkPlainText(query.get(1))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String databaseName = query.get(1).toLowerCase();
        queryHandler.setCurrentDatabase(databaseName);
        Use use = new Use();
        return use.switchDatabases(queryHandler.getCurrentServer().getStorageFolderPath(), databaseName, queryHandler);
    }


    public boolean parseCreate(QueryHandler queryHandler, List<String> query) {
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("id");

        if ((!query.get(1).equalsIgnoreCase("database") && !query.get(1).equalsIgnoreCase("table"))
                || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                || !checkPlainText(query.get(2))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String fileName = query.get(2).toLowerCase();
        if (query.size() > 4) {
            if (!query.get(3).equals("(") || !query.get(query.size() - 2).equals(")")) {
                queryHandler.setErrorLine("Invalid query parameters.");
                return false;
            }
            int index = 4;
            attributeList = addToList(attributeList, query, index, ")");
            if (!isListValid(attributeList, "AttributeList")) {
                queryHandler.setErrorLine("Invalid query parameters.");
                return false;
            }
        }
        Create create = new Create();
        if (query.get(1).equalsIgnoreCase("database")) {
            return create.createDatabase(queryHandler.getCurrentServer().getStorageFolderPath(), fileName, queryHandler);
        } else {
            String databasePath = queryHandler.getCurrentServer().getStorageFolderPath()
                    + File.separator + queryHandler.getCurrentDatabase();
            List<String> cleanAttributeList = removeSpecialCharacters(attributeList, query);
            return create.createTable(databasePath, fileName, cleanAttributeList, queryHandler);
        }
    }

    public boolean parseDrop(QueryHandler queryHandler, List<String> query) {
        if (query.size() != 4 || !query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table") || !checkPlainText(query.get(2))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String fileName = query.get(2).toLowerCase();
        Drop drop = new Drop();
        String filePath;
        if (query.get(1).equalsIgnoreCase("database")) {
            filePath = queryHandler.getCurrentServer().getStorageFolderPath() + File.separator + fileName;
        } else {
            filePath = queryHandler.getCurrentServer().getStorageFolderPath() + File.separator
                    + queryHandler.getCurrentDatabase() + File.separator + fileName + ".tab";
        }

        return drop.dropFile(filePath, queryHandler);
    }

    public boolean parseAlter(QueryHandler queryHandler, List<String> query) {
        if (query.size() != 6 || !query.get(1).equalsIgnoreCase("table")
                || !checkPlainText(query.get(2)) || (!query.get(3).equalsIgnoreCase("add")
                && !query.get(3).equalsIgnoreCase("drop"))
                || !checkPlainText(query.get(4))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String tableName = query.get(2).toLowerCase();
        String alterationType = query.get(3);
        String attributeName = query.get(4);

        Alter alter = new Alter();
        return alter.alterTable(queryHandler.getTable(tableName, queryHandler.getCurrentDatabase()),
                alterationType, attributeName, queryHandler);
    }

    public boolean parseInsert(QueryHandler queryHandler, List<String> query) {
        if (!query.get(1).equalsIgnoreCase(("into")) || !checkPlainText(query.get(2))
                || !query.get(3).equalsIgnoreCase(("values")) || !query.get(4).equalsIgnoreCase("(")) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String tableName = query.get(2).toLowerCase();
        List<String> valueList = new ArrayList<String>();
        int index = 5;

        if (!query.get(query.size() - 2).equalsIgnoreCase(")")) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        valueList = addToList(valueList, query, index, ")");
        if (!isListValid(valueList, "ValueList")) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        Insert insert = new Insert();
        List<String> cleanValueList = removeSpecialCharacters(valueList, query);
        return insert.insertIntoTable(queryHandler, queryHandler.getTable(tableName,
                queryHandler.getCurrentDatabase()), cleanValueList);
    }

    public boolean parseSelect(QueryHandler queryHandler, List<String> query) {
        List<String> wildAttributeList = new ArrayList<String>();
        int index = 1;

        if (query.get(1).equals("*")) {
            wildAttributeList.add(query.get(1));
        } else {
            wildAttributeList = addToList(wildAttributeList, query, index, "from");
        }
        index = wildAttributeList.size() + index;
        if (!query.get(index).equalsIgnoreCase("from")
                || !checkPlainText(query.get(index + 1))
                || (!isListValid(wildAttributeList, "WildAttributeList") && !query.get(1).equals("*"))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        index++;
        String tableName = query.get(index).toLowerCase();
        index++;

        if ((query.size() - 1) != (index)) {
            if (!query.get(index).equalsIgnoreCase("where")) {
                queryHandler.setErrorLine("Invalid query parameters.");
                return false;
            }
            if (!checkConditionBrackets(queryHandler, query, index + 1)
                    || !isConditionValid(queryHandler, query, index + 1)) {
                return false;
            }
        }
        List<String> conditionList = parseCondition(query, index + 1);

        Select select = new Select();
        List<String> cleanWildAttributeList = removeSpecialCharacters(wildAttributeList, query);
        return select.selectRecords(queryHandler.getTable(tableName, queryHandler.getCurrentDatabase()),
                cleanWildAttributeList, conditionList, queryHandler);
    }

    public boolean parseUpdate(QueryHandler queryHandler, List<String> query) {
        if (!checkPlainText(query.get(1))
                || !query.get(2).equalsIgnoreCase(("set"))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String tableName = query.get(1).toLowerCase();

        List<String> nameValueList = new ArrayList<String>();
        int index = 3;
        nameValueList = addToList(nameValueList, query, index, "where");

        index = nameValueList.size() + index;
        if (!query.get(index).equalsIgnoreCase("where")
                || !isListValid(nameValueList, "NameValueList")) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        index++;

        if (!checkConditionBrackets(queryHandler, query, index)
                || !isConditionValid(queryHandler, query, index)) {
            return false;
        }

        List<String> conditionList = parseCondition(query, index);
        Update update = new Update();
        List<String> cleanNameValueList = removeSpecialCharacters(nameValueList, query);
        return update.updateTable(queryHandler.getTable(tableName, queryHandler.getCurrentDatabase()),
                cleanNameValueList, conditionList, queryHandler);
    }

    public boolean parseDelete(QueryHandler queryHandler, List<String> query) {
        if (!query.get(1).equalsIgnoreCase(("from")) || !checkPlainText(query.get(2))
                || !query.get(3).equalsIgnoreCase("where")) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String tableName = query.get(2).toLowerCase();

        if (!checkConditionBrackets(queryHandler, query, 4)
                || !isConditionValid(queryHandler, query, 4)) {
            return false;
        }
        List<String> conditionList = parseCondition(query, 4);
        Delete delete = new Delete();
        return delete.deleteRecord(queryHandler.getTable(tableName, queryHandler.getCurrentDatabase()),
                conditionList, queryHandler);
    }

    public boolean parseJoin(QueryHandler queryHandler, List<String> query) {
        if (query.size() != 9 || !checkAlphaNumeric(query.get(1))
                || !query.get(2).equalsIgnoreCase("and") || !checkPlainText(query.get(3))
                || !query.get(4).equalsIgnoreCase("on") || !checkPlainText(query.get(5))
                || !query.get(6).equalsIgnoreCase("and") || !checkPlainText(query.get(7))) {
            queryHandler.setErrorLine("Invalid query parameters.");
            return false;
        }
        String fileNameOne = query.get(1).toLowerCase();
        String fileNameTwo = query.get(3).toLowerCase();

        String attributeNameOne = query.get(5).toLowerCase();
        String attributeNameTwo = query.get(7).toLowerCase();

        Join join = new Join();
        return join.joinTables(queryHandler.getTable(fileNameOne, queryHandler.getCurrentDatabase()),
                queryHandler.getTable(fileNameTwo, queryHandler.getCurrentDatabase()),
                attributeNameOne, attributeNameTwo, queryHandler);

    }

    public List<String> parseCondition(List<String> query, int startIndex) {
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

    public boolean isConditionValid(QueryHandler queryHandler, List<String> query, int startIndex) {
        String[] comparators = {"==", ">", "<", ">=", "<=", "!=", "LIKE"};
        List<String> trimmedQuery = (new ArrayList<String>(query)).subList(startIndex, query.size());
        trimmedQuery.removeAll(Collections.singleton("("));
        trimmedQuery.removeAll(Collections.singleton(")"));
        trimmedQuery.removeAll(Collections.singleton(";"));

        for (int i = 0; i < trimmedQuery.size(); i++) {
            String currentToken = trimmedQuery.get(i);
            if (!currentToken.equals(";") && !currentToken.equals(" ")) {
                if (((i - 1) % 4) == 0 && !isTokenComparator(currentToken, comparators)
                        || ((i - 2) % 4) == 0 && !checkValue(currentToken) || ((i + 1) % 4) == 0
                        && !currentToken.equalsIgnoreCase("and")
                        && !currentToken.equalsIgnoreCase("or")
                        || i % 4 == 0 && (!checkPlainText(currentToken))) {
                    queryHandler.setErrorLine("Invalid parameters in condition");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkConditionBrackets(QueryHandler queryHandler, List<String> query, int startIndex) {
        int openBracketCount = 0;
        int closeBracketCount = 0;

        for (int i = startIndex; i < query.size(); i++) {
            if (query.get(i).equals("(")) {
                if (i != startIndex && (!query.get(i - 1).equals("(")
                        && (!query.get(i - 1).equalsIgnoreCase("and")
                        && !query.get(i - 1).equalsIgnoreCase("or")))) {
                    queryHandler.setErrorLine("Opening bracket in wrong place.");
                    return false;
                }
                openBracketCount++;
            } else if (query.get(i).equals(")")) {
                if (i != (query.size() - 2) && (!query.get(i + 1).equals(")")
                        && !query.get(i + 1).equalsIgnoreCase("and")
                        && !query.get(i + 1).equalsIgnoreCase("or"))) {
                    queryHandler.setErrorLine("Closing bracket in wrong place.");
                    return false;
                }
                closeBracketCount++;
            }
        }
        if (openBracketCount != closeBracketCount) {
            queryHandler.setErrorLine("Too many/not enough brackets.");
            return false;
        }
        return true;
    }

    public boolean isTokenComparator(String token, String[] comparators) {
        for (String comparator : comparators) {
            if (comparator.equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }

    public List<String> addToList(List<String> chosenList, List<String> query, int index, String terminatingChar) {
        while (!query.get(index).equalsIgnoreCase(terminatingChar)) {
            chosenList.add(query.get(index));
            index++;
        }
        return chosenList;
    }

    public List<String> removeSpecialCharacters(List<String> originalList, List<String> query) {
        for ( String token : new ArrayList<String>(query) ) {
            if (token.equals(",") || token.equals("=")) {
                originalList.remove(token);
            }
        }
        originalList.replaceAll(token -> token.replace("'", ""));
        return originalList;
    }

    public boolean isListValid(List<String> chosenList, String listType) {
        for (int i = 0; i < chosenList.size(); i++) {
            if (listType.equalsIgnoreCase("NameValueList")) {
                if (!checkNameValueList(chosenList, i)) {
                    return false;
                }
            } else if (listType.equalsIgnoreCase("ValueList")) {
                if (!checkValueLists(chosenList, i)) {
                    return false;
                }
            } else {
                if (!checkAttributeLists(chosenList, i, listType)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkValueLists(List<String> chosenList, int index) {
        if (index % 2 == 0 && checkValue(chosenList.get(index))) { return true; }
        else if (index % 2 != 0 && chosenList.get(index).equals(",")) { return true; }
        else return false;
    }

    public boolean checkAttributeLists(List<String> chosenList, int index, String listType) {
        if (listType.equalsIgnoreCase("AttributeList")) {
            if (index == 0 && chosenList.get(index).equalsIgnoreCase("id")) { return true; }
            else if ((index % 2 == 0 && index != 0)
                    && chosenList.get(index).equals(",")) { return true; }
            else if (index % 2 != 0 && checkPlainText(chosenList.get(index))) { return true; }
        } else if (listType.equalsIgnoreCase("WildAttributeList")) {
            if (index % 2 != 0 && chosenList.get(index).equals(",")) { return true; }
            else if (index % 2 == 0 && checkPlainText(chosenList.get(index))) { return true; }
        }
        return false;
    }

    public boolean checkNameValueList(List<String> chosenList, int index) {
        if (((index - 1) % 4) == 0 && chosenList.get(index).equals("=")) { return true; }
        else if (((index - 2) % 4) == 0 && checkValue(chosenList.get(index))) { return true; }
        else if (((index + 1) % 4) == 0 && chosenList.get(index).equals(",")) { return true; }
        else if (index % 4 == 0 && (checkPlainText(chosenList.get(index)))) { return true; }
        else return false;
    }

    public boolean checkValue(String token) {
        boolean isString = true;
        boolean isNumber = true;

        // integer & float literals
        for (int i = 0; i < token.length(); i++) {
            if (i == 0 && !Character.isDigit(token.charAt(i))
                    && token.charAt(i) != '+' && token.charAt(i) != '-') {
                isNumber =  false;
            } else if (((i == token.length() - 1) || (i == 1 && (token.charAt(0) == '+' || token.charAt(0) == '-')) )
                    && !Character.isDigit(token.charAt(i))) {
                isNumber =  false;
            } else if (i != 0 && !Character.isDigit(token.charAt(i)) && token.charAt(i) != '.') {
                isNumber =  false;
            }
        }
        // string literal
        if (!isNumber) {
            if (token.charAt(0) != '\'' || token.charAt(token.length() - 1) != '\'') {
                isString = false;
            }
        }
        // boolean literal & "NULL"
        return isNumber || isString || token.equalsIgnoreCase("true")
                || token.equalsIgnoreCase("false") || token.equalsIgnoreCase("null");
    }

    public boolean checkPlainText(String token) {
        return checkAlphaNumeric(token) && !isThereReservedWord(token);
    }

    public boolean checkAlphaNumeric(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isLetterOrDigit(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isThereReservedWord(String token) {
        for (String word : this.reservedWords) {
            if (word.equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }
}



