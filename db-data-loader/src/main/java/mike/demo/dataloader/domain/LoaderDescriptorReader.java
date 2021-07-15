package mike.demo.dataloader.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import mike.bootstrap.utilities.exceptions.ApplicationException;
import mike.bootstrap.utilities.helpers.Resource;
import mike.demo.dataloader.model.LoaderColumn;
import mike.demo.dataloader.model.LoaderDescriptor;

class LoaderDescriptorReader {

    private static final String DSC_SEPARATOR = ";";
    private static final int DSC_TAB_ITEMS = 2;
    private static final int DSC_COL_ITEMS = 7;
    
    private final Resource resource;
    
    private int columnIdx;
    private String tabName;
    private List<LoaderColumn> columns = new ArrayList<>();
    
    LoaderDescriptorReader(Resource resource) {
        this.resource = resource;
    }
    
    LoaderDescriptor read() {
        
        try ( var br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                Stream<String> lines = br.lines() ) {
            lines.forEach(this::parse);
        } catch (IOException ioe) {
            throw new ApplicationException(ioe, "resource: %s (reason: %s)", resource.getName(), ioe.getMessage()); 
        }
        
        if ( tabName == null || tabName.isBlank() || columns.isEmpty() ) {
            throw new ApplicationException("resouce: %s (reason: no such table name or columns)", resource.getName());
        }
        
        return new LoaderDescriptor(tabName, columns);
    }
    
    private void parse(String line) {
        
        if ( line.startsWith("C") ) {
            this.parseColumn(line);
        } else if ( line.startsWith("T") ) {
            this.parseTable(line);
        }
    }
    
    private void parseTable(String line) {

        String[] items = this.parseLine(line, DSC_TAB_ITEMS);
        tabName = items[1];
    }
    
    private void parseColumn(String line) {
        
        String[] items = this.parseLine(line, DSC_COL_ITEMS);
        
        var column = new LoaderColumn();
        column.setPosition(++columnIdx);
        column.setName(items[1]);
        column.setStartAt(Integer.valueOf(items[2]));
        column.setLength(Integer.valueOf(items[3]));
        column.setDatatype(items[4]);
        column.setRequired(Boolean.valueOf(items[5]));
        column.setExpression(items[6]);
            
        columns.add(column);
    }
    
    private String[] parseLine(String line, int limit) {
        String[] items = line.split(DSC_SEPARATOR, limit);
        
        if ( items.length != limit ) {
            var type = line.startsWith("T") ? "table" : "column";
            throw new ApplicationException("%s: invalid number of items: %d (expected: %d), resource: %s", 
                    type, items.length, limit, resource.getName());
        }
        
        return items;
    }
}
