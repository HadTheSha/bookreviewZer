import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

import java.util.regex.Pattern; 
import java.util.regex.Matcher; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.io.IOException; 
import java.util.*; 
import org.jsoup.Connection.*;
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


public class Bookizer extends JFrame implements ActionListener, ItemListener {
	private JLabel background; 
	private JPanel Firstui;
	private JPanel secondui; 
	private JScrollPane scroll;
	private JTextField searchField; 
	
	public static void main (String [] args){
	    Bookizer gui = new Bookizer();
	    gui.setVisible(true);
	    
	  }
	
	public Bookizer(){
		setTitle("Book Review.ZER");
		setSize(700,300);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	    setLocationRelativeTo(null);
	    //setLayout(new BorderLayout()); 
	    
	    background=new JLabel(new ImageIcon("THEBG01.png"));
	    add(background);
	    background.setLayout(new FlowLayout());
	    
	   // FIRST LAYOUT  TO be set to Opaque(false) when search button is clicked 
	    Firstui  = new JPanel();
	    Firstui.setLayout(new BorderLayout());
	    Firstui.setOpaque(false);
	    background.add(Firstui);
	    
	    //SEARCH FIELD 
	     searchField= new JTextField("Author OR Title OR ISBN"); 
	    // SEARCH BUTTON 
	    JPanel btns  = new JPanel();
	    btns.setLayout(new FlowLayout());
	    
	    JPanel center= new JPanel();
	    center.setLayout(new GridLayout(2,1));
	    center.setOpaque(false);
	    
	    JButton searchButton = new JButton("Search");
	    searchButton.addActionListener(this);
	    searchButton.setPreferredSize(new Dimension(600, 50));
	    btns.setOpaque(false);
	    center.add(searchField);
	    center.add(searchButton);
	    btns.add(center);
	    Firstui.add(btns,BorderLayout.CENTER);
	    ImageIcon header=new ImageIcon("Header.png");
	    JLabel head= new JLabel(); 
	    head.setIcon(header);
	    Firstui.add(head, BorderLayout.NORTH);
	    
	} // end of bookizer 

