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

public class Process {	
	
	
	//入力ファイルを読み込み、マップに格納するためのメソッド
	//条件式(支店or商品, ファイルパス, 条件1 名前Map, 合計Map)
	public static boolean inSI(String junle, String filePath, String condit, HashMap<String, String> putMap, HashMap<String, Long> putSum){
		File file = new File(filePath);
		BufferedReader br = null;
		try{
			if(file.exists() == true){
				FileReader fl = new FileReader(file);
				br = new BufferedReader(fl);
				String s;	
				while((s = br.readLine()) != null){
					String[] arr;
					arr = s.split(",");	
					if(arr[0].matches(condit) && arr.length == 2){
						putMap.put(arr[0], arr[1]);
						putSum.put(arr[0],0L);
					}else{
						System.out.println(junle+"定義ファイルのフォーマットが不正です");
						return false;
					}
				}		
			}else{
				System.out.println(junle+"定義ファイルが存在しません");
				return false;
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if(br != null){
					br.close();
				}	
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
	
	
	//出力ファイルを作成するためのメソッド
	/*
	HashMap codeはコード、名前
	HashMap sumはコード、合計金額
	*/
	static boolean outBC(String filePath, HashMap<String,String> code, HashMap<String,Long> sum){
		//改行のための変数
		String crlf = System.getProperty("line.separator");
		//売上金額の合計のマップをリストに格納
        List<Map.Entry<String,Long>> Entries = new ArrayList<Map.Entry<String,Long>>(sum.entrySet());
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
	
	//メインメソッド
	public static void main(String[] args){
		
		
		//コマンドライン引数が存在するかのチェック
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		
		//コマンドライン引数を変数に代入		
		String cmdLine = args[0];
		
		//支店定義<店舗コード, 店舗名>
		HashMap<String, String> branch = new HashMap<String, String>();
		//商品定義<商品コード, 商品名>
		HashMap<String, String> commodity = new HashMap<String, String>();
		
		ArrayList<String> fileList = new ArrayList<String>();				//ディレクトリ内のファイルをすべて読み込む				
		ArrayList<Integer> fileSort = new ArrayList<Integer>();			//.rcdを除いたファイル名を数値として取得
		ArrayList<File> serialList = new ArrayList<File>();		 		//連番だけに絞る
		
		HashMap<String, Long> branchSum = new HashMap<String, Long>();		//店舗コード,　合計金額
		HashMap<String, Long> commoditySum = new HashMap<String,Long>();		//商品コード,　合計金額
				
		//支店定義ファイルを読み込む
		String brPath = cmdLine+File.separator+"branch.lst";
		boolean inBrResult = inSI("支店", brPath, "[0-9]{3}", branch, branchSum);
		if(inBrResult == false){
			return;
		}
		
		
		//商品定義ファイルを読み込む
		String comPath = cmdLine+File.separator+"commodity.lst"; 
		boolean inComResult = inSI("商品", comPath, "[0-9a-zA-Z]{8}", commodity, commoditySum);
		if(inComResult == false){
			return;
		}
		
		//売り上げファイルの読み込み
		File dir = new File(cmdLine);					//ファイル名をString型で取得、中でも.rcdのものだけリストに入れる
		String[] files = dir.list();
		for(int i = 0; i < files.length; i++){					
			if(files[i].contains(".rcd")){
				fileList.add(files[i]);
			}
		}
		//rcdListリストのファイル名を数値に変換して数値のものは新しいリスト拡張子つきで格納
		int rcdNum = fileList.size();
		for(int i = 0; i < rcdNum; i++){
			String[] arr;
			arr = fileList.get(i).split("\\.");
			int conversion = Integer.parseInt(arr[0]); 
			fileSort.add(conversion);
		}
		Collections.sort(fileSort);
		int fileSortNum = fileSort.size();
		
		//連番になっている場合のみプログラムを通す
		int j = fileSort.get(0);
		for(int i = 0; i < fileSortNum; i++){
			int num = fileSort.get(i);
			String rcdRen = String.format("%08d",num);
			String serial = String.format("%08d",j);
			File serialNumber = new File(cmdLine+File.separator+rcdRen+".rcd");
			if(rcdRen.equals(serial) && serialNumber.isFile()){
				serialList.add(serialNumber);
				j++;		
			}else{
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}	
		}
	
		//集計
		int renNum = serialList.size();			
		for(int i = 0; i < renNum; i++){
			BufferedReader br = null;
			try{
				FileReader fr = new FileReader(serialList.get(i));
				br = new BufferedReader(fr);
				
				//一行目(支店コード)を読み込む
				String first = br.readLine();
				if(first == null){
					System.out.println(serialList.get(i)+"のフォーマットが不正です");
					return;
				}
				//二行目(商品コード)を読み込む
				String second = br.readLine();
				if(second == null){
					System.out.println(serialList.get(i)+"のフォーマットが不正です");
					return;
				}
				//三行目(売上金額)を読み込む
				String third = br.readLine();
				if(third == null){
					System.out.println(serialList.get(i)+"のフォーマットが不正です");
					return;
				}
				//四行目があればエラー表示し、プログラムを終了する。
				String fourth = br.readLine();
				if(fourth != null){
					System.out.println(serialList.get(i)+"のフォーマットが不正です");
					return;
				}
				//コードがマップに存在しなかった場合の処理
				if(branch.containsKey(first) == false){		//一行目が支店定義ファイルで宣言されたコードか
					System.out.println(serialList.get(i)+"の支店コードが不正です");
					return;
				}
				if(commodity.containsKey(second) == false){
					System.out.println(serialList.get(i)+"の商品コードが不正です");
					return;
				}
				if(third.matches("^[0-9]*$") == false){
					System.out.println("予期せぬエラーが発生しました");		
					return;
				}
				//三行目の数字の文字列をLong型の数値に変換
				Long thirdNum = new Long(third);						
				//branchSum
				long sumsrc1 = branchSum.get(first);
				long bShopSum = sumsrc1 + thirdNum;
				int valLen1 = String.valueOf( bShopSum ).length();
				if(valLen1 < 11){
					branchSum.put(first, bShopSum);
				}else{
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				//commoditySum
				long sumsrc2 = commoditySum.get(second);
				long bItemSum = sumsrc2 + thirdNum;
				int valLen2 = String.valueOf( bItemSum ).length();
				if(valLen2 < 11){
					commoditySum.put(second,bItemSum);
				}else{
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				try{
					if(br != null){
						br.close();
					}	
				}catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}				
		}	
		
		
		
		//支店別集計ファイル　出力	
        String outBrPath = cmdLine+File.separator+"branch.out";
        boolean kekkaBr = outBC(outBrPath, branch, branchSum);
        if(kekkaBr == false){
        	System.out.println("予期せぬエラーが発生しました");
        	return;
        }
        
      //商品別集計ファイル　出力	
        String outComPath = cmdLine+File.separator+"commodity.out";
        boolean kekkaCom = outBC(outComPath, commodity, commoditySum);
        if(kekkaCom == false){
        	System.out.println("予期せぬエラーが発生しました");
        	return;
        }
	}
}