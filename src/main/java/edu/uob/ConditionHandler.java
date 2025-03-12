package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class ConditionHandler {

    public List<Integer> filterTable(Table chosenTable, List<String> conditions, QueryHandler queryHandler) {
        if (conditions.isEmpty()) {
            return null;
        }
        List<List<Integer>> resultList = new ArrayList<List<Integer>>();

        int index = 0;
        while (index < conditions.size()) {
            String currentToken = conditions.get(index);
            List<Integer> current = new ArrayList<Integer>();
            if (currentToken.equalsIgnoreCase("and") || currentToken.equalsIgnoreCase("or")) {
                current.add(currentToken.equalsIgnoreCase("and") ? 11 : 10);
                index += 1;
            } else {
                current = getValues(chosenTable,conditions, index, queryHandler);
                index += 3;
            }
            if (!current.isEmpty() && current.get(0) == -1) {
                queryHandler.setErrorLine("Requested column in conditions does not exist.");
                return current;
            } else { resultList.add(current); }
        }
        return combineResults(resultList);
    }

    public List<Integer> getValues(Table table, List<String> conditions, int index, QueryHandler handler) {
        String attribute = conditions.get(index).toLowerCase();
        String comparator = conditions.get(index + 1);
        String value = conditions.get(index + 2);
        return evaluateCondition(table, attribute, comparator, value, handler);
    }

    public List<Integer> combineResults(List<List<Integer>> allResults) {
        if (allResults.size() == 1) {
            return allResults.get(0);
        }
        int index = 0;
        while (allResults.size() > 1) {
            if (!allResults.get(index).isEmpty() && (allResults.get(index).get(0) == 11
                    || allResults.get(index).get(0) == 10)) {
                addTwoResults(allResults, index);
                index = 0;
            }
            index += 1;
        }
        allResults.get(0).sort(null);
        return allResults.get(0);
    }

    public void addTwoResults(List<List<Integer>> allResults, int index) {
        List<Integer> thisList;
        thisList = editResultLists(allResults.get(index - 1), allResults.get(index - 2), allResults.get(index).get(0));
        allResults.set(index, thisList);
        allResults.remove(index - 1);
        allResults.remove(index - 2);
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

    public List<Integer> evaluateCondition(Table table, String attri, String comp, String value, QueryHandler handler) {
        List<Integer> currentList = new ArrayList<Integer>();

        if (!table.hasRequestedHeader(attri)) {
            currentList.add(-1);
            return currentList;
        }
        int headerIndex = handler.findColumnIndex(table, attri);
        getResults(table, headerIndex, currentList, comp, value);
        return currentList;
    }

    public void getResults(Table chosenTable, int index, List<Integer> currentList, String comp, String value) {
        for (List<String> row : chosenTable.accessTable()) {
            if (compareValue(row, index, comp, value.replaceAll("'", ""))) {
                currentList.add(chosenTable.getTableIndex(row));
            }
        }
    }

    public boolean compareValue(List<String> currentRow, int index, String comparator, String value) {
        boolean valueMatches;

        if (comparator.equalsIgnoreCase("like")) {
            return currentRow.get(index).contains(value);
        } else if (comparator.equals("==")) {
            return compareEqual(currentRow, index, value);
        } else if (comparator.equals("!=")) {
            if (value.charAt(0) == '+') { value = value.replace("+", ""); }
            return !currentRow.get(index).equals(value);
        } else {
            valueMatches = compareInts(currentRow, index, comparator, value);
        }
        return valueMatches;
    }

    public boolean compareEqual(List<String> currentRow, int index, String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return currentRow.get(index).equalsIgnoreCase(value);
        } else if (value.charAt(0) == '+') { value = value.replace("+", ""); }
        return currentRow.get(index).equals(value);
    }

    public boolean compareInts(List<String> currentRow, int index, String comparator, String value) {
        boolean match = false;
        int compOne;
        int compTwo;
        try {
            compOne = Integer.parseInt(currentRow.get(index));
            compTwo = Integer.parseInt(value);

        } catch (NumberFormatException e) {
            return false;
        }
        if (comparator.equals(">")) { match = (compOne > compTwo); }
        if (comparator.equals("<")) { match = (compOne < compTwo); }
        if (comparator.equals(">=")) { match = (compOne >= compTwo); }
        if (comparator.equals("<=")) { match = (compOne <= compTwo); }

        return match;
    }

}
