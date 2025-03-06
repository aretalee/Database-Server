package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class ConditionHandler {

    public List<Integer> filterTable(Table chosenTable, List<List<String>> conditions) {

        if (isConditionListEmpty(conditions)) {
            return null;
        }

        List<List<Integer>> comparisonResults = new ArrayList<List<Integer>>();

            // comparisons organised in trios
        for (int index = 0; index < conditions.get(0).size() - 1; index++) {
            if (index % 3 == 0) {
                String attribute = conditions.get(0).get(index);
                String comparator = conditions.get(0).get(index + 1);
                String value = conditions.get(0).get(index + 2);
                comparisonResults.add(evaluateCondition(chosenTable, attribute, comparator, value));
            }
        }

        if (conditions.get(1).isEmpty()) {
            return comparisonResults.get(0);
        } else {
            return combineResults(comparisonResults, conditions.get(1));
        }
    }

    public boolean isConditionListEmpty(List<List<String>> conditions) {
        for (List<String> condition : conditions) {
            if (!condition.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> combineResults(List<List<Integer>> allResults, List<String> boolOperators) {
        List<Integer> tempList = allResults.get(0);
        int boolIndex = 0;

        for (int index = 1; index < allResults.size(); index++) {
            if (!tempList.isEmpty() && !allResults.get(index).isEmpty() && (boolIndex <= boolOperators.size())) {
                tempList = editLists(tempList, allResults.get(index), boolOperators.get(boolIndex));
                boolIndex++;
            }
        }

        tempList.sort(null);
        return tempList;
    }

    public List<Integer> editLists(List<Integer> listOne, List<Integer> listTwo, String operator) {
        if (operator.equalsIgnoreCase("and")) {
            listTwo.removeIf(i -> !listOne.contains(i));
            listOne.removeIf(j -> !listTwo.contains(j));
        } else {
            listOne.removeAll(listTwo);
            listOne.addAll(listTwo);
        }
        return listOne;
    }


    public List<Integer> evaluateCondition(Table chosenTable, String attribute, String comparator, String value) {
        List<Integer> result = new ArrayList<Integer>();
        int headerIndex = ColumnIndexFinder.findColumnIndex(chosenTable, attribute);

        for (List<String> row : chosenTable.accessTable()) {
            if (headerIndex != -1 && compareValue(row, headerIndex, comparator, value)) {
                result.add(chosenTable.accessTable().indexOf(row));
            }
        }
        return result;
    }

    public boolean compareValue(List<String> currentRow, int index, String comparator, String value) {

        boolean valueMatches = false;
        int compOne;
        int compTwo;

        if (comparator.equalsIgnoreCase("like")) {
            return currentRow.get(index).contains(value);
        } else if (comparator.equals("==")) {
            return currentRow.get(index).equalsIgnoreCase(value);
        } else if (comparator.equals("!=")) {
            return !currentRow.get(index).equalsIgnoreCase(value);
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





