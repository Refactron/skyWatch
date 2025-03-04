package com.ppc8.skyWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SkyWatchService {
    private static final Logger logger = LoggerFactory.getLogger(SkyWatchService.class);
    private final TickRepository tickRepository;
    private final CompanyRepository companyRepository;
    private final FinancialsRepository financialsRepository;
    private final String apiKey;

    public SkyWatchService(TickRepository tickRepository, CompanyRepository companyRepository,
                           FinancialsRepository financialsRepository, @Value("${api.key}") String apiKey) {
        this.tickRepository = tickRepository;
        this.companyRepository = companyRepository;
        this.financialsRepository = financialsRepository;
        this.apiKey = apiKey;
    }

    @Transactional
    public void fetchAndStore(String symbol, String fromDate, String toDate) {
        Company company = null;
        String effectiveToDate = (toDate == null || toDate.trim().isEmpty()) ? LocalDate.now().toString() : toDate;

        // Step 1: Fetch Company and Financials Data
        String financialsUrl = "https://api.polygon.io/vX/reference/financials?ticker=" + symbol +
                "&period_of_report_date.gte=" + fromDate + "&timeframe=quarterly&order=asc&limit=100&apiKey=" + apiKey;
        String nextFinancialsUrl = financialsUrl;

        while (nextFinancialsUrl != null) {
            logger.info("Fetching company and financials data from: {}", nextFinancialsUrl);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(nextFinancialsUrl, Map.class);

            if (response == null) {
                logger.error("Null response from Polygon financials API for {}", symbol);
                break;
            }

            if (response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    // Step 1a: Create Company (first result)
                    if (company == null) {
                        Map<String, Object> firstResult = results.get(0);
                        String companyName = (String) firstResult.get("company_name");

                        if (companyName != null && !companyName.trim().isEmpty()) {
                            if (!companyRepository.existsById(symbol)) {
                                company = new Company(symbol, companyName);
                                companyRepository.save(company);
                                logger.info("Created Company {} with name {}", symbol, companyName);
                            } else {
                                logger.info("Company {} already exists, skipping creation", symbol);
                                company = companyRepository.findById(symbol).get();
                            }
                        } else {
                            logger.warn("No company_name found in financials response for {}, skipping Company creation", symbol);
                        }
                    }

                    // Step 1b: Process Financials
                    for (Map<String, Object> result : results) {
                        try {
                            String fiscalYear = (String) result.get("fiscal_year");
                            String fiscalPeriod = (String) result.get("fiscal_period");
                            String startDateStr = (String) result.get("start_date");
                            String endDateStr = (String) result.get("end_date");

                            if (fiscalYear == null || fiscalPeriod == null || startDateStr == null || endDateStr == null) {
                                logger.warn("Missing required fields (fiscal_year, fiscal_period, start_date, or end_date) in financials result for {}, skipping: {}", symbol, result);
                                continue;
                            }

                            String id = symbol + "-" + fiscalYear + "-" + fiscalPeriod;
                            LocalDate startDate = LocalDate.parse(startDateStr);
                            LocalDate endDate = LocalDate.parse(endDateStr);

                            // Skip if Financials already exists
                            if (financialsRepository.existsById(id)) {
                                logger.info("Financials for {} already exists, skipping insert", id);
                                continue;
                            }

                            Map<String, Object> financials = (Map<String, Object>) result.get("financials");
                            if (financials == null) {
                                logger.warn("Missing 'financials' in result for {}, skipping: {}", symbol, result);
                                continue;
                            }

                            Map<String, Object> incomeStatement = (Map<String, Object>) financials.get("income_statement");
                            Map<String, Object> balanceSheet = (Map<String, Object>) financials.get("balance_sheet");

                            Double revenues = getDoubleValue(incomeStatement, "revenues");
                            Double netIncomeLoss = getDoubleValue(incomeStatement, "net_income_loss");
                            Double currentAssets = getDoubleValue(balanceSheet, "current_assets");
                            Double grossProfit = getDoubleValue(incomeStatement, "gross_profit");
                            Double operatingIncomeLoss = getDoubleValue(incomeStatement, "operating_income_loss");
                            Double basicEarningsPerShare = getDoubleValue(incomeStatement, "basic_earnings_per_share");
                            Long basicAverageShares = getLongValue(incomeStatement, "basic_average_shares");
                            Double inventory = getDoubleValue(balanceSheet, "inventory");
                            Double wages = getDoubleValue(balanceSheet, "wages");
                            Double costOfRevenue = getDoubleValue(incomeStatement, "cost_of_revenue");

                            Financials financial = new Financials(id, company, startDate, endDate, fiscalYear, fiscalPeriod,
                                    revenues, netIncomeLoss, currentAssets, grossProfit, operatingIncomeLoss, basicEarningsPerShare,
                                    basicAverageShares, inventory, wages, costOfRevenue);
                            financialsRepository.save(financial);
                            logger.info("Stored Financials for {} from {} to {}", id, startDate, endDate);
                        } catch (Exception e) {
                            logger.error("Failed to process financials for " + symbol, e);
                        }
                    }
                } else {
                    logger.warn("No financials results for {}, skipping Company and Financials", symbol);
                }
            } else {
                logger.error("No 'results' in financials response for {}, proceeding to Tick", symbol);
                break;
            }

            // Pagination for financials
            nextFinancialsUrl = (String) response.get("next_url");
            if (nextFinancialsUrl != null) {
                nextFinancialsUrl += "&apiKey=" + apiKey;
                logger.info("Next financials URL detected: {}", nextFinancialsUrl);
                try {
                    logger.info("Pausing for 12 seconds to respect API rate limit...");
                    Thread.sleep(12000); // 12-second delay for rate limit
                } catch (InterruptedException e) {
                    logger.error("Interrupted during API rate limit pause: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Step 2: Fetch Tick Data
        String initialTickUrl = "https://api.polygon.io/v2/aggs/ticker/" + symbol + "/range/1/minute/" +
                fromDate + "/" + effectiveToDate + "?apiKey=" + apiKey + "&limit=50000";
        String nextTickUrl = initialTickUrl;

        while (nextTickUrl != null) {
            logger.info("Fetching tick data from: {}", nextTickUrl);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(nextTickUrl, Map.class);

            if (response == null) {
                logger.error("Null response from Polygon API for {}-{} to {}", symbol, fromDate, effectiveToDate);
                break;
            }

            if (response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                int trend = 0;
                for (Map<String, Object> result : results) {
                    try {
                        Object openPriceObj = result.get("o");
                        Object closePriceObj = result.get("c");
                        Object volumeObj = result.get("v");
                        Object transactionsObj = result.get("n");
                        Object vwPriceObj = result.get("vw");
                        Long timestampObj = (Long) result.get("t");

                        if (openPriceObj == null || closePriceObj == null || volumeObj == null || transactionsObj == null || vwPriceObj == null || timestampObj == null) {
                            logger.warn("Missing data in result: {}", result);
                            continue;
                        }

                        double openPrice = toDouble(openPriceObj);
                        double closePrice = toDouble(closePriceObj);
                        long volume = toLong(volumeObj);
                        int transactions = toInt(transactionsObj);
                        double vwPrice = toDouble(vwPriceObj);
                        long timestamp = timestampObj;

                        if (openPrice < closePrice) {
                            trend = (trend > 0) ? trend + 1 : 1;
                        } else if (closePrice < openPrice) {
                            trend = (trend < 0) ? trend - 1 : -1;
                        }

                        String market = getMarketSegment(timestamp);
                        Tick tick = new Tick(symbol, openPrice, closePrice, volume, transactions, vwPrice, timestamp, market, company, trend);
                        String id = tick.getId();

                        if (tickRepository.existsById(id)) {
                            // Commented out to reduce chatter. Enable if you like log spam.
                            //  logger.info("Tick with id '{}' already exists, skipping insert", id);
                        } else {
                            tickRepository.insertIfNotExists(
                                    id, openPrice, closePrice, volume, transactions, vwPrice,
                                    tick.getDate(), tick.getTime(), market, trend
                            );
                            // logger.info("Stored {} for {} at {} (o: {}, c: {}, vol: {}, tx: {}, vw: {} market: {})",
                            //   symbol, tick.getDate(), tick.getTime(), openPrice, closePrice, volume, transactions, vwPrice, market);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to store tick: {}", e.getMessage());
                    }
                }
            } else {
                logger.error("No 'results' in tick response for {}-{} to {}", symbol, fromDate, effectiveToDate);
                break;
            }

            nextTickUrl = (String) response.get("next_url");
            if (nextTickUrl != null) {
                nextTickUrl += "&apiKey=" + apiKey;
                logger.info("Next tick URL detected: {}", nextTickUrl);
                try {
                    logger.info("Pausing for 12 seconds to respect API rate limit...");
                    Thread.sleep(12000); // 12-second delay for rate limit
                } catch (InterruptedException e) {
                    logger.error("Interrupted during API rate limit pause: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("DONE fetching data from {}", initialTickUrl);
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key)) {
            Map<String, Object> entry = (Map<String, Object>) map.get(key);
            if (entry != null && entry.containsKey("value")) {
                Object value = entry.get("value");
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
                logger.warn("Value for key {} is not a number: {}", key, value);
            }
        }
        return null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key)) {
            Map<String, Object> entry = (Map<String, Object>) map.get(key);
            if (entry != null && entry.containsKey("value")) {
                Object value = entry.get("value");
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                logger.warn("Value for key {} is not a number: {}", key, value);
            }
        }
        return null;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to double");
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to long");
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to int");
    }

    private String getMarketSegment(long timestamp) {
        LocalDateTime datetime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneId.of("America/New_York").getRules().getOffset(Instant.now()));
        int hour = datetime.getHour();
        int minute = datetime.getMinute();
        if ((hour < 9) || (hour == 9 && minute < 30)) return "PREMARKET"; // 4 AM-9:30 AM
        else if (hour < 16) return "REGULAR"; // 9:30 AM-4:00 PM
        else return "AFTERMARKET"; // 4:00 PM onward
    }
}