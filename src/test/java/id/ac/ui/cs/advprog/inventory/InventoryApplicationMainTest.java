package id.ac.ui.cs.advprog.inventory;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class InventoryApplicationMainTest {

    @Test
    void main_shouldDelegateToSpringApplicationRun() {
        String[] args = new String[]{"--spring.profiles.active=test"};
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(InventoryApplication.class, args))
                    .thenReturn(context);

            InventoryApplication.main(args);

            mocked.verify(() -> SpringApplication.run(InventoryApplication.class, args));
        }
    }
}
