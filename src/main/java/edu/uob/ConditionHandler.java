package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class ConditionHandler {

    public List<Integer> filterTable(Table chosenTable, List<String> conditions, QueryHandler queryHandler) {

        if (conditions.isEmpty()) {
            return null;
        }

        List<List<Integer>> resultList = new ArrayList<List<Integer>>();
        // change AND to 11, OR to 10

        int index = 0;
        while (index < conditions.size()) {
            String currentToken = conditions.get(index);
            if (currentToken.equalsIgnoreCase("and") || currentToken.equalsIgnoreCase("or")) {
                List<Integer> thisOp = new ArrayList<Integer>();
                thisOp.add(currentToken.equalsIgnoreCase("and") ? 11 : 10);
                resultList.add(thisOp);
                index += 1;
            } else {
                String attribute = conditions.get(index).toLowerCase();
                String comparator = conditions.get(index + 1);
                String value = conditions.get(index + 2);
                List<Integer> validRows = evaluateCondition(chosenTable, attribute, comparator, value);
                if (!validRows.isEmpty() && validRows.get(0) == -1) {
                    queryHandler.setErrorLine("Requested column in conditions does not exist.");
                    return validRows;
                }
                resultList.add(validRows);
                index += 3;
            }
        }

        return combineResults(resultList);
    }

    public List<Integer> combineResults(List<List<Integer>> allResults) {

        if (allResults.size() == 1) {
            return allResults.get(0);
        }

        List<Integer> tempList;
        int index = 0;
        while (allResults.size() > 1) {
            if (!allResults.get(index).isEmpty() && (allResults.get(index).get(0) == 11 || allResults.get(index).get(0) == 10)) {
                tempList = editResultLists(allResults.get(index - 1), allResults.get(index - 2), allResults.get(index).get(0));
                allResults.set(index, tempList);
                allResults.remove(index - 1);
                allResults.remove(index - 2);
                index = 0;
            }
            index += 1;
        }
        allResults.get(0).sort(null);
        return allResults.get(0);
    }

    public List<Integer> editResultLists(List<Integer> listOne, List<Integer> listTwo, int operator) {
        if (operator == 11) {
            listTwo.removeIf(i -> !listOne.contains(i));
            listOne.removeIf(j -> !listTwo.contains(j));
        } else {
            listOne.removeAll(listTwo);
            listOne.addAll(listTwo);
        }
        return listOne;
    }

    public List<Integer> evaluateCondition(Table chosenTable, String attribute, String comparator, String value) {
        List<Integer> currentList = new ArrayList<Integer>();

        if (!chosenTable.hasRequestedHeader(attribute)) {
            currentList.add(-1);
            return currentList;
        }
        int headerIndex = ColumnIndexFinder.findColumnIndex(chosenTable, attribute);
        for (List<String> row : chosenTable.accessTable()) {
            if (compareValue(row, headerIndex, comparator, value.replaceAll("'", ""))) {
                currentList.add(chosenTable.getTableIndex(row));
            }
        }
        return currentList;
    }

    public boolean compareValue(List<String> currentRow, int index, String comparator, String value) {

        boolean valueMatches = false;
        int compOne;
        int compTwo;

        if (comparator.equalsIgnoreCase("like")) {
            return currentRow.get(index).contains(value);
        } else if (comparator.equals("==")) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return currentRow.get(index).equalsIgnoreCase(value);
            }
            return currentRow.get(index).equals(value);
        } else if (comparator.equals("!=")) {
            return !currentRow.get(index).equals(value);
        } else {
            try {
                compOne = Integer.parseInt(currentRow.get(index));
                compTwo = Integer.parseInt(value);

            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (comparator.equals(">")) { valueMatches = (compOne > compTwo); }
        if (comparator.equals("<")) { valueMatches = (compOne < compTwo); }
        if (comparator.equals(">=")) { valueMatches = (compOne >= compTwo); }
        if (comparator.equals("<=")) { valueMatches = (compOne <= compTwo); }

        return valueMatches;
    }

}
