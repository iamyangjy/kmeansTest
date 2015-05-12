package com.ruijie.rbis.util;

/**
 * Created by OA on 2015/1/5.
 */
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class AlgDate {
     //Dao的util
    //获取相对日期
    public static Date getDateRelative(Date date, int day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }

    //获取两个日期内所有日期列表
    public static List<Date> getBetweenDay(Date startDate, Date endDate){
        //Date StartDate = dateFormate(StartDay);
        //Date endDate =
        List<Date> dateList = new ArrayList<Date>();
        dateList.add(startDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (true){
            cal.add(Calendar.DATE, 1);
            if(endDate.before(cal.getTime())){
                break;
            }else{
                dateList.add(cal.getTime());
            }
        }
        return dateList;
    }

}
