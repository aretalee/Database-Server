package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// need to switch out all magic numbers if possible
// try to trim down repeated code
// also try to make it more compact (maybe less if-statements?)

// error handling needs to be refined!!!!!!

// check if names should be turned to lowercase!!!!!!


public class QueryParser {

    String[] reservedWords = {"use", "create", "drop", "alter", "insert", "select", "update", "delete", "join"
                              , "database", "table", "into", "values", "from", "where", "set", "and", "or"
                              , "add", "true", "false", "null"};

    public boolean parseQuery(List<String> query, DBServer server) throws IOException {

        if (!isThereSemicolon(query.get(query.size() - 1))) {
            server.setErrorLine("Missing semicolon at end of query.");
            return false;
        }

        boolean queryValid;

        // need to make this less complex...

        switch (query.get(0).toLowerCase()) {
            case "use" ->  queryValid =  parseUse(server, query);
            case "create" ->  queryValid =  parseCreate(server, query);
            case "drop" ->  queryValid =  parseDrop(server, query);
            case "alter" ->  queryValid =  parseAlter(server, query);
            case "insert" ->  queryValid =  parseInsert(server, query);
            case "select" ->  queryValid =  parseSelect(server, query);
            case "update" ->  queryValid =  parseUpdate(server, query);
            case "delete" ->  queryValid =  parseDelete(server, query);
            case "join" ->  queryValid =  parseJoin(server, query);
            default -> {
                server.setErrorLine("First word is not a valid query type.");
                queryValid =  false;
            }

        }

        return queryValid;
    }

    public boolean parseUse(DBServer server, List<String> query) throws IOException {

        if (query.size() != 3 || !checkAlphaNumeric(query.get(1))
                || isThereReservedWord(query.get(1))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String databaseName = query.get(1).toLowerCase();
        server.setCurrentDatabase(databaseName);
        Use use = new Use();
        use.switchDatabases(server.getStorageFolderPath(), databaseName, server);

        return true;
    }

    public boolean parseCreate(DBServer server, List<String> query) throws IOException {

        // need to simplify this...
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("id");

        if ((!query.get(1).equalsIgnoreCase("database") && !query.get(1).equalsIgnoreCase("table"))
                || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))) {
            server.setErrorLine("Invalid query one.");
            return false;
        }
        String fileName = query.get(2).toLowerCase();

        if (query.size() > 4) {
            if (!query.get(3).equalsIgnoreCase("(")) {
                server.setErrorLine("Invalid query two.");
                return false;
            }
            int index = 4;

            attributeList = addToList(attributeList, query, index, ")");
            if (!query.get(query.size() - 2).equalsIgnoreCase(")")
                    || !isListValid(attributeList, "AttributeList")) {
                server.setErrorLine("Invalid query three.");
                return false;
            }
        }

