package edu.uob;

import java.util.List;

public class Insert {


    public boolean insertIntoTable(QueryHandler handler, Table chosenTable, List<String> valueParameters) {

        if (areThereInsertErrors(handler, chosenTable, valueParameters)) {
            return false;
        }
        int rowID = chosenTable.getNextID();
        valueParameters.add(0, String.valueOf(rowID));
        chosenTable.addToTableList(valueParameters);
        chosenTable.setCurrentID(rowID);

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            handler.setErrorLine("Could not insert values into table, please try again.");
            return false;
        }
        return true;
    }

    public boolean areThereInsertErrors(QueryHandler handler, Table chosenTable, List<String> valueParameters) {
        if (chosenTable == null) {
            handler.setErrorLine("Requested table does not exist.");
            return true;
        } else if (chosenTable.accessColumnHeaders().size() == 1) {
            handler.setErrorLine("No columns in table, please insert at least one using ALTER.");
            return true;
        } else if (valueParameters.size() != chosenTable.accessColumnHeaders().size() - 1) {
            handler.setErrorLine("Please insert correct number of values.");
            return true;
        }
        return false;
    }

}
