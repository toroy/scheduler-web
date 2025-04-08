package com.clubfactory.platform.scheduler.web.server.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.clubfactory.platform.scheduler.web.server.BaseTest;

public class PythonTest extends BaseTest {

	@Test
	public void test() {
		run();
	}

	public static void main(String[] args) {
//		PythonInterpreter interpreter = new PythonInterpreter(); 
//		// 运行python语句 
//		interpreter.exec("a = \"hello, Jython\"");  
//		interpreter.exec("print a");

		try {
			FileWriter file = new FileWriter("runoob.py");
			BufferedWriter out = new BufferedWriter(file);
			out.write("import numpy as np\n" + "\n" + "a = np.arange(12).reshape(3,4)\n" + "print(a)");
			out.close();
			System.out.println("文件创建成功！");
			
		} catch (IOException e) {
		}
		PythonTest test = new PythonTest();
		test.run();
		
		try{
            File file = new File("runoob.py");
            if(file.delete()){
                System.out.println(file.getName() + " 文件已被删除！");
            }else{
                System.out.println("文件删除失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
	}

	public void run() {
		Process proc;
		try {
			System.out.println("start");
			System.out.println("first:");
			proc = Runtime.getRuntime().exec("/usr/local/bin/python3 runoob.py");// 执行py文件
			// System.out.println("second:");
			// proc = Runtime.getRuntime().exec("/usr/local/bin/python3
			// /Users/zhoulijiang/Documents/gaia_demo.py depend 1 2");// 执行py文件
			// 用输入输出流来截取结果
			BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			while ((line = error.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			proc.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
