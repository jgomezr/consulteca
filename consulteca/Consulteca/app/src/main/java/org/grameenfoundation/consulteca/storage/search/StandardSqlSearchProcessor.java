package org.grameenfoundation.consulteca.storage.search;

import java.util.Collection;
import java.util.List;

/**
 * A search processor that generates Standard SQL from the <code>Search</code>s that are
 * passed to the methods.
 *
 * @author Charles Tumwebaze
 */
public class StandardSqlSearchProcessor implements SearchProcessor {
    private MetadataProvider metadataProvider;

    @Override
    public String generateQuery(Search search) {
        String select = generateSelectClause(search);
        String where = generateWhereClause(search);
        String orderBy = generateOrderByClause(search);
        String from = generateFromClause(search);

        StringBuilder sb = new StringBuilder();
        sb.append(select);
        sb.append(from);
        sb.append(where);
        sb.append(orderBy);

        return sb.toString();
    }

    @Override
    public String generateRowCountQuery(Search search) {
        String select = "SELECT COUNT(*) ";
        String where = generateWhereClause(search);
        String from = generateFromClause(search);

        StringBuilder sb = new StringBuilder();
        sb.append(select);
        sb.append(from);
        sb.append(where);

        return sb.toString();
    }

    @Override
    public String generateDeleteStatement(Search search) {
        String delete = generateDeleteClause(search);
        String where = generateWhereClause(search);
        String from = generateFromClause(search);

        StringBuilder sb = new StringBuilder();
        sb.append(delete);
        sb.append(from);
        sb.append(where);

        return sb.toString();
    }

    protected String generateDeleteClause(Search search) {
        return "DELETE";
    }

    protected String generateSelectClause(Search search) {
        StringBuilder sb = null;
        boolean first = true;
        for (Field field : search.getFields()) {
            if (first) {
                sb.append("SELECT ");
                if (search.isDistinct()) {
                    sb.append("DISTINCT ");
                }
                first = false;
            } else {
                sb.append(",");
            }

            switch (field.getOperator()) {
                case Field.OP_AVG:
                    sb.append("AVG(").append(field.getColumn()).append(")");
                    break;
                case Field.OP_COUNT:
                    sb.append("COUNT(").append(field.getColumn()).append(")");
                    break;
                case Field.OP_MAX:
                    sb.append("MAX(").append(field.getColumn()).append(")");
                    break;
                case Field.OP_MIN:
                    sb.append("MIN(").append(field.getColumn()).append(")");
                    break;
                case Field.OP_COUNT_DISTINCT:
                    sb.append("COUNT(DISTINCT ").append(field.getColumn()).append(")");
                    break;

            }
        }

        if (first) {
            // there are no fields
            if (search.isDistinct())
                return "SELECT DISTINCT *";
            else
                return "SELECT *";
        }

        return sb.toString();
    }

    protected String generateWhereClause(Search search) {
        String content = null;
        if (search.getFilters() == null || search.getFilters().size() == 0) {
            return "";
        } else {
            Filter junctionFilter =
                    new Filter(null, search.getFilters(), search.isDisjunction() ? Filter.OP_OR : Filter.OP_AND);
            content = filterToSql(search, junctionFilter);
        }

        return (content == null) ? "" : " WHERE " + content;
    }

