package accrawler.controllers;
import java.util.ArrayList;
import java.util.List;

import accrawler.classifier.ArticleCategorizer;
import accrawler.classifier.ArticleTagger;
import accrawler.common.Article;
import accrawler.common.DatabaseUtilities;
import accrawler.nlp.opennlp.ArticleInfo;
import accrawler.nlp.opennlp.IEUnit;

public class MainController {

	public void run() throws Exception {
		DatabaseUtilities.initDbConnection();
		
		List<Article> articles = Article.readArticlesFromDb();
		System.out.println("Articles Loaded");
		ArticleCategorizer.assignCategoryToArticles(articles);
		System.out.println("Articles categorized");
		ArticleTagger.tagArticles(articles);
		System.out.println("Articles tagged");
		Article.saveArticles(articles);
		System.out.println("Articles Saved");
		List<Article> relatedArticles = new ArrayList<Article>();
		// filtering out related articles
		for (Article a : articles) {
			if (a.isAccidentRelated()) {
				relatedArticles.add(a);
			}
		}
		System.out.println("Articles filtered");
		if (relatedArticles.size() > 0) {
			List<ArticleInfo> articleInfo = IEUnit.extractInformation(relatedArticles);
			System.out.println("Articles Information extracted");
			ArticleInfo.saveArticleInfo(articleInfo);
			System.out.println("Articles Saved");
		}

		DatabaseUtilities.closeDbConnection();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		MainController mc = new MainController();
		mc.run();
	}

}
