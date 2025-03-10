package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Join {

    public boolean joinTables(Table tableOne, Table tableTwo, String attributeOne, String attributeTwo, DBServer server) {

        if (tableOne == null || tableTwo == null) {
            server.setErrorLine("One or more requested tables do not exist.");
            return false;
        }

        // ensure ordering of tables is same as that of attributes
        // also need to check if attribute 1 is in table 1 & attribute 2 is in table 2
        if (!tableOne.hasRequestedHeader(attributeOne)
                || !tableTwo.hasRequestedHeader(attributeTwo)) {
            server.setErrorLine("One or more attributes do not belong to the corresponding tables.");
            return false;
        }

        // create new TABLE object for temp storage (if like SQL only need to generate output and no need so save?)
        // make headers first (need to append OG table name)
        List<String> headerList = new ArrayList<>();
        headerList.add("id");
        for (String header : tableOne.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase(attributeOne)) {
                headerList.add(tableOne.getTableName().replace(".tab", "") + "." + header);
            }
        }
        for (String header : tableTwo.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase(attributeTwo)) {
                headerList.add(tableTwo.getTableName().replace(".tab", "") + "." + header);
            }
        }

        Table jointTable = new Table(null, headerList, "none");
        int headerIndexOne = tableOne.getHeaderIndex(attributeOne);
        int headerIndexTwo = tableTwo.getHeaderIndex(attributeTwo);

        // maybe loop through foreign key row in table 2
        // then check table one to see if there's a match
        // if found a match -> add table 1 content first then add table two content behind

        List<String> tableOneValues = getAttributeValues(tableOne, headerIndexOne);
        List<String> tableTwoValues = getAttributeValues(tableTwo, headerIndexTwo);
        Insert insert = new Insert();

        System.out.println(tableOneValues);
        System.out.println(tableTwoValues);
//        for (String tableOneValue : tableOneValues) {
            for (String tableValue : tableOneValues) {
                System.out.println(tableValue);
                List<String> thisRow = new ArrayList<String>();
                if (tableTwoValues.contains(tableValue)) {
                    thisRow = addNewValues(tableOne.getTableRow(tableOneValues.indexOf(tableValue)), thisRow, headerIndexOne);
                    thisRow = addNewValues(tableTwo.getTableRow(tableTwoValues.indexOf(tableValue)), thisRow, headerIndexTwo);
                }
                insert.insertIntoTable(server, jointTable, thisRow);
            }
//        }


        // call SELECT to print them out
        Select select = new Select();
        List<String> printAll = new ArrayList<String>();
        printAll.add("*");
        List<String> conditions = new ArrayList<String>();
        select.selectRecords(jointTable, printAll, conditions, server);

        return true;
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


}
