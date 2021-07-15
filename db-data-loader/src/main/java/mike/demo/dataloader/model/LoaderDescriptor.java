package mike.demo.dataloader.model;

import java.util.List;

public class LoaderDescriptor {

    private final String tableName;
    private final List<LoaderColumn> columns;
    
    public LoaderDescriptor(String tableName, List<LoaderColumn> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
    
    public String getTableName() {
        return tableName;
    }

    public List<LoaderColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return String.format("LoaderDescriptor [tableName=%s, columns=%s]", tableName, columns);
    }
}
