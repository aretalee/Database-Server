package edu.uob;

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
//        queryType.add(query.get(0));
//        tempQueryTerms.add(queryType);

public class QueryParser {

    List<List<String>> queryTerms;

    String[] reservedWords = {"use", "create", "drop", "alter", "insert", "select", "update", "delete", "join"
                              , "database", "table", "into", "values", "from", "where", "set", "and", "or"
                              , "add", "true", "false", "null"};

    // all parsers with loops inside timeout :(

    public static void main(String[] args) {

//        String newQuery = "JOIN coursework AND marks ON insert AND id;";
//        QueryLexer lexer = new QueryLexer(newQuery);
//        lexer.setup();
//        ArrayList<String> tokens = lexer.getTokens();
//
//        QueryParser parser = new QueryParser();
//        List<List<String>> parsedQuery = parser.parseQuery(tokens);
//
//        for (List<String> queryTerm : parsedQuery) {
//            for (String term : queryTerm) {
//                System.out.println(term);
//            }
//        }
//
    }

    // function of query depends on 1st word in ArrayList<String>
    // USE vs SELECT vs CREATE etc.


    public void parseQuery(List<String> query, DBServer server) {

        // if return null -> invalid query

        List<List<String>> tempQueryTerms = new ArrayList<List<String>>();

        if (!isThereSemicolon(query.get(query.size() - 1))) {
            tempQueryTerms = null;
        }

        List<String> queryType = new ArrayList<String>();
        queryType.add(query.get(0));
        tempQueryTerms.add(queryType);


        // need to make this less complex...

        switch (query.get(0).toLowerCase()) {
            case "use" -> parseUse(tempQueryTerms, query);
            case "create" -> parseCreate(tempQueryTerms, query);
            case "drop" -> parseDrop(tempQueryTerms, query);
            case "alter" -> parseAlter(tempQueryTerms, query);
            case "insert" -> parseInsert(tempQueryTerms, query);
            case "select" -> parseSelect(tempQueryTerms, query);
            case "update" -> parseUpdate(tempQueryTerms, query);
            case "delete" -> parseDelete(tempQueryTerms, query);
            case "join" -> parseJoin(tempQueryTerms, query);
            default -> tempQueryTerms = null;

        }

//        return queryTerms;
    }

    public void parseUse(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 3 || !checkAlphaNumeric(query.get(1))
                || isThereReservedWord(query.get(1))) {
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();

        fileName.add(query.get(1));
        tempQueryTerms.add(fileName);

//        return tempQueryTerms;
    }

    public void parseCreate(List<List<String>> tempQueryTerms, List<String> query) {

        // need to simplify this...


        List<String> fileName = new ArrayList<String>();
        List<String> attributeList = new ArrayList<String>();


            if ((!query.get(1).equalsIgnoreCase("database") && !query.get(1).equalsIgnoreCase("table"))
                    || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                    || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                    || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))) {
                tempQueryTerms = null;
            }
            fileName.add(query.get(2));
            tempQueryTerms.add(fileName);

            if (query.size() > 4) {
                if (!query.get(3).equalsIgnoreCase("(")) {
                    tempQueryTerms = null;
                }
                int index = 4;
                attributeList = addToList(attributeList, query, index);
                if (!query.get(query.size() - 2).equalsIgnoreCase(")")
                        || !isListValid(attributeList, "AttributeList")) {
                    tempQueryTerms = null;
                }
                tempQueryTerms.add(attributeList);
            }
//            else {
//                return null;
//            }



