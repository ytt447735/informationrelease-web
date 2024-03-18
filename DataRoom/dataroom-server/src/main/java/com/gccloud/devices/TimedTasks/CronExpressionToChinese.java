package com.gccloud.devices.TimedTasks;

import java.util.HashMap;
import java.util.Map;

public class CronExpressionToChinese {

    private static final String[] MONTHS = {"", "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
    private static final String[] WEEKDAYS = {"", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public static String convertCronToChinese(String cronExpression) {
        String[] parts = cronExpression.split("\\s+");
        if (parts.length < 6) {
            return "Cron表达式无效";
        }

        StringBuilder description = new StringBuilder();

        // 分钟
        description.append("每隔").append(parts[1]).append("分钟");

        // 小时
        description.append("在").append(parts[2]).append("时");

        // 天数
        if (!"*".equals(parts[3])) {
            description.append("的第").append(parts[3]).append("天");
        }

        // 月份
        if (!"*".equals(parts[4])) {
            int monthIndex = Integer.parseInt(parts[4]);
            if (monthIndex >= 1 && monthIndex <= 12) {
                description.append("的").append(MONTHS[monthIndex]);
            }
        }

        // 星期
        if (!"*".equals(parts[5])) {
            int weekdayIndex = Integer.parseInt(parts[5]);
            if (weekdayIndex >= 1 && weekdayIndex <= 7) {
                description.append(WEEKDAYS[weekdayIndex]);
            }
        }

        return description.toString();
    }
}

