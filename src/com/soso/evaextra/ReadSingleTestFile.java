package com.soso.evaextra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.soso.evaextra.model.DataBean;
import com.soso.evaextra.model.QueryBean;

public class ReadSingleTestFile {
	
	public static Map getAllData(File file){
		
		Map map = new HashMap<String, List>();
		
		List<DataBean> datas = new ArrayList<DataBean>();
		List<QueryBean> querys = new ArrayList<QueryBean>();
		
		BufferedReader reader = null;
		InputStream is = null;
		
		try {
			//以行为单位，每次读取一行
			is = new FileInputStream(file); 
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String tempString;
			String query = null;
			boolean flag = false;
			Date timetag;
			QueryBean queryBean;
			int i = 0;//区分时间的标志，以防时间相同引起主键冲突
 			while((tempString = reader.readLine()) != null){
 				if(tempString != null && !("".equals(tempString))){
 					if(tempString.startsWith("[{")){
 						query = tempString;
 						flag = true;
 						continue;
 					}
 					i++;
					//将每一行的数据拆分城对象
	 				DataBean bean = new DataBean();
	 				String[] strs = tempString.split(",");
	 				if(flag && strs[1].equals("T")){
 						try {
 							//给时间戳也赋值，这样就拿到了query对象
 							queryBean = new QueryBean();
 							bean.setTime(strs[0] + "_" + i);
 							bean.setName(strs[1]);
 		 					bean.setLat(Double.parseDouble(strs[3]));
 		 					bean.setLng(Double.parseDouble(strs[4]));
 		 					bean.setAcc(Double.parseDouble(strs[5]));
 		 					bean.setImei(strs[6]);
 		 					bean.setTime_imei(strs[0] + "_" + i+"_"+strs[6]);
 		 					queryBean.setQuery(query);
	 		 					queryBean.setTimetag(strs[0] + "_" + i);
	 		 					queryBean.setTime_imei(strs[0] + "_" + i+"_"+strs[6]);
 		 					querys.add(queryBean);
 		 					map.put("query", querys);
 		 					flag = false;
//	 		 				queryBean = null;
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
	 				}else{
	 					try {
							bean.setTime(strs[0] + "_" + i);
							bean.setName(strs[1]);
		 					bean.setLat(Double.parseDouble(strs[3]));
		 					bean.setLng(Double.parseDouble(strs[4]));
		 					bean.setAcc(Double.parseDouble(strs[5]));
		 					bean.setImei(strs[6]);
		 					bean.setTime_imei(strs[0] + "_" + i+"_"+strs[6]);
						} catch (Exception e) {
							e.printStackTrace();
						}
	 				}
 				datas.add(bean);
 				map.put("data" , datas);
 				}
			}
			//将数据集合存入数据库即可
			reader.close();
		}  catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
		return map;
	}
	
	
	public static String parseSingleTestFile(File file){
		
		Map map = getAllData(file);
		
		StringBuffer sb = new StringBuffer();
		List<DataBean> datas =  (List<DataBean>) map.get("data");
		for(DataBean data : datas){
			sb.append(data);
			sb.append("!");
		}
		sb.deleteCharAt(sb.length() -1 );
		
		sb.append("@@@");//增加分割符，将DataBean的数据和QueryBean的数据分开
		
		//获取querybean信息的数据
		List<QueryBean> querybeans = (List<QueryBean>) map.get("query");
		for(QueryBean queryBean : querybeans){
			sb.append(queryBean);
			sb.append("&&&");
		}
		
		sb.delete(sb.length()-3, sb.length());
		
		return sb.toString();
	}
	

}
