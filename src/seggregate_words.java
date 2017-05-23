import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class seggregate_words {
	static ArrayList<String> positiveList=new ArrayList<String>();
	static ArrayList<String> negativeList=new ArrayList<String>();
	static ArrayList<String> reviews=new ArrayList<String>();
	int[] labels;
	int total_positive=0;
	int total_negative=0;
	String[] stopList;
	int review_count=0;
	public seggregate_words() {

	}
	public seggregate_words(String foldername){
		String currentLine;
		String[] tokens;
		int positive=0;
		int negative=0;
		File folder=new File(foldername);
		File[] files=folder.listFiles();
		stopList=generateStopList();
		review_count=0;
		for(int i=0;i<files.length;i++){
			ArrayList<String> pos_list = new ArrayList<String>();
			ArrayList<String> neg_list = new ArrayList<String>();
			if(!files[i].getName().equals(".DS_Store")){
				String file=files[i].getAbsolutePath();
				//		String file=foldername+"/china_beijing_ascott_beijing.txt";
				try {
					Scanner read=new Scanner(new FileReader(file));
					while(read.hasNextLine()){
						String line=read.nextLine();
						reviews.add(line);

					}
				} catch (IOException e) {
					e.printStackTrace();				}
				//	labels=new int[reviews.size()];

				String fname=foldername.substring(35);
//				System.out.println(fname);
				String pos_name=fname+"_pos";
				String neg_name=fname+"_neg";
				for(String review: reviews){

					//	i++;

					try {
						int label=processReview(review);
						if(label==1){
							pos_list.add(review);
//							transferToFile1(review, name);
						}
						else if(label==-1){
							neg_list.add(review);
//							negative++;
//							transferToFile2(review,name);
						}
						//	labels[i]=label;
						review_count++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					transferToFile1(pos_list,pos_name);
					transferToFile2(neg_list,neg_name);
				} catch (IOException e) {
					e.printStackTrace();
				}

				//		if(total_positive>total_negative){
				//			System.out.println("Positive");
				//		}
				//		else if(total_negative>total_positive){
				//			System.out.println("Negative");
				//		}
				//		else{
				//			System.out.println("Neutral");
				//		}
			}
		}

	}
	private int processReview(String line) {
		int positive=0;
		int negative=0;
		int result;
		int score=0;
		String[] tokenization=line.split("[     &*:;$%()\"--=/<\\t]+");
		ArrayList<String> tokens=new ArrayList<String>();
		for(String token: tokenization){
			token=token.toLowerCase();
			result=removeStopWord(token);
			if(result==-1){
				tokens.add(token);
			}
		}
		//			if(search(token,positiveList)>-1){
		//				positive++;
		//				System.out.println(token);
		//				System.out.println("Positive words are : "+positive);
		//			}
		//			else if(search(token,negativeList)>-1){
		//				negative++;
		//				System.out.println(token);
		//				System.out.println("Negative words are : "+negative);
		//			}
		//			//		}
		//		}
		for(int i=0;i<tokens.size()-1;i++){
			if(search(tokens.get(i),positiveList)>-1){
				if(search(tokens.get(i+1),negativeList)>-1){
					score=score-10;
					negative--;
					//		System.out.println("IN else if loop.......");
					i++;
				}
				else{
					score=score+10;
					positive++;
					i++;
				}
			}
			else if(search(tokens.get(i),negativeList)>-1){
				if(search(tokens.get(i+1),positiveList)>-1){
					//	score--;
					//	System.out.println("IN else if loop.......");
					score=score-10;
					negative--;
					i++;
				}
				else if(search(tokens.get(i+1),negativeList)>-1){
					//	score++;
					score=score+10;
					positive++;
					i++;
				}
				else{
					negative--;
				}

			}
		}
		int neg_score=Math.abs(negative);
		total_positive+=positive;
		total_negative+=neg_score;
		//	System.out.println("Negative Score      Positive Score");
	//	System.out.println(Math.abs(negative*1.0)+"          "+positive);
		//	System.out.println(score);
		//	System.out.println(tokenization.length);
		//	double weight=score/Math.sqrt(tokenization.length*1.0);
		//	System.out.println(weight);
		if(positive>neg_score){
	//		System.out.println("Positive Review");
			return 1;
		}
		else{
	//		System.out.println("Negative Review");
			return -1;
		}
	}

	public int removeStopWord(String token) {
		int lo=0;
		int high=stopList.length-1;
		while(lo<high){
			int mid=(lo+high)/2;
			if(token.compareTo(stopList[mid])<0) high=mid-1;
			else if(token.compareTo(stopList[mid])>0) lo=mid+1;
			else return mid;

		}
		return -1;
	}

	/**
	 * Generates an array of words from the file of stop words
	 * @param stopListFile       Name of the file which contains all the stop words
	 * @return array of stop words
	 */
	private String[] generateStopList() {
		//	String stopListFile="/home/stu15/s4/nnb7791/KPT/KPT_Project/stopword_Project.txt";
		String stopListFile="C:/Users/Nisha/Desktop/KPT/stopword_Project.txt";

		ArrayList<String> stopwords=new ArrayList<String>();
		String currentline;
		try {
			int count=0;
			BufferedReader reader=new BufferedReader(new FileReader(stopListFile));
			//Checks the total number of stop words in the document

			while((currentline=reader.readLine())!=null){
				stopwords.add(currentline);
			}
			stopList=new String[stopwords.size()];
			while(count!=stopwords.size()){
				stopList[count]=stopwords.get(count);
				count++;
			}
			Arrays.sort(stopList);
		} catch (IOException e) {

			e.printStackTrace();
		}

		return stopList;
	}
	public int search(String token, ArrayList<String> List) {
		//int value=List.indexOf(token);
		int value=binarySearch(token,List);
		return value;
	}
	private int binarySearch(String token,ArrayList<String> list) {
		int start=0;
		int end=list.size()-1;
		int mid;
		while(start<=end){
			if((start+end)%2!=0) mid=(start+end)/2+1;
			else mid=(start+end)/2;
			String current_word=list.get(mid);
			if(token.equals(current_word)) return mid;
			else if(token.compareTo(current_word)<0) end=mid-1;
			else if(token.compareTo(current_word)>0) start=mid+1;

		}
		return -1;

	}
	public void readPos_Neg() {
		String filename="C:/Users/Nisha/Desktop/KPT/positive-negative/positive-words.txt";
		String currentLine;
		//	File folder=new File(filename);
		//	File[] files=folder.listFiles();
		//	for(int i=0;i<files.length;i++){
		//	String file=files[i].getAbsolutePath();
		try {
			BufferedReader read=new BufferedReader(new FileReader(filename));
			while((currentLine=read.readLine())!=null){
				//	System.out.println(currentLine);
				positiveList.add(currentLine);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//	}
		filename="C:/Users/Nisha/Desktop/KPT/positive-negative/negative-words.txt";
		//	folder=new File(filename);
		//	files=folder.listFiles();
		//	for(int i=0;i<files.length;i++){
		//		String file=files[i].getAbsolutePath();
		try {
			BufferedReader read=new BufferedReader(new FileReader(filename));
			while((currentLine=read.readLine())!=null){
				//		System.out.println(currentLine);
				negativeList.add(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		filename="C:/Users/Nisha/Desktop/KPT/positive-negative/neg2.txt";
		//	folder=new File(filename);
		//	files=folder.listFiles();
		//	for(int i=0;i<files.length;i++){
		//		String file=files[i].getAbsolutePath();
		try {
			BufferedReader read=new BufferedReader(new FileReader(filename));
			while((currentLine=read.readLine())!=null){
				//	System.out.println(currentLine);
				negativeList.add(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(negativeList);

		//	}
	}

	/**
	 * This method generates a file of terms and their corresponding
	 * document frequency
	 * @param termList        list of all the terms
	 * @param docFreq         list of all the document frequencies of the terms
	 * @throws IOException
	 */
	private void transferToFile1(ArrayList<String> temp,String filename) throws IOException {
		String filePath="C:/Users/Nisha/Desktop/KPT/Reviews/pos/"+filename;
//		System.out.println(filename);
		File file=new File(filename);
		file.createNewFile();
		Writer writer = null;

		try {

			writer = new FileWriter(filePath);
			for (String review : temp){
				writer.write(review);
				writer.write("\r\n");
			}
		} catch (IOException e) {

			System.err.println("Error writing the file : ");

			e.printStackTrace();

		} finally {



			if (writer != null) {
				try {

					writer.close();

				} catch (IOException e) {
					System.err.println("Error closing the file : ");
					e.printStackTrace();
				}
			}
		}


	}

	/**
	 * This method generates a file of terms and their corresponding
	 * document frequency
	 * @param termList        list of all the terms
	 * @param docFreq         list of all the document frequencies of the terms
	 * @throws IOException
	 */
	private void transferToFile2(ArrayList<String > temp, String filename) throws IOException {
		String filePath="C:/Users/Nisha/Desktop/KPT/Reviews/neg/"+filename;
		File file=new File(filename);
		file.createNewFile();
		Writer writer = null;

		try {

			writer = new FileWriter(filePath);
			for (String review : temp){
				writer.write(review);
				writer.write("\r\n");
			}
		} catch (IOException e) {

			System.err.println("Error writing the file : ");

			e.printStackTrace();

		} finally {



			if (writer != null) {
				try {

					writer.close();

				} catch (IOException e) {
					System.err.println("Error closing the file : ");
					e.printStackTrace();
				}
			}
		}


	}


	public static void main(String[] args) throws IOException{
		long start = System.currentTimeMillis();
		seggregate_words sw=new seggregate_words();
		sw.readPos_Neg();
		seggregate_words read=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/new-delhi");		
		// seggregate_words read3=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/las-vegas");
		// seggregate_words read4=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/london");
		// seggregate_words read5=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/dubai");
		// seggregate_words read7=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/new-york-city");
		// seggregate_words read8=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/san-francisco");
		// seggregate_words read9=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/shanghai");
		// seggregate_words read10=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/beijing2");
		// seggregate_words read11=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/montreal");
		// seggregate_words read12=new seggregate_words("C:/Users/Nisha/Desktop/KPT/hotels/chicago");

		long end = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");


	}
}
