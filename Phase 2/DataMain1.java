
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class DataMain1 {
	
	static double no_of_wrongA = 0;
    static double no_of_wrongB = 0;
    static double no_of_wrongC = 0;
    static double no_of_wrongD = 0;
    static double no_of_wrongF = 0;
	
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;
        
        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }
        return inputReader;
    }
    
    public static Evaluation simpleClassify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation validation = new Evaluation(trainingSet);
        
        model.buildClassifier(trainingSet);
        validation.evaluateModel(model, testingSet);
        
        return validation;
    }
    
    public static double calculateAccuracy(ArrayList<ArrayList<Prediction>> predictionsList, String[][] splitOutput) {
        double correct = 0;
        double incorrect = 0;
        
        //double totalRows = 0;
       // System.out.println(predictionsList.size());
        String prediction = "";
        String actual = ""; 
        for (int i = 0; i < predictionsList.size(); i++) {
        	System.out.println("-----------False positives in fold no."+i+"----------");
        	System.out.println(" ");
        	System.out.println("rm_key | statement_date | predicted | actual ");
        	for (int j = 0; j < predictionsList.get(i).size(); j++) {
        		Prediction np = (Prediction) predictionsList.get(i).get(j);
//            	System.out.print(np.predicted()+"\t"+np.actual()+"\n");
	            if (np.predicted() == np.actual()) {
	                correct++;
	            }else{
	            	if(np.predicted() == 0) {
	            		prediction = "A";
	            		no_of_wrongA++; //Calculating number of wrongly predicted A's
	            	}
	            	if(np.predicted() == 1) {
	            		prediction = "B";
	            		no_of_wrongB++; //Calculating number of wrongly predicted B's
	            	}
	            	if(np.predicted() == 2) {
	            		prediction = "C";
	            		no_of_wrongC++; //Calculating number of wrongly predicted C's
	            	}
	            	if(np.predicted() == 3) {
	            		prediction = "D";
	            		no_of_wrongD++; //Calculating number of wrongly predicted D's
	            	}
	            	if(np.predicted() == 4) {
	            		prediction = "F";
	            		no_of_wrongF++; //Calculating number of wrongly predicted F's
	            	}
	            	if(np.actual() == 0) {
	            		actual = "A";
	            	}
	            	if(np.actual() == 1) {
	            		actual = "B";
	            	}
	            	if(np.actual() == 2) {
	            		actual = "C";
	            	}
	            	if(np.actual() == 3) {
	            		actual = "D";
	            	}
	            	if(np.actual() == 4) {
	            		actual = "F";
	            	}
	            	System.out.println(splitOutput[i][j]+"\t  "+prediction+"\t      "+actual);
	            	incorrect++;
	            }
	            
        	}
        	System.out.println("-------------------------------------------------");
        	System.out.println(" ");
        }
        
        System.out.println("Total false positives :"+incorrect);
        return correct;
    }
    
    public static double crossValidationSplit(Instances data, int numberOfFolds, String[] outputStrings) throws Exception {
        Instances[][] split = new Instances[5][numberOfFolds];
        String[][] splitOutput = new String[5][data.size()];
        
        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }
        
        //int numInstForFold = data.size() / numberOfFolds;
        
        for (int numFold = 0; numFold < numberOfFolds; numFold++) {
        	int offset;
        	if (numFold < data.size() % numberOfFolds) {
               // numInstForFold++;
                offset = numFold;
              } else {
                offset = data.size() % numberOfFolds;
              } 
              int first = numFold * (data.size() / numberOfFolds) + offset;
              System.arraycopy( outputStrings, first, splitOutput[numFold], 0,split[1][numFold].size()); 
        }
        
     // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits  = split[1];
        
        // Choose a set of classifiers
        Classifier model = new IBk();
        ArrayList<ArrayList<Prediction>> predictionsList = new ArrayList<ArrayList<Prediction>>();
            
            // For each training-testing split pair, train and test the classifier
            for(int i = 0; i < trainingSplits.length; i++) {
                Evaluation validation = simpleClassify(model, trainingSplits[i], testingSplits[i]);
                predictionsList.add(validation.predictions());
            }
            
            // Calculate overall accuracy of current classifier on all splits
            double accuracy = calculateAccuracy(predictionsList,splitOutput);
        
        return accuracy;
    }
    
    public static void main(String[] args) throws Exception {
        
    	CSVLoader loader = new CSVLoader();
    	
	    loader.setSource(new File("MassHousingTrainData.csv"));
//	    loader.setSource(new File("rows.csv"));
	    
	    //Instances data = loader.getDataSet();
	    //Instances wholeData = loader.getDataSet();
	    ArrayList<Instances> dataArray = new ArrayList<>();
	    
	    
	    int stmtYearIndex = 6;
	   // Instances wholeData = loader.getDataSet();
	    for(int j=2004,l=1999,p=1995; j<=2009 && l<=2004 && p<=2000; j++,l++,p++) { //removing instances with year numbers
	    	Instances wholeData = loader.getDataSet();
	    	//int startYear = p;
	    	//int lastYear = j;
	    	//int middleYear = l;
	    	
	    for (int i = wholeData.size()-1; i >= 0; i--) {
	    	int year = Integer.parseInt(wholeData.instance(i).stringValue(stmtYearIndex).substring(wholeData.instance(i).stringValue(stmtYearIndex).lastIndexOf("/")+1));
	    	
	    	if(year > j) {
	    		//delete those rows
	    		wholeData.delete(i);
	    	}
	    	if(l < year && year < j) {
	    		wholeData.delete(i);
    		}
	    	if(year < p) {
	    		wholeData.delete(i);
	    	}
	    }
	    dataArray.add(wholeData);
	   
	    //wholeData.delete();
	    }
	    
	    //changing the values of letter grade from A/B/C/D/F to 0/1
	    /*for (int i = 0; i < data.size(); i++) {
	    		if(data.get(i).stringValue(attIndex).equals("A") || 
	    			data.get(i).stringValue(attIndex).equals("B") || 
	    			data.get(i).stringValue(attIndex).equals("C")) {
		    		data.get(i).setValue(attIndex,1);
	    			//Onecount++;
	    		}
	    		else if(data.get(i).stringValue(attIndex).equals("D") || 
	    				data.get(i).stringValue(attIndex).equals("F")) {
	    			data.get(i).setValue(attIndex,0);
		    		//Zerocount++;
	    		}
	    }*/
	    
    	
	    for(int k=0;k<dataArray.size();k++) {
	    	double ACount = 0;
		    double BCount = 0;
		    double CCount = 0;
		    double DCount = 0;
		    double FCount = 0;
	    	System.out.println("-------------------Pass: "+k+"------------------" );
	    	Instances data = dataArray.get(k);
	    	//System.out.println(data.size());
	    for (int i = 0; i < data.size(); i++) { //Calculating total number of individual Alphabets from original dataset
	    	int gradeIndex = 7;
	    	if(data.get(i).stringValue(gradeIndex).equals("A")) {
    			ACount++;
    		}
    		else if(data.get(i).stringValue(gradeIndex).equals("B")) {
    			BCount++;
    		}
    		else if(data.get(i).stringValue(gradeIndex).equals("C")) {
    			CCount++;
    		}
    		else if(data.get(i).stringValue(gradeIndex).equals("D")) {
    			DCount++;
    		}
    		else if(data.get(i).stringValue(gradeIndex).equals("F")) {
    			FCount++;
    		}
	    }
	    String[] outputStrings = new String[data.size()];
	    for (int i = 0; i < data.size(); i++) {
	    	int rmkeyIndex = 0;
	    	outputStrings[i] = ((int)(data.get(i).value(rmkeyIndex))+"\t"+data.get(i).stringValue(stmtYearIndex));
	    }
	    // deleting unwanted attributes
	    for(int i=51;i>=14;i--){
	    	data.deleteAttributeAt(i);
	    }
	    data.deleteAttributeAt(12);
	    data.deleteAttributeAt(11);
	    data.deleteAttributeAt(9);
	    data.deleteAttributeAt(8);
	    for(int i=6;i>=0;i--){
	    	data.deleteAttributeAt(i);
	    }
	    
//      Instances data = new Instances(datafile);
        data.setClassIndex(0); //Choose the attribute to set as the class (Financial Letter grade for this dataset)
        
        // Choose a type of validation split
        double correctCount = crossValidationSplit(data, 5,outputStrings);
        
        double correctA = ACount - no_of_wrongA;
        double correctB = BCount - no_of_wrongB;
        double correctC = CCount - no_of_wrongC;
        double correctD = DCount - no_of_wrongD;
        double correctF = FCount - no_of_wrongF;
        //System.out.println(data.size());
            System.out.println("Total Prediction Accuracy: "+ correctCount*100/data.size());
            System.out.println("Total A's: "+ACount+", "+ "Correctly Predicted A's: "+correctA+", "+"Accuracy: "+ correctA*100/ACount);
            System.out.println("Total B's: "+BCount+", "+ "Correctly Predicted B's: "+correctB+", "+"Accuracy: "+ correctB*100/BCount);
            System.out.println("Total C's: "+CCount+", "+ "Correctly Predicted C's: "+correctC+", "+"Accuracy: "+ correctC*100/CCount);
            System.out.println("Total D's: "+DCount+", "+ "Correctly Predicted D's: "+correctD+", "+"Accuracy: "+ correctD*100/DCount);
            System.out.println("Total F's: "+FCount+", "+ "Correctly Predicted F's: "+correctF+", "+"Accuracy: "+ correctF*100/FCount);
            System.out.println("\n\n");
            no_of_wrongA = 0;
            no_of_wrongB = 0;
            no_of_wrongC = 0;
            no_of_wrongD = 0;
            no_of_wrongF = 0;
            data.delete();
	    }
    }
}