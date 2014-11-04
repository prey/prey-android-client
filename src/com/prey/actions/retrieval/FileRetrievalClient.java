package com.prey.actions.retrieval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import com.prey.PreyLogger;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

public class FileRetrievalClient {

	public void connect(String host, String port) {
		Socket socket = null;
		try {
			InetAddress addr = InetAddress.getByName(host);

			System.out.println("addr = " + addr);
			socket = new Socket(addr, Integer.parseInt(port));

			System.out.println("socket = " + socket);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

			while (true) {
				String str = in.readLine();
				if (str != null) {
					out.println(responder(str));
				}
			}
		} catch (Exception e) {
			PreyLogger.i("Erro,causa:"+e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {

				}
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public static String responder(String str) {
		
		if(str.indexOf("folder")>=0){
	str=str.replace("folder=", "");
		//if ("/".equals(str)) {
			File extStore = Environment.getExternalStorageDirectory();
			String path = extStore.getAbsolutePath()+str;

			File tempfile = new File(path);
		    File[] files = tempfile.listFiles();
StringBuffer sb=new StringBuffer();

		    if (files != null) {
		        for (File checkFile : files) {
		            if (checkFile.isDirectory()) {
		              //  allDirectories.add(checkFile.getName());
		               // listAllDirectories(checkFile.getAbsolutePath());
		                sb.append("<a href=\"?folder=").append(str).append("/").append(checkFile.getName()).append("\">").append(checkFile.getName()).append("</a>/<br>");
		            }else{
		            	 sb.append("<a href=\"?file=").append(str).append("/").append(checkFile.getName()).append("\">").append(checkFile.getName()).append("</a><br>");
		            }
		        }
		    }
			
			return sb.toString();
		}else{
			str=str.replace("file=", "");
			File extStore = Environment.getExternalStorageDirectory();
			String path = extStore.getAbsolutePath()+str;
			File file = new File(path);
			
			FileInputStream fileInputStream=null;
			 
	        
	 
			byte[] FileBytes = new byte[(int) file.length()];
	 
	        try {
	            //convert file into array of bytes
		    fileInputStream = new FileInputStream(file);
		    fileInputStream.read(FileBytes);
		    fileInputStream.close();
	 
		    
	 
		    
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        
			 

			byte[] encodedBytes = Base64.encode(FileBytes, 0);
			String encodedString = new String(encodedBytes);                                        
			 
			 return encodedString;
		}
		/*} else {
			return "3456777777777777777777";
		}*/
	}
}
