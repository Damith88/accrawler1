package accrawler.classifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Learner {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//saveAccidents();
		//saveChildren();
		saveSMOClassifier(true);
		saveSMOClassifier(false);
//		saveNBMTextClassifier(true);
//		saveJ48Classifier(true);
//		saveNBMClassifier(true);
	}
	
	static void saveNBMTextClassifier(boolean child) throws Exception {
		Instances trainData = loadFromFile(child ? "children.arff" : "accidents.arff");
		trainData.setClassIndex(trainData.numAttributes() - 1);
		Classifier cls = new NaiveBayesMultinomialText();
		cls.buildClassifier(trainData);
		saveModel("NBMT." + (child ? "child" : "accident") + ".model", cls);
	}
	
	static void saveSMOClassifier(boolean child) throws Exception {
		Instances trainData = loadFromFile(child ? "children2.arff" : "accidents2.arff");
		trainData.setClassIndex(trainData.numAttributes() - 1);
		FilteredClassifier cls = new FilteredClassifier();
		cls.setClassifier(new SMO());
		StringToWordVector f = new StringToWordVector();
		f.setInputFormat(trainData);
		f.setAttributeIndices("1-2");
		cls.setFilter(f);
		cls.buildClassifier(trainData);
		saveModel("SMO." + (child ? "child" : "accident") + ".model", cls);
	}
	
	static void saveJ48Classifier(boolean child) throws Exception {
		Instances trainData = loadFromFile(child ? "children.arff" : "accidents.arff");
		trainData.setClassIndex(trainData.numAttributes() - 1);
		FilteredClassifier cls = new FilteredClassifier();
		cls.setClassifier(new J48());
		StringToWordVector f = new StringToWordVector();
		f.setInputFormat(trainData);
		f.setAttributeIndices("1-2");
		cls.setFilter(f);
		cls.buildClassifier(trainData);
		saveModel("J48." + (child ? "child" : "accident") + ".model", cls);
	}
	
	static void saveNBMClassifier(boolean child) throws Exception {
		Instances trainData = loadFromFile(child ? "children.arff" : "accidents.arff");
		trainData.setClassIndex(trainData.numAttributes() - 1);
		FilteredClassifier cls = new FilteredClassifier();
		cls.setClassifier(new NaiveBayes());
		StringToWordVector f = new StringToWordVector();
		f.setInputFormat(trainData);
		f.setAttributeIndices("1-2");
		cls.setFilter(f);
		cls.buildClassifier(trainData);
		saveModel("NBM." + (child ? "child" : "accident") + ".model", cls);
	}
	
	static void saveModel(String modelName, Classifier cls) throws Exception {
		File file = new File("weka-models", modelName);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(cls);
        out.close();
	}
	
	static void saveAccidents() throws Exception {
		File outDir = new File("weka-instances");
		File outFile = new File(outDir, "accidents.arff");
		Instances instances = readInstancesFromDb(false);
		saveInstances(instances, outFile);
	}
	
	static void saveChildren() throws Exception {
		File outDir = new File("weka-instances");
		File outFile = new File(outDir, "children.arff");
		Instances instances = readInstancesFromDb(true);
		saveInstances(instances, outFile);
	}

	public static void saveInstances(Instances instances, String outFile) {
		File f = new File(outFile);
		saveInstances(instances, f);
	}

	public static void saveInstances(Instances instances, File outFile) {
		ArffSaver saver = new ArffSaver();
		saveInstances(saver, instances, outFile);
	}

	public static void saveInstances(AbstractFileSaver saver,
			Instances instances, File outputFile) {
		try {
			saver.setInstances(instances);
			saver.setFile(outputFile);
			saver.writeBatch();
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		}
	}

	public static Instances readInstancesFromDb(boolean child) throws Exception {
		InstanceQuery query = new InstanceQuery();
		query.setCustomPropsFile(new File("DatabaseUtils.props"));
		query.setUsername("root");
		query.setPassword("123");
		query.setQuery("SELECT heading, content, " +
				"IF(rating = " + (child ? '2' : '3') + " OR rating = 4, 'Yes', 'No') as '@@class@@' " +
				"FROM dn_article a " +
				"INNER JOIN rating10 r on r.article_id = a.id;");
		Instances instances = query.retrieveInstances();
		// nominal to string filter should be applied before saving as heading and content are string attributes.
		NominalToString f = new NominalToString();
		f.setAttributeIndexes("1-2");
		f.setInputFormat(instances);
		Instances filtered = Filter.useFilter(instances, f);
		filtered.setClassIndex(2);
		filtered.setRelationName(child ? "children" : "accident");
		return filtered;
	}

	public static Instances loadFromFile(String fileName) throws Exception {
		File outDir = new File("weka-instances");
		File file = new File(outDir, fileName);
		return loadFromFile(file);
	}

	public static Instances loadFromFile(File file) throws Exception {
		DataSource ds = new DataSource(new FileInputStream(file));
		return ds.getDataSet();		
	}

}
