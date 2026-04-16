package id.ac.ui.cs.advprog.inventory.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogSeedConfigTest {

    private final CatalogSeedConfig config = new CatalogSeedConfig();

    @Test
    void shouldSkipSeedingWhenCatalogAlreadyExists() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class)).thenReturn(3);

        CommandLineRunner runner = config.seedCatalog(jdbcTemplate);
        runner.run();

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldSeedCatalogWhenCountIsZero() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class)).thenReturn(0);

        CommandLineRunner runner = config.seedCatalog(jdbcTemplate);
        runner.run();

        verify(jdbcTemplate, times(6)).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldSeedCatalogWhenCountIsNull() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class)).thenReturn(null);

        CommandLineRunner runner = config.seedCatalog(jdbcTemplate);
        runner.run();

        verify(jdbcTemplate, times(6)).update(anyString(), any(Object[].class));
    }
}
