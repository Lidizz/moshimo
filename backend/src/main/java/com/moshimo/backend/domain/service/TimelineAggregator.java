package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates a daily timeline into the requested timeframe granularity.
 *
 * Extracted from InvestmentSimulationService to break a circular dependency:
 * BenchmarkService needs aggregation but must not depend on
 * InvestmentSimulationService — both depend on this class instead.
 *
 * Dependency graph:
 *   InvestmentSimulationService → TimelineAggregator
 *   BenchmarkService            → TimelineAggregator
 */
@Service
public class TimelineAggregator {

    /**
     * Aggregate a complete daily timeline to the requested granularity.
     * Samples across the ENTIRE period, not just recent data.
     *
     * @param dailyTimeline complete daily timeline
     * @param timeframe     requested granularity
     * @return sampled timeline
     */
    public List<SimulationResponse.TimelinePoint> aggregateTimeline(
            List<SimulationResponse.TimelinePoint> dailyTimeline,
            Timeframe timeframe) {

        if (dailyTimeline.isEmpty()) {
            return dailyTimeline;
        }

        return switch (timeframe) {
            case ONE_DAY  -> dailyTimeline;
            case ONE_WEEK  -> sampleWeekly(dailyTimeline);
            case ONE_MONTH -> sampleMonthly(dailyTimeline);
            case ONE_YEAR  -> sampleYearly(dailyTimeline);
            case ALL       -> sampleSmart(dailyTimeline, 500);
        };
    }

    /**
     * Sample the first trading day of each week (Monday, or next available).
     */
    private List<SimulationResponse.TimelinePoint> sampleWeekly(
            List<SimulationResponse.TimelinePoint> daily) {

        List<SimulationResponse.TimelinePoint> result = new ArrayList<>();
        java.time.LocalDate lastWeekStart = null;

        for (SimulationResponse.TimelinePoint point : daily) {
            java.time.LocalDate weekStart =
                    point.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (!weekStart.equals(lastWeekStart)) {
                result.add(point);
                lastWeekStart = weekStart;
            }
        }
        return result;
    }

    /**
     * Sample the first trading day of each month.
     */
    private List<SimulationResponse.TimelinePoint> sampleMonthly(
            List<SimulationResponse.TimelinePoint> daily) {

        List<SimulationResponse.TimelinePoint> result = new ArrayList<>();
        YearMonth lastMonth = null;

        for (SimulationResponse.TimelinePoint point : daily) {
            YearMonth currentMonth = YearMonth.from(point.date());
            if (!currentMonth.equals(lastMonth)) {
                result.add(point);
                lastMonth = currentMonth;
            }
        }
        return result;
    }

    /**
     * Sample the first trading day of each year (January).
     */
    private List<SimulationResponse.TimelinePoint> sampleYearly(
            List<SimulationResponse.TimelinePoint> daily) {

        List<SimulationResponse.TimelinePoint> result = new ArrayList<>();
        Integer lastYear = null;

        for (SimulationResponse.TimelinePoint point : daily) {
            int currentYear = point.date().getYear();
            if (!Integer.valueOf(currentYear).equals(lastYear)) {
                result.add(point);
                lastYear = currentYear;
            }
        }
        return result;
    }

    /**
     * Smart sampling: if data exceeds maxPoints, sample uniformly.
     * Algorithm: Sample every N-th point where N = totalPoints / maxPoints.
     * Always includes the last point.
     */
    private List<SimulationResponse.TimelinePoint> sampleSmart(
            List<SimulationResponse.TimelinePoint> daily, int maxPoints) {

        if (daily.size() <= maxPoints) {
            return daily;
        }

        int step = daily.size() / maxPoints;
        List<SimulationResponse.TimelinePoint> result = new ArrayList<>();

        for (int i = 0; i < daily.size(); i += step) {
            result.add(daily.get(i));
        }

        // Always include the last point
        SimulationResponse.TimelinePoint lastPoint = daily.get(daily.size() - 1);
        if (!result.get(result.size() - 1).equals(lastPoint)) {
            result.add(lastPoint);
        }

        return result;
    }
}
