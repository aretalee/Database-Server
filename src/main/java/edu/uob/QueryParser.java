package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// in-memory representation -> hash table/map (in DBServer class)
// exposing all info to DBcmd (filled up by parsing)
// different overwritten classes for each method that alter or read from file

// need to switch out all magic numbers if possible
// try to trim down repeated code
// also try to make it more compact (maybe less if-statements?)

// think of way to implement parsing for conditions
// may factor out adding to files as a general method e.g. the below
//        List<String> queryType = new ArrayList<String>();
//        queryType = (query.get(0));
//        queryType = (queryType);

// all places with something = null --> error handling needs to be added!!!!!!


public class QueryParser {

    List<List<String>> queryTerms;

    String[] reservedWords = {"use", "create", "drop", "alter", "insert", "select", "update", "delete", "join"
                              , "database", "table", "into", "values", "from", "where", "set", "and", "or"
                              , "add", "true", "false", "null"};

    // all parsers with loops inside timeout :(

    public static void main(String[] args) throws IOException {

        String newQuery = "INSERT INTO marks VALUES ('Simon', 65, TRUE);";
        QueryLexer lexer = new QueryLexer(newQuery);
        lexer.setup();
        ArrayList<String> tokens = lexer.getTokens();

        QueryParser parser = new QueryParser();
        DBServer server = new DBServer();
        Boolean parsedQuery = parser.parseQuery(tokens, server);

        System.out.println(parsedQuery);

    }

    // function of query depends on 1st word in ArrayList<String>
    // USE vs SELECT vs CREATE etc.


    public boolean parseQuery(List<String> query, DBServer server) throws IOException {

        // if return null -> invalid query

//        String queryType = new ArrayList<List<String>>();

        if (!isThereSemicolon(query.get(query.size() - 1))) {
            server.setErrorLine("Missing semicolon at end of query.");
            return false;
        }

        String queryType = query.get(0);


        // need to make this less complex...

        switch (query.get(0).toLowerCase()) {
            case "use" -> parseUse(server, query);
            case "create" -> parseCreate(server, query);
            case "drop" -> parseDrop(server, query);
            case "alter" -> parseAlter(server, query);
            case "insert" -> parseInsert(server, query);
            case "select" -> parseSelect(server, query);
            case "update" -> parseUpdate(server, query);
            case "delete" -> parseDelete(server, query);
            case "join" -> parseJoin(server, query);
            default -> {
                server.setErrorLine("First word is not a valid query type.");
                return false;
            }

        }

//        return queryTerms;
        return true;
    }

    public boolean parseUse(DBServer server, List<String> query) throws IOException {

        if (query.size() != 3 || !checkAlphaNumeric(query.get(1))
                || isThereReservedWord(query.get(1))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String databaseName = query.get(1);
        server.setCurrentDatabase(databaseName);
        Use use = new Use();
        use.switchDatabases(server.getStorageFolderPath(), databaseName, server);

//        return queryType;
        return true;
    }

    public boolean parseCreate(DBServer server, List<String> query) throws IOException {

        // need to simplify this...
        List<String> attributeList = new ArrayList<String>();

        if ((!query.get(1).equalsIgnoreCase("database") && !query.get(1).equalsIgnoreCase("table"))
                || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))) {
            server.setErrorLine("Invalid query.");
            return false;
        }
        String fileName = query.get(2);

        if (query.size() > 4) {
            if (!query.get(3).equalsIgnoreCase("(")) {
                server.setErrorLine("Invalid query.");
                return false;
            }
            int index = 4;

            attributeList = addToList(attributeList, query, index, "AttributeList");
            if (!query.get(query.size() - 2).equalsIgnoreCase(")")
                    || !isListValid(attributeList, "AttributeList")) {
                server.setErrorLine("Invalid query.");
                return false;
            }
        }
//            else {
//                return null;
//            }

