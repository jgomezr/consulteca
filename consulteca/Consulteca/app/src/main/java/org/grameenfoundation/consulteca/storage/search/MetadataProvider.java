package org.grameenfoundation.consulteca.storage.search;

/**
 * The search framework uses this interface to provide metadata about the
 * data storage system used.
 * <p/>
 * It provides an abstraction layer from the data storage specific ways of how to retrieve
 * that metadata.
 */
public interface MetadataProvider {

    /**
     * gets the table metadata for the given table name
     *
     * @param tableName
     * @return
     */
    TableMetadata getTableMetadata(String tableName);

    /**
     * gets the column metadata for the given columnName and tableName.
     *
     * @param tableName
     * @param columnName
     * @return
     */
    ColumnMetadata getColumnMetadata(String tableName, String columnName);

    /**
     * get the java class representing the type of the column in the
     * given table.
     *
     * @param tableName
     * @param column
     * @return
     */
    Class<?> getJavaClass(String tableName, String column);
}
