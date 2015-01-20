import java.util.ArrayList;
import java.util.List;

public class MainController {

	public void run() throws Exception{
		DatabaseUtilities.initDbConnection();
		
		List<Article> articles = Article.readArticlesFromDb();
		System.out.println("Articles Loaded");
		ArticleCategorizer.assignCategoryToArticles(articles);
		System.out.println("Articles categorized");
		ArticleTagger.tagArticles(articles);
		System.out.println("Articles tagged");
		Article.saveArticles(articles);
		System.out.println("Articles Saved");
		System.exit(0);
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
