package com.fer.hr.utils;

import com.fer.hr.model.GraphData;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.rest.dto.queryResult.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igor on 14/02/16.
 */
public class QueryResultConverter {
    public static GraphData convertToGraphData(QueryResult result) {
        List<List<String>> xLabels = new ArrayList<>();
        List<List<String>> yLabels = getYLabels(result);
        List<List<Number>> yValues = new ArrayList<>();

        int indexRow_lastColHeader = indexOfRow_lastColHeader(result);
        int indexRow_firstRowHeader = indexOfRow_firstRowHeader(result);

        for(int row=indexRow_firstRowHeader, rowCnt=result.getHeight(); row<rowCnt; row++) {
            Cell[] currentRow = result.getCellset().get(row);
            List<String> currRowXLabels = new ArrayList<>();
            List<Number> currRowYValues = new ArrayList<>();

            for(int col=0, colCnt=result.getWidth(); col<colCnt; col++) {
                Cell c = currentRow[col];

                if(row >= indexRow_firstRowHeader && col <= indexRow_lastColHeader) currRowXLabels.add(c.getValue());

                if(row >= indexRow_firstRowHeader && col > indexRow_lastColHeader) {
                    double cellVal = 0;
//                    if(!c.getValue().equals("-")) {
//                        cellVal = Double.valueOf(c.getValue().replace(",", "").trim());
//                        if(cellVal < 1000) cellVal /= 1000d;
//                        else cellVal = Double.valueOf(c.getValue().replace(",", ".").trim());
//                    }
                    if(!c.getValue().equals("-")) {
                        cellVal = Double.valueOf(c.getValue().replace(",", "").trim());
                    }
                    currRowYValues.add(cellVal);
                }
            }

            xLabels.add(currRowXLabels);
            yValues.add(currRowYValues);
        }

        return new GraphData(xLabels, yLabels, yValues);
    }

    private static List<List<String>> getYLabels(QueryResult result) {
        List<List<String>> yLabels = new ArrayList<>();
        int indexCol_firstColHeader = indexOfCol_firstColHeader(result);
        int indexCol_lastRowHeader = indexOfCol_lastRowHeader(result);

        for(int row=0; row<=indexCol_lastRowHeader; row++) {
            Cell[] currentRow = result.getCellset().get(row);

            for(int col=indexCol_firstColHeader, colPos=0; col<result.getWidth(); col++, colPos++) {
                if(row == 0) yLabels.add(new ArrayList<>());
                Cell c = currentRow[col];

                yLabels.get(colPos).add(row, c.getValue().trim());
            }
        }
        return yLabels;
    }

    private static int indexOfRow_firstRowHeader(QueryResult result) {
        int index = -1;

        for(Cell[] row: result.getCellset()) {
            index++;
            for(Cell c: row) {
                if(c.getType().equals("ROW_HEADER")) return index;
                break;
            }
        }

        return -1;
    }

    private static int indexOfRow_lastColHeader(QueryResult result) {
        int rowHeaderHeaderIndex = indexOfRow_firstRowHeader(result) - 1;
        int index = -1;

        Cell[] row = result.getCellset().get(rowHeaderHeaderIndex);
        for(Cell c: row) {
            if(c.getType().equals("ROW_HEADER_HEADER"))index++;
            else break;
        }

        return index;
    }

    private static int indexOfCol_firstColHeader(QueryResult result) {
        return indexOfRow_lastColHeader(result) + 1;
    }

    private static int indexOfCol_lastRowHeader(QueryResult result) {
        int indexOfColFirstColHeader = indexOfCol_firstColHeader(result);

        int index = -1;
        for(Cell[] row: result.getCellset()) {
            if(!row[indexOfColFirstColHeader].getType().equals("COLUMN_HEADER")) return index;
            index++;
        }

        return -1;
    }


}


