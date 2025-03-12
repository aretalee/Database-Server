package edu.uob;

public class Alter {

    public boolean alterTable(Table chosenTable, String valueType, String chosenHeader, QueryHandler handler) {

        if(valueType.equalsIgnoreCase("add")) {
            if (!addColumnHeader(chosenTable, chosenHeader, handler)) {
                return false;
            }
        } else if(valueType.equalsIgnoreCase("drop")) {
            if (!removeColumnHeader(chosenTable, chosenHeader, handler)) {
                return false;
            }
        }
        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            handler.setErrorLine("Could not alter table, please try again.");
            return false;
        }
        return true;
    }

    public boolean addColumnHeader(Table table, String header, QueryHandler handler) {

        if (table == null) {
            handler.setErrorLine("Requested table does not exist.");
            return false;
        } else if (table.hasRequestedHeader(header)) {
            handler.setErrorLine("Column already exists.");
            return false;
        }
        table.addToColumnHeaders(header);
        table.addColumnToRows();
        return true;
    }

    public boolean removeColumnHeader(Table table, String header, QueryHandler handler) {
        int chosenIndex = handler.findColumnIndex(table, header);

        if (chosenIndex == -1) {
            handler.setErrorLine("Column does not exist.");
            return false;
        } else if (header.equalsIgnoreCase("id")) {
            handler.setErrorLine("Cannot remove id column from table.");
            return false;
        }
        table.removeFromColumnHeaders(chosenIndex);
        table.removeColumnFromRow(chosenIndex);
        return true;
    }
}
