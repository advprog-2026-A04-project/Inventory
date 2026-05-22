package id.ac.ui.cs.advprog.inventory.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OutOfStockEventListener {

    private static final Logger log = LoggerFactory.getLogger(OutOfStockEventListener.class);

    @EventListener
    public void handleOutOfStockEvent(OutOfStockEvent event) {
        log.warn("OBSERVER PATTERN TRIGGERED: Product {} (ID: {}) has run out of stock!", 
                event.getProduct().getName(), event.getProduct().getId());
        // In a real scenario, this could send an email, a push notification, or alert other services.
    }
}
