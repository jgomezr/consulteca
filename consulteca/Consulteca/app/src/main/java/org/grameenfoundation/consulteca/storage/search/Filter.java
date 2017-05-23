package org.grameenfoundation.consulteca.storage.search;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p/>
 * A <code>Filter</code> is used by the <code>Search</code> class to specify a
 * restriction on what results should be returned in the search. For example, if
 * a filter <code>Filter.equal("name","Paul")</code> were added to the search,
 * only objects with the property "name" equal to the string "Paul" would be
 * returned.
 * <p/>
 * Nested properties can also be specified, for example
 * <code>Filter.greaterThan("employee.age",65)</code>.
 */
public class Filter implements Serializable {
    protected String column;
    protected Object value;

    /**
     * The type of comparison to do between the property and the value. The
     * options are limited to the integer constants on this class:
     * <code>OP_EQAUL, OP_NOT_EQUAL, OP_LESS_THAN, OP_GREATER_THAN, LESS_OR_EQUAL, OP_GREATER_OR_EQUAL,
     * OP_IN, OP_NOT_IN, OP_LIKE, OP_ILIKE, OP_NULL, OP_NOT_NULL, OP_EMPTY, OP_NOT_EMPTY, OP_SOME, OP_ALL,
     * OP_NONE, OP_AND, OP_OR, OP_NOT</code>
     * .
     */
    protected int operator;

    public static final int OP_EQUAL = 0;
    public static final int OP_NOT_EQUAL = 1;
    public static final int OP_LESS_THAN = 2;
    public static final int OP_GREATER_THAN = 3;
    public static final int OP_LESS_OR_EQUAL = 4;
    public static final int OP_GREATER_OR_EQUAL = 5;
    public static final int OP_LIKE = 6;
    public static final int OP_ILIKE = 7;
    public static final int OP_IN = 8;
    public static final int OP_NOT_IN = 9;
    public static final int OP_NULL = 10;
    public static final int OP_NOT_NULL = 11;
    public static final int OP_EMPTY = 12;
    public static final int OP_NOT_EMPTY = 13;
    public static final int OP_AND = 100;
    public static final int OP_OR = 101;
    public static final int OP_NOT = 102;
    public static final int OP_SOME = 200;
    public static final int OP_ALL = 201;
    public static final int OP_NONE = 202;


    public Filter() {
    }

    public Filter(String column, Object value, int operator) {
        this.column = column;
        this.value = value;
        this.operator = operator;
    }


    public Filter(String column, Object value) {
        this.column = column;
        this.value = value;
        this.operator = OP_EQUAL;
    }

    /**
     * Create a new Filter using the == operator.
     */
    public static Filter equal(String property, Object value) {
        return new Filter(property, value, OP_EQUAL);
    }


    /**
     * Create a new Filter using the < operator.
     */
    public static Filter lessThan(String property, Object value) {
        return new Filter(property, value, OP_LESS_THAN);
    }


    /**
     * Create a new Filter using the > operator.
     */
    public static Filter greaterThan(String property, Object value) {
        return new Filter(property, value, OP_GREATER_THAN);
    }


    /**
     * Create a new Filter using the <= operator.
     */
    public static Filter lessOrEqual(String property, Object value) {
        return new Filter(property, value, OP_LESS_OR_EQUAL);
    }


    /**
     * Create a new Filter using the >= operator.
     */
    public static Filter greaterOrEqual(String property, Object value) {
        return new Filter(property, value, OP_GREATER_OR_EQUAL);
    }


    /**
     * Create a new Filter using the IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static Filter in(String property, Collection<?> value) {
        return new Filter(property, value, OP_IN);
    }


    /**
     * Create a new Filter using the IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static Filter in(String property, Object... value) {
        return new Filter(property, value, OP_IN);
    }


    /**
     * Create a new Filter using the NOT IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static Filter notIn(String property, Collection<?> value) {
        return new Filter(property, value, OP_NOT_IN);
    }


    /**
     * Create a new Filter using the NOT IN operator.
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static Filter notIn(String property, Object... value) {
        return new Filter(property, value, OP_NOT_IN);
    }


    /**
     * Create a new Filter using the LIKE operator.
     */
    public static Filter like(String property, String value) {
        return new Filter(property, value, OP_LIKE);
    }


    /**
     * Create a new Filter using the ILIKE operator.
     */
    public static Filter ilike(String property, String value) {
        return new Filter(property, value, OP_ILIKE);
    }


    /**
     * Create a new Filter using the != operator.
     */
    public static Filter notEqual(String property, Object value) {
        return new Filter(property, value, OP_NOT_EQUAL);
    }


    /**
     * Create a new Filter using the IS NULL operator.
     */
    public static Filter isNull(String property) {
        return new Filter(property, true, OP_NULL);
    }


