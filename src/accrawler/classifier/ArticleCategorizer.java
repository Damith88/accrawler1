package accrawler.classifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import accrawler.common.Article;
import accrawler.common.Category;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ArticleCategorizer {
	
	public static final String accidentClassifierModelFile = "NBMT.accident.model";
	public static final String ACCIDENT_YES = "Yes";
	public static final String ACCIDENT_NO= "No";

	/**
	 * create weka instances from an article list
	 * 
	 * @param articles
	 * @return
	 */
	static Instances createInstances(List<Article> articles) {
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2);
		Attribute a1 = new Attribute("heading", (ArrayList<String>) null);
		Attribute a2 = new Attribute("content", (ArrayList<String>) null);

		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add(ACCIDENT_YES);
		classVal.add(ACCIDENT_NO);
		Attribute c = new Attribute("output", classVal);

		attributeList.add(a1);
		attributeList.add(a2);
		attributeList.add(c);

		Instances instances = new Instances("accidents", attributeList,
				articles.size());
		instances.setClassIndex(2);

		for (Article a : articles) {
			Instance inst = new DenseInstance(instances.numAttributes());
			inst.setDataset(instances);
			inst.setValue(0, a.getHeading());
			inst.setValue(1, a.getContent());
			inst.setMissing(2);			
			instances.add(inst);
		}

		return instances;
	}
	
	static Classifier getDefaultAccidentClassifier() {
		Classifier c = null;
		try {
			File modelFile = new File("weka-models", accidentClassifierModelFile);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile));
	        Object tmp = in.readObject();
	        c = (Classifier) tmp;
	        in.close();
		} catch (Exception e) {
			System.err.print(e.getStackTrace());
		}
        return c;
	}

	/**
	 * given a list of articles assigns category for them.
	 * 
	 * @param articles
	 */
	public static void assignCategoryToArticles(List<Article> articles) {
		Instances instances = createInstances(articles);
		Classifier cls = getDefaultAccidentClassifier();
		int yesIndex = instances.classAttribute().indexOfValue(ACCIDENT_YES);
		for (int k = 0; k < articles.size(); k++) {
			Instance i = instances.instance(k);
			try {
				int pred = (int) cls.classifyInstance(i);
				Category category = pred == yesIndex ? Category.ACCIDENT
						: Category.OTHER;
				articles.get(k).setCategory(category);
			} catch (Exception e) {
				System.err.print(e.getStackTrace());
			}
		}
	}

}
