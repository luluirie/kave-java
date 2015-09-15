package cc.kave.commons.model.groum;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import org.junit.Test;

import cc.kave.commons.model.groum.comparator.DFSGroumComparator;

import static org.junit.Assert.assertEquals;

import static cc.kave.commons.model.groum.GroumTestUtils.*;
import static cc.kave.commons.model.groum.GroumBuilder.*;

public class GroumTest {

	@Test
	public void producesSubGroumsOfSizeOne() {
		Node[] nodes = createNodes("A", "B");
		Groum groum = buildGroum(nodes).build();

		SubGroumMultiSet atomicSubGroums = groum.getAtomicSubGroums();

		assertContainsAll(atomicSubGroums, createSubGroum(groum, nodes[0]),
				createSubGroum(groum, nodes[1]));
	}

	@Test
	public void producesMultipleSubGroumsForEqualNodes() {
		Node[] nodes = createNodes("A", "A");
		Groum groum = buildGroum(nodes).build();

		SubGroumMultiSet atomicSubGroums = groum.getAtomicSubGroums();

		assertContainsAll(atomicSubGroums, createSubGroum(groum, nodes[0]),
				createSubGroum(groum, nodes[1]));
	}

	private SubGroum createSubGroum(Groum groum, Node node) {
		return new SubGroum(groum, Collections.singleton(node));
	}

	static void assertContainsAll(SubGroumMultiSet actuals, SubGroum... expecteds) {
		TreeSet<SubGroum> actual = new TreeSet<SubGroum>(new DFSGroumComparator());
		actual.addAll(actuals.getAllInstances());
		TreeSet<SubGroum> expected = new TreeSet<SubGroum>(new DFSGroumComparator());
		expected.addAll(Arrays.asList(expecteds));
		assertEquals(expected, actual);
	}
}