        Create create = new Create();
        if (query.get(1).equalsIgnoreCase("database")) {
            create.createDatabase(server.getStorageFolderPath(), fileName);
        } else {
            String databasePath = server.getStorageFolderPath() + File.separator + server.getCurrentDatabase();
            create.createTable(databasePath,  fileName, attributeList, server);
        }
        return true;
    }

    public boolean parseDrop(DBServer server, List<String> query) throws IOException {

        if (query.size() != 4 || (!query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String fileName = query.get(2).toLowerCase();

        Drop drop = new Drop();
        String filePath;
        if (query.get(1).equalsIgnoreCase("database")) {
            filePath = server.getStorageFolderPath() + File.separator + fileName;
        } else {
            filePath = server.getStorageFolderPath() + File.separator + server.getCurrentDatabase() + File.separator + fileName + ".tab";
            // does DROP TABLE only work in specified database? or is it able to drop table in another one
        }
        drop.dropFile(filePath, server);

        return true;
    }

    public boolean parseAlter(DBServer server, List<String> query) throws IOException {

        if (query.size() != 6 || !query.get(1).equalsIgnoreCase("table")
                || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))
                || (!query.get(3).equalsIgnoreCase("add") && !query.get(3).equalsIgnoreCase("drop"))
                || !checkAlphaNumeric(query.get(4)) || isThereReservedWord(query.get(4))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2).toLowerCase();
        String alterationType = query.get(3);
        String attributeName = query.get(4);

        Alter alter = new Alter();
        alter.alterTable(server.getTable(tableName), alterationType, attributeName);

        return true;
    }

    public boolean parseInsert(DBServer server, List<String> query) throws IOException{

        if (!query.get(1).equalsIgnoreCase(("into")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(3).equalsIgnoreCase(("values"))
                || !query.get(4).equalsIgnoreCase("(")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2).toLowerCase().toLowerCase();

        List<String> valueList = new ArrayList<String>();
        int index = 5;
        valueList = addToList(valueList, query, index, ")");

        if (!query.get(query.size() - 2).equalsIgnoreCase(")")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        Insert insert = new Insert();
        insert.insertIntoTable(server.getTable(tableName), valueList);

        return true;
    }

    public boolean parseSelect(DBServer server, List<String> query) {

        List<String> wildAttributeList = new ArrayList<String>();
        int index = 1;

        if (query.get(1).equalsIgnoreCase("*")) {
            wildAttributeList.add(query.get(1));
        } else {
            wildAttributeList = addToList(wildAttributeList, query, index, "from");
        }

        int numberOfCommas = wildAttributeList.size() - 1;
        index = wildAttributeList.size() + index + numberOfCommas;
        if (!query.get(index).equalsIgnoreCase("from")
                || !checkAlphaNumeric(query.get(index + 1))
                || isThereReservedWord(query.get(index + 1))
                || !isListValid(wildAttributeList, "WildAttributeList")) {
            server.setErrorLine("Invalid query one.");
            return false;
        }

        index++;

        String tableName = query.get(index).toLowerCase();
        index++;

        List<List<String>> conditionList = new ArrayList<>();
        if ((query.size() - 1) != (index)) {
            // query.size() - 1 to get to where index should be when list = *
            if (!query.get(index).equalsIgnoreCase("where")) {
                server.setErrorLine((query.size() - 2) + " " + index + " " + "Invalid query two.");
                return false;
            }
            if (!parseCondition(server, query, index + 1, conditionList)) {
                server.setErrorLine("Could not parse conditions.");
                return false;
            }
        }

        Select select = new Select();
        select.selectRecords(server, server.getTable(tableName), wildAttributeList, refineConditionList(conditionList));

        return true;
    }

    public boolean parseUpdate(DBServer server, List<String> query) throws IOException {

        if (!checkAlphaNumeric(query.get(1)) || isThereReservedWord(query.get(1))
                || !query.get(2).equalsIgnoreCase(("set"))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(1).toLowerCase();

        List<String> nameValueList = new ArrayList<String>();
        int index = 3;
        nameValueList = addToList(nameValueList, query, index, "where");

        int numberOfSymbols = nameValueList.size() - 1;
        index = nameValueList.size() + index + numberOfSymbols;
        if (!query.get(index).equalsIgnoreCase("where")
                || !isListValid(nameValueList, "NameValueList")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        index++;

        List<List<String>> conditionList = new ArrayList<>();
        if (!parseCondition(server, query, index, conditionList)) {
            server.setErrorLine("Could not parse conditions.");
            return false;
        }

        Update update = new Update();
        update.updateTable(server.getTable(tableName), nameValueList, refineConditionList(conditionList));


        return true;
    }

    public boolean parseDelete(DBServer server, List<String> query) throws IOException {

        if (!query.get(1).equalsIgnoreCase(("from")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(3).equalsIgnoreCase("where")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2).toLowerCase();

        List<List<String>> conditionList = new ArrayList<>();
        if (!parseCondition(server, query, 4, conditionList)) {
            server.setErrorLine("Could not parse conditions.");
            return false;
        }


        Delete delete = new Delete();
        delete.deleteRecord(server.getTable(tableName), refineConditionList(conditionList));

        return true;
    }

    public boolean parseJoin(DBServer server, List<String> query) {

        if (query.size() != 9 || !checkAlphaNumeric(query.get(1))
                || !query.get(2).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(3)) || isThereReservedWord(query.get(3))
                || !query.get(4).equalsIgnoreCase("on")
                || !checkAlphaNumeric(query.get(5)) || isThereReservedWord(query.get(5))
                || !query.get(6).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(7)) || isThereReservedWord(query.get(7))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String fileNameOne = query.get(1).toLowerCase();
        String fileNameTwo = query.get(3).toLowerCase();

        String attributeNameOne = query.get(5);
        String attributeNameTwo = query.get(7);

        Join join = new Join();


        return true;
    }

    public boolean parseCondition(DBServer server, List<String> query, int startIndex, List<List<String>> conditionList) {

        String[] comparators = {"==", ">", "<", ">=", "<=", "!=", " LIKE "};

        int lookAheadBy = 4;

        List<String> precedence = new ArrayList<String>();
        for (int i = startIndex; i < query.size(); i++) {
            String currentToken = query.get(i);
            if (currentToken.equals(")")) {
                query.set(i, " ");
                break; // see if there is workaround (avoid using break if possible)
            }
            if (currentToken.equals("(")) {
                parseCondition(server, query, i + 1, conditionList);
            }

            // trim this down if possible!
            if (!currentToken.equalsIgnoreCase("and") && !currentToken.equalsIgnoreCase("or")
                        && (i != 0 && !isTokenComparator(query.get(i - 1), comparators)
                        && !checkAlphaNumeric(currentToken) && isThereReservedWord(currentToken))
                        && !isTokenComparator(currentToken, comparators)) {
                server.setErrorLine("Invalid query.");
                return false;
            }
            if (!currentToken.equals("(") && !currentToken.equals(";") && !currentToken.equals(" ")) {
                precedence.add(currentToken);
            }
            query.set(i, " ");
        }

        conditionList.add(precedence);
        return true;
    }

    public List<List<String>> refineConditionList(List<List<String>> conditionList) {
        List<List<String>> refinedList = new ArrayList<List<String>>();
        List<String> comparisons = new ArrayList<String>();
        List<String> boolOperators = new ArrayList<String>();

        for (List<String> row : conditionList) {
            for (String item : row) {
                if (item.equals("and") || item.equals("or")) {
                    boolOperators.add(item);
                } else {
                    comparisons.add(item);
                }
            }
        }
        refinedList.add(comparisons);
        refinedList.add(boolOperators);
        return refinedList;
    }


    public boolean isTokenComparator(String token, String[] comparators) {
        for (String comparator : comparators) {
            if (comparator.equals(token)) {
                return true;
            }
        }
        return false;
    }


    public List<String> addToList(List<String> chosenList, List<String> query, int index, String terminatingChar) {

        while (!query.get(index).equalsIgnoreCase(terminatingChar)) {
            addItem(chosenList, query, index);
            index++;
        }

        return chosenList;
    }

    public void addItem(List<String> chosenList, List<String> query, int index) {
        if (!query.get(index).equals(",") && !query.get(index).equals("=")) {
            chosenList.add(query.get(index));
        }
    }

    public boolean isListValid(List<String> chosenList, String listType) {

        for (int i = 0; i < chosenList.size(); i++) {

            // repeated code...
            if (listType.equalsIgnoreCase("NameValueList")) {
                if (i % 4 == 0
                        && (!checkAlphaNumeric(chosenList.get(i))
                        || isThereReservedWord(chosenList.get(i)))) {
                    return false;
                }
            } else {
                if ((!chosenList.get(i).equals(",") && !chosenList.get(i).equals("*"))
                        && (!checkAlphaNumeric(chosenList.get(i))
                        || isThereReservedWord(chosenList.get(i)))) {
                    return false;
                }
            }
        }

        return true;
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

    public boolean isThereSemicolon(String token) {

        return token.equalsIgnoreCase(";");
    }

}



