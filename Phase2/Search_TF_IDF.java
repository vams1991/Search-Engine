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


public class Search_TF_IDF {
	private static final Integer Null = null;
	public static Map<Integer, Double> TF_IDF() throws Exception
	{
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		//Calculating IDF values and storing in the hash map "map2"
		HashMap<String, Double> map2=new HashMap<String, Double>();
		TermEnum t1 = r.terms();
		while(t1.next())
		{
			Term te = new Term("contents", t1.term().text());
            int docCount = r.docFreq(te);
            double idf;
            if(docCount==0)
            {
            	idf=0;
            }
            else
            {
            idf= Math.log((double)r.maxDoc()/(double)docCount);
            }
            map2.put(t1.term().text(), idf);
        }
		////Calculating weights of terms in each document and storing in hash map "myHashMap"
		Map<Integer, Double> map4=null;
		HashMap<Integer, ArrayList<Double>> myHashMap = new HashMap<Integer, ArrayList<Double>>();
		TermEnum t = r.terms();
		while(t.next())
		{
			Term te = new Term("contents", t.term().text());
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
				if (myHashMap.get(td.doc()) == null) myHashMap.put(td.doc(), new ArrayList<Double>());
				myHashMap.get(td.doc()).add(td.freq()*map2.get(t.term().text()));
			}
		}
		//Calculating LNorms of the documents and storing in Hash map "map1"
		Iterator<Entry<Integer, ArrayList<Double>>> i = myHashMap.entrySet().iterator();
		HashMap<Integer, Double> map1=new HashMap<Integer, Double>();
		while(i.hasNext())
		{
			int sum=0;
			Entry<Integer, ArrayList<Double>> entry = i.next();
			Integer key = entry.getKey();  
			ArrayList<Double> list = entry.getValue();
			Iterator<Double> itr =list.iterator();
	        while(itr.hasNext()){
	            sum+=Math.pow(itr.next(), 2);
	        }
	        double sumsqroot = Math.sqrt(sum);
            map1.put(key, sumsqroot);	
		}
		Scanner sc = new Scanner(System.in);
		String str = "";
		HashMap<Integer, Double> map3 = new HashMap<Integer, Double>();
		System.out.print("query> ");
		str = sc.nextLine();
		
		{
			
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
					double idf = map2.get(term.text());
					if (map3.get(tdocs.doc()) == null) 
					{	
						map3.put(tdocs.doc(), (double) tdocs.freq()*idf);
					}
					else
					{
						double sum = map3.get(tdocs.doc());
						sum= sum + (tdocs.freq()*idf);
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
			    map4 = sortByValues(map3);
			   	Iterator<Entry<Integer, Double>> m = map4.entrySet().iterator();
		return map4;
		}
	}
	//function to sort the the hashmap "map3" in descending order of values
	//Reference: http://beginnersbook.com/2013/12/how-to-sort-hashmap-in-java-by-keys-and-values/
	public static HashMap<Integer, Double> sortByValues(HashMap<Integer,Double> map) 
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