    /**
     * Create a new Filter using the IS NOT NULL operator.
     */
    public static Filter isNotNull(String property) {
        return new Filter(property, true, OP_NOT_NULL);
    }


    /**
     * Create a new Filter using the IS EMPTY operator.
     */
    public static Filter isEmpty(String property) {
        return new Filter(property, true, OP_EMPTY);
    }


    /**
     * Create a new Filter using the IS NOT EMPTY operator.
     */
    public static Filter isNotEmpty(String property) {
        return new Filter(property, true, OP_NOT_EMPTY);
    }


    /**
     * Create a new Filter using the AND operator.
     * This takes a variable number of parameters. Any number of
     * <code>Filter</code>s can be specified.
     */
    public static Filter and(Filter... filters) {
        Filter filter = new Filter("AND", null, OP_AND);
        for (Filter f : filters) {
            filter.add(f);
        }
        return filter;
    }


    /**
     * Create a new Filter using the OR operator.
     * This takes a variable number of parameters. Any number of
     * <code>Filter</code>s can be specified.
     */
    public static Filter or(Filter... filters) {
        Filter filter = and(filters);
        filter.column = "OR";
        filter.operator = OP_OR;
        return filter;
    }


    /**
     * Create a new Filter using the NOT operator.
     */
    public static Filter not(Filter filter) {
        return new Filter("NOT", filter, OP_NOT);
    }


    /**
     * Create a new Filter using the SOME operator.
     */
    public static Filter some(String property, Filter filter) {
        return new Filter(property, filter, OP_SOME);
    }


    /**
     * Create a new Filter using the ALL operator.
     */
    public static Filter all(String property, Filter filter) {
        return new Filter(property, filter, OP_ALL);
    }


    /**
     * Create a new Filter using the NONE operator. This is equivalent to NOT
     * SOME.
     */
    public static Filter none(String property, Filter filter) {
        return new Filter(property, filter, OP_NONE);
    }

    /**
     * Used with OP_OR and OP_AND filters. These filters take a collection of
     * filters as their value. This method adds a filter to that list.
     */
    @SuppressWarnings("unchecked")

    public void add(Filter filter) {
        if (value == null || !(value instanceof List)) {
            value = new ArrayList();
        }

        ((List) value).add(filter);
    }


