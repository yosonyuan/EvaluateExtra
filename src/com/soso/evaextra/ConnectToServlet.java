package com.soso.evaextra;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectToServlet {
	
public static String postToServlet(String json) throws Exception{
		
//		URL urlPath = new URL("http://localhost/data_monotor/dataDeal");
//		String url = "http://10.171.89.14:8080/data_monitor/dataDeal";
	//http://111.161.52.33:10001/data_monitor/index.jsp
		String url = "http://111.161.52.33:10001/data_monitor/dataDeal";
	
		URL urlPath = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlPath.openConnection();
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		conn.setUseCaches(false); 
		conn.setInstanceFollowRedirects(true); 
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
		conn.connect();
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
		out.write(json);
		out.flush();
		out.close();
		
		//获取从服务端返回的状态以及数据
		int code = conn.getResponseCode();
		if(code == 200){
			InputStream reader = conn.getInputStream();
			int len;
			byte[] buff = new byte[1*1024];
			if((len = reader.read(buff)) != -1){
				byte[] result = new byte[len];
				System.arraycopy(buff, 0, result, 0, len);
				reader.close();
				return new String(result , "utf-8");
			}
		}
		return null;
	}
	

}
