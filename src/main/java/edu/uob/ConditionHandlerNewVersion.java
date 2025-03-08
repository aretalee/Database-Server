package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class ConditionHandlerNewVersion {

    public List<Integer> filterTable(Table chosenTable, List<List<String>> conditions, DBServer server) {

        if (isConditionListEmpty(conditions)) {
            return null;
        }

        List<List<List<Integer>>> comparisonResults = new ArrayList<List<List<Integer>>>();

        for (List<String> condition : conditions) {
            System.out.println(condition);
        }

        // comparisons organised in trios
        for (int condIndex = 0; condIndex < conditions.size() - 1; condIndex++) {
            List<List<Integer>> tempList = new ArrayList<List<Integer>>();
            for (int itemIndex = 0; itemIndex < conditions.get(condIndex).size() - 1; itemIndex++) {
                if (itemIndex % 3 == 0) {
                    String attribute = conditions.get(condIndex).get(itemIndex);
                    String comparator = conditions.get(condIndex).get(itemIndex + 1);
                    String value = conditions.get(condIndex).get(itemIndex + 2);
                    tempList.add(evaluateCondition(chosenTable, attribute, comparator, value));
                }
            }
            comparisonResults.add(tempList);
        }

        for (List<List<Integer>> condition : comparisonResults) {
            System.out.println(condition + "\n");
        }

        System.out.println(conditions.get(conditions.size() - 1));
        if (conditions.get(conditions.size() - 1).isEmpty()) {
            return comparisonResults.get(0).get(0);
        } else {
            return combineAllResults(comparisonResults, conditions.get(conditions.size() - 1));
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

    public List<Integer> combineAllResults(List<List<List<Integer>>> allResults, List<String> boolOperators) {
        List<List<Integer>> combinedLists = allResults.get(0);
        int boolIndex = 0;

        for (List<List<Integer>> result : allResults) {
            combinedLists.add(combineResultLayer(result, boolOperators));
        }

        return combineResultLayer(combinedLists, boolOperators);
    }

    public List<Integer> combineResultLayer(List<List<Integer>> result, List<String> boolOperators) {
        List<Integer> tempList = result.get(0);
        int boolIndex = 0;

        for (int index = 1; index < result.size(); index++) {
            if (!tempList.isEmpty() && !result.get(index).isEmpty() && (boolIndex < boolOperators.size())) {
                tempList = editLists(tempList, result.get(index), boolOperators.get(boolIndex));
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
        List<Integer> currentList = new ArrayList<Integer>();

        if (!chosenTable.accessColumnHeaders().contains(attribute)) {
//            server.setErrorLine("Requested column does not exist.");
            currentList.add(-1);
            return currentList;
        }
        int headerIndex = ColumnIndexFinder.findColumnIndex(chosenTable, attribute);
        for (List<String> row : chosenTable.accessTable()) {
            if (compareValue(row, headerIndex, comparator, value)) {
                currentList.add(chosenTable.accessTable().indexOf(row));
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