//        return tempQueryTerms;
    }

    public void parseDrop(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 4 || (!query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2))) {
            System.out.println("error");
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

//        return tempQueryTerms;
    }

    public void parseAlter(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 6 || !query.get(1).equalsIgnoreCase("table")
                || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))
                || (!query.get(3).equalsIgnoreCase("add") && !query.get(3).equalsIgnoreCase("drop"))
                || !checkAlphaNumeric(query.get(4)) || isThereReservedWord(query.get(4))) {
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        List<String> alterationType = new ArrayList<String>();
        alterationType.add(query.get(3));
        tempQueryTerms.add(alterationType);

        List<String> attributeName = new ArrayList<String>();
        attributeName.add(query.get(4));
        tempQueryTerms.add(attributeName);

//        return tempQueryTerms;
    }

    public void parseInsert(List<List<String>> tempQueryTerms, List<String> query) {

        if (!query.get(1).equalsIgnoreCase(("into")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(3).equalsIgnoreCase(("values"))
                || !query.get(4).equalsIgnoreCase("(")) {
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        List<String> valueList = new ArrayList<String>();
        int index = 5;
        valueList = addToList(valueList, query, index);

        if (!query.get(query.size() - 2).equalsIgnoreCase(")")) {
            System.out.println("error here");
            tempQueryTerms = null;
        }

        tempQueryTerms.add(valueList);

//        return tempQueryTerms;
    }

    public void parseSelect(List<List<String>> tempQueryTerms, List<String> query) {

        List<String> wildAttributeList = new ArrayList<String>();
        int index = 1;

        if (query.get(1).equalsIgnoreCase("*")) {
            wildAttributeList.add(query.get(1));
        } else {
            wildAttributeList = addToList(wildAttributeList, query, index);
        }

        index = wildAttributeList.size() + index;
        if (!query.get(index).equalsIgnoreCase("from")
                || !checkAlphaNumeric(query.get(index + 1))
                || isThereReservedWord(query.get(index + 1))
                || !isListValid(wildAttributeList, "WildAttributeList")) {
            tempQueryTerms = null;
        }

        tempQueryTerms.add(wildAttributeList);
        index++;

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(index));
        tempQueryTerms.add(fileName);
        index++;

        if ((query.size() - 2) != (index)) {
            // query.size() - 2 cause minus ";" and [tablename] to et to where index should be when list = *
            if (!query.get(index).equalsIgnoreCase("where")) {
                tempQueryTerms = null;
            }
            parseCondition(tempQueryTerms, query, index + 1);
        }


//        return tempQueryTerms;
    }

    public void parseUpdate(List<List<String>> tempQueryTerms, List<String> query) {

        if (!checkAlphaNumeric(query.get(1)) || isThereReservedWord(query.get(1))
                || !query.get(2).equalsIgnoreCase(("set"))) {
            tempQueryTerms = null;
        }

        List<String> nameValueList = new ArrayList<String>();
        int index = 3;
        nameValueList = addToList(nameValueList, query, index);

        index = nameValueList.size() + index;
        if (!query.get(index).equalsIgnoreCase("where")
                || !isListValid(nameValueList, "NameValueList")) {
            tempQueryTerms = null;
        }

        tempQueryTerms.add(nameValueList);

        index++;
        parseCondition(tempQueryTerms, query, index);

//        return tempQueryTerms;
    }

    public void parseDelete(List<List<String>> tempQueryTerms, List<String> query) {

        if (!query.get(1).equalsIgnoreCase(("from")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(4).equalsIgnoreCase(("where"))
                ||!query.get(3).equalsIgnoreCase(("values")) || !query.get(4).equalsIgnoreCase("(")) {
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        parseCondition(tempQueryTerms, query, 4);

//        return tempQueryTerms;
    }

    public void parseJoin(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 9 || !checkAlphaNumeric(query.get(1))
                || !query.get(2).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(3)) || isThereReservedWord(query.get(3))
                || !query.get(4).equalsIgnoreCase("on")
                || !checkAlphaNumeric(query.get(5)) || isThereReservedWord(query.get(5))
                || !query.get(6).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(7)) || isThereReservedWord(query.get(7))) {
            tempQueryTerms = null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(1));
        fileName.add(query.get(3));
        tempQueryTerms.add(fileName);

        List<String> attributeName = new ArrayList<String>();
        attributeName.add(query.get(5));
        attributeName.add(query.get(7));
        tempQueryTerms.add(attributeName);

//        return tempQueryTerms;
    }

    public void parseCondition(List<List<String>> tempQueryTerms, List<String> query, int startIndex) {

        // maybe need to use recursive descent

        List<String> conditions = new ArrayList<String>();

        for (int i = startIndex; i < query.size(); i++) {
            if (! query.get(i).equalsIgnoreCase("(") && query.get(i).equalsIgnoreCase(")")
                      ) {
                tempQueryTerms = null;
            }

        }

//        return tempQueryTerms;
    }

    public List<String> addToList(List<String> chosenList, List<String> query, int index) {

        while (!query.get(index).equalsIgnoreCase(")")) {
            if (!query.get(index).equalsIgnoreCase(",")) {
                chosenList.add(query.get(index));
            }
            index++;
        }
        return chosenList;
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
                if ((!chosenList.get(i).equalsIgnoreCase(",") && !chosenList.get(i).equalsIgnoreCase("*"))
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