    /**
     * Used with OP_OR and OP_AND filters. These filters take a collection of
     * filters as their value. This method removes a filter from that list.
     */
    @SuppressWarnings("unchecked")
    public void remove(Filter filter) {
        if (value == null || !(value instanceof List)) {
            return;
        }

        ((List) value).remove(filter);
    }


    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;

    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }


    /**
     * Returns the value as a List, converting if necessary. If the value is a
     * List, it will be returned directly. If it is any other Collection type or
     * if it is an Array, an ArrayList will be returned with all the same
     * elements. If the value is any other non-null value, a List containing
     * just that one value will be returned. If it is <code>null</code>,
     * <code>null</code> will be returned.
     */
    public List<?> getValuesAsList() {
        if (value == null) {
            return null;
        } else if (value instanceof List<?>) {
            return (List<?>) value;
        } else if (value instanceof Collection<?>) {
            return new ArrayList<Object>((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            ArrayList<Object> list = new ArrayList<Object>(Array.getLength(value));
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(Array.get(value, i));
            }

            return list;
        } else {
            return Collections.singletonList(value);
        }
    }


    /**
     * Returns the value as a Collection, converting if necessary. If the value
     * is a Collection, it will be returned directly. If it is an Array, an
     * ArrayList will be returned with all the same elements. If the value is
     * any other non-null value, a Set containing just that one value will be
     * returned. If it is <code>null</code>, <code>null</code> will be returned.
     *
     * @return
     */
    public Collection<?> getValuesAsCollection() {
        if (value == null) {
            return null;
        } else if (value instanceof Collection<?>) {
            return (Collection<?>) value;
        } else if (value.getClass().isArray()) {
            ArrayList<Object> list = new ArrayList<Object>(Array.getLength(value));
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(Array.get(value, i));
            }

            return list;
        } else {
            return Collections.singleton(value);
        }
    }


    /**
     * @return true if the operator should have a single value specified.
     *         <code>EQUAL, NOT_EQUAL, LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREATER_OR_EQUAL, LIKE, ILIKE</code>
     */
    public boolean isTakesSingleValue() {
        return operator <= 7;
    }


    /**
     * @return true if the operator should have a list of values specified. <code>IN, NOT_IN</code>
     */
    public boolean isTakesListOfValues() {
        return operator == OP_IN || operator == OP_NOT_IN;
    }


    /**
     * @return true if the operator does not require a value to be specified.
     *         <code>NULL, NOT_NULL, EMPTY, NOT_EMPTY</code>
     */

    public boolean isTakesNoValue() {
        return (operator >= 10 && operator <= 13);
    }


    /**
     * @return true if the operator should have a single Filter specified for
     *         the value.
     *         <code>NOT, ALL, SOME, NONE</code>
     */

    public boolean isTakesSingleSubFilter() {
        return operator == OP_NOT || (operator >= 200 && operator < 300);
    }


    /**
     * @return true if the operator should have a list of Filters specified for
     *         the value.
     *         <code>AND, OR</code>
     */

    public boolean isTakesListOfSubFilters() {
        return operator == OP_AND || operator == OP_OR;
    }


    /**
     * @return true if the operator does not require a property to be specified.
     *         <code>AND, OR, NOT</code>
     */
    public boolean isTakesNoProperty() {
        return operator >= 100 && operator <= 102;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operator;
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());

        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Filter other = (Filter) obj;
        if (operator != other.operator)
            return false;

        if (column == null) {
            if (other.column != null)
                return false;
        } else if (!column.equals(other.column))
            return false;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;

        return true;
    }

    public static String paramDisplayString(Object val) {
        if (val == null) {
            return "null";
        } else if (val instanceof String) {
            return "\"" + val + "\"";
        } else if (val instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            sb.append(val.getClass().getSimpleName());
            sb.append(" {");

            boolean first = true;
            for (Object o : (Collection<?>) val) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(paramDisplayString(o));
            }

            sb.append("}");
            return sb.toString();
        } else if (val instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            sb.append(val.getClass().getComponentType().getSimpleName());
            sb.append("[] {");

            boolean first = true;
            for (Object o : (Object[]) val) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(paramDisplayString(o));
            }

            sb.append("}");
            return sb.toString();
        } else {
            return val.toString();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        switch (operator) {
            case Filter.OP_IN:
                return "`" + column + "` in (" + paramDisplayString(value) + ")";
            case Filter.OP_NOT_IN:
                return "`" + column + "` not in (" + paramDisplayString(value) + ")";
            case Filter.OP_EQUAL:
                return "`" + column + "` = " + paramDisplayString(value);
            case Filter.OP_NOT_EQUAL:
                return "`" + column + "` != " + paramDisplayString(value);
            case Filter.OP_GREATER_THAN:
                return "`" + column + "` > " + paramDisplayString(value);
            case Filter.OP_LESS_THAN:
                return "`" + column + "` < " + paramDisplayString(value);
            case Filter.OP_GREATER_OR_EQUAL:
                return "`" + column + "` >= " + paramDisplayString(value);
            case Filter.OP_LESS_OR_EQUAL:
                return "`" + column + "` <= " + paramDisplayString(value);
            case Filter.OP_LIKE:
                return "`" + column + "` LIKE " + paramDisplayString(value);
            case Filter.OP_ILIKE:
                return "`" + column + "` ILIKE " + paramDisplayString(value);
            case Filter.OP_NULL:
                return "`" + column + "` IS NULL";
            case Filter.OP_NOT_NULL:
                return "`" + column + "` IS NOT NULL";
            case Filter.OP_EMPTY:
                return "`" + column + "` IS EMPTY";
            case Filter.OP_NOT_EMPTY:
                return "`" + column + "` IS NOT EMPTY";
            case Filter.OP_AND:
            case Filter.OP_OR:
                if (!(value instanceof List)) {
                    return (operator == Filter.OP_AND ? "AND: " : "OR: ") + "**INVALID VALUE - NOT A LIST: (" + value
                            + ") **";
                }

                String op = operator == Filter.OP_AND ? " and " : " or ";
                StringBuilder sb = new StringBuilder("(");
                boolean first = true;

                for (Object o : ((List) value)) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(op);
                    }

                    if (o instanceof Filter) {
                        sb.append(o.toString());
                    } else {
                        sb.append("**INVALID VALUE - NOT A FILTER: (" + o + ") **");
                    }
                }

                if (first)
                    return (operator == Filter.OP_AND ? "AND: " : "OR: ") + "**EMPTY LIST**";

                sb.append(")");
                return sb.toString();
            case Filter.OP_NOT:
                if (!(value instanceof Filter)) {
                    return "NOT: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
                }

                return "not " + value.toString();
            case Filter.OP_SOME:
                if (!(value instanceof Filter)) {
                    return "SOME: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
                }

                return "some `" + column + "` {" + value.toString() + "}";
            case Filter.OP_ALL:
                if (!(value instanceof Filter)) {
                    return "ALL: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
                }

                return "all `" + column + "` {" + value.toString() + "}";
            case Filter.OP_NONE:
                if (!(value instanceof Filter)) {
                    return "NONE: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
                }

                return "none `" + column + "` {" + value.toString() + "}";
            default:
                return "**INVALID OPERATOR: (" + operator + ") - VALUE: " + paramDisplayString(value) + " **";
        }
    }


}
