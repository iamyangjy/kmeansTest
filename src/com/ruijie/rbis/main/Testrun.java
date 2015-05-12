package com.ruijie.rbis.main;

import java.util.ArrayList;
import java.util.Random;

import com.ruijie.rbis.algorithm.Kmeans;

//import ncl.Matrix;

public class Testrun {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		double[][] items = new double[120][2];
		Random ran = new Random();
		for(int i = 0;i < items.length;i++){
			if(i % 3 == 0){
				for(int k = 0;k < items[i].length;k++){
					//items[i][k] = ran.nextInt(5);
					items[i][k] = ran.nextDouble();
				}
			}else if(i % 3 ==1){
				for(int k = 0;k < items[i].length;k++){
					//items[i][k] = 10+ran.nextInt(5);
					items[i][k] = 1+ran.nextDouble();
				}
			}else{
				for(int k = 0;k < items[i].length;k++){
					//items[i][k] = 20+ran.nextInt(5);
					items[i][k] = 2+ran.nextDouble();
				}
			}
		}
		
		for(int i = 0;i < items.length;i++){
			System.out.println(items[i][0] + "\t" + items[i][1]);
		}
		System.out.println("===");
		
		double[][] norItems = normalize(items);
		for(int i = 0;i < norItems.length;i++){
			System.out.println(norItems[i][0] + "\t" + norItems[i][1]);
		}
		System.out.println("kmeans 实现");
		
		/*FSDP km = new FSDP(norItems);
		km.init();
		km.setDc(0.1);
		km.assignCluster();*/
		
		Kmeans km = new Kmeans(items);
		int numClusters = 3;
		km.init(numClusters);
		km.trainEclidAvgCenter();
		
		ArrayList<ArrayList<Integer>> allClusters = km.getAllClusters();
		ArrayList<double[]> centers = km.getCenters();

		System.out.println("输出簇");
		for(ArrayList<Integer> cluster:allClusters){
			for(int i = 0;i < cluster.size();i++){
				System.out.println(cluster.get(i));
			}
			System.out.println("=============");
		}
		
		for(int i = 0;i < allClusters.size();i++){
			double[] center = centers.get(i);
			System.out.println(center[0] + "," + center[1]);
		}

		System.out.println("输出簇及其对应的簇ID");
		for(double[] dd: items){
			for(double d: dd){
				System.out.print(d + "\t");
			}
			System.out.println("簇ID:" + km.getClusterID_EculidDist(dd));
		}
	}
	
	public static double[][] normalize(double[][] array){
		double[] max = new double[array[0].length];
		double[] min = new double[array[0].length];
		for(int i = 0;i < max.length;i++){
			max[i] = -Double.MAX_VALUE;
			min[i] = Double.MAX_VALUE;
		}
		for(int i = 0;i < array.length;i++){
			for(int k = 0;k < array[i].length;k++){
				if(array[i][k] > max[k])max[k] = array[i][k];
				if(array[i][k] < min[k])min[k] = array[i][k];
			}
		}
		double[][] norArr = new double[array.length][];
		for(int i = 0;i < norArr.length;i++){
			norArr[i] = new double[array[i].length];
			for(int k = 0;k < norArr[i].length;k++){
				norArr[i][k] = (array[i][k] - min[k]) / (max[k] - min[k]);
			}
		}
		return norArr;
	}

}
