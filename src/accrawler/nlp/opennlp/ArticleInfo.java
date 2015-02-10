package accrawler.nlp.opennlp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import accrawler.common.DatabaseUtilities;

public class ArticleInfo {

	int articleId;
	String sentences;
	List<NamedEntity> namedEntities;
	boolean hasOverlappingSpans = false;
	
	public int getArticleId() {
		return articleId;
	}

	public void setArticleId(int articleId) {
		this.articleId = articleId;
	}

	public String getSentences() {
		return sentences;
	}

	public void setSentences(String sentences) {
		this.sentences = sentences;
	}

	public List<NamedEntity> getNamedEntities() {
		return namedEntities;
	}

	public void setNamedEntities(List<NamedEntity> namedEntities) {
		this.namedEntities = namedEntities;
	}

	public boolean hasOverlappingSpans() {
		return hasOverlappingSpans;
	}

	public void setHasOverlappingSpans(boolean hasOverlappingSpans) {
		this.hasOverlappingSpans = hasOverlappingSpans;
	}

	public ArticleInfo(int articleId, String sentences,
			List<NamedEntity> namedEntities, boolean hasOverlappingSpans) {
		this.articleId = articleId;
		this.sentences = sentences;
		this.namedEntities = namedEntities;
		this.hasOverlappingSpans = hasOverlappingSpans;
	}

	public static void saveNamedEntities(List<ArticleInfo> articleInfoList) {
		PreparedStatement stmt = null;
		Connection conn = DatabaseUtilities.getDbConnection();
		String query = "INSERT INTO named_entity (article_id, type, name) VALUES (?, ?, ?)";
		try {
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(query);
			for (ArticleInfo a : articleInfoList) {
				for (NamedEntity ne : a.getNamedEntities()) {
					stmt.setInt(1, a.getArticleId());
					stmt.setString(2, ne.getType());
					stmt.setString(3, ne.getValue());
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

	public static void saveArticleInfo(List<ArticleInfo> articleInfoList) {
		PreparedStatement stmt = null;
		Connection conn = DatabaseUtilities.getDbConnection();
		String query = "INSERT INTO article_info (article_id, sentences, named_entity_info, has_overlapping_entities) VALUES (?, ?, ?, ?)";
		try {
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(query);
			for (ArticleInfo a : articleInfoList) {
				stmt.setInt(1, a.getArticleId());
				stmt.setString(2, a.getSentences());
				stmt.setString(3, a.getNamedEntities().toString());
				stmt.setBoolean(4, a.hasOverlappingSpans());
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
		saveNamedEntities(articleInfoList);
	}
}
