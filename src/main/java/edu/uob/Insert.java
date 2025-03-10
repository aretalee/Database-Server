package edu.uob;

import java.util.List;

public class Insert {


    public boolean insertIntoTable(DBServer server, Table chosenTable, List<String> valueParameters) {

        if (areThereInsertErrors(server, chosenTable, valueParameters)) {
            return false;
        }

        int rowID = chosenTable.getNextID();
        valueParameters.add(0, String.valueOf(rowID));
        chosenTable.addToTableList(valueParameters);
        chosenTable.setCurrentID(rowID);

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            server.setErrorLine("Could not insert values into table, please try again.");
            return false;
        }

        return true;
    }

    public boolean areThereInsertErrors(DBServer server, Table chosenTable, List<String> valueParameters) {
        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return true;
        } else if (chosenTable.accessColumnHeaders().size() == 1) {
            server.setErrorLine("No columns in table, please insert at least one using ALTER.");
            return true;
        } else if (valueParameters.size() != chosenTable.accessColumnHeaders().size() - 1) {
            server.setErrorLine("Must insert " + (chosenTable.accessColumnHeaders().size() - 1)
                    + " values but you have inputted " + valueParameters.size() + " .");
            return true;
        }

        return false;
    }

}
