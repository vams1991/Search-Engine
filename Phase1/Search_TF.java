package edu.asu.irs13;
import java.io.*;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class Search_TF {
	private static final Integer Null = null;
	public static void main(String[] args) throws Exception
	{
		//Calculating weights of terms in each document and storing in hash map "myHashMap"
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		HashMap<Integer, ArrayList<Integer>> myHashMap = new HashMap<Integer, ArrayList<Integer>>();
		TermEnum t = r.terms();
		while(t.next())
		{
			Term te = new Term("contents", t.term().text());
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
				if (myHashMap.get(td.doc()) == null) myHashMap.put(td.doc(), new ArrayList<Integer>());
				myHashMap.get(td.doc()).add(td.freq());
			}
		}
		Iterator<Entry<Integer, ArrayList<Integer>>> i = myHashMap.entrySet().iterator();
		//Calculating LNorms of the documents and storing in Hash map "map1"
		HashMap<Integer, Double> map1=new HashMap<Integer, Double>();
		while(i.hasNext())
		{
			int sum=0;
			Entry<Integer, ArrayList<Integer>> entry = i.next();
			Integer key = entry.getKey();  
			ArrayList<Integer> list = entry.getValue();
			Iterator<Integer> itr =list.iterator();
	        while(itr.hasNext()){
	            sum+=Math.pow(itr.next(), 2);
	        }
	        double sumsqroot = Math.sqrt(sum);
            map1.put(key, sumsqroot);	
		}
		Scanner sc = new Scanner(System.in);
		String str = "";
		HashMap<Integer, Double> map3 = new HashMap<Integer, Double>();
		//Taking query as input from user
		System.out.print("query> ");
		
		while(!(str = sc.nextLine()).equals("quit"))
		{
			//Calculating query vector and storing in hash map "qvector"
			HashMap<String,Integer> qvector=new HashMap<String, Integer>();
			String[] terms = str.split(" ");
			for(String temp: terms)
			{
				if(qvector.get(temp)==Null)
				{
					qvector.put(temp, 1);
				}
				else
				{
					int val=qvector.get(temp);
					val++;
					qvector.put(temp, val);
				}
			}
			//finding|q| using values stored in the hashmap qvector
			Iterator<Entry<String, Integer>> p = qvector.entrySet().iterator();
			int qsum=0;
			while(p.hasNext())
			{
				Entry<String, Integer> entry1 = p.next();
				qsum+=Math.pow(entry1.getValue(), 2);
			}
			double mod_q= Math.sqrt(qsum);
		
			for(String word : terms)
			{
				//Calculating cosine values of documents and storing in hash map "map3"
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				while(tdocs.next())
				{
					if (map3.get(tdocs.doc()) == null) 
					{	
						map3.put(tdocs.doc(), (double) tdocs.freq());
					}
					else
					{
						double sum = map3.get(tdocs.doc());
						sum+= tdocs.freq();
						map3.put(tdocs.doc(), sum);
					}
				}
			}
			
			//Normalizing the cosine values and storing in hash map "map3"
			Iterator<Entry<Integer, Double>> j = map3.entrySet().iterator();
			while(j.hasNext())
			{
				Entry<Integer, Double> entry = j.next();
				Integer key = entry.getKey();
				Double value = entry.getValue();
				Double denom = map1.get(key);
				value= value/(denom*mod_q);
				map3.put(key, value);
			}
			//Sorting the values in map3 and storing in map4
			   	Map<Integer, Double> map4 = sortByValues(map3);
			   	Iterator<Entry<Integer, Double>> m = map4.entrySet().iterator();
			   	//printing the results
				while(m.hasNext())
				{
					Entry<Integer, Double> entry = m.next();
					String d_url = r.document(entry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
					System.out.println("["+entry.getKey()+"] " + d_url);
				}
			   	System.out.print("query> ");
		}
		
	}
	//function to sort the the hashmap "map3" in descending order of values
	//Reference: http://beginnersbook.com/2013/12/how-to-sort-hashmap-in-java-by-keys-and-values/
	private static HashMap<Integer, Double> sortByValues(HashMap<Integer,Double> map)
	{ 
	       List list = new LinkedList(map.entrySet());
	       Collections.sort(list, Collections.reverseOrder(new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       }));

	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
}
