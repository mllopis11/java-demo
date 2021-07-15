package mike.demo.dataloader.test;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.exceptions.ApplicationException;
import mike.demo.dataloader.domain.LoaderDescriptorFactory;
import mike.demo.dataloader.model.LoaderColumn;
import mike.demo.dataloader.model.LoaderDescriptor;

@DisplayName("LoaderDescriptorFactory")
class LoaderDescriptorFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(LoaderDescriptorFactoryTest.class);
    
    private static final Path testDirectory = Path.of("./run", "test");
    
    @BeforeAll
    static void init() throws IOException {
        Files.createDirectories(testDirectory);
    }
    
    @AfterAll
    static void tearDown() throws IOException {
        Files.list(testDirectory).forEach(f -> {
            try {
                Files.delete(f);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        } );
    }
    
    @Test
    void should_throw_application_exception_when_desc_file_not_found() {
        
        String filename = "dbl-not-exists.dsc";
        
        assertThatExceptionOfType(ApplicationException.class)
            .isThrownBy( () -> LoaderDescriptorFactory.newLoaderDescriptor(filename))
            .withMessageContaining(filename);
    }
    
    @ParameterizedTest
    @ValueSource( strings= { "C;id;1;10;N;true;",  "T;  ", "T;LDR_USER_STG" })
    void should_throw_application_exception_when_no_table_or_columns(String line) throws IOException {
        
        Path tempTestFile = Files.createTempFile(testDirectory, "dbl-test", ".dsc");
        
        log.debug("Temporary file: {}", tempTestFile);
        
        Files.writeString(tempTestFile, line, StandardOpenOption.TRUNCATE_EXISTING);
        
        assertThatExceptionOfType(ApplicationException.class)
            .isThrownBy( () -> LoaderDescriptorFactory.newLoaderDescriptor(tempTestFile))
            .withMessageContaining("no such table name or columns");
    }
    
    @ParameterizedTest
    @ValueSource( strings= { "C;id;1;10;N;true",  "T" })
    void should_throw_application_exception_when_line_malformatted(String line) throws IOException {
        
        Path tempTestFile = Files.createTempFile(testDirectory, "dbl-test", ".dsc");
        
        log.debug("Temporary file: {}", tempTestFile);
        
        Files.writeString(tempTestFile, line, StandardOpenOption.TRUNCATE_EXISTING);
        
        assertThatExceptionOfType(ApplicationException.class)
            .isThrownBy( () -> LoaderDescriptorFactory.newLoaderDescriptor(tempTestFile))
            .withMessageContaining("invalid number of items");
    }
    
    @Test
    void should_return_user_descriptor_when_user_resource() {
        
        String userDescFilename = "dbl-test-user.dsc";
        
        LoaderDescriptor userDescriptor = LoaderDescriptorFactory.newLoaderDescriptor(userDescFilename);
        
        assertThat(userDescriptor.getTableName()).isNotNull().isEqualTo("LDR_USER_STG");
        assertThat(userDescriptor.getColumns()).isNotNull().hasSize(7);
        assertThat(userDescriptor.toString()).startsWith("LoaderDescriptor [").contains("LDR_USER_STG");
        
        assertThat(userDescriptor.getColumns().get(0).getName()).isEqualTo("id");
        assertThat(userDescriptor.getColumns().get(6).getName()).isEqualTo("is_enabled");
        
        LoaderColumn birthdateColumn = userDescriptor.getColumns().get(4);
        
        assertThat(birthdateColumn.getPosition()).isEqualTo(5);
        assertThat(birthdateColumn.getName()).isEqualTo("birthdate");
        assertThat(birthdateColumn.getStartAt()).isEqualTo(52);
        assertThat(birthdateColumn.getLength()).isEqualTo(10);
        assertThat(birthdateColumn.getDatatype()).isEqualTo("C");
        assertThat(birthdateColumn.isRequired()).isTrue();
        assertThat(birthdateColumn.getExpression()).startsWith("TO_DATE");
    }
}
