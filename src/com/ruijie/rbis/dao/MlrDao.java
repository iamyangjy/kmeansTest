package com.ruijie.rbis.dao;

/**
 * Created by OA on 2015/1/5.
 */

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.ruijie.rbis.util.AlgDate;
import com.ruijie.rbis.pojo.mlr.Factor;
import com.ruijie.rbis.pojo.mlr.Weight;
import com.ruijie.rbis.algorithm.Mlr;

public class MlrDao {
    private static Logger logger = LogManager.getLogger(MlrDao.class.getName());

    private JdbcTemplate jdbcTemplate;

    private int historyDateLine = 35;

    //日期格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setHistoryDateLine(int historyDateLine) {
        this.historyDateLine = historyDateLine;
    }

    public  void insertWeightOfFactor(int buildingId, Date apTime){
        Weight weight = getWeightOfFactor(buildingId, apTime);
         //如果没有存在的话,调用pca接口计算
        String insertSql = "replace into t_factor_weight(building_id, aptime, count_type, all_num, enter_num, stay_num, " +
                " avg_enter_time, avg_stay_time, new_num, old_num, cof, r_square) " +
                " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        jdbcTemplate.update(insertSql, new Object[]{weight.getBuildingId(), weight.getApTime(), 1, weight.getAllNum(), weight.getEnterNum(),
                weight.getStayNum(), weight.getAvgEnterTime(), weight.getAvgStayTime(), weight.getNewNum(), weight.getOldNum(),
                weight.getCof(), weight.getrSquare()});
    }

    public Weight getWeightOfFactor(int buildingId, Date apTime){
        //获取Raw矩阵
        List<Factor> factors = getRawData(buildingId, apTime);
        //List<Double> sales = getSale(buildingId, apTime);
        //如果数据库后端没有数据的话(当前时间的前35天内)
        if(factors.size() == 0){
            //System.out.println("没有数据");
            Weight weight = new Weight();
            weight.setBuildingId(buildingId);
            weight.setApTime(apTime);
            return weight;
        }
        //如果有数据，进行多元回归计算
        //转化为二维数组,行数和列数为定值
        //System.out.println("数据:" + factors.size());
        double[][] rawMatrix = new double[factors.size()][8];
        double[] rawSale = new double[factors.size()];
        //变量矩阵
        int i = 0;
        for(Factor f:factors){
            //自变量
            //常数项
            rawMatrix[i][0] = 1.0 ;
            rawMatrix[i][1] = f.getAllNum();
            rawMatrix[i][2] = f.getEnterNum();
            rawMatrix[i][3] = f.getStayNum();
            rawMatrix[i][4] = f.getAvgEnterTime();
            rawMatrix[i][5] = f.getAvgStayTime();
            rawMatrix[i][6] = f.getNewNum();
            rawMatrix[i][7] = f.getOldNum();
            //因变量
            rawSale[i] = f.getSale();
            i++;
        }

        //多元线性回归
        Mlr mlr = new Mlr(rawMatrix, rawSale);
        //多元系数
        Weight weight = new Weight();
        weight.setBuildingId(buildingId);
        weight.setApTime(apTime);
        weight.setCof(mlr.beta(0));
        //System.out.println("常数项系数: " + mlr.beta(0));
        weight.setAllNum(mlr.beta(1));
        //System.out.println("总客流系数: " + mlr.beta(1));
        weight.setEnterNum(mlr.beta(2));
        //System.out.println("进店客流系数: " + mlr.beta(2));
        weight.setStayNum(mlr.beta(3));
        //System.out.println("驻留客流系数: " + mlr.beta(3));
        weight.setAvgEnterTime(mlr.beta(4));
        //System.out.println("平均进店时长系数: " + mlr.beta(4));
        weight.setAvgStayTime(mlr.beta(5));
        //System.out.println("驻留时长系数: " + mlr.beta(5));
        weight.setNewNum(mlr.beta(6));
        //System.out.println("新顾客系数: " + mlr.beta(6));
        weight.setOldNum(mlr.beta(7));
        //System.out.println("老顾客系数: " + mlr.beta(7));
        weight.setrSquare(mlr.R2());
        //System.out.println("评价系数: " + mlr.R2());

        return  weight;
    }

    //获取原始矩阵，当天起前HistoryDateLine的数据
    private List<Factor> getRawData(int buildingId, Date apTime){
        //获取当前时间前36到前1天数据作为样本数据。
        Date startDate = AlgDate.getDateRelative(apTime, -1 * historyDateLine);
        Date endDate = AlgDate.getDateRelative(apTime, -1);
        //转化为String
        String startDay = sdf.format(startDate);
        String endDay = sdf.format(endDate);
        logger.info("rawData get, start: " + startDay + ", end: " + endDay);

        String sql = "SELECT t1.building_id as building_id, t1.aptime as aptime, t1.sale as sale, t2.all_num as all_num, t2.enter_num as enter_num, " +
                " t2.stay_num as stay_num, t2.enter_time/60.0 as avg_enter_time, t2.stay_time/60.0 as avg_stay_time, t2.new_num as new_num, t2.old_num as old_num " +
                " from (SELECT building_id, aptime, sale from t_mall_sale where building_id=? and aptime>=? and aptime<=?) t1 " +
                " LEFT JOIN t_mall_static_day t2 on t1.building_id=t2.building_id and t1.aptime=t2.aptime and t2.is_shop=1 " +
                " ORDER BY t1.aptime desc limit ?";
        List<Factor> factors = jdbcTemplate.query(sql, new Object[]{buildingId, startDay, endDay, historyDateLine}, new RowMapper<Factor>() {
            @Override
            public Factor mapRow(ResultSet resultSet, int i) throws SQLException {
                Factor factor = new Factor();
                factor.setBuildingId(resultSet.getInt("building_id"));
                factor.setAllNum(resultSet.getInt("all_num"));
                factor.setEnterNum(resultSet.getInt("enter_num"));
                factor.setStayNum(resultSet.getInt("stay_num"));
                factor.setAvgEnterTime(resultSet.getDouble("avg_enter_time"));
                factor.setAvgStayTime(resultSet.getDouble("avg_stay_time"));
                factor.setNewNum(resultSet.getInt("new_num"));
                factor.setOldNum(resultSet.getInt("old_num"));
                factor.setSale(resultSet.getDouble("sale"));
                return  factor;
            }
        });
        return factors;
    }

    //获取原始矩阵，当天起前HistoryDateLine的数据
    private List<Factor> getRawMatrix(int buildingId, Date apTime){
        //获取当前时间前36到前1天数据作为样本数据。
        Date startDate = AlgDate.getDateRelative(apTime, -1 * historyDateLine);
        Date endDate = AlgDate.getDateRelative(apTime, -1);
        //转化为String
        String startDay = sdf.format(startDate);
        String endDay = sdf.format(endDate);
        //System.out.println("开始时间" + startDay + "结束时间" + endDay);

        String sql = "select building_id, all_num, enter_num, stay_num, enter_time/60.0 as avg_enter_time, " +
                " stay_time/60.0 as avg_stay_time, new_num, old_num " +
                " from t_mall_static_day where building_id=? and aptime>=? and aptime<=? and is_shop=1 order by aptime";
        List<Factor> factors = jdbcTemplate.query(sql, new Object[]{buildingId, startDay, endDay}, new RowMapper<Factor>() {
            @Override
            public Factor mapRow(ResultSet resultSet, int i) throws SQLException {
                Factor factor = new Factor();
                factor.setBuildingId(resultSet.getInt("building_id"));
                factor.setAllNum(resultSet.getInt("all_num"));
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

    private List<Double> getSale(int buildingId, Date apTime){
        //获取当前时间前36到前1天数据作为样本数据。
        Date startDate = AlgDate.getDateRelative(apTime, -1 * historyDateLine);
        Date endDate = AlgDate.getDateRelative(apTime, -1);
        //转化为String
        String startDay = sdf.format(startDate);
        String endDay = sdf.format(endDate);
        //System.out.println("开始时间" + startDay + "结束时间" + endDay);

        String sql = "select building_id,  aptime, sale from t_mall_sale" +
                " where building_id=? and aptime>=? and aptime<=? order by aptime";
        List<Double> sales = jdbcTemplate.query(sql, new Object[]{buildingId, startDate, endDate}, new RowMapper<Double>() {
            @Override
            public Double mapRow(ResultSet resultSet, int i) throws SQLException {
                Double sale = new Double(0.0);
                sale = resultSet.getDouble("sale");
                return sale;
            }
        });
        return sales;
    }
}