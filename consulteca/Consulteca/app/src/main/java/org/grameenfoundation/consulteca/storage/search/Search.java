package org.grameenfoundation.consulteca.storage.search;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A query definition object that is passed to a search processor to define the parameter for a
 * search. There are types of parameters that can be set.
 * <ul>
 * <li>tableName - The table to search from.</li>
 * <li>Filters = Any number of filters may be specified for the search. Filters
 * specify a column and a condition that it must match for a record to be
 * included in the result. Filters are ANDed together by default, but a disjunction can be used
 * and can be set by <code>disjunction = true</code></li>
 * <li>Sorts - Any number of sorts may be specified. Each sort consists of a column, a flag for
 * ascending or descending and a flag for whether or not to ignore the case</li>
 * <li>Fields - The fields that are returned for each row in the result set.
 * .<br/><br/>
 * Additionally, fields can be specified using column operators:
 * <code>COUNT, COUNT DISTINCT, SUM, AVG, MAX, MIN</code>. Note that fields with
 * column operators can not be mixed with fields that do not use column
 * operators.<br/><br/>
 * Set <code>distinct</code> to <code>true</code> in order to filter out
 * duplicate results.<br/><br/>
 * </li>
 * <li>Paging - The maximum number of results may be specified with
 * <code>maxResults</code>. (This can also be thought of as results per page.)
 * The first result can be specified using either <code>firstResult</code>
 * </li>
 * </ul>
 *
 * @author Charles Tumwebaze
 */
public class Search implements Serializable {
    protected int firstResult = -1; // -1 stands for unspecified
    protected int maxResults = -1; // -1 stands for unspecified
    protected String tableName;
    protected List<Filter> filters = new ArrayList<Filter>();
    protected List<Sort> sorts = new ArrayList<Sort>();
    protected List<Field> fields = new ArrayList<Field>();
    protected boolean distinct;
    protected boolean disjunction;

    /**
     * default constructor
     */
    public Search() {
    }


    public String getTableName() {
        return this.tableName;
    }

    public Search setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public void addFilter(Filter filter) {
        if (filters == null) {
            filters = new ArrayList<Filter>();
        }

        filters.add(filter);
    }

    public Search addFilters(Filter... filters) {
        for (Filter filter : filters) {
            addFilter(filter);
        }

        return this;
    }


    /**
     * Add a filter that uses the == operator.
     */

    public Search addFilterEqual(String column, Object value) {
        addFilter(new Filter(column, value));
        return this;

    }


    /**
     * Add a filter that uses the >= operator.
     */
    public Search addFilterGreaterOrEqual(String column, Object value) {
        addFilter(new Filter(column, value, Filter.OP_GREATER_OR_EQUAL));
        return this;
    }


    /**
     * Add a filter that uses the > operator.
     */
    public Search addFilterGreaterThan(String column, Object value) {
        addFilter(new Filter(column, value, Filter.OP_GREATER_THAN));
        return this;
    }


    /**
     * Add a filter that uses the IN operator.
     */
    public Search addFilterIn(String column, Collection<?> value) {
        addFilter(new Filter(column, value, Filter.OP_IN));
        return this;
    }


    /**
     * Add a filter that uses the IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * <p/>
     * specified.
     */
    public Search addFilterIn(String column, Object... value) {
        addFilter(new Filter(column, value, Filter.OP_IN));
        return this;
    }


    /**
     * Add a filter that uses the NOT IN operator.
     */
    public Search addFilterNotIn(String column, Collection<?> value) {
        addFilter(new Filter(column, value, Filter.OP_NOT_IN));
        return this;
    }


    /**
     * Add a filter that uses the NOT IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * <p/>
     * specified.
     */
    public Search addFilterNotIn(String column, Object... value) {
        addFilter(new Filter(column, value, Filter.OP_NOT_IN));
        return this;
    }


    /**
     * Add a filter that uses the <= operator.
     */
    public Search addFilterLessOrEqual(String column, Object value) {
        addFilter(new Filter(column, value, Filter.OP_LESS_OR_EQUAL));
        return this;
    }

    /**
     * Add a filter that uses the < operator.
     */
    public Search addFilterLessThan(String column, Object value) {
        addFilter(new Filter(column, value, Filter.OP_LESS_THAN));
        return this;
    }

    /**
     * Add a filter that uses the LIKE operator.
     */
    public Search addFilterLike(String column, String value) {
        addFilter(new Filter(column, value, Filter.OP_LIKE));
        return this;
    }


    /**
     * Add a filter that uses the ILIKE operator.
     */
    public Search addFilterILike(String column, String value) {
        addFilter(new Filter(column, value, Filter.OP_ILIKE));
        return this;
    }


