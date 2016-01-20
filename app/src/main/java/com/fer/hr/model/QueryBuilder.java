package com.fer.hr.model;

import com.annimon.stream.Stream;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by igor on 19/01/16.
 */
public class QueryBuilder {
    private static enum Axis {COLLUMN, ROW, MEASURE, FILTER}

    private List<Level> rowAxis = new ArrayList<>();
    private List<Level> collAxis = new ArrayList<>();
    private List<SaikuMeasure> measureAxis = new ArrayList<>();
    private List<SaikuMember> filterAxis = new ArrayList<>();

    public boolean putOnColumns(Level level) {
        boolean isAdded = false;

        if(collAxis.contains(level)) isAdded = false;
        else if(checkIfEntityDimensionIsPresentOnOtherAxis(Axis.COLLUMN, level)) isAdded = false;
        else if(checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.COLLUMN, level)) isAdded = false;
        else {
            rowAxis.remove(level);
            Stream.of(filterAxis)
                    .filter(fm -> fm.getLevelUniqueName().equals(level.getData().getUniqueName()))
                    .findFirst().ifPresent(fm -> filterAxis.remove(fm));

            insertEntityInHierachy(Axis.COLLUMN, level);
            isAdded = true;
        }

        return isAdded;
    }

    public boolean removeFromColumns(Level level) {
        return collAxis.remove(level);
    }

    public boolean putOnRows(Level level) {
        boolean isAdded = false;

        if(rowAxis.contains(level)) isAdded = false;
        else if(checkIfEntityDimensionIsPresentOnOtherAxis(Axis.ROW, level)) isAdded = false;
        else if(checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.ROW, level)) isAdded = false;
        else {
            collAxis.remove(level);
            Stream.of(filterAxis)
                    .filter(fm -> fm.getLevelUniqueName().equals(level.getData().getUniqueName()))
                    .findFirst().ifPresent(fm -> filterAxis.remove(fm));

            insertEntityInHierachy(Axis.ROW, level);
            isAdded = true;
        }

        return isAdded;
    }

    public boolean removeFromRows(Level level) {
        return rowAxis.remove(level);
    }

    public boolean putOnMeasures(SaikuMeasure measure) {
        if(measureAxis.contains(measure)) return false;
        else {
            measureAxis.add(measure);
            return  true;
        }
    }

    public boolean removeFromMeasures(SaikuMember measure) {
        return measureAxis.remove(measure);
    }

    public boolean putOnFilters(SaikuMember filter) {
        boolean isAdded = false;

        if(filterAxis.contains(filter)) isAdded = false;
        else if(checkIfEntityDimensionIsPresentOnOtherAxis(Axis.FILTER, filter)) isAdded = false;
        else if(checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.FILTER, filter)) isAdded = false;
        else {
            Stream.of(collAxis)
                    .filter( l -> l.getData().getUniqueName().equals(filter.getLevelUniqueName()))
                    .findFirst().ifPresent( l -> collAxis.remove(l));

            Stream.of(rowAxis)
                    .filter(l -> l.getData().getUniqueName().equals(filter.getLevelUniqueName()))
                    .findFirst().ifPresent(l -> rowAxis.remove(l));

            insertEntityInHierachy(Axis.FILTER, filter);
            isAdded = true;
        }

        return isAdded;
    }

    public boolean removeFromFilters(SaikuMember filter) {
        return filterAxis.remove(filter);
    }

    private boolean checkIfEntityDimensionIsPresentOnOtherAxis(Axis excludeAxis, Object excludeEntity) {
        boolean isOnOtherAxis = false;
        switch (excludeAxis) {
            case COLLUMN: {
                SaikuLevel excludedLevel = ((Level)excludeEntity).getData(); //click happened on collumns so excludeElement is for sure of Level type
                long rowMatchCnt = Stream.of(rowAxis)
                        .filter(l -> l != excludeEntity) //filter excludeElement from rowAxis -> true only when excludedElement is present on rowAxis and we want to move it columnAxis
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count(); //count how many rowAxis elements(levels) have equal dimension as excludedElement -> if any found that's forbbidden state
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName()) ) //filter all members of excludedElement from filter axis --> true only when members of excludedElement are present on filterAxis and we want to "move" them to columnAxis
                        .filter(fm -> fm.getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count(); //count how many filter members belong to dimension of excludedElement -> if any found that's forbbidden state
                if (rowMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case ROW: {
                SaikuLevel excludedLevel = ((Level)excludeEntity).getData();
                long collumnMatchCnt = Stream.of(collAxis)
                        .filter(l -> l != excludeEntity)
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count();
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName()) )
                        .filter(fm -> fm.getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count();
                if (collumnMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case FILTER: {
                SaikuMember excludedFilter = (SaikuMember)excludeEntity;
                long collumnMatchCnt = Stream.of(collAxis)
                        .filter(l -> !l.getData().getUniqueName().equals(excludedFilter.getLevelUniqueName())) //filter all levels which excludedFilter belongs to
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedFilter.getDimensionUniqueName())).count(); //filter all levels which belong to same dimension as excludedFilter
                long rowMatchCnt = Stream.of(rowAxis)
                        .filter(l -> !l.getData().getUniqueName().equals(excludedFilter.getLevelUniqueName()))
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedFilter.getDimensionUniqueName())).count();
                if (collumnMatchCnt > 0 || rowMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected input!");
        }
        return isOnOtherAxis;
    }

    private boolean checkIfEntityHierachyConflictsWithAxisHierarchies(Axis searchAxis, Object newEntity) {
        long hierachiesMatcedCnt;
        switch (searchAxis) {
            case COLLUMN: {
                SaikuLevel newLevel = ((Level) newEntity).getData();
                hierachiesMatcedCnt = Stream.of(collAxis)
                        .filter(l -> l.getData().getDimensionUniqueName().equals(newLevel.getDimensionUniqueName()))
                        .filter(l -> !l.getData().getHierarchyUniqueName().equals(newLevel.getHierarchyUniqueName()))
                        .count();
                break;
            }
            case ROW: {
                SaikuLevel newLevel = ((Level) newEntity).getData();
                hierachiesMatcedCnt = Stream.of(rowAxis)
                        .filter(l -> l.getData().getDimensionUniqueName().equals(newLevel.getDimensionUniqueName()))
                        .filter(l -> !l.getData().getHierarchyUniqueName().equals(newLevel.getHierarchyUniqueName()))
                        .count();
                break;
            }
            case FILTER: {
                SaikuMember newFilter = (SaikuMember)newEntity;
                hierachiesMatcedCnt = Stream.of(filterAxis)
                        .filter(l -> l.getDimensionUniqueName().equals(newFilter.getDimensionUniqueName()))
                        .filter(l -> !l.getHierarchyUniqueName().equals(newFilter.getHierarchyUniqueName()))
                        .count();
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected input!");
        }
        if (hierachiesMatcedCnt > 0) return true;
        else return false;
    }

    private void insertEntityInHierachy(Axis destinationAxis, Object entity) {
        switch (destinationAxis) {
            case COLLUMN:
            case ROW:
                List<Level> sourceAxes = destinationAxis == Axis.COLLUMN ? collAxis : rowAxis;
                Level newLevel = (Level)entity;
                long rootHierarchyPosition;
                Level rootHierarchyLevel = Stream.of(sourceAxes)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(newLevel.getData().getHierarchyUniqueName()))
                        .findFirst().orElse(null);
                if(rootHierarchyLevel != null) {
                    int insertHierarchyPosition = sourceAxes.indexOf(rootHierarchyLevel);
                    while(insertHierarchyPosition < sourceAxes.size()
                            && sourceAxes.get(insertHierarchyPosition).getData().getHierarchyUniqueName().equals(newLevel.getData().getHierarchyUniqueName())
                            && newLevel.getHierarchyPosition() > sourceAxes.get(insertHierarchyPosition).getHierarchyPosition()) insertHierarchyPosition++;
                    sourceAxes.add(insertHierarchyPosition, newLevel);
                }
                else {
                    sourceAxes.add(newLevel);
                }
                break;
            case FILTER:
                filterAxis.add((SaikuMember)entity);
                break;
            default:
                throw new IllegalArgumentException("Unexpected input!");
        }
    }


    public String buildMdx() {
        return null;
    }
}
