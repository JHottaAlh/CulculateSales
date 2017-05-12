package jp.alhinc.hotta_jun.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Process {		
	public static void main(String[] args){
		
		
		//コマンドライン引数が存在するかのチェック
		if(args.length == 0){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		if(args.length > 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		
		//コマンドライン引数の末尾にセパレータがあるかどうか		
		String cmdLine;
		if(args[0].matches("File.separator$")){
			cmdLine = args[0];
		}else{
			cmdLine = args[0]+File.separator;
		}
		
		//支店定義<店舗コード, 店舗名>
		HashMap<String, String> shop = new HashMap<String, String>();
		//商品定義<商品コード, 商品名>
		HashMap<String, String> item = new HashMap<String, String>();
		
		ArrayList<String> fileList = new ArrayList<String>();				//ディレクトリ内のファイルをすべて読み込む		
		ArrayList<String> rcdList = new ArrayList<String>();				//.rcdが含まれるものだけのリスト		
		ArrayList<Integer> fileSort = new ArrayList<Integer>();			//.rcdを除いたファイル名を数値として取得
		ArrayList<String> renbanList = new ArrayList<String>();		 	//連番だけに絞る
		
		HashMap<String, Long> shopSum = new HashMap<String, Long>();		//店舗コード,　合計金額
		HashMap<String, Long> itemSum = new HashMap<String,Long>();		//商品コード,　合計金額
				
		//支店定義ファイルを読み込む
		File branch = new File(cmdLine+"branch.lst");
		BufferedReader branchBr = null;
		try{
			if(branch.exists() == true){
				FileReader fl = new FileReader(branch);
				branchBr = new BufferedReader(fl);
				String s;	
				while((s = branchBr.readLine()) != null){
					String[] arr;
					arr = s.split(",");	
					int arrLength = arr[0].length();
					boolean judge;
					try{
						Integer.parseInt(arr[0]);
						judge = true;
					}catch(NumberFormatException e){
						judge = false;
					}
					if(arrLength == 3 && judge){							//支店コードが三桁かどうか、数値かどうか
						if(arr.length == 2){								//支店名がカンマ、改行を含まない
							shop.put(arr[0], arr[1]);
							shopSum.put(arr[0],0L);
						}else{
							System.out.println("支店定義ファイルのフォーマットが不正です");
							branchBr.close();
							return;
						}
					}else{
						System.out.println("支店定義ファイルのフォーマットが不正です");
						branchBr.close();
						return;
					}
				}		
			}else{
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				branchBr.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
		
		//商品定義ファイルを読み込む
		File commodity = new File(cmdLine+"commodity.lst");
		BufferedReader commodityBr = null;
		try{
			if(commodity.exists() == true){
				FileReader fl = new FileReader(commodity);
				commodityBr = new BufferedReader(fl);
				String s;	
				while((s = commodityBr.readLine()) != null){
					String[] arr;
					arr = s.split(",");	
					int arrLeng = arr[0].length();
					Pattern p = Pattern.compile("^[0-9a-zA-Z]*$");
					Matcher m = p.matcher(arr[0]);
					if(arrLeng == 8 && m.find() == true){				//商品コードがアルファベットと数字の八桁
						if(arr.length == 2){							//商品名がカンマ、改行を含まない
							item.put(arr[0], arr[1]);
							itemSum.put(arr[0],0L);
						}else{
							System.out.println("商品定義ファイルのフォーマットが不正です");
							commodityBr.close();
							return;
						}
					}else{
						System.out.println("商品定義ファイルのフォーマットが不正です");
						commodityBr.close();
						return;
					}
				}		
			}else{
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				commodityBr.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
		
		//売り上げファイルの読み込み
		String filePath = cmdLine;
		File dir = new File(filePath);
		String[] files = dir.list();
		for(int i = 0; i < files.length; i++){					//ファイル名をString型で取得、ArrayList fileListに保管
			fileList.add(files[i]);
		}
		int size = fileList.size();							//.rcdを含む文字列だけをArrayList rcdListに保管
		String[] fileName = new String[size];
		for(int i = 0; i < size; i++){
			fileName[i] = fileList.get(i);
			if(fileName[i].contains(".rcd")){					//.rcdが含まれるならrcdListに加える
				rcdList.add(fileName[i]);				
			}		
		}
		int rcdNum = rcdList.size();		
		
		//rcdListリストのファイル名を数値に変換して数値のものは新しいリスト拡張子つきで格納
		for(int i = 0; i < rcdNum; i++){
			String splitRcd = rcdList.get(i);
			String[] arr;
			arr = splitRcd.split("\\.");
			int henkan = Integer.parseInt(arr[0]); 
			fileSort.add(henkan);
		}
		Collections.sort(fileSort);
		int fileSortNum = fileSort.size();
		
		//連番になっている場合のみプログラムを通す
		int j = fileSort.get(0);
		for(int i = 0; i < fileSortNum; i++){
			int num = fileSort.get(i);
			String rcdRen = String.format("%08d",num);
			String renban = String.format("%08d",j);
			if(rcdRen.equals(renban)){
				renbanList.add(rcdRen+".rcd");
				j++;
			}else{
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}	
		}
	
		//集計
		int renNum = renbanList.size();			
		for(int i = 0; i < renNum; i++){
			try{
				File file = new File(cmdLine+renbanList.get(i));		//売上ファイルを読み込む
				if(file.isFile() == false){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				
				//一行目(支店コード)を読み込む
				String first = br.readLine();
				if(shop.get(first) == null){
					System.out.println("<"+renbanList.get(i)+">の支店コードが不正です");
					return;
				}
				
				//二行目(商品コード)を読み込む
				String second = br.readLine();
				if(item.get(second) == null){
					System.out.println("<"+renbanList.get(i)+">の商品コードが不正です");
					return;
				}
				
				//三行目(売上金額)を読み込む
				String third = br.readLine();
				//三行目の数字の文字列をLong型の数値に変換
				Long thirdNum = new Long(third);	
				
				//四行目があればエラー表示し、プログラムを終了する。
				String fourth = br.readLine();
				if(fourth == null){				
					//shopSum
					long sumsrc1 = shopSum.get(first);
					long bShopSum = sumsrc1 + thirdNum;
					int valLen1 = String.valueOf( bShopSum ).length();
					if(valLen1 < 10){
						shopSum.put(first, bShopSum);
					}else{
						System.out.println("合計金額が10桁を超えました");
						return;
					}
				
					//itemSum
					long sumsrc2 = itemSum.get(second);
					long bItemSum = sumsrc2 + thirdNum;
					int valLen2 = String.valueOf( bItemSum ).length();
					if(valLen2 < 10){
						itemSum.put(second,bItemSum);
					}else{
						System.out.println("合計金額が10桁を超えました");
						return;
					}
				}else{
					System.out.println("<"+renbanList.get(i)+">のフォーマットが不正です");
					return;
				}
										
				br.close();
			}catch(IOException e){
				System.out.println(e);
			}				
		}	
		
		//店舗売上金額の合計のマップをリストに格納
        List<Map.Entry<String,Long>> shopEntries = 
              new ArrayList<Map.Entry<String,Long>>(shopSum.entrySet());
        Collections.sort(shopEntries, new Comparator<Map.Entry<String,Long>>() { 
            @Override
            public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
                return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
            }
        });
        
      //商品売上金額の合計のマップをリストに格納
        List<Map.Entry<String,Long>> itemEntries = 
              new ArrayList<Map.Entry<String,Long>>(itemSum.entrySet());
        Collections.sort(itemEntries, new Comparator<Map.Entry<String,Long>>() { 
            @Override
            public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
                return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
            }
        });
		
		//支店別集計ファイル　出力	
		BufferedWriter outBra = null;
		try{
			File file = new File(cmdLine+"branch.out");
			FileWriter fw = new FileWriter(file);
			outBra = new BufferedWriter(fw);
			for(Entry<String,Long> s : shopEntries){
				outBra.write(s.getKey()+","+shop.get(s.getKey())+","+s.getValue()+"\r\n");
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				outBra.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}	
		//商品別集計ファイル　出力
		BufferedWriter outCom = null;
		try{
			File file = new File(cmdLine+"commodity.out");
			FileWriter fw = new FileWriter(file);
			outCom = new BufferedWriter(fw);
			for(Entry<String,Long> s : itemEntries){
				outCom.write(s.getKey()+","+item.get(s.getKey())+","+s.getValue()+"\r\n");		
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				outCom.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
	}
}