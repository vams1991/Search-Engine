package edu.asu.irs13;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class Pagerank {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		LinkAnalysis.numDocs = 25054;
        LinkAnalysis l = new LinkAnalysis();
        double error=100;
        double threshold=0.0000002;
        int itCount=0;
        double value1=(((double)1.0000000)/LinkAnalysis.numDocs);
        //declaring the rank vector
        double[] rank=new double[LinkAnalysis.numDocs];
        double[] sumofDen=new double[LinkAnalysis.numDocs];
        //Calculating the number of links of each document and storing it in a array. This is later used to create stochastic matrix
        for(int k=0;k<LinkAnalysis.numDocs;k++)
		{
			int[] links = l.getLinks(k);
			int count=0;
			for(int m: links) count++;
			sumofDen[k]= count;
		}
        //Computing the sink nodes
        int[] sinkNodes=new int[LinkAnalysis.numDocs];;
        for(int k=0;k<LinkAnalysis.numDocs;k++)
		{
			int[] links = l.getLinks(k);
			sinkNodes[k]=0;
			if(links.length==0)
			{
			  sinkNodes[k]=1;
			}
		}
        //initializing the rank vector to 1/n
		for(int m=0; m<rank.length;m++) rank[m]=(((double)1.0000000)/LinkAnalysis.numDocs);
          while(error>threshold)
          {
        	int n=0;
        	double[] temprank=new double[LinkAnalysis.numDocs];
    		for(int m=0; m<rank.length;m++) temprank[m]=rank[m];
        	while(n<(LinkAnalysis.numDocs))
        	{
        		
        		int[] citn = l.getCitations(n);
        		//declaring a row array to store the row of M* which is created on fly
        		double [] row=new double[LinkAnalysis.numDocs];
        		//the below four for loops are used to build the row of M*
        		for(int m=0; m<LinkAnalysis.numDocs;m++)
        		{
        			row[m]=0;
        			for(int k=0;k<citn.length;k++)
        			{
        				if(citn[k]==m)
        				{
        					row[m]=1;
        					break;
        				}
        			}
        		}
        		for(int k=0;k<LinkAnalysis.numDocs;k++)
        		{
        			if(sumofDen[k]!=0)
        			{
        			 row[k]=row[k]/sumofDen[k];
        			}
        		}	
        		for(int k=0;k<LinkAnalysis.numDocs;k++)
        		{
        			
        			if(sinkNodes[k]==1)
        			{
        				row[k]=row[k]+value1;
        			}
        		}
        		double c=0.8;
        		for(int m=0; m<row.length;m++) 
        		{
        			row[m]=(c*row[m])+(1-c)*((value1));
        		}
        		double sum=0;
        		//Multiplying the row of M* and rank vector
        		for(int m=0; m<row.length;m++)
        		{
        			sum+=row[m]*rank[m];
        		}
        		rank[n]=sum;
        		n++;
        		
        	}
        	//Normalizing the rank vector
        	double l1norm=0;
        	for(int m=0; m<rank.length;m++) l1norm+=rank[m];
        	for(int m=0; m<rank.length;m++) rank[m]=rank[m]/l1norm;
        	System.out.println(l1norm);
        	//Calculating error
        	double largesterror=0;
        	for(int m=0; m<rank.length;m++)
        	{
        		double diff=Math.abs(rank[m]-temprank[m]);
        		if(diff>largesterror) largesterror=diff;
        	}
        	error=largesterror;
        	itCount++;
        	System.out.println(error);
        	System.out.println("--------"+itCount+"--------");
        }
          //Doing min/max normalization of the rank vector
          double max=rank[0];
          for(int i=0;i<rank.length;i++)
          {
        	  if(rank[i]>max) max=rank[i];
          }
          double min=rank[0];
          for(int i=0;i<rank.length;i++)
          {
        	  if(rank[i]<min) min=rank[i];
          }
          
          double diff=max-min;
          for(int i=0;i<rank.length;i++)
          {
        	  rank[i]=(rank[i]-min)/diff;
          }
  		  while(true)
  		  {
  		  //getting the results of TF/IDF values
  		  Search_TF_IDF obj=new Search_TF_IDF();
  		  HashMap <Integer, Double> TF_IDF = (HashMap<Integer, Double>) obj.TF_IDF();
  		  HashMap <Integer, Double> pageR_Sim = new HashMap <Integer, Double>();
  		  double w=0.4;
  		  int n=0;
	   	  Iterator<Entry<Integer, Double>> m = TF_IDF.entrySet().iterator();
	   	  //combining the pagerank value and similarity value of a document and storing in a hashmap
		  while(m.hasNext())
		  {
			 Entry<Integer, Double> entry = m.next();
			 int docid= entry.getKey();
			 pageR_Sim.put(docid, w*rank[docid]+(1-w)*entry.getValue());
		  }
		  //sorting the results
		  Map<Integer, Double> sorted_pageR_Sim= Search_TF_IDF.sortByValues(pageR_Sim);
		  Iterator<Entry<Integer, Double>> k = sorted_pageR_Sim.entrySet().iterator();
		  //printing the results
		   	int c=0;
			 while(k.hasNext())
			 {
				 Entry<Integer, Double> entry = k.next();
				 String d_url = r.document(entry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
				 System.out.println("["+entry.getKey()+"] " + d_url);
				 c++;
				 if(c==10) break;
  		  }
	}

}
}
