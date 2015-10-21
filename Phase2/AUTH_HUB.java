package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class AUTH_HUB {
	
	private static final Integer Null = null;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		       while(true)
		      {
		        Search_TF_IDF obj=new Search_TF_IDF();
		        //Getting the TF_IDF results
		        HashMap <Integer, Double> TF_IDF = (HashMap<Integer, Double>) obj.TF_IDF();
		        IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
                Iterator<Entry<Integer, Double>> m = TF_IDF.entrySet().iterator();
                ArrayList<Integer> topk = new ArrayList<Integer>();
                int n=0;
                //storing the top 10 results in an arraylist
                while(m.hasNext())
				{
					n++;
					Entry<Integer, Double> entry = m.next();
					topk.add(entry.getKey());
					if(n==10) break;
				}
                LinkAnalysis.numDocs = 25054;
                LinkAnalysis l = new LinkAnalysis();
                //Storing the root set in a hashmap
                HashMap<Integer, ArrayList<Integer>> adjMatrix = new HashMap<Integer, ArrayList<Integer>>();
                for(int z: topk)
                {
                	
        		  adjMatrix.put(z, new ArrayList<Integer>());
        		}
                //computing the base set and storing in the hashmap
                for(int z: topk)
                {
                	int[] links=l.getLinks(z);
    				int[] citations=l.getCitations(z);
    				for(int x: links)
    				{
    					if(adjMatrix.containsKey(x)!=true) adjMatrix.put(x, new ArrayList<Integer>());
                    }
    				for(int x: citations)
    				{
    					if(adjMatrix.containsKey(x)!=true) adjMatrix.put(x, new ArrayList<Integer>());
                    }
	            }
                //Computing the adjacency matrix
                Iterator<Entry<Integer, ArrayList<Integer>>> y = adjMatrix.entrySet().iterator();
        		while(y.hasNext())
        		{
        			Entry<Integer, ArrayList<Integer>> entry = y.next();
        			int[] links=l.getLinks(entry.getKey());
        			Iterator<Entry<Integer, ArrayList<Integer>>> s = adjMatrix.entrySet().iterator();
    				while(s.hasNext())
    				{
    					
    					Entry<Integer, ArrayList<Integer>> entry1 = s.next();
    					
    					int q=0;
    					for(int w: links)
    					{
    						if(entry1.getKey()==w)
    						{
    							adjMatrix.get(entry.getKey()).add(1);
    							q++;
    						}
    					}
    					if(q==0) adjMatrix.get(entry.getKey()).add(0);
    					
    				}
    				
        		}
        		//Constructing transpose of adjacency matrix
        		String timestampAMT = new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss:SSSSSSS a").format(new Date());
                HashMap<Integer, ArrayList<Integer>> tradjMatrix = new HashMap<Integer, ArrayList<Integer>>();
                Iterator<Entry<Integer, ArrayList<Integer>>> e = adjMatrix.entrySet().iterator();
				while(e.hasNext())
				{
					
					Entry<Integer, ArrayList<Integer>> entry = e.next();
					tradjMatrix.put(entry.getKey(), new ArrayList<Integer>());
				}
				Iterator<Entry<Integer, ArrayList<Integer>>> f = adjMatrix.entrySet().iterator();
					while(f.hasNext())
					{
					Entry<Integer, ArrayList<Integer>> entry = f.next();
					ArrayList<Integer> alist=entry.getValue();
					int z=0;
					Iterator<Entry<Integer, ArrayList<Integer>>> fl = adjMatrix.entrySet().iterator();
					while(fl.hasNext())
					{
						Entry<Integer, ArrayList<Integer>> entry1 = fl.next();
						tradjMatrix.get(entry1.getKey()).add(alist.get(z));
						z++;
					}
					
				
				}
				//Initializing an arbitrary vectors for authorities and hubs
				int rows=adjMatrix.size();
				double value=1/(double)rows;
				double threshold=0.000000002;
				double[] auth=new double[rows];
				double[] hub=new double[rows];
				for(int a=0; a<rows; a++)
				{
					auth[a]=value;
					hub[a]=value;
				}
				double largesterror=100;
				int iteration=1;
				while(largesterror>threshold)
				{
					//declaring temporary auth and hub vectors and initializing them
					double[] tempauth=new double[rows];
					double[] temphub=new double[rows];
					
					for(int c=0;c<rows;c++)
					{
						tempauth[c]=auth[c];
						temphub[c]=hub[c];
					}
					//Multiplying transpose of adjacency matrix and hub
					int count2=0;
					
					Iterator<Entry<Integer, ArrayList<Integer>>> fr = tradjMatrix.entrySet().iterator();
					while(fr.hasNext())
					{
						double sum1=0;
						Entry<Integer, ArrayList<Integer>> entry1 = fr.next();
						ArrayList<Integer> blist=entry1.getValue();
						for(int count1=0;count1<rows;count1++)
						{
						 sum1+=(blist.get(count1))*(temphub[count1]);
						}
						auth[count2]=sum1;
						count2++;
					}
					
					int count3=0;
					//Multiplying adjacency matrix and authority
					Iterator<Entry<Integer, ArrayList<Integer>>> fr1 = adjMatrix.entrySet().iterator();
					while(fr1.hasNext())
					{
						double sum2=0;
						Entry<Integer, ArrayList<Integer>> entry2 = fr1.next();
						ArrayList<Integer> clist=entry2.getValue();
						for(int count4=0;count4<rows;count4++)
						{
						 sum2+=(clist.get(count4))*(auth[count4]);
						}
						hub[count3]=sum2;
						count3++;
					}
					
					//Normalizing authority and hub vectors
					String timestampAHN = new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss:SSSSSSS a").format(new Date());
					
					double sum3=0,sum4=0;
					for(int count=0; count<rows;count++)
					{
						sum3+=Math.pow(auth[count],2);
						sum4+=Math.pow(hub[count], 2);
					}
					double sum3sqroot = Math.sqrt(sum3);
					double sum4sqroot = Math.sqrt(sum4);
					for(int count=0; count<rows;count++)
					{
						auth[count]=(auth[count]/sum3sqroot);
						hub[count]=(hub[count]/sum4sqroot);
					}
					
					//Computing largest errors
					double authLerror=0;
					double hubLerror=0;
					for(int count=0; count<rows;count++)
					{
					 double tempdiff=Math.abs(auth[count]-tempauth[count]);
					
					 if(tempdiff>authLerror) authLerror=tempdiff;
					}
					for(int count=0; count<rows;count++)
					{
					 double tempdiff=Math.abs(hub[count]-temphub[count]);
					 if(tempdiff>hubLerror) hubLerror=tempdiff;
					}
					if(hubLerror>=authLerror) largesterror=hubLerror;
					else largesterror=authLerror;
					iteration++;
					
				}
				//storing the auth and hub vectors in hashmaps
				HashMap<Integer, Double> authlist=new HashMap<Integer,Double>();
				HashMap<Integer, Double> hublist=new HashMap<Integer,Double>();
				int ll=0;
				Iterator<Entry<Integer, ArrayList<Integer>>> fz = adjMatrix.entrySet().iterator();
				while(fz.hasNext())
				{
				 Entry<Integer, ArrayList<Integer>> entry = fz.next();
				 authlist.put(entry.getKey(), auth[ll]);
				 hublist.put(entry.getKey(), hub[ll]);
				 ll++;
				}
				//sorting auth and hub vectors
				Map<Integer, Double> sortedauthlist= Search_TF_IDF.sortByValues(authlist);
				Map<Integer, Double> sortedhublist= Search_TF_IDF.sortByValues(hublist);
                System.out.println("Documents in decreasing order of Authority Values:");
                Iterator<Entry<Integer, Double>> fx = sortedauthlist.entrySet().iterator();
                int c=0;
                
				while(fx.hasNext())
				{
				 Entry<Integer, Double> entry = fx.next();
				 String d_url = r.document(entry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
			     System.out.println("["+entry.getKey()+"] " + d_url);
				 
				 c++;
				 if(c==10) break;
				}
				System.out.println("Documents in decreasing order of Hub Values:");
                Iterator<Entry<Integer, Double>> fw = sortedhublist.entrySet().iterator();
                
				while(fw.hasNext())
				{
				 c++;
				 if(c==21) break;
				 Entry<Integer, Double> entry = fw.next();
				 String d_url = r.document(entry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
			     System.out.println("["+entry.getKey()+"] " + d_url);
				}
				
		}
				
          }
}

