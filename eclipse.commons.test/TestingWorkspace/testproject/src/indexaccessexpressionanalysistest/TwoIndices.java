package indexaccessexpressionanalysistest;

public class TwoIndices {

	int[][] i = { { 1, 2, 3 }, { 3, 4, 5 }, { 6, 7, 8 } };

	public void method() {
		int j = i[1][2];
	}
}
