package edu.asu.irs13;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.awt.FlowLayout;

public class GUI_MiniGoogle {

	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI_MiniGoogle window = new GUI_MiniGoogle();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI_MiniGoogle() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 764, 783);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(244, 72, 248, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("SEARCH");
		btnSearch.setBounds(267, 91, 83, 23);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=textField.getText();
				//Creating object of Snippets_GUI class
				Snippets_GUI obj=new Snippets_GUI();
				try {
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
					//Calling the snipp() function and retrieving values
					String[] urlSnip=obj.snipp(str);
					   	int c=0;
					   	String results="";
						while(c<10)
						{
							results+=urlSnip[c].replace("%%", "/");
							results+="\n\n";
							c++;
						}
						textArea.setText(results);
		                
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		frame.getContentPane().add(btnSearch);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(211, 0, 317, 62);
		lblNewLabel.setIcon(new ImageIcon("C:\\Users\\vams1991\\Desktop\\googlelogo.jpg"));
		frame.getContentPane().add(lblNewLabel);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 125, 728, 605);
		frame.getContentPane().add(textArea);
		
		JButton btnKmaps = new JButton("KMEANS");
		btnKmaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Creating object of Kmeans_GUI
				KmeansGUI obj1=new KmeansGUI();
				String str=textField.getText();
				try {
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
					//Calling the kmeans() function using the object
					obj1.kmeans(str);
					String results1="";
					//Retreiving values from the hashmap Csmapfinal which is a global variable in class KmeansGUI
					Iterator<Entry<Integer, HashMap<Integer,Double>>> i6 = KmeansGUI.Csmapfinal.entrySet().iterator();
		    		 int j1=0;
		         	 while(i6.hasNext())
		         	 {
		         		Entry<Integer, HashMap<Integer, Double>> entry = i6.next();
		         		HashMap<Integer, Double> temp=entry.getValue();
		         		results1+="\n";
		         		results1+="Cluster"+j1;
		         		results1+="\n";
		         		int k=0;
		         		Iterator<Entry<Integer, Double>> i1 = temp.entrySet().iterator();
		         		while(i1.hasNext())
		         		{
		         			Entry<Integer, Double> entry1 = i1.next();
		         			String d_url = r.document(entry1.getKey()).getFieldable("path").stringValue().replace("%%", "/");
		         			results1+=d_url+"\n";
		         			k++;
		         			if(k==3) break;
		         		}
		         		results1+="Summary:";
		         		//Retreiving values from the hashmap sorttermTFfinal which is a global variable in class KmeansGUI
		         		HashMap<Integer, Double> temp2=KmeansGUI.sorttermTFfinal.get(entry.getKey());
		         		Iterator<Entry<Integer, Double>> i3 = temp2.entrySet().iterator();
		         		int k1=0;
		         		while(i3.hasNext())
		         		{
		         			k1++;
		         			Entry<Integer, Double> entry2 = i3.next();
		         			//Retreiving values from the array tlistfinal which is a global variable in class KmeansGUI
		         			results1+=KmeansGUI.tlistfinal[entry2.getKey()]+",";
		         			if(k1==10) break;
		         		}
		         		results1+="\n";
		         		j1++;
		         	
				}
		         	textArea.setText(results1);
		      } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		btnKmaps.setBounds(371, 91, 89, 23);
		frame.getContentPane().add(btnKmaps);
	}
}