    private String filterToSql(Search search, Filter junctionFilter) {
        String column = junctionFilter.getColumn();
        Object value = junctionFilter.getValue();
        int operator = junctionFilter.getOperator();

        // for IN and NOT IN, if value is empty list, return false, and true
        // respectively
        if (operator == Filter.OP_IN || operator == Filter.OP_NOT_IN) {
            if (value instanceof Collection && ((Collection) value).size() == 0) {
                return operator == Filter.OP_IN ? "1 = 2" : "1 = 1";
            }
            if (value instanceof Object[] && ((Object[]) value).length == 0) {
                return operator == Filter.OP_IN ? "1 = 2" : "1 = 1";
            }
        }

        // convert numbers to the expected type if needed (ex: Integer to Long)
        if (junctionFilter.isTakesListOfValues()) {
            value = prepareValue(search.getTableName(), column, value, true);
        } else if (junctionFilter.isTakesSingleValue()) {
            value = prepareValue(search.getTableName(), column, value, false);
        }

        switch (operator) {
            case Filter.OP_NULL:
                return column + " is null";
            case Filter.OP_NOT_NULL:
                return column + " is not null";
            case Filter.OP_IN:
                return column + " in (" + param(value) + ")";
            case Filter.OP_NOT_IN:
                return column + " not in (" + param(value) + ")";
            case Filter.OP_EQUAL:
                return column + " = " + param(value);
            case Filter.OP_NOT_EQUAL:
                return column + " != " + param(value);
            case Filter.OP_GREATER_THAN:
                return column + " > " + param(value);
            case Filter.OP_LESS_THAN:
                return column + " < " + param(value);
            case Filter.OP_GREATER_OR_EQUAL:
                return column + " >= " + param(value);
            case Filter.OP_LESS_OR_EQUAL:
                return column + " <= " + param(value);
            case Filter.OP_LIKE:
                return column + " like " + param(value.toString() + "%");
            case Filter.OP_ILIKE:
                return "lower(" + column + ") like lower(" + param(value.toString() + "%") + ")";
            case Filter.OP_AND:
            case Filter.OP_OR:
                if (!(value instanceof List)) {
                    return null;
                }

                String op = junctionFilter.getOperator() == Filter.OP_AND ? " and " : " or ";

                StringBuilder sb = new StringBuilder("(");
                boolean first = true;
                for (Object o : ((List) value)) {
                    if (o instanceof Filter) {
                        String filterStr = filterToSql(search, (Filter) o);
                        if (filterStr != null) {
                            if (first) {
                                first = false;
                            } else {
                                sb.append(op);
                            }
                            sb.append(filterStr);
                        }
                    }
                }
                if (first)
                    return null;

                sb.append(")");
                return sb.toString();
            case Filter.OP_NOT:
                if (!(value instanceof Filter)) {
                    return null;
                }
                String filterStr = filterToSql(search, (Filter) value);
                if (filterStr == null)
                    return null;

                return "not " + filterStr;
            case Filter.OP_EMPTY:
                return "(" + column + " is null or " + column + " = '')";
            case Filter.OP_NOT_EMPTY:
                return "(" + column + " is not null and " + column + " != '')";
            default:
                throw new IllegalArgumentException("Filter comparison ( " + operator + " ) is invalid.");
        }
    }

    protected Object prepareValue(String tableName, String column, Object value, boolean isCollection) {
        if (value == null) return null;
        Class<?> expectedClass = metadataProvider.getJavaClass(tableName, column);

        // convert numbers to the expected type if needed (ex: Integer to Long)
        if (isCollection) {
            // Check each element in the collection.
            Object[] val2;

            if (value instanceof Collection) {
                val2 = new Object[((Collection) value).size()];
                int i = 0;
                for (Object item : (Collection) value) {
                    val2[i++] = convertIfNeeded(item, expectedClass);
                }
            } else {
                val2 = new Object[((Object[]) value).length];
                int i = 0;
                for (Object item : (Object[]) value) {
                    val2[i++] = convertIfNeeded(item, expectedClass);
                }
            }
            return val2;
        } else {
            return convertIfNeeded(value, expectedClass);
        }
    }

    /**
     * <p/>
     * Return an instance of the given class type that has the given value. For
     * example, if type is <code>Long</code> and <code>Integer</code> type with
     * the value 13 is passed in, a new instance of <code>Long</code> will be
     * returned with the value 13.
     * <p/>
     * <p/>
     * If the value is already of the correct type, it is simply returned.
     *
     * @throws ClassCastException if the value cannot be converted to the given type.
     */
    public static Object convertIfNeeded(Object value, Class<?> type) throws ClassCastException {
        // Since we're returning an object, we will never be able to return a primitive value.
        // We will return the boxed type instead.
        if (type.isPrimitive()) {
            return getBoxedPrimitiveType(type);
        }

        if (value == null)
            return null;

        if (type.isInstance(value))
            return value;

        if (String.class.getName().equalsIgnoreCase(type.getName())) {
            return value.toString();
        } else if (Number.class.isAssignableFrom(type)) {
            return getNumericValue(value, type);
        } else if (Class.class.equals(type)) {
            try {
                return Class.forName(value.toString());
            } catch (ClassNotFoundException e) {
                throw new ClassCastException("Unable to convert value " + value.toString() + " to type Class");
            }
        }

        throw new ClassCastException("Unable to convert value of type " + value.getClass().getName() + " to type "
                + type.getName());
    }

