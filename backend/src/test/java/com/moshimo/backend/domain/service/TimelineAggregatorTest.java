package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimelineAggregator.
 *
 * Plain unit tests — no Spring context, no mocks needed.
 * TimelineAggregator is a pure function class.
 */
@DisplayName("TimelineAggregator Tests")
class TimelineAggregatorTest {

    private TimelineAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new TimelineAggregator();
    }

    // ── Edge cases ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Empty timeline → returns empty list for any timeframe")
    void aggregateTimeline_emptyInput_returnsEmpty() {
        for (Timeframe tf : Timeframe.values()) {
            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(Collections.emptyList(), tf);
            assertTrue(result.isEmpty(), "Expected empty for timeframe " + tf);
        }
    }

    @Test
    @DisplayName("Single point → returns that point for any timeframe")
    void aggregateTimeline_singlePoint_returnsSamePoint() {
        SimulationResponse.TimelinePoint point =
                point(LocalDate.of(2024, 3, 15), "1000.00");
        List<SimulationResponse.TimelinePoint> input = List.of(point);

        for (Timeframe tf : Timeframe.values()) {
            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(input, tf);
            assertEquals(1, result.size(), "timeframe " + tf);
            assertEquals(point, result.get(0));
        }
    }

    // ── ONE_DAY ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ONE_DAY (daily)")
    class OneDayTests {

        @Test
        @DisplayName("Returns all points unchanged")
        void aggregateTimeline_oneDay_returnsAllPoints() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 30);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_DAY);

            assertEquals(daily.size(), result.size());
            assertEquals(daily, result);
        }
    }

    // ── ONE_WEEK ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ONE_WEEK (weekly)")
    class OneWeekTests {

        @Test
        @DisplayName("5 weeks of daily data → 5 weekly points")
        void aggregateTimeline_oneWeek_samplesFirstDayOfEachWeek() {
            // 35 calendar days → ~5 weeks
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 35); // Mon Jan 1 2024

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_WEEK);

            // Each result point should be from a different ISO week (Monday-based)
            Set<LocalDate> weekStarts = result.stream()
                    .map(p -> p.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                    .collect(Collectors.toSet());
            assertEquals(result.size(), weekStarts.size(), "Each point should be from a distinct week");

            assertTrue(result.size() >= 5 && result.size() <= 6,
                    "Expected 5-6 weekly points, got " + result.size());
        }

        @Test
        @DisplayName("Weekly output is strictly fewer than daily input")
        void aggregateTimeline_oneWeek_reducesPointCount() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 60);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_WEEK);

            assertTrue(result.size() < daily.size());
        }
    }

    // ── ONE_MONTH ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("ONE_MONTH (monthly)")
    class OneMonthTests {

        @Test
        @DisplayName("6 months of daily data → 6 monthly points")
        void aggregateTimeline_oneMonth_samplesFirstDayOfEachMonth() {
            // ~180 days across Jan–Jun
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 180);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_MONTH);

            // Each result point should be from a different year-month
            Set<YearMonth> months = result.stream()
                    .map(p -> YearMonth.from(p.date()))
                    .collect(Collectors.toSet());
            assertEquals(result.size(), months.size(), "Each point should be from a distinct month");

            assertEquals(6, result.size(), "Expected 6 monthly points for ~180 days from Jan");
        }

        @Test
        @DisplayName("Monthly output ≤ 12 points for 1 year of data")
        void aggregateTimeline_oneMonth_atMost12PointsPerYear() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 365);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_MONTH);

            assertTrue(result.size() <= 13, "Expected ≤13 monthly points for 1 year, got " + result.size());
            assertTrue(result.size() >= 12, "Expected ≥12 monthly points for 1 year, got " + result.size());
        }
    }

    // ── ONE_YEAR ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ONE_YEAR (yearly)")
    class OneYearTests {

        @Test
        @DisplayName("5 years of daily data → 5 yearly points")
        void aggregateTimeline_oneYear_samplesFirstDayOfEachYear() {
            // ~ 5 years of data
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2020, 1, 1), 365 * 5);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ONE_YEAR);

            // Each point from a different year
            Set<Integer> years = result.stream()
                    .map(p -> p.date().getYear())
                    .collect(Collectors.toSet());
            assertEquals(result.size(), years.size());

            // 2020, 2021, 2022, 2023, 2024 → up to 5 (depending on exact day count)
            assertTrue(result.size() >= 5 && result.size() <= 6,
                    "Expected 5-6 yearly points, got " + result.size());
        }
    }

    // ── ALL (smart sampling) ────────────────────────────────────────────

    @Nested
    @DisplayName("ALL (smart sampling)")
    class AllTests {

        @Test
        @DisplayName("Data ≤ 500 points → returns all points unchanged")
        void aggregateTimeline_all_underThreshold_returnsAll() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2024, 1, 1), 300);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ALL);

            assertEquals(300, result.size());
        }

        @Test
        @DisplayName("3000 points → output capped near 500")
        void aggregateTimeline_all_3000Points_cappedAt500() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2015, 1, 1), 3000);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ALL);

            // step = 3000/500 = 6, so ~500 sampled + possible last point = ~501
            assertTrue(result.size() <= 510,
                    "Expected ≤ 510 points, got " + result.size());
            assertTrue(result.size() >= 490,
                    "Expected ≥ 490 points, got " + result.size());
        }

        @Test
        @DisplayName("Smart sampling always includes the last data point")
        void aggregateTimeline_all_alwaysIncludesLastPoint() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2015, 1, 1), 3000);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ALL);

            SimulationResponse.TimelinePoint expectedLast = daily.get(daily.size() - 1);
            assertEquals(expectedLast, result.get(result.size() - 1),
                    "Last point of smart-sampled output should match last input point");
        }

        @Test
        @DisplayName("Smart sampling preserves chronological order")
        void aggregateTimeline_all_preservesOrder() {
            List<SimulationResponse.TimelinePoint> daily = generateDailyPoints(
                    LocalDate.of(2015, 1, 1), 2000);

            List<SimulationResponse.TimelinePoint> result =
                    aggregator.aggregateTimeline(daily, Timeframe.ALL);

            for (int i = 1; i < result.size(); i++) {
                assertTrue(result.get(i).date().isAfter(result.get(i - 1).date())
                                || result.get(i).date().isEqual(result.get(i - 1).date()),
                        "Points should be in chronological order at index " + i);
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Generate consecutive daily timeline points starting from the given date.
     */
    private List<SimulationResponse.TimelinePoint> generateDailyPoints(LocalDate start, int count) {
        List<SimulationResponse.TimelinePoint> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BigDecimal value = BigDecimal.valueOf(1000 + i);
            points.add(new SimulationResponse.TimelinePoint(start.plusDays(i), value));
        }
        return points;
    }

    private SimulationResponse.TimelinePoint point(LocalDate date, String value) {
        return new SimulationResponse.TimelinePoint(date, new BigDecimal(value));
    }
}
