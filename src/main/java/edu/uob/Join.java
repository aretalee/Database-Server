package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Join {

    public boolean joinTables(Table tableOne, Table tableTwo, String attributeOne, String attributeTwo, DBServer server) {

        if (areThereJoinErrors(tableOne, tableTwo, attributeOne, attributeTwo, server)) {
            return false;
        }

        List<String> headerList = createHeaderList(tableOne, tableTwo, attributeOne, attributeTwo);
        Table jointTable = new Table(null, headerList, "none");
        int headerIndexOne = tableOne.getHeaderIndex(attributeOne);
        int headerIndexTwo = tableTwo.getHeaderIndex(attributeTwo);

        Insert insert = new Insert();
        for (List<String> rowOne : tableOne.accessTable()) {
            for (List<String> rowTwo : tableTwo.accessTable()) {
                List<String> thisRow = new ArrayList<String>();
                if (rowTwo.get(headerIndexTwo).equals(rowOne.get(headerIndexOne))) {
                    thisRow = addNewValues(rowOne, thisRow, headerIndexOne);
                    thisRow = addNewValues(rowTwo, thisRow, headerIndexTwo);
                    insert.insertIntoTable(server, jointTable, thisRow);
                }
            }
        }
        outputJointTable(jointTable, server);

        return true;
    }

    public boolean areThereJoinErrors(Table tableOne, Table tableTwo, String attributeOne, String attributeTwo, DBServer server) {
        if (tableOne == null || tableTwo == null) {
            server.setErrorLine("One or more requested tables do not exist.");
            return true;
        } else if (tableOne.getTableName().equals(tableTwo.getTableName())) {
            server.setErrorLine("Cannot join the same table.");
            return true;
        } else if (!tableOne.hasRequestedHeader(attributeOne)
                || !tableTwo.hasRequestedHeader(attributeTwo)) {
            server.setErrorLine("One or more attributes do not belong to the corresponding tables.");
            return true;
        }
        return false;
    }

    public List<String> createHeaderList(Table tableOne, Table tableTwo, String attributeOne, String attributeTwo) {
        List<String> tempList = new ArrayList<>();
        tempList.add("id");
        for (String header : tableOne.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase(attributeOne)) {
                tempList.add(tableOne.getTableName().replace(".tab", "") + "." + header);
            }
        }
        for (String header : tableTwo.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase(attributeTwo)) {
                tempList.add(tableTwo.getTableName().replace(".tab", "") + "." + header);
            }
        }
        return tempList;
    }

    public List<String> getAttributeValues(Table table, int headerColumnIndex) {
        List<String> columnValues = new ArrayList<String>();

        for (List<String> row : table.accessTable()) {
            columnValues.add(row.get(headerColumnIndex));
        }

        return columnValues;
    }

    public List<String> addNewValues(List<String> chosenRow, List<String> newRow, int headerIndex) {

        for (String item : chosenRow) {
            if (chosenRow.indexOf(item) != 0 && chosenRow.indexOf(item) != headerIndex) {
                newRow.add(item);
            }
        }

        return newRow;
    }

    public void outputJointTable(Table jointTable, DBServer server) {
        Select select = new Select();
        List<String> printAll = new ArrayList<String>();
        printAll.add("*");
        List<String> conditions = new ArrayList<String>();
        select.selectRecords(jointTable, printAll, conditions, server);
    }


}
