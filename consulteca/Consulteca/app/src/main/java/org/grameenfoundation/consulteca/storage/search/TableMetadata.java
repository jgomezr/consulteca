package org.grameenfoundation.consulteca.storage.search;

import java.util.ArrayList;
import java.util.List;

/**
 * holds metadata about the table
 */
public class TableMetadata {
    private String tableName;
    private List<ColumnMetadata> columnMetadataList;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnMetadata> getColumnMetadataList() {
        return columnMetadataList;
    }

    public void addColumnMetadata(ColumnMetadata columnMetadata) {
        if (columnMetadataList == null)
            columnMetadataList = new ArrayList<ColumnMetadata>();

        columnMetadataList.add(columnMetadata);
        columnMetadata.setTableMetadata(this);
    }

    public void remoteColumnMetadata(ColumnMetadata columnMetadata) {
        if (this.columnMetadataList == null)
            return;

        columnMetadataList.remove(columnMetadata);
    }

    public ColumnMetadata getColumnMetadata(String columnName) {
        if (this.columnMetadataList == null)
            return null;

        ColumnMetadata columnMetadata = null;
        for (ColumnMetadata metadata : columnMetadataList) {
            if (metadata.getColumnName().equalsIgnoreCase(columnName)) {
                columnMetadata = metadata;
                break;
            }
        }

        return columnMetadata;
    }
}