        Create create = new Create();
        if (query.get(1).equalsIgnoreCase("database")) {
            create.createDatabase(server.getStorageFolderPath(), fileName);
        } else {
            String databasePath = server.getStorageFolderPath() + File.separator + server.getCurrentDatabase();
            create.createTable(databasePath,  fileName, attributeList, server);
        }
//        return queryType;
        return true;
    }

    public boolean parseDrop(DBServer server, List<String> query) throws IOException {

        if (query.size() != 4 || (!query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String fileName = query.get(2);

        Drop drop = new Drop();
        if (query.get(1).equalsIgnoreCase("database")) {
            String filePath = server.getStorageFolderPath() + File.separator + fileName;
            drop.dropFile(filePath, server);
        } else {
            String filePath = server.getStorageFolderPath() + File.separator + server.getStorageFolderPath() + File.separator + fileName;
            drop.dropFile(filePath, server);
            // does DROP TABLE only work in specified database? or is it able to drop table in another one
        }
        String filePath = server.getStorageFolderPath() + File.separator + server.getCurrentDatabase();


//        return queryType;
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

        String tableName = query.get(2);
        String alterationType = query.get(3);
        String attributeName = query.get(4);

        Alter alter = new Alter();
        alter.alterTable(server.getTable(tableName), alterationType, attributeName);

//        return queryType;
        return true;
    }

    public boolean parseInsert(DBServer server, List<String> query) throws IOException{

        if (!query.get(1).equalsIgnoreCase(("into")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(3).equalsIgnoreCase(("values"))
                || !query.get(4).equalsIgnoreCase("(")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2);

        List<String> valueList = new ArrayList<String>();
        int index = 5;
        valueList = addToList(valueList, query, index, "ValueList");

        if (!query.get(query.size() - 2).equalsIgnoreCase(")")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        Insert insert = new Insert();
        insert.insertIntoTable(server.getTable(tableName), valueList);

//        return queryType;
        return true;
    }

    public boolean parseSelect(DBServer server, List<String> query) throws IOException {

        List<String> wildAttributeList = new ArrayList<String>();
        int index = 1;

        if (query.get(1).equalsIgnoreCase("*")) {
            wildAttributeList.add(query.get(1));
        } else {
            wildAttributeList = addToList(wildAttributeList, query, index, "WildAttributeList");
        }

        index = wildAttributeList.size() + index;
        if (!query.get(index).equalsIgnoreCase("from")
                || !checkAlphaNumeric(query.get(index + 1))
                || isThereReservedWord(query.get(index + 1))
                || !isListValid(wildAttributeList, "WildAttributeList")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        index++;

        String tableName = query.get(index);
        index++;

        if ((query.size() - 2) != (index)) {
            // query.size() - 2 cause minus ";" and [tablename] to et to where index should be when list = *
            if (!query.get(index).equalsIgnoreCase("where")) {
                server.setErrorLine("Invalid query.");
                return false;
            }
            parseCondition(server, query, index + 1);
        }

        Select select = new Select();
        select.selectRecords(server.getTable(tableName), wildAttributeList);

//        return queryType;
        return true;
    }

    public boolean parseUpdate(DBServer server, List<String> query) throws IOException {

        if (!checkAlphaNumeric(query.get(1)) || isThereReservedWord(query.get(1))
                || !query.get(2).equalsIgnoreCase(("set"))) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2);

        List<String> nameValueList = new ArrayList<String>();
        int index = 3;
        nameValueList = addToList(nameValueList, query, index, "NameValueList");

        index = nameValueList.size() + index;
        if (!query.get(index).equalsIgnoreCase("where")
                || !isListValid(nameValueList, "NameValueList")) {
            server.setErrorLine("Invalid query.");
            return false;
        }


        index++;
        parseCondition(server, query, index);

        Update update = new Update();
        update.updateTable(server.getTable(tableName), nameValueList);


//        return queryType;
        return true;
    }

    public boolean parseDelete(DBServer server, List<String> query) throws IOException {

        if (!query.get(1).equalsIgnoreCase(("from")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(4).equalsIgnoreCase(("where"))
                ||!query.get(3).equalsIgnoreCase(("values")) || !query.get(4).equalsIgnoreCase("(")) {
            server.setErrorLine("Invalid query.");
            return false;
        }

        String tableName = query.get(2);

        parseCondition(server, query, 4);

        Delete delete = new Delete();
        delete.deleteRecord(server.getTable(tableName));

//        return queryType;
        return true;
    }

    public boolean parseJoin(DBServer server, List<String> query) throws IOException {

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

        String fileNameOne = query.get(1);
        String fileNameTwo = query.get(3);

        String attributeNameOne = query.get(5);
        String attributeNameTwo = query.get(7);

        Join join = new Join();


//        return queryType;
        return true;
    }

    public boolean parseCondition(DBServer server, List<String> query, int startIndex) throws IOException {

        // maybe need to use recursive descent

        List<String> conditions = new ArrayList<String>();

        for (int i = startIndex; i < query.size(); i++) {
            if (! query.get(i).equals("(") && query.get(i).equals(")")
                      ) {
                server.setErrorLine("Invalid query.");
                return false;
            }

        }

//        return queryType;
        return true;
    }

    public List<String> addToList(List<String> chosenList, List<String> query, int index, String listType) {

        if (listType.equalsIgnoreCase("NameValueList")) {
            while (!query.get(index).equalsIgnoreCase("WHERE")) {
                addItem(chosenList, query, index);
                index++;
            }
        } else if (listType.equalsIgnoreCase("WildAttributeList")) {
            while (!query.get(index).equalsIgnoreCase("FROM")) {
                addItem(chosenList, query, index);
                index++;
            }
        } else {
            while (!query.get(index).equals(")")) {
                addItem(chosenList, query, index);
                index++;
            }
        }
        return chosenList;
    }

    public void addItem(List<String> chosenList, List<String> query, int index) {
        if (!query.get(index).equals(",")) {
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



