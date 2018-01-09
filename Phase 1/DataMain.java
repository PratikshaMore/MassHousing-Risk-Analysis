
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.CSVLoader;




public class DataMain {
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
        double totalRows = 0;
        
        
        for (int i = 0; i < predictionsList.size(); i++) {
        	System.out.println("\n----------------------False positives in fold no."+i+"------------------------");
        	System.out.println(" ");
        	System.out.println("rm_key | statement_date | predicted | actual ");
        	for (int j = 0; j < predictionsList.get(i).size(); j++) {
        		NominalPrediction np = (NominalPrediction) predictionsList.get(i).get(j);

	            if (np.predicted() == np.actual()) {
	                correct++;
	            }else{
	            	System.out.println(splitOutput[i][j]+"\t"+np.predicted()+"\t\t"+np.actual());
	            	incorrect++;
	            }
	            
        	}
        }
        
        System.out.println("\n\nTotal false positives :"+incorrect);
        return correct;
    }
    
    public static double crossValidationSplit(Instances data, int numberOfFolds, String[] outputStrings) throws Exception {
        
    	//Array to store row data in 5 chunks 
    	Instances[][] split = new Instances[5][numberOfFolds];
    	
    	//Array to store the output data in the same format 
        String[][] splitOutput = new String[5][data.size()];
        
        //Train on 4/5th of the data and test in 1/5th of data
        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }
 
        //Implement the same logic of the split data 
        //for mapping to the output strings 
         for (int numFold = 0; numFold < numberOfFolds; numFold++) {
        	int offset;
        	if (numFold < data.size() % numberOfFolds) {
                offset = numFold;
              } else {
                offset = data.size() % numberOfFolds;
              } 
              int first = numFold * (data.size() / numberOfFolds) + offset;
              //Fill up the split output strings as per the test chunks
              System.arraycopy( outputStrings, first, splitOutput[numFold], 0,split[1][numFold].size()); 
        }
        
        
        
        
        
        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits  = split[1];
        
        
        // Use the KNN Classifier
        Classifier model = new IBk();
        
        //Create a list of the predictions for each train-test pair
        ArrayList<ArrayList<Prediction>> predictionsList = new ArrayList<ArrayList<Prediction>>();
            
            for(int i = 0; i < trainingSplits.length; i++) {
            	//Run the classifier on the training and testing data using the KNN model
                Evaluation validation = simpleClassify(model, trainingSplits[i], testingSplits[i]);
                predictionsList.add(validation.predictions());
            }
            
            // Calculate overall accuracy of current classifier on all splits
            double accuracy = calculateAccuracy(predictionsList,splitOutput);
        
        
        return accuracy;
    }
    
    public static void main(String[] args) throws Exception {
    	
    	//Load the CSV File 
    	CSVLoader loader = new CSVLoader();
	    loader.setSource(new File("MassHousingTrainData.csv"));
	    
	    //Create instances from the rows of data
	    Instances data = loader.getDataSet();

	    //Keep an array to store the attribute values 
	    //of rm_key and statementdate for output
	    String[] outputStrings = new String[data.size()];

	    //changing the values of letter grade from A/B/C/D/F to 0/1 ie Attribute index 7
	    int attIndex = 7;
	    
	    //Keep the attribute data for output printing
	    int rmkeyIndex = 0;
	    int stmtYearIndex = 6;
	    
	    for (int i = 0; i < data.size(); i++) {
	    	outputStrings[i] = ((int)(data.get(i).value(rmkeyIndex))+"\t"+data.get(i).stringValue(stmtYearIndex));
	    }
	    
	    //Convert A B C to 1 and D F to 0
	    for (int i = 0; i < data.size(); i++) {
	    		if(data.get(i).stringValue(attIndex).equals("A") || 
	    			data.get(i).stringValue(attIndex).equals("B") || 
	    			data.get(i).stringValue(attIndex).equals("C")) {
		    			data.get(i).setValue(attIndex,1);
	    		}
	    		else if(data.get(i).stringValue(attIndex).equals("D") || 
	    				data.get(i).stringValue(attIndex).equals("F")) {
	    					data.get(i).setValue(attIndex,0);
				}
	    }
	    
	    //Remove rest of unwanted attributes before training/testing
	    for(int i=51;i>=10;i--){
	    	data.deleteAttributeAt(i);
	    }

	    for(int i=6;i>=0;i--){
	    data.deleteAttributeAt(i);
	    }

	    //Set the output class index 0 (1 and 0 attribute column)
	    data.setClassIndex(0);
        
        // Do cross validation split with classification
        double correctCount = crossValidationSplit(data, 5,outputStrings);
        
        System.out.println("Accuracy "+correctCount*100/data.size());
           
        
    }
}