    /**
     * Add a filter that uses the != operator.
     */
    public Search addFilterNotEqual(String column, Object value) {
        addFilter(new Filter(column, value, Filter.OP_NOT_EQUAL));
        return this;
    }


    /**
     * Add a filter that uses the IS NULL operator.
     */
    public Search addFilterNull(String column) {
        addFilter(new Filter(column, null, Filter.OP_NULL));
        return this;
    }


    /**
     * Add a filter that uses the IS NOT NULL operator.
     */
    public Search addFilterNotNull(String column) {
        addFilter(new Filter(column, null, Filter.OP_NOT_NULL));
        return this;
    }


    /**
     * Add a filter that uses the IS EMPTY operator.
     */
    public Search addFilterEmpty(String column) {
        addFilter(new Filter(column, "", Filter.OP_EMPTY));
        return this;
    }


    /**
     * Add a filter that uses the IS NOT EMPTY operator.
     */
    public Search addFilterNotEmpty(String column) {
        addFilter(new Filter(column, null, Filter.OP_NOT_EMPTY));
        return this;
    }


    /**
     * Add a filter that uses the AND operator.
     * This takes a variable number of parameters. Any number of <code>Filter
     * </code>s can be specified.
     */
    public Search addFilterAnd(Filter... filters) {
        addFilter(Filter.and(filters));
        return this;
    }

    /**
     * Add a filter that uses the OR operator.
     * This takes a variable number of parameters. Any number of <code>Filter</code>s can be specified.
     */
    public Search addFilterOr(Filter... filters) {
        addFilter(Filter.or(filters));
        return this;
    }

    /**
     * Add a filter that uses the NOT operator.
     */
    public Search addFilterNot(Filter filter) {
        filter.setOperator(Filter.OP_NOT);
        addFilter(filter);
        return this;
    }

    public void removeFilter(Filter filter) {
        this.filters.remove(filter);
    }

    public void clearFilters() {
        this.filters.clear();
    }

    public boolean isDisjunction() {
        return disjunction;
    }

    /**
     * Filters added to a search are "ANDed" together if this is false (default)
     * and "ORed" if it is set to true.
     */
    public Search setDisjunction(boolean disjunction) {
        this.disjunction = disjunction;
        return this;
    }

    // Sorts
    public Search addSort(Sort sort) {
        this.sorts.add(sort);
        return this;
    }

    public Search addSorts(Sort... sorts) {
        for (Sort sort : sorts) {
            this.sorts.add(sort);
        }

        return this;
    }

    /**
     * Add ascending sort by column
     */
    public Search addSortAsc(String column) {
        this.sorts.add(new Sort(column, false));
        return this;
    }

    /**
     * Add ascending sort by column
     */
    public Search addSortAsc(String column, boolean ignoreCase) {
        this.sorts.add(Sort.asc(column, ignoreCase));
        return this;
    }


    /**
     * Add descending sort by column
     */
    public Search addSortDesc(String column) {
        this.sorts.add(Sort.desc(column));
        return this;
    }

    /**
     * Add descending sort by column
     */
    public Search addSortDesc(String column, boolean ignoreCase) {
        this.sorts.add(Sort.desc(column, ignoreCase));
        return this;
    }


    /**
     * Add sort by column. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public Search addSort(String column, boolean desc) {
        if (desc)
            this.sorts.add(Sort.desc(column));
        else
            this.sorts.add(Sort.asc(column));

        return this;
    }

    /**
     * Add sort by column. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public Search addSort(String column, boolean desc, boolean ignoreCase) {
        this.sorts.add(new Sort(column, desc, ignoreCase));
        return this;
    }

    public void removeSort(Sort sort) {
        this.sorts.remove(sort);
    }

    public void clearSorts() {
        this.sorts.clear();
    }

    // Fields
    public Search addField(Field field) {
        this.fields.add(field);
        return this;
    }

    public Search addFields(Field... fields) {
        for (Field field : fields) {
            addField(field);
        }

        return this;
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <code>column</code> will also be used as the key for this value in the
     * map.
     */
    public Search addField(String column) {
        addField(new Field(column));
        return this;
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <p/>
     * <code>column</code> will also be used as the key for this value in the
     * <p/>
     * map.
     */
    public Search addField(String column, int operator) {
        addField(new Field(column, operator));
        return this;
    }

    public void removeField(Field field) {
        this.fields.remove(field);
    }

    public void clearFields() {
        this.fields.clear();
    }

    public boolean isDistinct() {
        return distinct;
    }

    public Search setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    // Paging
    public int getFirstResult() {
        return firstResult;
    }

    public Search setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public Search setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Search setFilters(List<Filter> filters) {
        this.filters = filters;
        return this;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public Search setSorts(List<Sort> sorts) {
        this.sorts = sorts;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Search setFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }
}
