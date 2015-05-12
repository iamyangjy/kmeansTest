package com.ruijie.rbis.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.ruijie.rbis.algorithm.Pca;
import com.ruijie.rbis.pojo.pca.Factor;
import com.ruijie.rbis.pojo.pca.Weight;
import com.ruijie.rbis.util.AlgDate;

/**
 * Created by OA on 2014/12/23.
 */
public class PcaDao {

    private static Logger logger = LogManager.getLogger(Pca.class.getName());

    private JdbcTemplate jdbcTemplate;
    //历史记录数
    private int historyDateLine = 35;
    //日期格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setHistoryDateLine(int historyDateLine) {
        this.historyDateLine = historyDateLine;
    }

    public  List<Weight> getMultiWeightOfFactor(int buildingId, Date startDate, Date endDate){
        List<Date> dateList = AlgDate.getBetweenDay(startDate, endDate);
        List<Weight> weights = new ArrayList<Weight>();
        for(Date d: dateList){
           Weight weight = getWeightOfFactor(buildingId, d);
           weights.add(weight);
        }
        return weights;
    }

    public Weight getWeightOfFactor(int buildingId, Date apTime){
         //获取Raw矩阵
        List<Factor> factors = getRawMatrix(buildingId, apTime);
        //原始无数据的话，直接返回
        if(factors.size()==0){
            Weight weight = new Weight();
            weight.setBuildingId(buildingId);
            weight.setApTime(apTime);
            return weight;
        }
        //转化为二维数组,行数和列数为定值
        double[][] rawMatrix = new double[historyDateLine][7];
        //初始化数组,全部为0
/*        for(int i=0; i<historyDateLine; i++){
            for(int j=0; j<7; j++){
                rawMatrix[i][j] = 0;
            }
        }*/
        int i = 0;
        for(Factor f:factors){
            rawMatrix[i][0] = f.getAllNum();
            rawMatrix[i][1] = f.getEnterNum();
            rawMatrix[i][2] = f.getStayNum();
            rawMatrix[i][3] = f.getAvgEnterTime();
            rawMatrix[i][4] = f.getAvgStayTime();
            rawMatrix[i][5] = f.getNewNum();
            rawMatrix[i][6] = f.getOldNum();
            i++;
        }
        //实例化pca算法接口
        Pca pca = new Pca();
        //原始矩阵标准化
        double[][] standard = pca.Standardlizer(rawMatrix);
        //生成协方差矩阵
        double[][] association = pca.CoefficientOfAssociation(standard);
        //获取特征值
        double[][] flagValue = pca.FlagValue(association);
        //获取特征向量
        double[][] flagVector = pca.FlagVector(association);
        //阈值内所组成的下标值
        int[]index = pca.SelectPrincipalComponent(flagValue);
        //获取特征值总和
        double sum = 0.0;
        for(int id:index){
            sum += flagValue[id][id];
        }
        //获取权重值
        double[] weightValue = new double[7];
        for(int id:index){
            for(int w=0; w<7; w++){
                weightValue[w] += flagValue[id][id]/sum * flagVector[w][id];
            }
        }
        //返回权重值
        Weight weight = new Weight();
        weight.setBuildingId(buildingId);
        weight.setApTime(apTime);
        weight.setAllNum(weightValue[0]);
        weight.setEnterNum(weightValue[1]);
        weight.setStayNum(weightValue[2]);
        weight.setAvgEnterTime(weightValue[3]);
        weight.setAvgStayTime(weightValue[4]);
        weight.setNewNum(weightValue[5]);
        weight.setOldNum(weightValue[6]);

        return weight;
    }

    //获取原始矩阵，当天起前HistoryDateLine的数据
    private List<Factor> getRawMatrix(int buildingId, Date apTime){
        //获取当前时间前36到前1天数据作为样本数据。
        Date startDate = AlgDate.getDateRelative(apTime, -1 * historyDateLine);
        Date endDate = AlgDate.getDateRelative(apTime, -1);
        //转化为String
        String startDay = sdf.format(startDate);
        String endDay = sdf.format(endDate);
        logger.info("rawData get, start: " + startDay + ", end: " + endDay);

        String sql = "select building_id, all_num, enter_num, stay_num, enter_time/60.0 as avg_enter_time, " +
                " stay_time/60.0 as avg_stay_time, new_num, old_num " +
                " from t_mall_static_day where building_id=? and aptime>=? and aptime<=? and is_shop=1 ";
        List<Factor> factors = jdbcTemplate.query(sql, new Object[]{buildingId, startDay, endDay}, new RowMapper<Factor>() {
            @Override
            public Factor mapRow(ResultSet resultSet, int i) throws SQLException {
                Factor factor = new Factor();
                logger.info("buildingId" + resultSet.getInt("building_id"));
                factor.setBuildingId(resultSet.getInt("building_id"));
                factor.setAllNum(resultSet.getInt("all_num"));
                logger.info("all_num" + resultSet.getInt("all_num"));
                factor.setEnterNum(resultSet.getInt("enter_num"));
                factor.setStayNum(resultSet.getInt("stay_num"));
                factor.setAvgEnterTime(resultSet.getDouble("avg_enter_time"));
                factor.setAvgStayTime(resultSet.getDouble("avg_stay_time"));
                factor.setNewNum(resultSet.getInt("new_num"));
                factor.setOldNum(resultSet.getInt("old_num"));
                return  factor;
            }
        });
        return factors;
    }

    public void insertWeightOfFactor(int buildingId, Date apTime){
        logger.info("get factor");
        //获取系数
        Weight weight = getWeightOfFactor(buildingId, apTime);
        logger.info("get factor success");
        //如果没有存在的话,调用pca接口计算
/*        String insertSql = "replace into t_principal_weight(building_id, aptime, all_num, enter_num, stay_num, " +
                " avg_enter_time, avg_stay_time, new_num, old_num) " +
                " values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql, new Object[]{weight.getBuildingId(), weight.getApTime(),
            weight.getAllNum(), weight.getEnterNum(), weight.getStayNum(), weight.getAvgEnterTime(),
            weight.getAvgStayTime(), weight.getNewNum(), weight.getOldNum()});
 */
        String insertSql = "replace into t_factor_weight(building_id, aptime, count_type, all_num, enter_num, stay_num, " +
               " avg_enter_time, avg_stay_time, new_num, old_num) " +
               " values(?,?, ?, ?, ?, ?, ?, ?, ?, ?)";

       jdbcTemplate.update(insertSql, new Object[]{weight.getBuildingId(), weight.getApTime(),
           2, weight.getAllNum(), weight.getEnterNum(), weight.getStayNum(), weight.getAvgEnterTime(),
           weight.getAvgStayTime(), weight.getNewNum(), weight.getOldNum()});

    }


}
