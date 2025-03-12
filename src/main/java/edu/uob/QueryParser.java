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

    public boolean parseQuery(List<String> query, QueryHandler handler) {
        boolean queryValid;
        if (!query.get(query.size() - 1).equalsIgnoreCase(";")) { queryValid = false; }
        else {
            switch (query.get(0).toLowerCase()) {
                case "use" -> queryValid = parseAndExecuteUse(handler, query);
                case "create" -> queryValid = parseCreate(handler, query);
                case "drop" -> queryValid = parseDrop(handler, query);
                case "alter" -> queryValid = parseAndExecuteAlter(handler, query);
                case "insert" -> queryValid = parseInsert(handler, query);
                case "select" -> queryValid = parseSelect(handler, query);
                case "update" -> queryValid = parseUpdate(handler, query);
                case "delete" -> queryValid = parseAndExecuteDelete(handler, query);
                case "join" -> queryValid = parseAndExecuteJoin(handler, query);
                default -> queryValid = false;
            }
        }
        if (!queryValid && handler.getErrorLine() == null) { handler.setErrorLine("Invalid query parameters."); }
        return queryValid;
    }

    public boolean parseAndExecuteUse(QueryHandler handler, List<String> query) {
        if (query.size() != 3 || !checkPlainText(query.get(1))) {
            return false;
        }
        handler.setCurrentDatabase(query.get(1).toLowerCase());

        Use use = new Use();
        String storageFolderPath = handler.getCurrentServer().getStorageFolderPath();
        return use.switchDatabases(storageFolderPath, query.get(1).toLowerCase(), handler);
    }


    public boolean parseCreate(QueryHandler handler, List<String> query) {
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("id");

        if ((!query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table"))
                || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                || !checkPlainText(query.get(2))) {
            return false;
        }
        if (query.size() > 4) {
            if (!query.get(3).equals("(") || !query.get(query.size() - 2).equals(")")) {
                return false;
            }
            attributeList = addToList(attributeList, query, 4, ")");
            if (!isListValid(attributeList, "AttributeList")) {
                return false;
            }
        }
        return executeCreate(handler, query, query.get(2).toLowerCase(), attributeList);
    }
    
    public boolean executeCreate(QueryHandler handler, List<String> query, String fileName, List<String> attriList) {
        Create create = new Create();
        String storageFolderPath = handler.getCurrentServer().getStorageFolderPath();
        if (query.get(1).equalsIgnoreCase("database")) {
            return create.createDatabase(storageFolderPath, fileName, handler);
        } else {
            String databasePath = storageFolderPath + File.separator + handler.getCurrentDatabase();
            return create.createTable(databasePath, fileName, removeSpecialCharacters(attriList, query), handler);
        }
    }

    public boolean parseDrop(QueryHandler handler, List<String> query) {
        if (query.size() != 4 || !query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table") || !checkPlainText(query.get(2))) {
            return false;
        }
        return executeDrop(handler, query, query.get(2).toLowerCase());
    }

    public boolean executeDrop(QueryHandler handler, List<String> query, String fileName) {
        Drop drop = new Drop();
        String filePath;
        String storageFolderPath = handler.getCurrentServer().getStorageFolderPath();
        if (query.get(1).equalsIgnoreCase("database")) {
            filePath = storageFolderPath + File.separator + fileName;
        } else {
            filePath = storageFolderPath + File.separator + handler.getCurrentDatabase()
                    + File.separator + fileName + ".tab";
        }
        return drop.dropFile(filePath, handler);
    }

    public boolean parseAndExecuteAlter(QueryHandler handler, List<String> query) {
        if (query.size() != 6 || !query.get(1).equalsIgnoreCase("table")
                || !checkPlainText(query.get(2)) || (!query.get(3).equalsIgnoreCase("add")
                && !query.get(3).equalsIgnoreCase("drop"))
                || !checkPlainText(query.get(4))) {
            return false;
        }
        Alter alter = new Alter();
        Table thisTable = handler.getTable(query.get(2).toLowerCase(), handler.getCurrentDatabase());
        return alter.alterTable(thisTable, query.get(3), query.get(4), handler);
    }

    public boolean parseInsert(QueryHandler handler, List<String> query) {
        if (!query.get(1).equalsIgnoreCase(("into")) || !checkPlainText(query.get(2))
                || !query.get(3).equalsIgnoreCase(("values")) || !query.get(4).equalsIgnoreCase("(")
                || !query.get(query.size() - 2).equalsIgnoreCase(")")) {
            return false;
        }
        List<String> valueList = new ArrayList<String>();
        valueList = addToList(valueList, query, 5, ")");
        if (!isListValid(valueList, "ValueList")) {
            return false;
        }
        return executeInsert(handler, query, query.get(2).toLowerCase(), valueList);
    }

    public boolean executeInsert(QueryHandler handler, List<String> query, String table, List<String> valueList) {
        Insert insert = new Insert();
        Table thisTable = handler.getTable(table, handler.getCurrentDatabase());
        return insert.insertIntoTable(handler, thisTable, removeSpecialCharacters(valueList, query));
    }

    public boolean parseSelect(QueryHandler handler, List<String> query) {
        List<String> wildAttributeList = new ArrayList<String>();

        if (!isThereKeyword(query, "from")) { return false; }

        if (query.get(1).equals("*")) {
            wildAttributeList.add(query.get(1));
        } else {
            wildAttributeList = addToList(wildAttributeList, query, 1, "from");
        }
        int index = wildAttributeList.size() + 1;
        if (!checkPlainText(query.get(++index))
                || (!isListValid(wildAttributeList, "WildAttributeList") && !query.get(1).equals("*"))) {
            return false;
        }
        String tableName = query.get(index).toLowerCase();

        if ((query.size() - 1) != (++index)) {
            if (!query.get(index).equalsIgnoreCase("where")
                    || !checkConditionBrackets(query, index + 1)
                    || !isConditionValid(query, index + 1)) {
                return false;
            }
        }
        List<String> conditionList = parseCondition(query, index + 1);
        return executeSelect(handler, query, tableName, wildAttributeList, conditionList);
    }

    public boolean executeSelect(QueryHandler handler, List<String> query, String table,
                                 List<String> attributes, List<String> conditions) {
        Select select = new Select();
        Table thisTable = handler.getTable(table, handler.getCurrentDatabase());
        return select.selectRecords(thisTable, removeSpecialCharacters(attributes, query), conditions, handler);
    }

    public boolean parseUpdate(QueryHandler handler, List<String> query) {
        if (!checkPlainText(query.get(1)) || !query.get(2).equalsIgnoreCase(("set"))
                || !isThereKeyword(query, "where")) {
            return false;
        }
        List<String> nameValueList = new ArrayList<String>();
        nameValueList = addToList(nameValueList, query, 3, "where");

        int index = nameValueList.size() + 3;
        if (!isListValid(nameValueList, "NameValueList")
                || !checkConditionBrackets(query, index + 1)
                || !isConditionValid(query, index + 1)) {
            return false;
        }
        List<String> conditionList = parseCondition(query, index + 1);
        return executeUpdate(handler, query, query.get(1).toLowerCase(), nameValueList, conditionList);
    }

    public boolean executeUpdate(QueryHandler handler, List<String> query, String table,
                                 List<String> nValueList, List<String> conditions) {
        Update update = new Update();
        Table thisTable = handler.getTable(table, handler.getCurrentDatabase());
        return update.updateTable(thisTable, removeSpecialCharacters(nValueList, query), conditions, handler);
    }

    public boolean parseAndExecuteDelete(QueryHandler handler, List<String> query) {
        if (!query.get(1).equalsIgnoreCase(("from")) || !checkPlainText(query.get(2))
                || !query.get(3).equalsIgnoreCase("where")
                || !checkConditionBrackets(query, 4) || !isConditionValid(query, 4)) {
            return false;
        }
        List<String> conditionList = parseCondition(query, 4);

        Delete delete = new Delete();
        Table thisTable = handler.getTable(query.get(2).toLowerCase(), handler.getCurrentDatabase());
        return delete.deleteRecord(thisTable, conditionList, handler);
    }

    public boolean parseAndExecuteJoin(QueryHandler handler, List<String> query) {
        if (query.size() != 9 || !checkAlphaNumeric(query.get(1))
                || !query.get(2).equalsIgnoreCase("and") || !checkPlainText(query.get(3))
                || !query.get(4).equalsIgnoreCase("on") || !checkPlainText(query.get(5))
                || !query.get(6).equalsIgnoreCase("and") || !checkPlainText(query.get(7))) {
            return false;
        }
        Join join = new Join();
        Table tableOne = handler.getTable(query.get(1).toLowerCase(), handler.getCurrentDatabase());
        Table tableTwo = handler.getTable(query.get(3).toLowerCase(), handler.getCurrentDatabase());
        return join.joinTables(tableOne, tableTwo, query.get(5).toLowerCase(), query.get(7).toLowerCase(), handler);

    }

    public List<String> parseCondition(List<String> query, int startIndex) {
        List<String> parsedConditions = new ArrayList<String>();
        Stack<String> operators = new Stack<String>();

        for (int i = startIndex; i < query.size(); i++) {
            String currentToken = query.get(i);
            if (currentToken.equals(")")) {
                while (!operators.empty() && !operators.peek().equals("(")) {
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

    public boolean isConditionValid(List<String> query, int startIndex) {
        String[] comparators = {"==", ">", "<", ">=", "<=", "!=", "LIKE"};
        List<String> trimmedQuery = trimQuery((new ArrayList<String>(query)).subList(startIndex, query.size()));

        if (trimmedQuery.isEmpty() || (trimmedQuery.size() + 1) % 4 != 0) {
            return false;
        }
        for (int i = 0; i < trimmedQuery.size(); i++) {
            String currentToken = trimmedQuery.get(i);
            if (!currentToken.equals(";") && !currentToken.equals(" ")) {
                if (((i - 1) % 4) == 0 && !isTokenComparator(currentToken, comparators)
                        || ((i - 2) % 4) == 0 && !checkValue(currentToken)
                        || ((i + 1) % 4) == 0 && !currentToken.equalsIgnoreCase("and")
                        && !currentToken.equalsIgnoreCase("or")
                        || i % 4 == 0 && (!checkPlainText(currentToken))) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> trimQuery(List<String> query) {
        query.removeAll(Collections.singleton("("));
        query.removeAll(Collections.singleton(")"));
        query.removeAll(Collections.singleton(";"));
        return query;
    }

    public boolean checkConditionBrackets(List<String> query, int startIndex) {
        int openBracketCount = 0;
        int closeBracketCount = 0;

        for (int i = startIndex; i < query.size(); i++) {
            if (query.get(i).equals("(")) {
                if (i != startIndex && (!query.get(i - 1).equals("(")
                        && (!query.get(i - 1).equalsIgnoreCase("and")
                        && !query.get(i - 1).equalsIgnoreCase("or")))) {
                    return false;
                }
                openBracketCount++;
            } else if (query.get(i).equals(")")) {
                if (i != (query.size() - 2) && (!query.get(i + 1).equals(")")
                        && !query.get(i + 1).equalsIgnoreCase("and")
                        && !query.get(i + 1).equalsIgnoreCase("or"))) {
                    return false;
                }
                closeBracketCount++;
            }
        }
        return openBracketCount == closeBracketCount;
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
        if (index % 2 == 0 && checkValue(chosenList.get(index))) {
            if (chosenList.get(index).charAt(0) != '\'' && chosenList.get(index).charAt(0) == '+') {
                chosenList.set(index, chosenList.get(index).replace("+", ""));
            }
            return true;
        }
        else if (index % 2 != 0 && chosenList.get(index).equals(",")) { return true; }
        return false;
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
        else if (((index - 2) % 4) == 0 && checkValue(chosenList.get(index))) {
            if (chosenList.get(index).charAt(0) != '\'' && chosenList.get(index).charAt(0) == '+') {
                chosenList.set(index, chosenList.get(index).replace("+", ""));
            }
            return true;
        }
        else if (((index + 1) % 4) == 0 && chosenList.get(index).equals(",")) { return true; }
        else if (index % 4 == 0 && (checkPlainText(chosenList.get(index)))) { return true; }
        else return false;
    }

    public boolean checkValue(String token) {
        boolean isString = true;
        boolean isNumber = true;

        if (token.charAt(0) == '"' || token.charAt(token.length() - 1) == '"') {
            return false;
        }
        // string literal
        if (token.charAt(0) != '\'' || token.charAt(token.length() - 1) != '\'') {
            isString = false;
        }
        // integer & float literals
        if (!isString) {
            for (int i = 0; i < token.length(); i++) {
                isNumber = checkNumberLiterals(token, i, isNumber);
            }
        }
        // boolean literal & "NULL"
        return isNumber || isString || token.equalsIgnoreCase("true")
                || token.equalsIgnoreCase("false") || token.equalsIgnoreCase("null");
    }

    public boolean checkNumberLiterals(String token, int index, boolean isNumber) {
        if (index == 0 && !Character.isDigit(token.charAt(index))
                && token.charAt(index) != '+' && token.charAt(index) != '-') {
            isNumber = false;
        } else if (((index == token.length() - 1) || (index == 1 && (token.charAt(0) == '+' || token.charAt(0) == '-')))
                && !Character.isDigit(token.charAt(index))) {
            isNumber = false;
        } else if (index != 0 && !Character.isDigit(token.charAt(index)) && token.charAt(index) != '.') {
            isNumber = false;
        }
        return isNumber;
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

    public boolean isThereKeyword(List<String> query, String keyword) {
        for (String s : query) {
            if (s.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }
}



