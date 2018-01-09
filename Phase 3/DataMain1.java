package com.AI.phase1;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;

public class DataMain1 {
	
	static double no_of_wrongA = 0;
    static double no_of_wrongB = 0;
    static double no_of_wrongC = 0;
    static double no_of_wrongD = 0;
    static double no_of_wrongF = 0;
    
    public static Evaluation simpleClassify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
        
    	Evaluation eval = new Evaluation(trainingSet);
        model.buildClassifier(trainingSet);
        
        eval.evaluateModel(model, testingSet);
        
        return eval;
    }
    
    public static double calAccuracy(ArrayList<Prediction> predictionsList, String[] outputStrings) {
    	double correct = 0;
        double incorrect = 0;
        
        String prediction = "";
        String actual = "";
        System.out.println("rm_key | statement_date | predicted | actual ");
        for (int j = 0; j < predictionsList.size(); j++) {
        	Prediction np = (Prediction) predictionsList.get(j);
//          System.out.print(np.predicted()+"\t"+np.actual()+"\n");
	        if (np.predicted() == np.actual()) {
	            correct++;
	        }
	        else
	        {
	            if(np.predicted() == 0) {
	            	prediction = "A";
	            	no_of_wrongC++; //Calculating number of wrongly predicted A's
	            }
	            if(np.predicted() == 1) {
	            	prediction = "B";
	            	no_of_wrongD++; //Calculating number of wrongly predicted B's
	            }
	            if(np.predicted() == 2) {
	            	prediction = "D";
	            	no_of_wrongF++; //Calculating number of wrongly predicted C's
	            }
	            if(np.predicted() == 3) {
	            	prediction = "C";
	           		no_of_wrongB++; //Calculating number of wrongly predicted D's
	            }
	            if(np.predicted() == 4) {
	            	prediction = "F";
	            	no_of_wrongA++; //Calculating number of wrongly predicted F's
	            }
	            if(np.actual() == 0) {
	            	actual = "A";
	            }
	            if(np.actual() == 1) {
	            	actual = "B";
	            }
	            if(np.actual() == 2) {
	            	actual = "D";
	            }
	            if(np.actual() == 3) {
	            	actual = "C";
	            }
	            if(np.actual() == 4) {
	            	actual = "F";
	            }
	            
	            if(prediction == actual) {
	            	correct++;
	            }
//	            else {
	            	System.out.println(outputStrings[j]+"\t  "+prediction+"\t      "+actual);
	            	incorrect++;
	            }  
        }
        System.out.println("-------------------------------------------------");
        System.out.println(" ");
    	
        System.out.println("Total false positives :"+incorrect);
        return correct;
    }
    
    public static Instances filterColumns (Instances data) {
    	//delete unwanted attributes
    	//keep columns 7,10,13
    	for(int i=51;i>=18;i--){
	    	data.deleteAttributeAt(i);
	    }
    	data.deleteAttributeAt(14);
	    data.deleteAttributeAt(12);
	    data.deleteAttributeAt(11);
	    data.deleteAttributeAt(9);
	    data.deleteAttributeAt(8);
	    for(int i=6;i>=0;i--){
	    	data.deleteAttributeAt(i);
	    }
	    data.setClassIndex(0); //Choose the attribute to set as the class (Financial Letter grade for this dataSet)
    	return data;
    }
    
    public static Instances filterRows (Instances data) {
    	double ACount = 0;
	    double deletedA = 0;
	    
    	for(int i = data.size()-1; i>=0; i--) {
    		if(data.instance(i).stringValue(7).equals("A")) {
    			ACount++;
    			
    			if(data.instance(i).value(8) < 1.5 || data.instance(i).value(8) > 3) {
    				data.delete(i);
    				deletedA++;
    			}
    		}
    	}
    	System.out.println("Total nos of A = "+ACount);
    	System.out.println("No of deleted A = "+deletedA);
    	System.out.println("data size = "+data.size());
    	return data;
    }
    
    public static void main(String[] args) throws Exception {
        
    	CSVLoader loaderTrain = new CSVLoader();
    	CSVLoader loaderTest = new CSVLoader();
	    loaderTrain.setSource(new File("MassHousingTrainData.csv"));
	    loaderTest.setSource(new File("Phase 3_Test_Set.csv"));
	    
	    int stmtYearIndex = 6;
	    String[] outputStrings = new String[loaderTest.getDataSet().size()];
	    for (int i = 0; i < loaderTest.getDataSet().size(); i++) {
	    	int rmkeyIndex = 0;
	    	outputStrings[i] = ((int)(loaderTest.getDataSet().get(i).value(rmkeyIndex))+"\t"+loaderTest.getDataSet().get(i).stringValue(stmtYearIndex));
	    }
	    
	    Instances trainData = filterColumns(loaderTrain.getDataSet());
	    Instances testData = filterColumns(loaderTest.getDataSet());
	    
	    /*Standardize filter = new Standardize();
	    filter.setInputFormat(trainData);  // initializing the filter once with training set
	    Instances newTrain = Filter.useFilter(trainData, filter);  // configures the Filter based on train instances and returns filtered instances
	    Instances newTest = Filter.useFilter(testData, filter);*/    // create new test set
	    
	    Classifier model = new IBk();
        ArrayList<Prediction> predictionsList;// = new ArrayList<Prediction>();
        
//        Evaluation validation = simpleClassify(model, trainData, testData);
        Evaluation validation = simpleClassify(model, trainData, testData);
        //System.out.println(Arrays.deepToString(validation.confusionMatrix()));
        /*for(int i=0; i<testData.size();i++) {
        	double predictions = validation.evaluateModelOnceAndRecordPrediction(model, testData.get(i));
        	System.out.println(predictions);
        }*/
        
        predictionsList = validation.predictions();
	    
        double accuracy = calAccuracy(predictionsList,outputStrings);
	    
        System.out.println("Total Prediction Accuracy: "+ accuracy*100/testData.size());
	    
    }
}