	public void actionPerformed(ActionEvent e){
		String buttonString = e.getActionCommand(); 
		
		if(buttonString.equals("Search")){
			if( searchField.getText().equals("Author OR Title OR ISBN")){
				searchField.setText("seems like you haven't type anything, try again please");
				searchField.setForeground(Color.RED);
			}
			else{
			// Get book's name : 
			String bookName= searchField.getText();
			
			searchField.setText("Connecting...");
			searchField.setForeground(Color.GREEN);

			
			// main layout
			secondui= new JPanel();
			secondui.setLayout(new GridLayout(6,0)); 
			secondui.setOpaque(false);
			secondui.setVisible(true);
			background.add(secondui);
			background.setOpaque(false);
			scroll= new JScrollPane(secondui);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			add(scroll);
			
		
			
			// START WEB SCRABING: 
			//
			try{
				
				// Sending a GET action 
				Response response = 
		                Jsoup.connect("https://www.goodreads.com/search")
		                .data("action", "/search")
		                .data("type", "text")
		                .data("query", bookName)
		                .followRedirects(true)
		                .execute();
				// parsing the document by HTML elements
				Document searchResult = response.parse();
				Elements booksNames= searchResult.select(".bookTitle");
				Elements bookAuthor= searchResult.select(".authorName");
				Elements bookRatings= searchResult.select(".minirating");
				Elements img= searchResult.select(".bookSmallImg");
				URL url; 
				BufferedImage image; 
				
	
				Document bookRev; 
				Document amzonInfo; 
				for(int i=0; i<5; i++){
					//book image: 
					url= new URL(img.get(i).attr("src"));
		            image = ImageIO.read(url); 
		            JLabel bookImg1 = new JLabel(new ImageIcon(image));
		            secondui.add(bookImg1);

		            // REVIEWS : 
					 String revLink= "https://www.goodreads.com"+booksNames.get(i).attr("href");
					 bookRev= Jsoup.connect(revLink).get();
					 Elements bookReview= bookRev.select("#description"); 
					 
					// NAMES RATINGS AND AUTHORS: 
					 
					 String newResult= "\n"+booksNames.get(i).text() +" \n"+"by: "+
					 bookAuthor.get(i).text() + "Rating: "+
					 bookRatings.get(i).text()+ bookReview.get(0).text();
					 JTextArea revText= new JTextArea(10,30); 
					 revText.setText(stringShortened(newResult));
					 textAreaProp(revText);
					 
					 // PRICE :  
					 
					// get amazon link
					 try {
						Font  f1= new Font("Cambria",Font.BOLD|Font.ITALIC,15);
						 
						 Elements btn= bookRev.select("#buyButton");
							String amznLink= "https://www.goodreads.com"+btn.get(0).attr("href");
							amzonInfo=Jsoup.connect(amznLink).get();
							Elements amzBookInfo= amzonInfo.select(".s-access-detail-page");
							Connection.Response Amzresponse = Jsoup.connect(amznLink).execute();
							
						if(amzBookInfo.size()==0){
								Elements amzPrice= amzonInfo.select("span.a-color-secondary:contains($)");
								revText.append("\n"+getCurrencyInSR(amzPrice.get(0).text()));
								revText.setFont(f1);
								
							} else {
								String amazonBookTitle; 
								String amazonBookLink;
								// TODO: do the search whether if the name is applicable 
								for(int j=0 ; j<i; j++){
									amazonBookTitle= amzBookInfo.get(j).attr("title");
									amazonBookLink=amzBookInfo.get(j).attr("href")+"\n";
									if (amazonBookTitle.equals(booksNames.get(i).text()));
								    	Document amazonInnerLink= Jsoup.connect(amazonBookLink).get(); 
										Elements amzPrice= amazonInnerLink.select("span.a-color-secondary:contains($)");
										 revText.append("\n"+getCurrencyInSR(amzPrice.get(0).text()));
										 revText.setFont(f1);
								

								}
							}
					 }catch(HttpStatusException ex){
						 searchField.setText("Opps, seems like no accessible amazon page");
							searchField.setForeground(Color.RED);
							ex.printStackTrace(); 
							
							
					 }
						
					 
					 
					 JPanel section= new JPanel();
					 section.setLayout(new BorderLayout());
					 secondui.add(section);
					 
				 
					 
				}
				
	
				
				// Resetting the result window 
				Firstui.setVisible(false);
				setSize(780,600);
				setLocationRelativeTo(null);
				
				
			} catch(IOException ex){
				searchField.setText("Ops looks like something went wrong ");
				searchField.setForeground(Color.RED);
			}
			}
		}// end of IF searchBtn clicked 

		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}
	

	private String stringShortened(String text){
		String result= text; 
		if (text.length()<=300)
			return text ;
		else {
			result = result.substring(0,301)+ "...";
			return result;
		}
			
	}
	
	
	private void textAreaProp(JTextArea revText){
		 revText.setEditable(false);  
		 revText.setCursor(null);  
		 revText.setOpaque(false);  
		 revText.setFocusable(false);
		 revText.setLineWrap(true);
		 revText.setWrapStyleWord(true);
		 secondui.add(revText); 
		 
	}
	
	private String getCurrencyInSR(String price){
		double n=0; 
		Pattern p = Pattern.compile("(?<!(?:\\d|\\.))\\d+\\.\\d{2}(?!\\.)");
		Matcher m = p.matcher(price);
		while (m.find()) {
			n = Double.parseDouble(m.group()); 
		}
		
		double valSr= n*3.75; 
		String srVal = "Price: "+String.valueOf(valSr)+" SR";
		
		return srVal; 
		
	}
	



}
