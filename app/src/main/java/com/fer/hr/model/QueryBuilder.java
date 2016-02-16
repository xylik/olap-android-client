package com.fer.hr.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.activity.fragments.DrillFragment;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.repository.IRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by igor on 19/01/16.
 */
public class QueryBuilder implements Serializable {
    public static String ROW_H = "ROW_HEADER";
    public static String COL_H = "COLUMN_HEADER";
    public static String ROW_HH = "ROW_HEADER_HEADER";
    public static String VALUE = "DATA_CELL";

    private static QueryBuilder instance;

    private QueryBuilder() {
    }

    public static synchronized QueryBuilder instance() {
        if (instance == null) {
            instance = new QueryBuilder();
            repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);
        }
        return instance;
    }

    public QueryBuilder getCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(instance);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            return (QueryBuilder) new ObjectInputStream(bais).readObject();
        }catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void refreshRepository() {
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);
    }

    private static enum Axis {COLLUMN, ROW, MEASURE, FILTER}

    private SaikuCube cube = new SaikuCube("", "", "", "", "", "");
    private List<SaikuMeasure> measureAxis = new ArrayList<>();
    private List<Level> collAxis = new ArrayList<>();
    private List<Level> rowAxis = new ArrayList<>();
    private List<SaikuMember> filterAxis = new ArrayList<>();
    private String mdx = "";
    private static IRepository repository;

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
//        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.COLLUMN, level)) isAdded = false;
//        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.COLLUMN, level)) isAdded = false;
        else if (checkForConflictsWithHierarchyMembers(Axis.COLLUMN, level)) isAdded = false;
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
//        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.ROW, level)) isAdded = false;
//        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.ROW, level)) isAdded = false;
        else if (checkForConflictsWithHierarchyMembers(Axis.ROW, level)) isAdded = false;
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
//        else if (checkIfEntityDimensionIsPresentOnOtherAxis(Axis.FILTER, filter)) isAdded = false;
//        else if (checkIfEntityHierachyConflictsWithAxisHierarchies(Axis.FILTER, filter)) isAdded = false;
        else if (checkForConflictsWithHierarchyMembers(Axis.FILTER, filter)) isAdded = false;
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

    private boolean checkForConflictsWithHierarchyMembers(Axis excludeAxis, Object excludeEntity) {
        boolean isOnOtherAxis = false;
        switch (excludeAxis) {
            case COLLUMN: {
                SaikuLevel excludedLevel = ((Level) excludeEntity).getData(); //click happened on collumns so excludeElement is for sure of Level type
                long rowMatchCnt = Stream.of(rowAxis)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(excludedLevel.getHierarchyUniqueName()))
                        .filter(l -> l != excludeEntity).count(); //filter excludeElement from rowAxis -> true only when excludedElement is present on rowAxis and we want to move it columnAxis
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> fm.getHierarchyUniqueName().equals(excludedLevel.getHierarchyUniqueName()))
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName())).count(); //filter all members of excludedElement from filter axis --> true only when members of excludedElement are present on filterAxis and we want to "move" them to columnAxis
                if (rowMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case ROW: {
                SaikuLevel excludedLevel = ((Level) excludeEntity).getData();
                long collumnMatchCnt = Stream.of(collAxis)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(excludedLevel.getHierarchyUniqueName()))
                        .filter(l -> l != excludeEntity).count();
                long filterMatchCnt = Stream.of(filterAxis)
                        .filter(fm -> fm.getHierarchyUniqueName().equals(excludedLevel.getHierarchyUniqueName()))
                        .filter(fm -> !fm.getLevelUniqueName().equals(excludedLevel.getUniqueName())).count();
                if (collumnMatchCnt > 0 || filterMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            case FILTER: {
                SaikuMember excludedFilter = (SaikuMember) excludeEntity;
                long collumnMatchCnt = Stream.of(collAxis)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(excludedFilter.getHierarchyUniqueName()))
                        .filter(l -> !l.getData().getUniqueName().equals(excludedFilter.getLevelUniqueName())).count(); //filter all levels which excludedFilter belongs to
                long rowMatchCnt = Stream.of(rowAxis)
                        .filter(l -> l.getData().getHierarchyUniqueName().equals(excludedFilter.getHierarchyUniqueName()))
                        .filter(l -> !l.getData().getUniqueName().equals(excludedFilter.getLevelUniqueName())).count();
                if (collumnMatchCnt > 0 || rowMatchCnt > 0) isOnOtherAxis = true;
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected input!");
        }
        return isOnOtherAxis;
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


    public String drillDown(Cell c) {
        DrillFragment.AxisPosition position;
        if (c.getType().equals(COL_H)) position = DrillFragment.AxisPosition.COLS;
        else if (c.getType().equals(ROW_H)) position = DrillFragment.AxisPosition.ROWS;
        else return null;

        String levelUniqueName = c.getProperties().getProperty(Cell.LEVEL_PROP);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Non Empty ")
                .append(position == DrillFragment.AxisPosition.COLS ?
                        crossJoinOrHierarchizeWithDrill(collAxis, c) : crossJoinOrHierarchize(collAxis));
        if (measureAxis.size() > 0) {
            sb.append(" * {");
            Stream.of(measureAxis).forEach(m -> sb.append("[Measures].[" + m.getName() + "],"));
            sb.replace(sb.length() - 1, sb.length(), "} ");
        }
        sb.append("ON COLUMNS, Non Empty ")
                .append(position == DrillFragment.AxisPosition.ROWS ?
                        crossJoinOrHierarchizeWithDrill(rowAxis, c) : crossJoinOrHierarchize(rowAxis))
                .append("ON ROWS ")
                .append("FROM ")
                .append("[" + cube.getName() + "] ");
        if (filterAxis.size() > 0)
            sb.append("WHERE (")
                    .append(groupAndCrossJoin(filterAxis))
                    .append(")");

        return sb.toString();
    }

    public String buildMdx() {
        if (!mdx.isEmpty()) return mdx;
        else {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT Non Empty ")
                    .append(crossJoinOrHierarchize(collAxis));
            if (measureAxis.size() > 0) {
                sb.append(" * {");
                Stream.of(measureAxis).forEach(m -> sb.append("[Measures].[" + m.getName() + "],"));
                sb.replace(sb.length() - 1, sb.length(), "} ");
            }
            sb.append("ON COLUMNS, Non Empty ")
                    .append(crossJoinOrHierarchize(rowAxis))
                    .append("ON ROWS ")
                    .append("FROM ")
                    .append("[" + cube.getName() + "] ");

            if (filterAxis.size() > 0)
                sb.append("WHERE (")
                        .append(groupAndCrossJoin(filterAxis))
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
                sb.append(hLevel.getUniqueName() + ".Members,");
                i++;
            }
            String levelUniqueName = axis.get(i).getData().getUniqueName();
            String lMemers = levelUniqueName + ".Members";
            String crossOperator = (i == axis.size() - 1) ? " " : " * ";

            if (!isHierarchyStart) sb.append(lMemers + "}) ");
            else sb.append(lMemers);

            sb.append(crossOperator);
        }
        return sb.toString();
    }

    private String groupAndCrossJoin(List<SaikuMember> members) {
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


    private String crossJoinOrHierarchizeWithDrill(List<Level> axis, Cell c) {
        StringBuilder sb = new StringBuilder();
        StringBuilder hsb = null;

        for (int i = 0; i < axis.size(); i++) {

            boolean isHierarchyStart = true;
            while (i + 1 < axis.size() && axis.get(i).getData().getHierarchyUniqueName().equals(axis.get(i + 1).getData().getHierarchyUniqueName())) {
                SaikuLevel hLevel = axis.get(i).getData();
                if (isHierarchyStart) {
                    hsb = new StringBuilder();
                    hsb.append("Hierarchize({");
                    isHierarchyStart = false;
                }
                hsb.append(hLevel.getUniqueName() + ".Members,");
                i++;
            }
            String crossOperator = (i == axis.size() - 1) ? " " : " * ";
            SaikuLevel l = axis.get(i).getData();
            String lUniqueName = l.getUniqueName();
            String lMembers = lUniqueName + ".Members";
            String cHierarchy = c.getProperties().getProperty(Cell.HIERARCHY_PROP);

            //"uniquename": "[Customer].[Customers].[Canada].[BC]"
            if (!isHierarchyStart) {
                hsb.append(lMembers + "})"); //close hierarchy with last member

                if (cHierarchy.equals(l.getHierarchyUniqueName())) { //if drill cell belongs to currently processed level hierarchy
                    sb.append("DrillDownMember(")
                            .append(hsb.toString())
                            .append(", {")
                            .append(getDrillKey(l, c))
                            .append("}, RECURSIVE)");
                } else sb.append(hsb.toString()); //add hierarchized levels to query
            } else if (cHierarchy.equals(l.getHierarchyUniqueName())) {
                sb.append("DrillDownMember(")
                        .append(lMembers)
                        .append(", {")
                        .append(getDrillKey(l, c))
                        .append("}, RECURSIVE)");
            } else {   //drill cell doesn't belong to currently processed level hierarhcy
                sb.append(lMembers);
            }

            sb.append(crossOperator);
        }
        return sb.toString();
    }

    /*
        Hierarchize({
            Customer.Customers.Country.Members,
            Customer.Customers.[State Province].Members
        }),
        {
            Customer.Customers.[State Province].&WA&USA,		--with WA Children included
            Customer.Customers.City.&Yakima&WA&USA				--with Yakima Children included
        }
     */
    private String getDrillKey(SaikuLevel currentLevel, Cell c) {
        String cHierarchy = c.getProperties().getProperty(Cell.HIERARCHY_PROP);
        String cLevel = c.getProperties().getProperty(Cell.LEVEL_PROP);
        String cUniqueName = c.getProperties().getProperty(Cell.UNIQUE_NAME_PROP);

        String[] cellKeys = cUniqueName.substring(cHierarchy.length() + ".[".length(), cUniqueName.length()).split("[\\]\\[\\.]+");
        String cellKey = "";
        StringBuilder sb = new StringBuilder();
        for (int k = cellKeys.length - 1; k >= 0; k--) cellKey += "&[" + cellKeys[k] + "]";

        List<SaikuLevel> hLelvels = repository.getLevelsOfHierarchy(cube, cHierarchy);
        boolean shouldSkipLevel = true;
        for (int i = 0, end = hLelvels.size(); i < end && i <= cellKeys.length; i++) { //ignore all levels on path up to currentLevel in level hierarchy
            SaikuLevel l = hLelvels.get(i);
            if (shouldSkipLevel && !l.equals(currentLevel)) continue;
            shouldSkipLevel = false;
            sb.append(cHierarchy)
                    .append(".[" + l.getName() + "].")
                    .append(getLevelKey(i, cellKeys) + ", ");
        }
        sb.delete(sb.lastIndexOf(", "), sb.length());

        return sb.toString();
    }

    /*
    0       1       2       3       4
    All     Country State   City    Name
            USA     WA      Yakima  [Charles Wilson]
     */
    private String getLevelKey(int levelDepth, String[] cellKeys) {
        StringBuilder sb = new StringBuilder();
        for (int i = levelDepth - 1; i >= 0; i--) { //i=1 skip [All] level
            sb.append("&[" + cellKeys[i] + "]");
        }
        return sb.toString();
    }

    public void updateMdx(String mdx) {
        this.mdx = mdx;
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
}
