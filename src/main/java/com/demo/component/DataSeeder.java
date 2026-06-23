package com.demo.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final DataSeederService dataSeederService;

    // Prevent double execution (ContextRefreshedEvent can fire twice in some setups)
    private final AtomicBoolean seeded = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (seeded.getAndSet(true)) return;
        try {
            dataSeederService.seed();
        } catch (Exception e) {
            log.warn("DataSeeder: skipping - {}", e.getMessage());
            seeded.set(false);
        }
    }
}
