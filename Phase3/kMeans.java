package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class kMeans {

	public static void main(String[] args) throws Exception {
		
		int topN=50;
		System.out.println("Enter the number of Clusters:");
		Scanner sc = new Scanner(System.in);
		int numClusters = sc.nextInt();
        System.out.println("Number of Clusters: "+numClusters);
        Search_TF_IDF obj=new Search_TF_IDF();
        //Getting the TF_IDF results
        HashMap <Integer, Double> TF_IDF = (HashMap<Integer, Double>) obj.TF_IDF();
        IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
        Iterator<Entry<Integer, Double>> m = TF_IDF.entrySet().iterator();
        //Initializing an array to store top 50 results
        int[] topk =new int[topN];
        int n=0;
        while(m.hasNext())
		{
			Entry<Integer, Double> entry = m.next();
			topk[n]=entry.getKey();
			n++;
			if(n==topN) break;
		}
        //Creating vectors of term frequencies*IDF for top 50 documents and storing in the hashmap kvector
        HashMap<Integer, double[]> kvector=new HashMap<Integer, double[]>();
        int termNo=0;
        TermEnum t = r.terms();
		while(t.next())
		{
			Term te = new Term("contents", t.term().text());
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
			  for(int i=0; i<topk.length;i++)
				  if(td.doc()==topk[i])
				   {
				    if (kvector.get(td.doc()) == null) kvector.put(td.doc(), new double[366533]);
				    double temp[]=kvector.get(td.doc());
				    temp[termNo]=td.freq()*obj.map2.get(t.term().text());
				    kvector.put(td.doc(), temp);
				   }
			}
			termNo++;
		}
		//Selecting random documents as centroids and storing in an array centrois
        Random rand = new Random();
        int[] centroids = new int[numClusters];
        double[][] sim=new double[topN][numClusters];
        int temp1;
        for(int i=0; i<numClusters;i++)
        {
        	temp1=topk[rand.nextInt(topk.length)];
            for(int l=0;l<i;l++)
            {
            	if(temp1==centroids[l])
            	{
            		temp1=topk[rand.nextInt(topk.length)];
            	}
            }
        	centroids[i] = temp1;
        }
        //Initializing a hashmap clusters1 to store clusters of documents
        HashMap<Integer, ArrayList<Integer>> clusters1=new HashMap<Integer, ArrayList<Integer>>();
        for(int i=0; i<centroids.length;i++)
        {
        	clusters1.put(centroids[i], new ArrayList());
        }
        //Computing similarities between the top 50 documents and the centroids
        for(int i=0; i<topk.length;i++)
        {
        	
        		double temp2[]=kvector.get(topk[i]);
        		for(int j=0; j<centroids.length;j++)
        		{
        		  double temp3[]=kvector.get(centroids[j]);
        		  for(int k=0; k<temp2.length;k++)
        		  {
        			  sim[i][j]+=temp2[k]*temp3[k];
        		  }
        		  sim[i][j]=sim[i][j]/(obj.map1.get(topk[i])*obj.map1.get(centroids[j]));
        		}
        }
        //Forming clusters and storing them in the hashmap clusters1
        for(int i=0; i<topN; i++)
    	{
    		double max=0;
    		int maxj=0;
    		for(int j=0; j<numClusters; j++)
    		{
    			if(sim[i][j]>max)
    			{
    				max=sim[i][j];
    				maxj=j;
    			}
    			
    		}
    		clusters1.get(centroids[maxj]).add(topk[i]);
    	}
        int iteration=2;
        boolean converged=false;
         while(converged==false)
        {
        	System.out.println("Iteration"+iteration);
        	converged=true;
        	//Creating a copy of hashmap used to store the clusters of documents in previous iteration
        	HashMap<Integer, ArrayList<Integer>> temp=new HashMap<Integer, ArrayList<Integer>>();
        	Iterator<Entry<Integer, ArrayList<Integer>>> m7 = clusters1.entrySet().iterator();
        	int f=0;
        	while(m7.hasNext())
        	{
        		Entry<Integer, ArrayList<Integer>> entry=m7.next();
        		ArrayList<Integer>temp8=entry.getValue();
        		temp.put(f, temp8);
        		f++;
        	}
        	//Initializing hashmap used to store the clusters of documents in previous iteration and the new clusters of documents will be stored
        	clusters1=new HashMap<Integer,ArrayList<Integer>>();
        	//Calculating the term freqency*IDF vectors of the centroids and storing it in the hashmap Centroid
        	HashMap<Integer, double[]> Centroid=new HashMap<Integer, double[]>();
        	Iterator<Entry<Integer, ArrayList<Integer>>> m2 = temp.entrySet().iterator();
        	int l=0;
        	while(m2.hasNext())
        	{
        		Entry<Integer, ArrayList<Integer>> entry = m2.next();
        		ArrayList<Integer> list=entry.getValue();
        		
        		for(int k=0;k<366533;k++)
        		{
        			double sum=0;
        			Iterator<Entry<Integer, double[]>> m3 = kvector.entrySet().iterator();
                	while(m3.hasNext())
                	{
        			Entry<Integer, double[]> entry1 = m3.next();
        			for(int i=0; i<list.size();i++)
        			{
        				int doc1=entry1.getKey();
        				int doc2=list.get(i);
        				if(doc1==doc2)
        				{
        					double[] temp3=entry1.getValue();
        					sum+=temp3[k];
        					
        				}
        				
        			}
        			
        		    }
                	sum=(double)sum/(double)list.size();
        			if(Centroid.get(l)==null)
        				
        			Centroid.put(l,new double[366533]);
        			double temp3[]=Centroid.get(l);
        			temp3[k]=sum;
        			Centroid.put(l,temp3);
        		}
        		l++;
        	}
        	
        	//calculating norm for centroids and storing in HashMap centroidL2norm
        	HashMap<Integer, Double> centroidL2norm=new HashMap<Integer, Double>();
        	Iterator<Entry<Integer, double[]>> i1 = Centroid.entrySet().iterator();
    		//HashMap<Integer, Double> map1=new HashMap<Integer, Double>();
    		while(i1.hasNext())
    		{
    			int sum=0;
    			Entry<Integer, double[]> entry = i1.next();
    			Integer key = entry.getKey();  
    			double[] temp6 = entry.getValue();
    	        for(int j=0; j<temp6.length;j++){
    	            sum+=Math.pow(temp6[j], 2);
    	        }
    	        double sumsqroot = Math.sqrt(sum);
    	        centroidL2norm.put(key, sumsqroot);	
    		}
    		//Calculating similarity values between the top 50 documents and centroids
        	for(int i=0; i<topk.length;i++)
            {
            	
            		double temp2[]=kvector.get(topk[i]);
            		for(int j=0; j<numClusters;j++)
            		{
            		  double temp3[]=Centroid.get(j);
            		  for(int k=0; k<temp2.length;k++)
            		  {
            			  sim[i][j]+=temp2[k]*temp3[k];
            		  }
            		  sim[i][j]=sim[i][j]/(obj.map1.get(topk[i])*centroidL2norm.get(j));
            		}
            }
            //Forming clusters of documents
            for(int i=0; i<topN; i++)
        	{
        		double max=0;
        		int maxj=0;
        		for(int j=0; j<numClusters; j++)
        		{
        			if(sim[i][j]>max)
        			{
        				max=sim[i][j];
        				maxj=j;
        			}
        			
        		}
        		if(clusters1.get(maxj)==null)
        			clusters1.put(maxj, new ArrayList());
        		clusters1.get(maxj).add(topk[i]);
            }
            if(!clusters1.equals(temp))
            	converged=false;
            //storing the last set of similarity values in a text file which later used in summary computation
            if(converged==true)
            {
            	PrintWriter writer = new PrintWriter("Csim.txt", "UTF-8");
            	for(int i=0; i<topN; i++)
            	{
            		double max=0;
            		int maxj=0;
            		for(int j=0; j<numClusters; j++)
            		{
            			if(sim[i][j]>max)
            			{
            				max=sim[i][j];
            				maxj=j;
            			}
            			
            		}
            		writer.println(topk[i]+":"+maxj+":"+max);
            	}
            	writer.close();
            }
            iteration++;
	}
         //Storing the cluster no, documents and similarities in the hashmap Csmap
         HashMap<Integer, HashMap<Integer, Double>> Csmap=new HashMap<Integer,HashMap<Integer, Double>>();
         for(int i=0;i<numClusters;i++) Csmap.put(i, new HashMap<Integer, Double>());
         BufferedReader br = new BufferedReader(new FileReader("Csim.txt"));
         String line = null;
     	 while ((line = br.readLine()) != null) 
     	 {
     		 String[] temp= line.split(":");
     		 Csmap.get(Integer.valueOf(temp[1])).put(Integer.valueOf(temp[0]), Double.valueOf(temp[2]));
     	 }
     	 br.close();
     	 Iterator<Entry<Integer, HashMap<Integer,Double>>> i = Csmap.entrySet().iterator();
     	 int j=0;
     	 while(i.hasNext())
     	 {
     		Entry<Integer, HashMap<Integer, Double>> entry = i.next();
     		HashMap<Integer, Double> temp=entry.getValue();
     		//sorting the documents of a cluster in decreasing order of similarity
     		temp=obj.sortByValues(temp);
     		Csmap.put(entry.getKey(), temp);
     		j++;
     	 }
     	//Storing all the list of terms in an array
			String[] tList=new String[366533];
			HashMap<Integer,double[]> termTF=new HashMap<Integer, double[]>();
			TermEnum t1 = r.terms();
			int count=0;
			while(t1.next())
			{
				tList[count]=t1.term().text();
				count++;
			}
			//Finding sum of term frequencies across all documents in each cluster and storing in termTF
			Iterator<Entry<Integer, ArrayList<Integer>>> m2 = clusters1.entrySet().iterator();
        	int l=0;
        	while(m2.hasNext())
        	{
        		Entry<Integer, ArrayList<Integer>> entry = m2.next();
        		ArrayList<Integer> list=entry.getValue();
        		
        		for(int k=0;k<366533;k++)
        		{
        			double sum=0;
        			Iterator<Entry<Integer, double[]>> m3 = kvector.entrySet().iterator();
                	while(m3.hasNext())
                	{
        			Entry<Integer, double[]> entry1 = m3.next();
        			for(int i1=0; i1<list.size();i1++)
        			{
        				int doc1=entry1.getKey();
        				int doc2=list.get(i1);
        				if(doc1==doc2)
        				{
        					double[] temp3=entry1.getValue();
        					if(obj.map2.get(tList[k])!=0)
        					sum+=temp3[k]/obj.map2.get(tList[k]);
        					sum+=0;
        				}
        				
        			}
        			
        		    }
        			if(termTF.get(l)==null)
        				
        			termTF.put(l,new double[366533]);
        			double temp3[]=termTF.get(l);
        			temp3[k]=sum;
        			termTF.put(l,temp3);
        		}
        		l++;
        	}
        	//Computing inverse cluster frequency and storing in HashMap ICF
        	HashMap<Integer, Double> ICF=new HashMap<Integer, Double>();
        	for(int q=0;q<366533;q++)
        	{
        		int count1=0;
        		Iterator<Entry<Integer, double[]>> m5 = termTF.entrySet().iterator();
        		while(m5.hasNext())
        		{
        			Entry<Integer, double[]> entry = m5.next();
        			double[] temp=entry.getValue();
        			if(temp[q]!=0) count1++;
        		}
        		if(count1!=0)
        		{
        		  ICF.put(q, Math.log((double)numClusters/(double)(count1)));
        		}
        		else
        		{
        			ICF.put(q, 0.0);
        		}
        	}
        	//calculating TF*ICF and storing it in a map termTFICF
        	HashMap<Integer,HashMap<Integer, Double>> sorttermTF=new HashMap<Integer,HashMap<Integer, Double>>();
        	for(int v=0;v<numClusters;v++)
        	{
        		sorttermTF.put(v, new HashMap<Integer, Double>());
        	}
        	Iterator<Entry<Integer, double[]>> m8 = termTF.entrySet().iterator();
    		while(m8.hasNext())
    		{
    			Entry<Integer, double[]> entry = m8.next();
    			double[] temp=entry.getValue();
    			for(int r1=0; r1<366533; r1++)
    			{
    				temp[r1]=temp[r1]*ICF.get(r1);
    			}
    			termTF.put(entry.getKey(), temp);
    			HashMap<Integer, Double> tempmap= sorttermTF.get(entry.getKey());
    			for(int r2=0; r2<366533; r2++)
    			{
    				tempmap.put(r2, temp[r2]);
    			}
    			//Sorting the terms according to TF*ICF values
    			tempmap=obj.sortByValues(tempmap);
    			sorttermTF.put(entry.getKey(), tempmap);
    		}
    		//Printing the top 3 documents in each cluster and corresponding summaries
    		 Iterator<Entry<Integer, HashMap<Integer,Double>>> i6 = Csmap.entrySet().iterator();
    		 int j1=0;
         	 while(i6.hasNext())
         	 {
         		Entry<Integer, HashMap<Integer, Double>> entry = i6.next();
         		HashMap<Integer, Double> temp=entry.getValue();
         		System.out.println();
         		System.out.println("Cluster"+j1);
         		int k=0;
         		Iterator<Entry<Integer, Double>> i1 = temp.entrySet().iterator();
         		while(i1.hasNext())
         		{
         			Entry<Integer, Double> entry1 = i1.next();
         			System.out.print(entry1.getKey()+" ");
         			k++;
         			if(k==3) break;
         		}
         		System.out.println();
         		System.out.println("Summary:");
         		HashMap<Integer, Double> temp2=sorttermTF.get(entry.getKey());
         		Iterator<Entry<Integer, Double>> i3 = temp2.entrySet().iterator();
         		int k1=0;
         		while(i3.hasNext())
         		{
         			k1++;
         			Entry<Integer, Double> entry2 = i3.next();
         			System.out.print(tList[entry2.getKey()]+",");
         			if(k1==20) break;
         		}
         		System.out.println();
         		j1++;
         	 }
        	
	}
}
