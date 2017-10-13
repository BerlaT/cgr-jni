/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Classe per fare un semplice test.
 * doSomething esegue del semplice codice C
 * doSomethingWithLists esegue del codice C che chiama a sua volta codice Java
 * @author michele
 *
 */
public class Main {

static final boolean ACTIVATE_PROMPT = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int result = JNITest.doSomething(1, "pippo");
		System.out.println(result);
		result = JNITest.doSomethingWithLists(2, new String[]{"Ciaone", "comelava'?", "sei un somaro", "puzzi"});
		System.out.println(result);
		result = JNITest.doSomethingWithPsm(3, new String[]{"Ciaone", "comelava'?", "sei un somaro", "puzzi", "vacca s'le boun"});
		System.out.println(result);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line, nodeNum = "", command = "";
		int num;
		StringTokenizer tok;
		Map<Integer, CGRouter> map = new HashMap<>();
		try {
			for (int i = 1; i < 3; i++)
			{
				CGRouter router = new CGRouter();
				router.init(i);
				router.processLine("@ 2016/1/1-00:00:00");
				router.readContactPlan("/home/michele/workspacemars/jni.test/resources/contact_plan.txt");
				router.processLine("l contact");
				router.processLine("a contact +1000 +1100 1 2 100000");
				router.processLine("d contact +1000 1 2");
				map.put(i, router);
				router.testMessage();
				System.out.flush();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("PORCAMERDA");
		}
		if (ACTIVATE_PROMPT)
		{
			try {
				while ((line = in.readLine()) != null)
				{
					if (line.equals("q"))
						break;
					tok = new StringTokenizer(line);
					if (tok.hasMoreTokens())
					{
						try {
							nodeNum = tok.nextToken();
							if (tok.hasMoreTokens())
								command = line.substring(nodeNum.length() + 1);
							num = Integer.parseInt(nodeNum);
							map.get(num).processLine(command);
						} catch (NumberFormatException e)
						{
							System.out.println("Invalid node number: " + nodeNum);
							continue;
						}
					}
				}
			} catch (Exception e)
			{
				System.out.println("Cannot read from console");
			}
		}
	}
	

}