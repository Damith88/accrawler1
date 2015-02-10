package accrawler.controllers;

import java.util.List;

import accrawler.classifier.ArticleCategorizer;
import accrawler.classifier.ArticleTagger;
import accrawler.common.Article;
import accrawler.common.DatabaseUtilities;

public class ClassifierController {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logMessage("creating database connection");
		DatabaseUtilities.initDbConnection();
		
		logMessage("loading articles from database");
		List<Article> articles = Article.readArticlesFromDb();
		logMessage("Articles Loaded");
		
		logMessage("start article categorizing");
		ArticleCategorizer.assignCategoryToArticles(articles);
		logMessage("Articles categorized");
		
		logMessage("saving article categories");
		Article.saveArticleCatagories(articles);
		logMessage("Article categories Saved");
		
		logMessage("start article tagging");
		ArticleTagger.tagArticles(articles);
		logMessage("Articles tagged");
		
		logMessage("saving article tags");
		Article.saveArticleTags(articles);
		logMessage("Article tags Saved");
		
		logMessage("closing database connection");
		DatabaseUtilities.closeDbConnection();
	}
	
	private static void logMessage(String message) {
		System.out.println(message);
	}

}
