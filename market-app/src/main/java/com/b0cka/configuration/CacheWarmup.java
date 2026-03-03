package com.b0cka.configuration;

import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmup {

    private final ItemService itemService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent() {
        log.info("START: Начинаем фоновый прогрев кеша...");
        itemService.refreshAllCaches()
                .subscribe(
                        null,
                        e -> log.error("Ошибка при прогреве кеша", e),
                        () -> log.info("DONE: Кеш успешно прогрет!")
                );
    }
}