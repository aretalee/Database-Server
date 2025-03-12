package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Join {

    public boolean joinTables(Table tableOne, Table tableTwo, String attriOne, String attriTwo, QueryHandler handler) {

        if (checkJoinErrors(tableOne, tableTwo, attriOne, attriTwo, handler)) {
            return false;
        }
        List<String> headerList = createHeaderList(tableOne, tableTwo, attriOne, attriTwo);
        Table jointTable = new Table(null, headerList, "none");
        int headerIndexOne = tableOne.getHeaderIndex(attriOne);
        int headerIndexTwo = tableTwo.getHeaderIndex(attriTwo);

        Insert insert = new Insert();
        for (List<String> rowOne : tableOne.accessTable()) {
            for (List<String> rowTwo : tableTwo.accessTable()) {
                List<String> thisRow = new ArrayList<String>();
                if (rowTwo.get(headerIndexTwo).equals(rowOne.get(headerIndexOne))) {
                    thisRow = addNewValues(rowOne, thisRow, headerIndexOne);
                    thisRow = addNewValues(rowTwo, thisRow, headerIndexTwo);
                    insert.insertIntoTable(handler, jointTable, thisRow);
                }
            }
        }
        outputJointTable(jointTable, handler);
        return true;
    }

    public boolean checkJoinErrors(Table tableOne, Table tableTwo, String attriOne, String attriTwo, QueryHandler handler) {
        if (tableOne == null || tableTwo == null) {
            handler.setErrorLine("One or more requested tables do not exist.");
            return true;
        } else if (tableOne.getTableName().equals(tableTwo.getTableName())) {
            handler.setErrorLine("Cannot join the same table.");
            return true;
        } else if (!tableOne.hasRequestedHeader(attriOne) || !tableTwo.hasRequestedHeader(attriTwo)) {
            handler.setErrorLine("One or more attributes do not belong to the corresponding tables.");
            return true;
        }
        return false;
    }

    public List<String> createHeaderList(Table tableOne, Table tableTwo, String attriOne, String attriTwo) {
        List<String> tempList = new ArrayList<>();
        tempList.add("id");

        tempList = addEachTableHeader(tempList, tableOne, attriOne);
        tempList = addEachTableHeader(tempList, tableTwo, attriTwo);
        return tempList;
    }

    public List<String> addEachTableHeader(List<String> tempList, Table table, String attribute) {
        for (String header : table.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase(attribute)) {
                tempList.add(table.getTableName().replace(".tab", "") + "." + header);
            }
        }
        return tempList;
    }

    public List<String> addNewValues(List<String> chosenRow, List<String> newRow, int headerIndex) {
        for (String item : chosenRow) {
            if (chosenRow.indexOf(item) != 0 && chosenRow.indexOf(item) != headerIndex) {
                newRow.add(item);
            }
        }
        return newRow;
    }

    public void outputJointTable(Table jointTable, QueryHandler queryHandler) {
        Select select = new Select();
        List<String> printAll = new ArrayList<String>();
        printAll.add("*");
        List<String> conditions = new ArrayList<String>();
        select.selectRecords(jointTable, printAll, conditions, queryHandler);
    }


}
