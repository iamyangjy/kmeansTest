package com.ruijie.rbis.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Kmeans {

	//所有items
	private double[][] m_allItems;
	//簇的个数
	private int m_numClusters;
	//中心点的特侦值
	private ArrayList<double[]> m_centers;
	//各个簇的中心点
	private ArrayList<ArrayList<Integer>> m_allClusters;
	//
	private HashMap<Integer,Integer> m_itemID2clusterID;

	//距离是否运用对数函数
	private boolean m_logDist = false;
	
	public Kmeans(double[][] items){
		m_allItems = items.clone();
		m_numClusters = 10;
		m_centers = new ArrayList<double[]>(m_numClusters);
		m_allClusters = new ArrayList<ArrayList<Integer>>(m_numClusters);
		for(int i = 0;i < m_numClusters;i++){
			ArrayList<Integer> itemList = new ArrayList<Integer>();
			m_allClusters.add(i, itemList);
		}
		m_itemID2clusterID = new HashMap<Integer,Integer>();
		m_logDist = false;
	}
	
	public void setNumClusters(int k){
		m_numClusters = k;
		m_centers = new ArrayList<double[]>(m_numClusters);
		m_allClusters = new ArrayList<ArrayList<Integer>>(m_numClusters);
		for(int i = 0;i < m_numClusters;i++){
			ArrayList<Integer> itemList = new ArrayList<Integer>();
			m_allClusters.add(i, itemList);
		}
	}
	
	public ArrayList<ArrayList<Integer>> getAllClusters(){
		return m_allClusters;
	}
	
	public ArrayList<double[]> getCenters(){
		return m_centers;
	}
	
	public void setLogDist(boolean sta){
		m_logDist = sta;
	}
	public boolean getLogDist(){
		return m_logDist;
	}
	
	public void init(int k){
		setNumClusters(k);
		Random ran = new Random();
		HashMap<Integer,Integer> tmpAddedIdx = new HashMap<Integer,Integer>();
		int centerIdx = ran.nextInt(m_allItems.length);
		double[] tmpcenter = m_allItems[centerIdx].clone();
		m_centers.add(tmpcenter);
		tmpAddedIdx.put(centerIdx, 0);
		while(m_centers.size() < k){
			//新的中心点添加标准：
			//任选100个点，选与已添加的所有的中心点的距离和最大的点
			double maxDis = 0;
			for(int i = 0;i < m_allItems.length/2 && i < m_allItems.length-tmpAddedIdx.size();i++){
				int tmpIdx;
				do{
					tmpIdx = ran.nextInt(m_allItems.length);
				}while(tmpAddedIdx.containsKey(tmpIdx));
				double[] centerCandicate = m_allItems[tmpIdx].clone();
				//计算选出来的点与所有已有的中心点的最小距离，更新最大值maxDis以及centerIdx
				double minDist = Double.MAX_VALUE;
				for(double[] center:m_centers){
					double tmpDist = getEuclideanDistance(centerCandicate,center);
					if(tmpDist < minDist)minDist = tmpDist;
				}
				if(minDist > maxDis){
					maxDis = minDist;
					centerIdx = tmpIdx;
				}
			}
			tmpcenter = m_allItems[centerIdx].clone();
			m_centers.add(tmpcenter);
			tmpAddedIdx.put(centerIdx, 0);
		}
		/**
		 * 划分各个点所属的簇，初始化m_itemID2clusterID和m_allClusters
		 */
		for(int itemID = 0;itemID < m_allItems.length;itemID++){
			double[] currentItem = m_allItems[itemID].clone();
			int clusterID = getClusterID_EculidDist(currentItem);
			if(clusterID == -1){
				System.out.println(m_centers.size());
			}
			m_itemID2clusterID.put(itemID, clusterID);
			m_allClusters.get(clusterID).add(itemID);
			/*ArrayList<Integer> tmplist = m_allClusters.remove(clusterID);
			tmplist.add(itemID);
			m_allClusters.add(clusterID, tmplist);*/
			
		}
	}
	
	public void updateCentersByAvgCoordinate(){
		for(int i = 0;i < m_allClusters.size();i++){
			ArrayList<Integer> cluster = m_allClusters.get(i);
			m_centers.remove(i);
			m_centers.add(i,calCenterByAvgCoordinate(cluster));
		}
	}
	
	//返回中心点，该中心点与item的距离最小
	public int getClusterID_EculidDist(double[] item){
		double minDist = Double.MAX_VALUE;
		int clusterID = -1;
		for(int idx = 0;idx < m_centers.size();idx++){
			double dist = getEuclideanDistance(m_centers.get(idx),item);
			if(dist < minDist){
				minDist = dist;
				clusterID = idx;
			}
		}
		return clusterID;
	}
	
	public void trainEclidAvgCenter(){
		//int count = 0;
		while (true){
			/**
			 * 1. 计算各个簇的中心点，更新m_centers
			 * 2. 划分各个点所属类，更新m_itemID2clusterID和m_allClusters，所有点的簇都没有变化，则退出while
			 */
			//System.out.println(++count);
			updateCentersByAvgCoordinate();
			
			boolean changed = false;
			for(int itemID:m_itemID2clusterID.keySet()){
				int clusterID = m_itemID2clusterID.get(itemID);
				int newClusterID = getClusterID_EculidDist(m_allItems[itemID]);
				if(newClusterID != clusterID){
					changed = true;
					m_itemID2clusterID.put(itemID, newClusterID);
				}
			}
			if(!changed)break;
			for(int clusterID = 0;clusterID < m_allClusters.size();clusterID++){
				m_allClusters.get(clusterID).clear();
			}
			for(int itemID:m_itemID2clusterID.keySet()){
				int clusterID = m_itemID2clusterID.get(itemID);
				m_allClusters.get(clusterID).add(itemID);
			}
		}
		
	}
	
	/** @param 簇对应的item的id，以及所有item(为一个矩阵)
	 *  @return 该簇的中心点
	 */
	public double[] calCenterByAvgCoordinate(ArrayList<Integer> items_idx){
		int num = m_allItems[0].length;
		double[] center = new double[num];
		for(int i = 0;i < items_idx.size();i++){
			int idx = items_idx.get(i);
			double[] item = m_allItems[idx];
			for(int k = 0;k < item.length;k++){
				center[k] += item[k];
			}
		}
		for(int i = 0;i < center.length;i++){
			center[i] /= (double)items_idx.size();
		}
		return center;
	}
	
	public static double[] minus(double[] array1,double[] array2){
		if(array1.length != array2.length){
			System.err.println("Matris Minus: Arrays are not matched!\n");
			System.exit(1);
		}
		double[] arrayR = new double[array1.length];
		for(int i = 0;i < array1.length;i++){
			arrayR[i] = array1[i] - array2[i];
		}
		return arrayR;
	}	
	public static double secondNormalForm(double[] array){
		double sum = 0;
		for(int i = 0;i < array.length;i++){
			sum += array[i] * array[i];
		}
		return (double)Math.sqrt(sum);
	}
	public  double getEuclideanDistance(double[] arrayOne,double[] arrayTwo){
		double[] minusMat = minus(arrayOne, arrayTwo);
		double dist = secondNormalForm(minusMat);
		if(m_logDist)dist = Math.log(dist);
		return dist;
	}
	
	
}
