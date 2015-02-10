package accrawler.classifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import accrawler.common.Article;
import accrawler.common.Tag;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ArticleTagger {

	public static final String accidentChildClassifierModelFile = "NBMT.child.model";
	public static final String YES = "Yes";
	public static final String NO = "No";

	static Map<Tag, Classifier> classifierMap = new HashMap<Tag, Classifier>();

	static void initialise() {
		classifierMap.put(Tag.CHILD, getDefaultAccidentChildClassifier());
	}

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
		classVal.add(YES);
		classVal.add(NO);
		Attribute c = new Attribute("output", classVal);

		attributeList.add(a1);
		attributeList.add(a2);
		attributeList.add(c);

		Instances instances = new Instances("myInstances", attributeList,
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

	static Classifier getDefaultAccidentChildClassifier() {
		Classifier c = null;
		try {
			File modelFile = new File("weka-models", accidentChildClassifierModelFile);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					modelFile));
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
	public static void tagArticles(List<Article> articles) {
		Instances instances = createInstances(articles);
		initialise();
		int yesIndex = instances.classAttribute().indexOfValue(YES);
		for (Entry<Tag, Classifier> e : classifierMap.entrySet()) {
			Tag t = e.getKey();
			Classifier cls = e.getValue();
			for (int k = 0; k < articles.size(); k++) {
				Instance i = instances.instance(k);
				try {
					int pred = (int) cls.classifyInstance(i);
					if (pred == yesIndex) {
						articles.get(k).addTag(t);
					}
				} catch (Exception ex) {
					System.err.print(ex.getStackTrace());
				}
			}
		}
	}

}
