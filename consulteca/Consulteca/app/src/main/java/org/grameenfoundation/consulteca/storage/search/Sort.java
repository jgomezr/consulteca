package org.grameenfoundation.consulteca.storage.search;

import java.io.Serializable;

/**
 * Used to specify field ordering in <code>Search</code>
 *
 * @see Search
 */
public class Sort implements Serializable {
    protected String column;
    protected boolean desc = false;
    protected boolean ignoreCase = false;


    public Sort() {
    }

    public Sort(String column, boolean desc, boolean ignoreCase) {
        this.column = column;
        this.desc = desc;
        this.ignoreCase = ignoreCase;
    }

    public Sort(String column, boolean desc) {
        this.column = column;
        this.desc = desc;
    }

    public Sort(String column) {
        this.column = column;
    }

    public static Sort asc(String column) {
        return new Sort(column);
    }

    public static Sort asc(String column, boolean ignoreCase) {
        return new Sort(column, ignoreCase);
    }

    public static Sort desc(String column) {
        return new Sort(column, true);
    }

    public static Sort desc(String column, boolean ignoreCase) {
        return new Sort(column, true, ignoreCase);
    }

    /**
     * column on which to sort
     */
    public String getColumn() {
        return column;
    }


    /**
     * column on which to sort
     */
    public void setColumn(String column) {
        this.column = column;
    }


    /**
     * If true, sort descending by the given column; otherwise, sort
     * <p/>
     * ascending.
     */
    public boolean isDesc() {
        return desc;
    }

    /**
     * If true, sort descending by the given column; otherwise, sort
     * <p/>
     * ascending.
     */
    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    /**
     * If true the ordering will be case insensitive for this column. Ignore
     * <p/>
     * case has no effect when customExpression is specified.
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }


    /**
     * If true the ordering will be case insensitive for this column. Ignore
     * <p/>
     * case has no effect when customExpression is specified.
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (desc ? 1231 : 1237);
        result = prime * result + (ignoreCase ? 1231 : 1237);
        result = prime * result
                + ((column == null) ? 0 : column.hashCode());

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

        Sort other = (Sort) obj;
        if (desc != other.desc)
            return false;
        if (ignoreCase != other.ignoreCase)
            return false;
        if (column == null) {
            if (other.column != null)
                return false;
        } else if (!column.equals(other.column))
            return false;
        return true;
    }


    @Override

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (column == null) {
            sb.append("null");
        } else {
            sb.append("`");
            sb.append(column);
            sb.append("`");
        }

        sb.append(desc ? " desc" : " asc");
        return sb.toString();
    }
}
