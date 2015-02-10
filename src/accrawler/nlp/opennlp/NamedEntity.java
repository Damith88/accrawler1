package accrawler.nlp.opennlp;
public class NamedEntity {
	private int sentIndex;
	private String type;
	private int start;
	private int end;
	private String value;
	private double prob;

	public int getSentIndex() {
		return sentIndex;
	}

	public void setSentIndex(int sentIndex) {
		this.sentIndex = sentIndex;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public NamedEntity(int sentIndex, String type, int start, int end,
			String value, double prob) {
		this.sentIndex = sentIndex;
		this.type = type;
		this.start = start;
		this.end = end;
		this.value = value;
		this.prob = prob;
	}

	@Override
	public String toString() {
		return sentIndex + " " + type + " " + start + " "
				+ end + " " + prob;
	}
}
