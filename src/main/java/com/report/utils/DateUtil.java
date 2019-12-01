package com.report.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Charles Wesley
 * @date 2019/11/30 19:36
 */
public class DateUtil {
    /**
     * 传入日期，获取当日所在周的周一和周天
     */
    public static Map<String, Date> getDayWeeks(Date date){
        Map<String,Date> map = new HashMap<>(2);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        //获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if(dayWeek==1){
            dayWeek = 8;
        }

        //通过星期的差值，计算出日期的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - dayWeek);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date mondayDate = cal.getTime();

        //cal.getFirstDayOfWeek()永远等于2，将周一向后挪动6天，必然就是周日了
        cal.add(Calendar.DATE, 4 +cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date sundayDate = cal.getTime();

        map.put("Monday", mondayDate);
        map.put("Sunday", sundayDate);
        return map;
    }

    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime date2LocalDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.of("+08:00");
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * LocalDateTime转换为Date
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime){
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.of("+08:00"));
        return Date.from(zdt.toInstant());
    }
}
