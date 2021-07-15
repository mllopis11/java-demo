package mike.demo.dataloader.model;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class LoaderStatements {

    private final LoaderDescriptor descriptor;
    
    public LoaderStatements(LoaderDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    public String getInsertStatement(boolean useBulk) {
        String columnNames = descriptor.getColumns().stream().map(LoaderColumn::getName).collect(Collectors.joining(","));
        
        return String.format("INSERT%sINTO %s (%s) VALUES (%s)", 
                useBulk ? " /*+ APPEND */ " : " ",
                descriptor.getTableName(), columnNames, 
                StringUtils.repeat("?", ",", descriptor.getColumns().size()) );
    }
}
