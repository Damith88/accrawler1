import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Article {

	private int id;
	private String heading = null;
	private String content = null;
	private Category category;
	private List<Tag> tags;

	public Article(int id, String heading, String content) {
		this.id = id;
		this.heading = heading;
		this.content = content;
		category = null;
		tags = new ArrayList<Tag>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public boolean addTag(Tag t) {
		return tags.add(t);
	}

	public boolean isAccidentRelated() {
		return category == Category.ACCIDENT;
	}

	public boolean isChildRelated() {
		return tags.contains(Tag.CHILD);
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public static List<Article> readArticlesFromDb() {
		List<Article> articles = new ArrayList<Article>();
		try {
			Connection conn = DatabaseUtilities.getDbConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select id, heading, content from article where category_id IS NULL");
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

	public static void saveArticles(List<Article> articles) {
		saveArticleCatagories(articles);
		saveArticleTags(articles);
	}

	public static void saveArticleCatagories(List<Article> articles) {
		PreparedStatement stmt = null;
		Connection conn = DatabaseUtilities.getDbConnection();
		String query = "UPDATE article SET category_id = ? where id = ?";
		try {
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(query);
			for (Article a : articles) {
				stmt.setInt(1, a.getCategory().getId());
				stmt.setInt(2, a.getId());
				stmt.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					excep.printStackTrace();
				}
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				conn.setAutoCommit(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveArticleTags(List<Article> articles) {
		PreparedStatement stmt = null;
		Connection conn = DatabaseUtilities.getDbConnection();
		String query = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
		try {
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(query);
			for (Article a : articles) {
				for (Tag t : a.getTags()) {
					stmt.setInt(1, a.getId());
					stmt.setInt(2, t.getId());
					stmt.executeUpdate();
				}
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					excep.printStackTrace();
				}
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				conn.setAutoCommit(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

enum Category {
	OTHER(1, "other"), ACCIDENT(2, "accident");

	private String name;
	private int id;

	Category(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}

enum Tag {
	CHILD(1, "child");

	private String name;
	private int id;

	Tag(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
