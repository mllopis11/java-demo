package mike.demo.dataloader.test;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.demo.dataloader.domain.LoaderDescriptorFactory;
import mike.demo.dataloader.model.LoaderDescriptor;
import mike.demo.dataloader.model.LoaderStatements;

@DisplayName("LoaderStatements")
class LoaderStatementsTest {

    private static final Logger log = LoggerFactory.getLogger(LoaderStatementsTest.class);
    
    @Test
    void should_return_insert_statement_when_user_descriptor() {
        
        String userDescFilename = "dbl-test-user.dsc";
        
        LoaderDescriptor userDescriptor = LoaderDescriptorFactory.newLoaderDescriptor(userDescFilename);
        
        LoaderStatements statements = new LoaderStatements(userDescriptor);
        
        log.debug("InsertStatement: {}", statements.getInsertStatement(false));
        log.debug("InsertStatement: {}", statements.getInsertStatement(true));
        
        String expectedColumnNames = "id,firstname,lastname,gender,birthdate,email,is_enabled";
        String expectedValuePattern = "(?,?,?,?,?,?,?)";
        
        assertThat(statements.getInsertStatement(false))
            .startsWith("INSERT INTO " + userDescriptor.getTableName())
            .contains(expectedColumnNames, expectedValuePattern);
    }
}
