package com.gaminglounge.bll;

import java.time.LocalDate;
import java.util.Map;

import com.gaminglounge.dal.StatisticsDAL;

public class StatisticsService {
    private StatisticsDAL statisticsDAL;

    public StatisticsService() {
        statisticsDAL = new StatisticsDAL();
    }

    public double getTotalDepositRevenue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getDepositRevenue(startDate, endDate);
    }

    public double getTotalTimeSalesRevenue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getTimeSalesRevenue(startDate, endDate);
    }

    public double getTotalOrderRevenue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getOrderRevenue(startDate, endDate);
    }

    public double getTotalSessionRevenue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getSessionRevenue(startDate, endDate);
    }
    
    public Map<String, Double> getDailyDepositRevenue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getDailyDepositRevenue(startDate, endDate);
    }

    public int getImportCount(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getImportCount(startDate, endDate);
    }

    public int getExportCount(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getExportCount(startDate, endDate);
    }

    public double getImportValue(LocalDate startDate, LocalDate endDate) {
        return statisticsDAL.getImportValue(startDate, endDate);
    }
}
