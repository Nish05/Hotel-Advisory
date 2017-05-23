import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NBClassifyBiwords {
	String[] trainingDocs;
	String[] testingDocs;
	int[] trainingLabels;
	int[] testingLabels;
	int numClasses;
	ArrayList<Integer> trainlabels=new ArrayList<Integer>();
	ArrayList<Integer> testlabels=new ArrayList<Integer>();
	int[] classCounts; //number of docs per class
	String[] classStrings; //concatenated string for a given class
	int[] classTokenCounts; //total number of tokens per class
	HashMap<String,Double>[] condProb;
	HashSet<String> vocabulary; //entire vocabuary
	ArrayList<String> trainfilenames=new ArrayList<String>();
	ArrayList<String> testfilenames=new ArrayList<String>();
	ArrayList<String> termList=new ArrayList<String>();
	ArrayList<String> biwordList=new ArrayList<String>();
	int count=0;
	String filename;
	String[] stopList;
	int total_pos;
	int total_neg;
	int tp_neg;
	int tp_pos;
	int fp_neg;
	int fp_pos;
	int fn_neg;
	int fn_pos;
	/**
	 * Build a Naive Bayes Classify using a training document set
	 * @param trainDataFolder the training document folder
	 */
	public NBClassifyBiwords(String trainDataFolder)
	{
		System.out.println("Starting Preprocessing");
		preprocess(trainDataFolder);
		System.out.println("End of Preprocessing");
		System.out.println("Start of generateStopList");
		stopList=generateStopList();
		System.out.println("End of generateStopList");
		System.out.println("Start of generateTermList");
		generateTermList();
		System.out.println("End of generateTermList");
		System.out.println("Start of generateBiWordList");
		generateBiWordList();
		System.out.println("End of generateBiWordList");
		numClasses=2;
		classCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<String>();
		for(int i=0;i<numClasses;i++){
			classStrings[i] = "";
			condProb[i] = new HashMap<String,Double>();
		}
//		classCounts[0]=91119;
//		classCounts[1]=20036;
        classCounts[0]=94374;
        classCounts[1]=19958;
		for(int i=0;i<trainingLabels.length;i++){
		//	classCounts[trainingLabels[i]]++;
			classStrings[trainingLabels[i]] += (trainingDocs[i] + " ");
		}
		System.out.println("Start of Tokenization");
		for(int i=0;i<numClasses;i++){
			String[] tokens = classStrings[i].split("[ .,?!:;$%()\"--'/\\t]+");
			classTokenCounts[i] = tokens.length;
			
			//collecting the counts
			for(int j=0;j<tokens.length-1;j++){
				String token=tokens[j].toLowerCase();
				token=stemWord(token);
				int result=search(token,termList);
				if(result!=-1){
					vocabulary.add(token);
					if(condProb[i].containsKey(token)){
						double count = condProb[i].get(token);
						condProb[i].put(token, count+1);
					}
					else
						condProb[i].put(token, 1.0);
				}
				token=tokens[j].toLowerCase()+" "+tokens[j+1].toLowerCase();
				result=search(token,biwordList);
				if(result!=-1){
					vocabulary.add(token);
					if(condProb[i].containsKey(token)){
						double count = condProb[i].get(token);
						condProb[i].put(token, count+1);
					}
					else
						condProb[i].put(token, 1.0);
				}
			}
		}
		//computing the class conditional probability
		for(int i=0;i<numClasses;i++){
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vSize = vocabulary.size();
			while(iterator.hasNext())
			{
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				count = (count+1)/(classTokenCounts[i]+vSize);
				condProb[i].put(token, count);
			}
//			System.out.println(condProb[i]);
		}
		System.out.println("Start of Tokenization");

	}
	private void generateBiWordList() {
		String filename="/home/stu4/s7/jad6566/Desktop/KPT/Classification/Results/bigram_v2";
		String currentLine;
		String token;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			while((currentLine=reader.readLine())!=null){
				token=currentLine;
				biwordList.add(token);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(biwordList);

	}
	public void generateTermList(){
		String filename="/home/stu4/s7/jad6566/Desktop/KPT/Classification/Results/result_termFreq";
		String currentLine;
		String token;
		ArrayList<String> termlist1=new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			while((currentLine=reader.readLine())!=null){
				token=currentLine;
				termlist1.add(token);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i=termlist1.size()-1;i>=0;i--){
			termList.add(termlist1.get(i));
		}
		Collections.sort(termList);
	}
	private String[] generateStopList() {
		String stopListFile="/home/stu4/s7/jad6566/Desktop/KPT/Classification/Results/stopword_Project.txt";
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
	/**
	 * Classify a test doc
	 * @param doc test doc
	 * @return class label
	 */
	public int classify(String doc){
		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];
		for(int i=0;i<score.length;i++){
			score[i] = Math.log(classCounts[i]*1.0/trainingDocs.length);
		}
		String[] tokens = doc.split("[ .,?!:;$%()\"--'/\\t]+");
		for(int i=0;i<numClasses;i++){
			for(int j=0;j<tokens.length-1;j++){
				   String token=tokens[j].toLowerCase();
				   token=stemWord(tokens[j]);
				    int result=search(token,termList);
				    if(result!=-1){
				if(condProb[i].containsKey(token))
					score[i] += Math.log(condProb[i].get(token));
				else
					score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
			}
				token=tokens[j].toLowerCase()+" "+tokens[j+1].toLowerCase();
				result=search(token,biwordList);
				if(result!=-1){
					if(condProb[i].containsKey(token))
						score[i] += Math.log(condProb[i].get(token));
					else
						score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
				}
			   }
		}


		double maxScore = score[0];
		for(int i=0;i<score.length;i++){
			if(score[i]>maxScore)
				label = i;
		}

		return label;
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

	public int search(String token, ArrayList<String> List) {
		int value=binarySearch(token,List);
		return value;
	}
	private int binarySearch(String token,ArrayList<String> list) {
		int start=0;
		int end=list.size()-1;
		int mid;
		while(start<end){
			if((start+end)%2!=0) mid=(start+end)/2+1;
			else mid=(start+end)/2;
			String current_word=list.get(mid);
			if(token.equals(current_word)) return mid;
			else if(token.compareTo(current_word)<0) end=mid-1;
			else if(token.compareTo(current_word)>0) start=mid+1;

		}
		return -1;

	}
	/**
	 * This method performs stemming of the token
	 * @param token      token to be stemmed
	 * @return stemmed token
	 */
	public String stemWord(String token) {
		Stemmer st=new Stemmer();
		st.add(token.toCharArray(),token.length());
		st.stem();
		return st.toString();
	}

	/**
	 * Load the training documents
	 * @param trainDataFolder
	 */
	public void preprocess(String trainDataFolder)
	{
		File folder=new File(trainDataFolder);

		fileList(folder,"train");
		trainingDocs=new String[trainfilenames.size()];
		trainingLabels=new int[trainlabels.size()];
		for(int i=0;i<trainfilenames.size();i++){
			trainingDocs[i]=parse(trainfilenames.get(i));
			trainingLabels[i]=trainlabels.get(i);
		}
	}
	private void fileList(File folder,String folder_type) {
		if(folder_type.equals("train")){
			listFile1(folder);
		}
		else{
			listFile2(folder);
		}


	}
	private void listFile1(File folder) {
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				listFile1(file);
			}
			else{
				if(file.isFile()){
					if(!file.getName().equals(".DS_Store")){
						if(folder.getName().equals("neg")){
							trainlabels.add(new Integer(1));
						}
						else{
							trainlabels.add(new Integer(0));
						}
						trainfilenames.add(file.getAbsolutePath());
					}
				}
			}
		}
	}
	private void listFile2(File folder) {
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				listFile2(file);
			}
			else{
				if(file.isFile()){
					if(!file.getName().equals(".DS_Store")){
						if(folder.getName().equals("neg")){
							testlabels.add(new Integer(1));
							total_neg++;
						}
						else{
							testlabels.add(new Integer(0));
							total_pos++;
						}
						testfilenames.add(file.getAbsolutePath());
					}
				}
			}
		}

	}

	public String parse(String fileName) {
		String allLines="";
		String currentLine;

		try {
			BufferedReader reader=new BufferedReader(new FileReader(fileName));
			while((currentLine=reader.readLine())!=null){
				allLines+=currentLine;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return allLines;
	}
	/**
	 *  Classify a set of testing documents and report the accuracy
	 * @param testDataFolder fold that contains the testing documents
	 * @return classification accuracy
	 */
	public double classifyAll(String testDataFolder)
	{
		File folder=new File(testDataFolder);

		fileList(folder,"test");
		int true_classify=0;
		double accuracy;
		ArrayList<String> reviews=new ArrayList<String>();
		ArrayList<Integer> labels=new ArrayList<Integer>();
		for(int i=0;i<testfilenames.size();i++){
			reviews=splitToReviews(testfilenames.get(i));
			if(testlabels.get(i)==0){
				for(int k = 0 ;k< reviews.size(); k++){
					labels.add(0);
				}			}
			else{
				for(int k = 0 ;k< reviews.size(); k++){
					labels.add(1);
				}			}
		}
		testingDocs=new String[reviews.size()];
		testingLabels=new int[labels.size()];
		for(int i=0;i<reviews.size();i++){
			testingDocs[i]=reviews.get(i);
			testingLabels[i]=labels.get(i).intValue();
		}
		for(int i=0;i<testingDocs.length;i++){
			int label=classify(testingDocs[i]);
			if(label==testingLabels[i]){
				if(label==0){
					tp_pos++;
				}
				else{
					tp_neg++;
				}
				true_classify=true_classify+1;
			}else{
				if(label==0){
					fp_pos++;
				}
				else if(label==1){
					fp_neg++;
				}
				if(testingLabels[i]==0){
					fn_pos++;
				}
				else if(testingLabels[i]==1){
					fn_neg++;
				}
			}

		}
		double precision_pos=(tp_pos*1.0)/(tp_pos+fp_pos);
		System.out.println("Precision for positive : "+precision_pos);
		double recall_pos=(tp_pos*1.0)/(tp_pos+fn_pos);
		System.out.println("Recall for positive : "+recall_pos);

		double precision_neg=(tp_neg*1.0)/(tp_neg+fp_neg);
		System.out.println("Precision for negative : "+precision_neg);
		double recall_neg=(tp_neg*1.0)/(tp_neg+fn_neg);
		System.out.println("Recall for negative : "+recall_neg);

		System.out.println("Correctly classified are "+true_classify+" out of "+testingDocs.length);
		accuracy=(true_classify*1.0)/testingDocs.length;
		return accuracy;
	}


	private ArrayList<String> splitToReviews(String string) {
		
		ArrayList<String> reviews=new ArrayList<String>();

		try {
			Scanner sc=new Scanner(new FileReader(string));
			while(sc.hasNextLine()){
				reviews.add(sc.nextLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return reviews;
	}
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		NBClassifyBiwords nb=new NBClassifyBiwords("/home/stu4/s7/jad6566/Desktop/KPT/Classification/Results/Nisha/Reviews/train");
		double accuracy=nb.classifyAll("/home/stu4/s7/jad6566/Desktop/KPT/Classification/Results/Nisha/Reviews/test");
		System.out.println("Accuracy : "+accuracy);
		long end = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");


	}
}
