package accrawler.classifier;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;
import accrawler.common.DatabaseUtilities;

public class Learner2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("aaa");
		createWekaInstances();
		System.out.println("Done");
	}
	
	public static void createWekaInstances() throws Exception {
		saveAccidents();
		saveChildren();		
	}
	
	public static Instances readInstancesFromDb(boolean isChild) {
		String name = isChild ? "child" : "accident";
		String query = "select distinct(at.article_id) as id from nf_article_tag at left join nf_tag t on t.id = at.tag_id where t.name like '%" + name + "%'";
		List<Integer> accidentIdList = new ArrayList<Integer>();
		
		List<Integer> idList = new ArrayList<Integer>(8978);
		for(int i = 1; i < 8978; i++)
		{
			idList.add(i);
		}		
		
		try {
			Connection conn = DatabaseUtilities.getDbConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int id = rs.getInt("id");
				accidentIdList.add(id);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		idList.removeAll(accidentIdList);
		Collections.shuffle(idList);
		
		List<Integer> newIdList = new ArrayList<Integer>(1300);
		for (int i = 0; i < 1000; i++) {
			newIdList.add(idList.get(i));
		}
		newIdList.addAll(accidentIdList);
		
		int numberOfInstances = newIdList.size();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select id, heading, content from nf_article where id in (");		
	    sb.append(newIdList.remove(0));
	    for(int s : newIdList) {
	    	sb.append(",");
	    	sb.append(s);
	    }	    
	    sb.append(") order by id");	    
	    query = sb.toString();
	    
	    ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2);
		Attribute a1 = new Attribute("heading", (ArrayList<String>) null);
		Attribute a2 = new Attribute("content", (ArrayList<String>) null);

		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("Yes");
		classVal.add("No");
		Attribute c = new Attribute("@@class@@", classVal);

		attributeList.add(a1);
		attributeList.add(a2);
		attributeList.add(c);
		
		Instances instances = new Instances(name, attributeList, numberOfInstances);
		instances.setClassIndex(2);
	    
	    try {
			Connection conn = DatabaseUtilities.getDbConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int id = rs.getInt("id");
				String heading = rs.getString("heading");
				String content = rs.getString("content");
				String category = accidentIdList.contains(id) ? "Yes" : "No";
				Instance inst = new DenseInstance(instances.numAttributes());
				inst.setDataset(instances);
				inst.setValue(0, heading);
				inst.setValue(1, content);
				inst.setValue(2, category);			
				instances.add(inst);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return instances;
	}
	
	static void saveAccidents() throws Exception {
		File outDir = new File("weka-instances");
		File outFile = new File(outDir, "accidents1.arff");
		Instances instances = readInstancesFromDb(false);
		saveInstances(instances, outFile);
	}
	
	static void saveChildren() throws Exception {
		File outDir = new File("weka-instances");
		File outFile = new File(outDir, "children1.arff");
		Instances instances = readInstancesFromDb(true);
		saveInstances(instances, outFile);
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

}
