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

        String newQuery = "JOIN coursework AND marks ON insert AND id;";
        QueryLexer lexer = new QueryLexer(newQuery);
        lexer.setup();
        ArrayList<String> tokens = lexer.getTokens();

        QueryParser parser = new QueryParser();
        List<List<String>> parsedQuery = parser.parseQuery(tokens);

        for (List<String> queryTerm : parsedQuery) {
            for (String term : queryTerm) {
                System.out.println(term);
            }
        }

    }

    // function of query depends on 1st word in ArrayList<String>
    // USE vs SELECT vs CREATE etc.


    public List<List<String>> parseQuery(List<String> query) {

        // if return null -> invalid query

        List<List<String>> tempQueryTerms = new ArrayList<List<String>>();

        if (!isThereSemicolon(query.get(query.size() - 1))) {
            queryTerms = null;
        }

        List<String> queryType = new ArrayList<String>();
        queryType.add(query.get(0));
        tempQueryTerms.add(queryType);


        // need to make this less complex...

        switch (query.get(0).toLowerCase()) {
            case "use" -> queryTerms = parseUse(tempQueryTerms, query);
            case "create" -> queryTerms = parseCreate(tempQueryTerms, query);
            case "drop" -> queryTerms = parseDrop(tempQueryTerms, query);
            case "alter" -> queryTerms = parseAlter(tempQueryTerms, query);
            case "insert" -> queryTerms = parseInsert(tempQueryTerms, query);
            case "select" -> queryTerms = parseSelect(tempQueryTerms, query);
            case "update" -> queryTerms = parseUpdate(tempQueryTerms, query);
            case "delete" -> queryTerms = parseDelete(tempQueryTerms, query);
            case "join" -> queryTerms = parseJoin(tempQueryTerms, query);
            default -> queryTerms = null;

        }

        return queryTerms;
    }

    public List<List<String>> parseUse(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 3 || !checkAlphaNumeric(query.get(1))
                || isThereReservedWord(query.get(1))) {
            return null;
        }

        List<String> fileName = new ArrayList<String>();

        fileName.add(query.get(1));
        tempQueryTerms.add(fileName);

        return tempQueryTerms;
    }

    public List<List<String>> parseCreate(List<List<String>> tempQueryTerms, List<String> query) {

        // need to simplify this...


        List<String> fileName = new ArrayList<String>();
        List<String> attributeList = new ArrayList<String>();


            if ((!query.get(1).equalsIgnoreCase("database") && !query.get(1).equalsIgnoreCase("table"))
                    || (query.get(1).equalsIgnoreCase("database") && query.size() != 4)
                    || (query.get(1).equalsIgnoreCase("table") && query.size() < 4)
                    || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))) {
                return null;
            }
            fileName.add(query.get(2));
            tempQueryTerms.add(fileName);

            if (query.size() > 4) {
                if (!query.get(3).equalsIgnoreCase("(")) {
                    return null;
                }
                int index = 4;
                attributeList = addToList(attributeList, query, index);
                if (!query.get(query.size() - 2).equalsIgnoreCase(")")
                        || !isListValid(attributeList, "AttributeList")) {
                    return null;
                }
                tempQueryTerms.add(attributeList);
            }
//            else {
//                return null;
//            }

        return tempQueryTerms;
    }

    public List<List<String>> parseDrop(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 4 || (!query.get(1).equalsIgnoreCase("database")
                && !query.get(1).equalsIgnoreCase("table")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2))) {
            System.out.println("error");
            return null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        return tempQueryTerms;
    }

    public List<List<String>> parseAlter(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 6 || !query.get(1).equalsIgnoreCase("table")
                || !checkAlphaNumeric(query.get(2)) || isThereReservedWord(query.get(2))
                || (!query.get(3).equalsIgnoreCase("add") && !query.get(3).equalsIgnoreCase("drop"))
                || !checkAlphaNumeric(query.get(4)) || isThereReservedWord(query.get(4))) {
            return null;
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

        return tempQueryTerms;
    }

    public List<List<String>> parseInsert(List<List<String>> tempQueryTerms, List<String> query) {

        if (!query.get(1).equalsIgnoreCase(("into")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(3).equalsIgnoreCase(("values"))
                || !query.get(4).equalsIgnoreCase("(")) {
            return null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        List<String> valueList = new ArrayList<String>();
        int index = 5;
        valueList = addToList(valueList, query, index);

        if (!query.get(query.size() - 2).equalsIgnoreCase(")")) {
            System.out.println("error here");
            return null;
        }

        tempQueryTerms.add(valueList);

        return tempQueryTerms;
    }

    public List<List<String>> parseSelect(List<List<String>> tempQueryTerms, List<String> query) {

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
            return null;
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
                return null;
            }
            tempQueryTerms = parseCondition(tempQueryTerms, query, index + 1);
        }


        return tempQueryTerms;
    }

    public List<List<String>> parseUpdate(List<List<String>> tempQueryTerms, List<String> query) {

        if (!checkAlphaNumeric(query.get(1)) || isThereReservedWord(query.get(1))
                || !query.get(2).equalsIgnoreCase(("set"))) {
            return null;
        }

        List<String> nameValueList = new ArrayList<String>();
        int index = 3;
        nameValueList = addToList(nameValueList, query, index);

        index = nameValueList.size() + index;
        if (!query.get(index).equalsIgnoreCase("where")
                || !isListValid(nameValueList, "NameValueList")) {
            return null;
        }

        tempQueryTerms.add(nameValueList);

        index++;
        tempQueryTerms = parseCondition(tempQueryTerms, query, index);

        return tempQueryTerms;
    }

    public List<List<String>> parseDelete(List<List<String>> tempQueryTerms, List<String> query) {

        if (!query.get(1).equalsIgnoreCase(("from")) || !checkAlphaNumeric(query.get(2))
                || isThereReservedWord(query.get(2)) || !query.get(4).equalsIgnoreCase(("where"))
                ||!query.get(3).equalsIgnoreCase(("values")) || !query.get(4).equalsIgnoreCase("(")) {
            return null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(2));
        tempQueryTerms.add(fileName);

        tempQueryTerms = parseCondition(tempQueryTerms, query, 4);

        return tempQueryTerms;
    }

    public List<List<String>> parseJoin(List<List<String>> tempQueryTerms, List<String> query) {

        if (query.size() != 9 || !checkAlphaNumeric(query.get(1))
                || !query.get(2).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(3)) || isThereReservedWord(query.get(3))
                || !query.get(4).equalsIgnoreCase("on")
                || !checkAlphaNumeric(query.get(5)) || isThereReservedWord(query.get(5))
                || !query.get(6).equalsIgnoreCase("and")
                || !checkAlphaNumeric(query.get(7)) || isThereReservedWord(query.get(7))) {
            return null;
        }

        List<String> fileName = new ArrayList<String>();
        fileName.add(query.get(1));
        fileName.add(query.get(3));
        tempQueryTerms.add(fileName);

        List<String> attributeName = new ArrayList<String>();
        attributeName.add(query.get(5));
        attributeName.add(query.get(7));
        tempQueryTerms.add(attributeName);

        return tempQueryTerms;
    }

    public List<List<String>> parseCondition(List<List<String>> tempQueryTerms, List<String> query, int startIndex) {

        // maybe need to use recursive descent

        List<String> conditions = new ArrayList<String>();

        for (int i = startIndex; i < query.size(); i++) {
            if (! query.get(i).equalsIgnoreCase("(") && query.get(i).equalsIgnoreCase(")")
                      ) {
                return null;
            }

        }

        return tempQueryTerms;
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



