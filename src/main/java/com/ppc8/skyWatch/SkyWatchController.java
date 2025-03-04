package com.ppc8.skyWatch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
public class SkyWatchController {
    private final SkyWatchService service;

    public SkyWatchController(SkyWatchService service) {
        this.service = service;
    }

    @GetMapping("/fetchData")
    public String fetch(
            @RequestParam String symbol,
            @RequestParam String fromDate,
            @RequestParam(required = false) String toDate) {

        // If toDate is null or empty, default to today
        String effectiveToDate = (toDate == null || toDate.trim().isEmpty())
                ? LocalDate.now().toString()
                : toDate;

        service.fetchAndStore(symbol.toUpperCase(), fromDate, effectiveToDate);
        return "Fetched and stored data for " + symbol + " from " + fromDate + " to " + effectiveToDate;
    }
}