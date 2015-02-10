package accrawler.nlp.opennlp;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import accrawler.common.Article;
import accrawler.common.DatabaseUtilities;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

public class IEUnit {

	static SentenceDetectorME sdetector;
	static Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
	static String[] finderTypes = { "person", "location", "date" };
	static NameFinderME[] finders;

	public static void initialize() throws Exception {
		File modelDir = new File("opennlp-models");
		InputStream is = new FileInputStream(new File(modelDir, "en-sent.bin"));
		SentenceModel model = new SentenceModel(is);
		sdetector = new SentenceDetectorME(model);
		is.close();
		finders = new NameFinderME[3];
		String[] types = finderTypes;
		for (int mi = 0; mi < types.length; mi++) {
			is = new FileInputStream(new File(modelDir, "en-ner-" + types[mi]
					+ ".bin"));
			finders[mi] = new NameFinderME(new TokenNameFinderModel(is));
			is.close();
		}
	}

	public static List<ArticleInfo> extractInformation(List<Article> articles) {
		try {
			initialize();
		} catch (Exception e) {
			System.err.print(e.getStackTrace());
		}
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for (Article a : articles) {
			String content = a.getContent();
			// if there is nothing to extract, then skip
			if (content.isEmpty() || content.length() < 10) {
				continue;
			}
			Span[] sSpans = sdetector.sentPosDetect(content);
			String[] sentences = new String[sSpans.length];
			for (int si = 0; si < sentences.length; si++) {
				Span s = sSpans[si];
				sentences[si] = content.substring(s.getStart(), s.getEnd());
			}
			List<NamedEntity> namedEntities = new ArrayList<NamedEntity>();
			boolean hasOverlappingSpans = false; // whether article contains
													// overlapping named
													// entities
//			Map<String, List<String>> entityMap = new HashMap<String, List<String>>();
//			entityMap.put("person", new ArrayList<String>());
//			entityMap.put("location", new ArrayList<String>());
//			entityMap.put("date", new ArrayList<String>());
			for (int si = 0; si < sentences.length; si++) {
				String sentence = sentences[si];
				Span[] tokenSpans = tokenizer.tokenizePos(sentence);
				String[] tokens = Span.spansToStrings(tokenSpans, sentence);
				List<Span> allSpans = new ArrayList<Span>();
				for (int fi = 0; fi < finders.length; fi++) {
					NameFinderME finder = finders[fi];
					Span[] spans = finder.find(tokens);
					if (!hasOverlappingSpans) {
						allSpans.addAll(Arrays.asList(spans));
					}
					double[] probs = finder.probs(spans);
					for (int j = 0; j < spans.length; j++) {
						Span s = spans[j];
						int nameStart = tokenSpans[s.getStart()].getStart();
						int nameEnd = tokenSpans[s.getEnd() - 1].getEnd();
						String value = sentence.substring(nameStart, nameEnd);
						NamedEntity ne = new NamedEntity(si, finderTypes[fi],
								nameStart, nameEnd, value, probs[j]);
						namedEntities.add(ne);
//						entityMap.get(finderTypes[fi]).add(
//								sentence.substring(nameStart, nameEnd));
					}
				}
				int nSpans = allSpans.size();
				// chances are to nSpans size would be less than 2
				if (!hasOverlappingSpans && nSpans > 1) {
					outerLoop: for (int j = 0; j < nSpans - 1; j++) {
						Span span = allSpans.get(j);
						for (int k = j + 1; k < nSpans; k++) {
							if (span.intersects(allSpans.get(k))) {
								hasOverlappingSpans = true;
								break outerLoop;
							}
						}
					}
				}
			}
			StringBuilder sb = new StringBuilder(sSpans[0].toString());
			for (int j = 1; j < sSpans.length; j++) {
				sb.append(',');
				sb.append(sSpans[j].toString());
			}
			ArticleInfo articleInfo = new ArticleInfo(a.getId(), sb.toString(),
					namedEntities, hasOverlappingSpans);
			articleInfoList.add(articleInfo);
			for (NameFinderME f : finders) {
				f.clearAdaptiveData();
			}
		}
		return articleInfoList;
	}
	
	private static int getMaxProcessedId() {
		int i = 0;
		int count = 0;
		try {
			Connection conn = DatabaseUtilities.getDbConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select IFNULL(max(article_id), 0) from article_info;");
			if (rs.next()) {
				i = rs.getInt("id");
				count++;
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (count != 1) { // asserting count equals to 1
			System.err.println("error occured while retrieving max processed id");
			System.exit(1);
		}
		return i;
	}

	public static List<Article> readArticlesFromDb() {
		int maxProcessedId = getMaxProcessedId();
		int accidentCategoryId = 2;
		List<Article> articles = new ArrayList<Article>();
		try {
			Connection conn = DatabaseUtilities.getDbConnection();
			Statement stmt = conn.createStatement();
			String query = "select id, heading, content from article where category_id = " + accidentCategoryId + " AND id > " + maxProcessedId;
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int id = rs.getInt("id");
				String heading = rs.getString("heading");
				String content = rs.getString("content");
				Article article = new Article(id, heading, content);
				articles.add(article);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return articles;
	}
}
