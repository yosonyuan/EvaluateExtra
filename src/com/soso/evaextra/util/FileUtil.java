package com.soso.evaextra.util;

import java.io.File;
import java.io.FileWriter;

import com.soso.evaextra.model.Result;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
/**
 * 
* @ClassName: FileUtil 
* @Description: 这个类没被用到  暂时保留
* @author kejiwang@tencent.com
* @date 2014-11-28 下午4:07:46 
*
 */
public class FileUtil {

	private Context mContext;
	private SharedPreferences preferences;

	public FileUtil(Context mContext) {
		super();
		this.mContext = mContext;
		
		preferences=mContext.getSharedPreferences("StartTime",Activity.MODE_PRIVATE);
		
	}

	public void write2Sd(String filename,Result result)
	{
		
		String content=null;
		if(result.getData().equals("0"))
		{
			
			content=result.getLat()+"\t"+result.getLng()+"\t"+TimeUtil.millsToStr(result.getTime())+"\t"+result.getRadius()+"\t"+result.getOffSet()+"\n";
		}
		else {
			System.out.println("写入文件offset----------"+result.getOffSet());
			content=result.getLat()+"\t"+result.getLng()+"\t"+TimeUtil.millsToStr(result.getTime())+"\t"+result.getRadius()+"\t"+result.getData()+"\t"+result.getOffSet()+"\n";
		}
		
		String path=Environment.getExternalStorageDirectory()+"/MapEva/";
		
		File destDir=new File(path);
		 if (!destDir.exists()) {
			   destDir.mkdirs();
			  }
		 
		String timeFlag=preferences.getString("starttime","");
		
		String fName=timeFlag+"_"+filename;
		
		File file=new File(path+fName);
		
		try {
			
			if(!file.exists())
			{
				file.createNewFile();
			}
			
		    //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
		    FileWriter writer = new FileWriter(file, true);
		    writer.write(content);
		    writer.close();
		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write2Sd(String filename,String...args)
	{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<args.length;i++)
		{
			sb.append(args[i]+"\t");
		}
		
		String path=Environment.getExternalStorageDirectory()+"/MapEva/";
		
		File destDir=new File(path);
		 if (!destDir.exists()) {
			   destDir.mkdirs();
			  }
		 
		
		File file=new File(path+filename);
		
		try {
			
			if(!file.exists())
			{
				file.createNewFile();
			}
			
		    //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
		    FileWriter writer = new FileWriter(file, true);
		    writer.write(sb.toString()+"\n");
		    writer.close();
		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
