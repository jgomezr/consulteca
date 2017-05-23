package org.grameenfoundation.consulteca.storage.search;

import java.io.Serializable;

/**
 * Used to specify field selection in the search
 *
 * @author Charles Tumwebaze
 */
public class Field implements Serializable {
    protected String column;
    protected int operator = 0;
    /**
     * Possible value for <code>operator</code>. This is the default value
     * and does not apply any operator to the column. All the rows in the result
     * set are returned.
     */
    public static final int OP_column = 0;

    /**
     * Possible value for <code>operator</code>. This returns the number of
     * rows in the result set where the given column is non-null.
     */
    public static final int OP_COUNT = 1;


    /**
     * Possible value for <code>operator</code>. This returns the number of
     * distinct values of the given column in the result set.
     */
    public static final int OP_COUNT_DISTINCT = 2;


    /**
     * Possible value for <code>operator</code>. This returns the maximum
     * value of the given column in the result set.
     */
    public static final int OP_MAX = 3;


    /**
     * Possible value for <code>operator</code>. This returns the minimum
     * value of the given column in the result set.
     */
    public static final int OP_MIN = 4;

    /**
     * Possible value for <code>operator</code>. This returns the sum of the
     * given column in all rows of the result set.
     */
    public static final int OP_SUM = 5;

    /**
     * Possible value for <code>operator</code>. This returns the average
     * value of the given column in the result set.
     */
    public static final int OP_AVG = 6;

    public Field() {

    }


    public Field(String column) {
        this.column = column;
    }

    public Field(String column, int operator) {
        this.column = column;
        this.operator = operator;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operator;
        result = prime * result + ((column == null) ? 0 : column.hashCode());
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
        Field other = (Field) obj;

        if (operator != other.operator)
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
        boolean parens = true;
        switch (operator) {
            case OP_AVG:
                sb.append("AVG(");
                break;
            case OP_COUNT:
                sb.append("COUNT(");
                break;
            case OP_COUNT_DISTINCT:
                sb.append("COUNT_DISTINCT(");
                break;
            case OP_MAX:
                sb.append("MAX(");
                break;
            case OP_MIN:
                sb.append("MIN(");
                break;

            case OP_column:
                parens = false;
                break;
            case OP_SUM:
                sb.append("SUM(");
                break;
            default:
                sb.append("**INVALID OPERATOR: (" + operator + ")** ");
                parens = false;
                break;
        }


        if (column == null) {
            sb.append("null");
        } else {
            sb.append("`");
            sb.append(column);
            sb.append("`");
        }

        if (parens)
            sb.append(")");

        return sb.toString();
    }

}
