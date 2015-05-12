package com.ruijie.rbis.dao;

/**
 * Created by OA on 2015/1/8.
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ruijie.rbis.algorithm.Kmeans;
import com.ruijie.rbis.pojo.kmeans.Factor;

public class KmeansDao {

    Logger logger = LogManager.getLogger(KmeansDao.class.getName());

    private JdbcTemplate jdbcTemplate;
    //聚类的簇数
    private int numCluster = 3;

    //日期格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    private SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setNumCluster(int numCluster) {
        this.numCluster = numCluster;
    }

    public  void insertKmeans(final int buildingId, Date apTime, final int timetype){

        //格式化插入的时间格式
        //月的格式为yyyy-MM,年的格式yyyy，历史的格式0000
        String tmpDate = null;
        //timetype=1 为month
        if(timetype==1){
            tmpDate = sdf.format(apTime);
        //timetype=2 为year
        }else if(timetype==2){
            tmpDate = sdfYear.format(apTime);
        }else{
            tmpDate ="0000";
        }
        final String runDate = tmpDate;

        final List<Factor> factors = getKmeansInfo(buildingId, sdf.format(apTime), timetype);

        //插入每个mac地址所属簇的信息,t_kmeans_static
        String sql = "replace into t_kmeans_static(building_id, aptime, mac, cluster_id, visit_frequency, stay_time, last_vf, " +
                " store_vf, store_st, wifi_duration, device_model) " +
                "values(?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Factor factor = factors.get(i);
                preparedStatement.setInt(1, buildingId);
                preparedStatement.setString(2, runDate);
                preparedStatement.setString(3, factor.getMac());
                preparedStatement.setInt(4, factor.getClusterId());
                preparedStatement.setDouble(5, factor.getVisitFrequency());
                preparedStatement.setDouble(6, factor.getStayTime());
                preparedStatement.setDouble(7, factor.getLastVf());
                preparedStatement.setDouble(8, factor.getStoreVisitFrequency());
                preparedStatement.setDouble(9, factor.getStoreStayTime());
                preparedStatement.setDouble(10, factor.getWifiDuration());
                preparedStatement.setString(11, factor.getDeviceModel());
            }

            @Override
            public int getBatchSize() {
                return factors.size();
            }
        });

        //插入每个簇的信息
        //String deleteSql = "truncate t_kmeans_cluster_static";
        //jdbcTemplate.execute("truncate t_kmeans_cluster_static");

/*        String clusterSql = "replace into t_kmeans_cluster_static SELECT building_id, aptime, time_type, cluster_id, count(*) as mac_num, " +
                " sum(visit_frequency) as visit_frequency, sum(stay_time) as stay_time, " +
                "sum(last_vf) as last_vf " +
                "from t_kmeans_static WHERE building_id=? and aptime=? and time_type=? GROUP BY cluster_id";
 */
        String clusterSql = "replace into t_kmeans_cluster_static SELECT building_id, aptime, cluster_id, count(*) as mac_num ,min(visit_frequency) as visit_frequency_min, " +
                " ceil(sum(visit_frequency)/count(*)) as visit_frequency_avg, max(visit_frequency) as visit_frequency_max,min(stay_time/visit_frequency) as stay_time_min, " +
                " sum(stay_time)/sum(visit_frequency) as stay_time_avg, max(stay_time/visit_frequency) as stay_time_max ,min(last_vf) as last_vf_min, " +
                " ceil(sum(last_vf)/count(*)) as last_vf_avg, max(last_vf) as last_vf_max ,min(store_vf) as store_vf_min, ceil(sum(store_vf)/count(*)) as store_vf_avg, " +
                " max(store_vf) as store_vf_max ,min(store_st/store_vf) as store_st_min, sum(store_st)/sum(store_vf) as store_st_avg, " +
                " max(store_st/store_vf) as store_st_max ,min(wifi_duration/visit_frequency) as wifi_duration_min, " +
                " sum(wifi_duration)/sum(visit_frequency) as wifi_duration_avg, max(wifi_duration/visit_frequency) as wifi_duration_max " +
                " from t_kmeans_static WHERE building_id=? and aptime=? GROUP BY cluster_id";
        jdbcTemplate.update(clusterSql, new Object[]{buildingId, runDate});

    }

    private List<Factor> getKmeansInfo(int buildingId, String apTime, int timetype){

        List<Factor> factors = getRawData(buildingId, apTime, timetype);
        //将数据写入数组中，
        //String[] mac = new String[factors.size()];
        //增加店铺进入总次数和店铺停留总时长
        double[][] indexInfo = new double[factors.size()][5];
        int i = 0;
        for(Factor f: factors){
            /*System.out.println("mac:" + f.getMac() + "|visit:" + f.getVisitFrequency() + "|staytime:" + f.getStayTime()
            + "|wifi:" + f.getWifiDuration());*/
            //mac[i] = f.getMac();
            indexInfo[i][0] = f.getVisitFrequency();
            indexInfo[i][1] = f.getStayTime();
            indexInfo[i][2] = f.getLastVf();
            //增加店铺进入总次数和店铺停留总时长
            indexInfo[i][3] = f.getStoreVisitFrequency();
            indexInfo[i][4] = f.getStayTime();
            i++;
        }
        //传入kmeans接口
        Kmeans km = new Kmeans(indexInfo);
        km.init(numCluster);
        km.trainEclidAvgCenter();
        //获取cluster_id
        i = 0;
        for(Factor f: factors){
            f.setClusterId(km.getClusterID_EculidDist(indexInfo[i]));
            i++;
            /*System.out.println("mac:" + f.getMac() + "|visit:" + f.getVisitFrequency() + "|staytime:" + f.getStayTime()
                    + "|wifi:" + f.getWifiDuration() + "|cluster" + f.getClusterId());*/
        }
        //由于kmeans算法的cluster_id是随机的。而数据中的cluster_id是与忠诚度的高低所对应，
        //因此需要替换cluster_id为真正的id。根据每个簇的平均驻留时长进行判断
        //建立二维数组保存cluster_id的人数和每个mac的驻留的时长
        double[][] items = new double[][]{
                {0.0, 0.0},
                {0.0, 0.0},
                {0.0, 0.0},
        };
        for(Factor f: factors){
            items[f.getClusterId()][0] += 1;
            items[f.getClusterId()][1] += f.getStayTime();
        }

        //使用两个list将原始的cluster_id对应到正确的id号，并保存为map形式
        //2表示高忠诚度，1表示中忠诚度，0表示低忠诚度度，
        List<Double> tmpList = new ArrayList<Double>();
        List<Double> tmpListSort = new ArrayList<Double>();
        for (int p = 0; p < items.length; p++) {
            tmpList.add(p, items[p][1] / items[p][0]);
            tmpListSort.add(p, items[p][1] / items[p][0]);
        }
        Collections.sort(tmpListSort);

        HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
        for(int j=0; j< tmpListSort.size(); j++){
            idMap.put(tmpList.indexOf(tmpListSort.get(j)), j);
            //System.out.println(tmpList.indexOf(tmpListSort.get(j)) + "to" + j);
        }
        //重新设置cluster_id
        for(Factor f: factors){
           f.setClusterId(idMap.get(f.getClusterId()));
        }

        return  factors;
    }

    private List<Factor> getRawData(int buildingId, String apTime, int timetype){

        String sql = "";
        String apTimeArg = "";
        if(timetype==1){
            //月
/*            sql = "SELECT t.mac as mac, t.visit_frequency as visit_frequency, t.total_stay as stay_time, 0 as last_vf, t.device_model, 0 as wifi_duration, " +
                    " ifnull((SELECT sum(visit_frequency) from t_customer_figure where building_id= ? and aptime= \"" + apTime + "\" and store_id > 0 and mac = t.mac), 0) as store_visit_frequency, " +
                    " ifnull((SELECT sum(total_stay) from t_customer_figure where building_id= ? and aptime= \"" + apTime + "\" and store_id > 0 and mac = t.mac), 0) as store_stay_time " +
                    " from (SELECT mac, device_model, visit_frequency, total_stay from t_customer_figure " +
                    " where building_id = ? and store_id = 0 and aptime = \"" + apTime + "\" ) as t";*/
             sql = "SELECT mac, visit_frequency, total_stay as stay_time, last_vf, wifi_duration, store_vf, store_st, device_model\n" +
                    "from t_customer_figure " +
                    "WHERE store_id=0 and aptime=? and building_id=?";
            apTimeArg = apTime + "-01";

        }else if(timetype==2){
/*             sql = "SELECT t.mac as mac, t.visit_frequency as visit_frequency, t.total_stay as stay_time, 0 as last_vf, t.device_model, 0 as wifi_duration, " +
                    " ifnull((SELECT sum(visit_frequency) from t_customer_figure where building_id= ? " +
                     " and aptime>=\"" + apTime.substring(0,5) + "00\"" + "and aptime<= \"" + apTime + "\" and store_id > 0 and mac = t.mac), 0) as store_visit_frequency, " +
                    " ifnull((SELECT sum(total_stay) from t_customer_figure where building_id= ? " +
                     " and aptime>=\"" + apTime.substring(0,5) + "00\"" + "and aptime= \"" + apTime + "\" and store_id > 0 and mac = t.mac), 0) as store_stay_time " +
                    " from (SELECT mac, device_model, visit_frequency, total_stay from t_customer_figure " +
                    " where building_id = ? and store_id = 0 and aptime = \"" + apTime + "\" ) as t";*/
            sql = "SELECT mac, visit_frequency, total_stay as stay_time, last_vf, wifi_duration, store_vf, store_st, device_model\n" +
                    "from t_customer_figure " +
                    "WHERE aptime=? and building_id=?";
            apTimeArg = apTime.substring(0,5) + "00-00";
        }else if(timetype==3){
            //历史
/*            sql = " SELECT t1.mac as mac, (t1.visit_frequency + t1.last_vf) as visit_frequency, (t1.total_stay + t1.last_ts) as stay_time, t1.last_vf as last_vf, t1.device_model as device_model, " +
                    " t1.wifi_duration as wifi_duration,\n" +
                    " ifnull((SELECT sum(visit_frequency) from t_customer_figure where building_id= ? and store_id > 0 and mac = t1.mac), 0) as store_visit_frequency, " +
                    " ifnull((SELECT sum(total_stay) from t_customer_figure where building_id= ? and store_id > 0 and mac = t1.mac), 0) as store_stay_time " +
                    " FROM " +
                    " (" +
                    " select " +
                    " mac, device_model, building_id, sum(visit_frequency) as visit_frequency, sum(total_stay) as total_stay, sum(wifi_duration) as wifi_duration, " +
                    " ifnull((SELECT visit_frequency from t_customer_figure where building_id = ? and aptime = \"" + apTime + "\" and store_id = 0 and mac = t.mac LIMIT 1), 0) as last_vf, " +
                    " ifnull((SELECT total_stay from t_customer_figure where building_id = " + buildingId + " and aptime = \"" + apTime + "\" and store_id = 0 and mac = t.mac LIMIT 1), 0) as last_ts " +
                    " from t_customer_figure t " +
                    " where building_id = " + buildingId + " and store_id = 0 and aptime < \"" + apTime + "\" GROUP BY mac " +
                    " ) as t1";*/
            sql = "SELECT mac, visit_frequency, total_stay as stay_time, last_vf, wifi_duration, store_vf, store_st, device_model\n" +
                    "from t_customer_figure " +
                    "WHERE aptime=? and building_id=?";
            apTimeArg = "0000-00-00";
        } else {
/*            sql = "SELECT mac, sum(visit_frequency) as visit_frequency, sum(total_stay) as total_stay, 0 as last_vf " +
                    "from t_customer_figure where building_id=? and building_id=? and store_id=0 GROUP BY mac";
 */
            sql = "SELECT mac, visit_frequency, total_stay as stay_time, last_vf, wifi_duration, store_vf, store_st, device_model\n" +
                    "from t_customer_figure " +
                    "WHERE store_id=0 and aptime=? and building_id=?";
            apTimeArg = apTime;
        }
        logger.info("aptime:" + apTimeArg + " building:" + buildingId);

        logger.info("sql running");
        List<Factor> factors = jdbcTemplate.query(sql, new Object[]{apTimeArg, buildingId}, new RowMapper<Factor>() {
            @Override
            public Factor mapRow(ResultSet resultSet, int i) throws SQLException {
                Factor factor = new Factor();
                factor.setMac(resultSet.getString("mac"));
                factor.setVisitFrequency(resultSet.getDouble("visit_frequency"));
                factor.setStayTime(resultSet.getDouble("stay_time"));
                factor.setLastVf(resultSet.getDouble("last_vf"));
                factor.setDeviceModel(resultSet.getString("device_model"));
                factor.setStoreVisitFrequency(resultSet.getDouble("store_vf"));
                factor.setStoreStayTime(resultSet.getDouble("store_st"));
                factor.setWifiDuration(resultSet.getDouble("wifi_duration"));
                return factor;
            }
        });
        logger.info("data get success");
        logger.info("size:" + factors.size());
        return factors;
    }

     //往日指标里插入数据，处于两个理由
    //1、记录jar是否运行，且是否运行成功
    //2、依赖的存储过程监测，并作出相应处理
    public void updateLogTable(String tableName, String date, boolean isupdate){
        String sql=null;
        if(isupdate){
            sql = " UPDATE t_pro_execute_log set run_state = 1, cost_time = TIME_TO_SEC(NOW()) - TIME_TO_SEC(start_time) " +
                    " WHERE pro_name = ? and refer_time = ? ORDER BY id DESC LIMIT 1";
        }else{
            sql="insert into t_pro_execute_log (pro_name, refer_time, start_time, cost_time, content, frequency, run_state) " +
                    " VALUES (?, ?, CURRENT_TIMESTAMP(), NULL, '顾客忠诚度分群', 'sometime', 0)";
        }

        jdbcTemplate.update(sql, new Object[]{tableName, date});
    }

}
