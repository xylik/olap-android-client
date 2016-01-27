package com.fer.hr.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by igor on 19/01/16.
 */
public class QueryBuilder {
    private static enum Axis {COLLUMN, ROW, MEASURE, FILTER}

    private SaikuCube cube = new SaikuCube("", "", "", "", "", "");
    private List<SaikuMeasure> measureAxis = new ArrayList<>();
    private List<Level> collAxis = new ArrayList<>();
    private List<Level> rowAxis = new ArrayList<>();
    private List<SaikuMember> filterAxis = new ArrayList<>();
    private String mdx = "";

    public void clear() {
        cube = new SaikuCube("", "", "", "", "", "");
        measureAxis = new ArrayList<>();
        collAxis = new ArrayList<>();
        rowAxis = new ArrayList<>();
        filterAxis = new ArrayList<>();
        mdx = "";
    }

    public void setCube(SaikuCube cube) {
        this.cube = cube;
    }

    public void removeCube() {
        this.cube = null;
    }

    public boolean putOnColumns(Level level) {
        mdx = "";
        boolean isAdded = false;

        if (collAxis.contains(level)) isAdded = false;
        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.COLLUMN, level)) isAdded = false;
        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.COLLUMN, level))
            isAdded = false;
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
        mdx = "";
        return collAxis.remove(level);
    }

    public boolean putOnRows(Level level) {
        mdx = "";
        boolean isAdded = false;

        if (rowAxis.contains(level)) isAdded = false;
        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.ROW, level)) isAdded = false;
        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.ROW, level))
            isAdded = false;
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
        mdx = "";
        return rowAxis.remove(level);
    }

    public boolean putOnMeasures(SaikuMeasure measure) {
        mdx = "";
        if (measureAxis.contains(measure)) return false;
        else {
            measureAxis.add(measure);
            return true;
        }
    }

    public boolean removeFromMeasures(SaikuMember measure) {
        mdx = "";
        return measureAxis.remove(measure);
    }

    public boolean putOnFilters(SaikuMember filter) {
        mdx = "";
        boolean isAdded = false;

        if (filterAxis.contains(filter)) isAdded = false;
        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.FILTER, filter)) isAdded = false;
        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.FILTER, filter))
            isAdded = false;
        else {
            Stream.of(collAxis)
                    .filter(l -> l.getData().getUniqueName().equals(filter.getLevelUniqueName()))
                    .findFirst().ifPresent(l -> collAxis.remove(l));

            Stream.of(rowAxis)
                    .filter(l -> l.getData().getUniqueName().equals(filter.getLevelUniqueName()))
                    .findFirst().ifPresent(l -> rowAxis.remove(l));

            insertEntityInHierachy(Axis.FILTER, filter);
            isAdded = true;
        }

        return isAdded;
    }

    public boolean removeFromFilters(SaikuMember filter) {
        mdx = "";
        return filterAxis.remove(filter);
    }

    private boolean checkIfEntityDimensionIsPresentOnOtherAxis(Axis excludeAxis, Object excludeEntity) {
        boolean isOnOtherAxis = false;
        switch (excludeAxis) {
            case COLLUMN: {
                SaikuLevel excludedLevel = ((Level) excludeEntity).getData(); //click happened on collumns so excludeElement is for sure of Level type
                long rowMatchCnt = Stream.of(rowAxis)
                        .filter(l -> l != excludeEntity) //filter excludeElement from rowAxis -> true only when excludedElement is present on rowAxis and we want to move it columnAxis
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count(); //count how many rowAxis elements(levels) have equal dimension as excludedElement -> if any found that's forbbidden state
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName())) //filter all members of excludedElement from filter axis --> true only when members of excludedElement are present on filterAxis and we want to "move" them to columnAxis
                        .filter(fm -> fm.getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count(); //count how many filter members belong to dimension of excludedElement -> if any found that's forbbidden state
                if (rowMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case ROW: {
                SaikuLevel excludedLevel = ((Level) excludeEntity).getData();
                long collumnMatchCnt = Stream.of(collAxis)
                        .filter(l -> l != excludeEntity)
                        .filter(l -> l.getData().getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count();
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName()))
                        .filter(fm -> fm.getDimensionUniqueName().equals(excludedLevel.getDimensionUniqueName())).count();
                if (collumnMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case FILTER: {
                SaikuMember excludedFilter = (SaikuMember) excludeEntity;
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
                SaikuMember newFilter = (SaikuMember) newEntity;
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
                Level newLevel = (Level) entity;
                long rootHierarchyPosition;
                Level rootHierarchyLevel = Stream.of(sourceAxes)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(newLevel.getData().getHierarchyUniqueName()))
                        .findFirst().orElse(null);
                if (rootHierarchyLevel != null) {
                    int insertHierarchyPosition = sourceAxes.indexOf(rootHierarchyLevel);
                    while (insertHierarchyPosition < sourceAxes.size()
                            && sourceAxes.get(insertHierarchyPosition).getData().getHierarchyUniqueName().equals(newLevel.getData().getHierarchyUniqueName())
                            && newLevel.getHierarchyPosition() > sourceAxes.get(insertHierarchyPosition).getHierarchyPosition())
                        insertHierarchyPosition++;
                    sourceAxes.add(insertHierarchyPosition, newLevel);
                } else sourceAxes.add(newLevel);
                break;
            case FILTER:
                filterAxis.add((SaikuMember) entity);
                break;
            default:
                throw new IllegalArgumentException("Unexpected input!");
        }
    }

    public HashMap<Integer, SelectionGroup> getEntitySelection() {
        HashMap<Integer, SelectionGroup> result = new HashMap<>();

        List<SelectionEntity> measureEntites = Stream.of(measureAxis)
                .map(m -> new SelectionEntity(m.getUniqueName(), m.getCaption()))
                .collect(Collectors.<SelectionEntity>toList());

        List<SelectionEntity> collumnEntites = Stream.of(collAxis)
                .map(c -> new SelectionEntity(c.getData().getUniqueName(), c.getData().getCaption()))
                .collect(Collectors.<SelectionEntity>toList());

        List<SelectionEntity> rowEntites = Stream.of(rowAxis)
                .map(r -> new SelectionEntity(r.getData().getUniqueName(), r.getData().getCaption()))
                .collect(Collectors.<SelectionEntity>toList());

        List<SelectionEntity> filterEntites = Stream.of(filterAxis)
                .map(f -> new SelectionEntity(f.getLevelUniqueName(), f.getUniqueName(), f.getCaption()))
                .collect(Collectors.<SelectionEntity>toList());

        result.put(0, new SelectionGroup(App.getAppContext().getString(R.string.measures), measureEntites));
        result.put(1, new SelectionGroup(App.getAppContext().getString(R.string.collumns), collumnEntites));
        result.put(2, new SelectionGroup(App.getAppContext().getString(R.string.rows), rowEntites));
        result.put(3, new SelectionGroup(App.getAppContext().getString(R.string.filters), filterEntites));
        return result;
    }

    public void removeEntity(SelectionEntity entity) {
        //TODO passBy axis identificator

        mdx = "";
        String entityUniqueName = entity.getUniqueName();
        Stream.of(measureAxis)
                .filter(m -> m.getUniqueName().equals(entityUniqueName))
                .findFirst()
                .ifPresent(m -> {
                    measureAxis.remove(m);
                    //TODO fix state change should be handled here not in OlapNavigator.selectionListener
                    return;
                });

        Stream.of(collAxis)
                .filter(c -> c.getData().getUniqueName().equals(entityUniqueName))
                .findFirst()
                .ifPresent(c -> {
                    collAxis.remove(c);
                    c.setState(Level.State.NEUTRAL);
                    return;
                });

        Stream.of(rowAxis)
                .filter(r -> r.getData().getUniqueName().equals(entityUniqueName))
                .findFirst()
                .ifPresent(r -> {
                    rowAxis.remove(r);
                    r.setState(Level.State.NEUTRAL);
                    return;
                });

        Stream.of(filterAxis)
                .filter(f -> f.getUniqueName().equals(entityUniqueName))
                .findFirst()
                .ifPresent(f -> {
                    filterAxis.remove(f);
                    //TODO fix state change should be handled here not in OlapNavigator.selectionListener
                    return;
                });
    }

    public void updateMdx(String mdx) {
        this.mdx = mdx;
    }

    public String buildMdx() {
        if (!mdx.isEmpty()) return mdx;
        else {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT ")
                    .append(crossJoinOrHierarchize(collAxis));
            if (measureAxis.size() > 0) {
                sb.append(" * {");
                Stream.of(measureAxis).forEach(m -> sb.append("[Measures].[" + m.getName() + "],"));
                sb.replace(sb.length() - 1, sb.length(), "} ");
            }
            sb.append("ON COLUMNS, ")
                    .append(crossJoinOrHierarchize(rowAxis))
                    .append("ON ROWS ")
                    .append("FROM ")
                    .append("[" + cube.getName() + "] ");

            if (filterAxis.size() > 0)
                sb.append("WHERE (")
                        .append(groupAndCroosJoin(filterAxis))
                        .append(")");

            return sb.toString();
        }
    }

    private String crossJoinOrHierarchize(List<Level> axis) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < axis.size(); i++) {

            boolean isHierarchyStart = true;
            while (i + 1 < axis.size() && axis.get(i).getData().getHierarchyUniqueName().equals(axis.get(i + 1).getData().getHierarchyUniqueName())) {
                SaikuLevel hLevel = axis.get(i).getData();
                if (isHierarchyStart) {
                    sb.append("Hierarchize({");
                    isHierarchyStart = false;
                }
                sb.append(hLevel.getUniqueName().replaceFirst("\\]\\.\\[", ".") + ".Members,");
                //TODO remove replace it's not necessary
                i++;
            }
            String levelUniqueName = axis.get(i).getData().getUniqueName();
            String lNameFormated = levelUniqueName.replaceFirst("\\]\\.\\[", ".");
            String lMemers = lNameFormated + ".Members";
            String crossOperator = (i == axis.size() - 1) ? " " : " * ";

            if (!isHierarchyStart) sb.append(lMemers + "}) ");
            else sb.append(lMemers);

            sb.append(crossOperator);
        }
        return sb.toString();
    }

    private String groupAndCroosJoin(List<SaikuMember> members) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {

            boolean isFilterGroupStart = true;
            while (i + 1 < members.size() && members.get(i).getLevelUniqueName().equals(members.get(i + 1).getLevelUniqueName())) {
                SaikuMember fLevel = members.get(i);
                if (isFilterGroupStart) {
                    sb.append("{");
                    isFilterGroupStart = false;
                }
                sb.append(fLevel.getUniqueName() + ", ");
                i++;
            }
            String f = members.get(i).getUniqueName();
            String crossOperator = (i == members.size() - 1) ? " " : " * ";

            if (!isFilterGroupStart) sb.append(f + "} ");
            else sb.append(f);

            sb.append(crossOperator);
        }
        return sb.toString();
    }
}
