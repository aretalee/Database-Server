package edu.uob;

public class Alter {

    public boolean alterTable(Table chosenTable, String valueType, String chosenHeader, QueryHandler queryHandler) {

        if(valueType.equalsIgnoreCase("add")) {
            if (!addColumnHeader(chosenTable, chosenHeader, queryHandler)) {
                return false;
            }
        } else if(valueType.equalsIgnoreCase("drop")) {
            if (!removeColumnHeader(chosenTable, chosenHeader, queryHandler)) {
                return false;
            }
        }
        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            queryHandler.setErrorLine("Could not alter table, please try again.");
            return false;
        }
        return true;
    }

    public boolean addColumnHeader(Table table, String header, QueryHandler queryHandler) {

        if (table == null) {
            queryHandler.setErrorLine("Requested table does not exist.");
            return false;
        } else if (table.hasRequestedHeader(header)) {
            queryHandler.setErrorLine("Column already exists.");
            return false;
        }
        table.addToColumnHeaders(header);
        table.addColumnToRows();
        return true;
    }

    public boolean removeColumnHeader(Table table, String header, QueryHandler queryHandler) {
        int chosenIndex = queryHandler.findColumnIndex(table, header);

        if (chosenIndex == -1) {
            queryHandler.setErrorLine("Column does not exist.");
            return false;
        } else if (header.equalsIgnoreCase("id")) {
            queryHandler.setErrorLine("Cannot remove id column from table.");
            return false;
        }
        table.removeFromColumnHeaders(chosenIndex);
        table.removeColumnFromRow(chosenIndex);
        return true;
    }
}