    private static Object getNumericValue(Object value, Class<?> type) {
        // the desired type is a number
        if (value instanceof Number) {
            // the value is also a number of some kind. do a conversion
            // to the correct number type.
            Number num = (Number) value;

            if (type.equals(Double.class)) {
                return new Double(num.doubleValue());
            } else if (type.equals(Float.class)) {
                return new Float(num.floatValue());
            } else if (type.equals(Long.class)) {
                return new Long(num.longValue());
            } else if (type.equals(Integer.class)) {
                return new Integer(num.intValue());
            } else if (type.equals(Short.class)) {
                return new Short(num.shortValue());
            } else {
                try {
                    return type.getConstructor(String.class).newInstance(value.toString());
                } catch (Exception e) {
                }
            }
        } else if (value instanceof String) {
            //the value is a String. attempt to parse the string
            try {
                if (type.equals(Double.class)) {
                    return Double.parseDouble((String) value);
                } else if (type.equals(Float.class)) {
                    return Float.parseFloat((String) value);
                } else if (type.equals(Long.class)) {
                    return Long.parseLong((String) value);
                } else if (type.equals(Integer.class)) {
                    return Integer.parseInt((String) value);
                } else if (type.equals(Short.class)) {
                    return Short.parseShort((String) value);
                } else if (type.equals(Byte.class)) {
                    return Byte.parseByte((String) value);
                }
            } catch (NumberFormatException ex) {
                //fall through to the error thrown below
            }
        }

        return value;
    }

    protected static Class<?> getBoxedPrimitiveType(Class<?> type) {
        if (boolean.class.equals(type)) {
            type = Boolean.class;
        } else if (char.class.equals(type)) {
            type = Character.class;
        } else if (byte.class.equals(type)) {
            type = Byte.class;
        } else if (short.class.equals(type)) {
            type = Short.class;
        } else if (int.class.equals(type)) {
            type = Integer.class;
        } else if (long.class.equals(type)) {
            type = Long.class;
        } else if (float.class.equals(type)) {
            type = Float.class;
        } else if (double.class.equals(type)) {
            type = Double.class;
        }

        return type;
    }

    protected String param(Object value) {
        if (value instanceof Class) {
            return ((Class<?>) value).getName();
        }

        if (value instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Collection) value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }

                sb.append(o);
            }
            return sb.toString();
        } else if (value instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Object[]) value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }

                sb.append(o);
            }
            return sb.toString();
        } else if (value instanceof String) {
            return "'" + value.toString() + "'";
        } else {
            return value.toString();
        }
    }


    /**
     * internal method for generating order by clause. Uses sort options from search.
     *
     * @param search
     * @return
     */
    protected String generateOrderByClause(Search search) {
        StringBuilder sb = null;
        boolean first = true;
        for (Sort sort : search.getSorts()) {
            if (first) {
                sb = new StringBuilder(" order by ");
                first = false;
            } else {
                sb.append(", ");
            }

            if (sort.isIgnoreCase()) {
                sb.append("lower(").append(sort.getColumn()).append(")");
            } else {
                sb.append(sort.getColumn());
            }

            sb.append(sort.isDesc() ? " desc" : " asc");

        }
        if (first)
            return "";

        return sb.toString();
    }

    /**
     * method for generating the from clause.
     *
     * @param search
     * @return
     */
    protected String generateFromClause(Search search) {
        StringBuilder sb = new StringBuilder(" FROM ");
        sb.append(search.getTableName());

        return sb.toString();
    }

    public MetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    public void setMetadataProvider(MetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }
}
