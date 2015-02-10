package accrawler.controllers;
import java.util.List;

import accrawler.common.Article;
import accrawler.common.DatabaseUtilities;
import accrawler.nlp.opennlp.ArticleInfo;
import accrawler.nlp.opennlp.IEUnit;

public class IEController {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logMessage("creating database connection");
		DatabaseUtilities.initDbConnection();
		
		logMessage("loading articles from database");
		List<Article> articles = IEUnit.readArticlesFromDb();
		logMessage("Articles Loaded");
		
		logMessage("Extracting information from articles");
		List<ArticleInfo> articleInfo = IEUnit.extractInformation(articles);
		logMessage("Article Information extracted");
		
		logMessage("saving article information");
		ArticleInfo.saveArticleInfo(articleInfo);
		logMessage("article information Saved");
		
		logMessage("closing database connection");
		DatabaseUtilities.closeDbConnection();
	}
	
	private static void logMessage(String message) {
		System.out.println(message);
	}

}
