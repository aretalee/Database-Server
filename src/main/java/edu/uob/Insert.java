package edu.uob;

import java.util.List;

public class Insert {


    public boolean insertIntoTable(DBServer server, Table chosenTable, List<String> valueParameters) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        } else if (chosenTable.accessColumnHeaders().size() == 1) {
            server.setErrorLine("No columns in table, please insert at least one using ALTER.");
            return false;
        } else if (valueParameters.size() != chosenTable.accessColumnHeaders().size() - 1) {
            server.setErrorLine("Must insert " + (chosenTable.accessColumnHeaders().size() - 1)
                    + " values but you have inputted " + valueParameters.size() + " .");
            return false;
        }

        int rowID = chosenTable.getCurrentID();
        valueParameters.add(0, String.valueOf(rowID));
        chosenTable.addToTableList(valueParameters);

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            server.setErrorLine("Could not insert values into table, please try again.");
            return false;
        }

        return true;
    }

}
