package jp.alhinc.hotta_jun.calculate_sales;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MethodList {
	
	
	//出力ファイルを作成するためのメソッド
	//HashMap codeはコード、名前
	//HashMap sumはコード、合計金額
	
	static boolean outBC(String filePath, HashMap<String,String> code, HashMap<String,Long> sum){
		String crlf = System.getProperty("line.separator");
		//商品売上金額の合計のマップをリストに格納
        List<Map.Entry<String,Long>> Entries = 
              new ArrayList<Map.Entry<String,Long>>(sum.entrySet());
        Collections.sort(Entries, new Comparator<Map.Entry<String,Long>>() { 
            @Override
            public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
                return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
            }
        });
        
		BufferedWriter br = null;
		try{
			File file = new File(filePath);
			FileWriter fw = new FileWriter(file);
			br = new BufferedWriter(fw);
			for(Entry<String,Long> s : Entries){
				br.write(s.getKey()+","+code.get(s.getKey())+","+s.getValue()+crlf);
			}
		}catch(IOException e){
			return false;
		}finally{
			try{
				if(br != null){
					br.close();
				}else{
					return false;
				}
			}catch(IOException e){
				return false;
			}
		}
		return true;
	}
	
}
