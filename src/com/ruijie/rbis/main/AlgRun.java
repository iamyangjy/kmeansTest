package com.ruijie.rbis.main;

/**
 * Created by OA on 2015/1/4.
 */

import java.text.DateFormat;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileInputStream;
import java.io.File;

import com.ruijie.rbis.algorithm.Kmeans;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.ConfigurationSource;

import com.ruijie.rbis.dao.PcaDao;
import com.ruijie.rbis.dao.MlrDao;
import com.ruijie.rbis.dao.KmeansDao;
import com.ruijie.rbis.pojo.pca.Weight;

public class AlgRun {

    public static void main(String[] args){

        //配置log4 2.0配置文件
        ConfigurationSource source;
        try{
            //File config=new File("E:\\IdeaProjects\\project\\alg_jar\\conf\\log4j2.xml");
            File config=new File("conf/log4j2.xml");
            source=new ConfigurationSource(new FileInputStream(config),config);
            Configurator.initialize(null, source);
        } catch (Exception e){
            e.printStackTrace();
        }
        //
        Logger logger = LogManager.getLogger(AlgRun.class.getName());

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = "";
        String strBuilding = "";
        //检测是否有参数进来,没有进来使用当前的日期
        if (args.length == 0 ){
            logger.error("enter argument: BuildingID and date，date format: yyyy-MM-dd");
            System.exit(1);
        }else if(args.length == 1){
            //建筑物ID
            strBuilding = args[0];
            //日期
            Date date = new Date();
            strDate = sdf.format(date);
        }else{
            strBuilding = args[0];
            strDate = args[1];
        }

        //建筑物ID
        int building = 0;
        try{
            building = Integer.parseInt(args[0]);
            logger.info("BuildingID: " + building);
        }catch (NumberFormatException nfe){
            logger.error("BilidngID error，enter number");
            System.exit(1);
        }
         //格式化获取date
        Date runDate = new Date();
        try{
            runDate = sdf.parse(strDate);
            logger.info("date: " + strDate);
        } catch (Exception e){
            logger.error("date eroor, date format: yyyy-MM-dd");
            e.printStackTrace();
            System.exit(1);
        }


        logger.info("load spring configuration");
        //String[] path = {"E:\\IdeaProjects\\project\\alg_jar\\conf\\applicationContext.xml"};
        //ApplicationContext context = new FileSystemXmlApplicationContext(path);
        ApplicationContext context = new ClassPathXmlApplicationContext("conf/applicationContext.xml");
        logger.info("load spring configuration, success");

/*
        //pricipal component
        logger.info("pricipal component model, start....");
        PcaDao pcaDao = (PcaDao) context.getBean("pcaDao");
        pcaDao.insertWeightOfFactor(building, runDate);
        logger.info("pricipal component, success");
        //multi linear regression
        logger.info("multi linear regression model, start....");
        MlrDao mlrDao = (MlrDao) context.getBean("mlrDao");
        mlrDao.insertWeightOfFactor(building, runDate);
        logger.info("multi linear regression, success");
*/

        KmeansDao kmeansDao = (KmeansDao) context.getBean("kmeansDao");
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(new Date());
        String tableName1 = "t_kmeans_static";
        String tableName2 = "t_kmeans_cluster_static";
        //运行前插入日志表
        kmeansDao.updateLogTable(tableName1, today, false);
        kmeansDao.updateLogTable(tableName2, today, false);

        Calendar cal = Calendar.getInstance();
        cal.setTime(runDate);
        logger.info("first day of month, kmeans model start...");
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date bDate = cal.getTime();
        logger.info("timetype:month");
        kmeansDao.insertKmeans(building, bDate, 1);
        logger.info("timetype: month,success");
        logger.info("timetype:year");
        kmeansDao.insertKmeans(building, bDate, 2);
        logger.info("timetype:year, success");
        logger.info("timetype:history");
        kmeansDao.insertKmeans(building, bDate, 3);
        logger.info("timetype:history, success");

        //运行前插入日志表
        kmeansDao.updateLogTable(tableName1, today, true);
        kmeansDao.updateLogTable(tableName2, today, true);
    }
}
