package cc.kave.episodes.mining.evaluation;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PatternValidationTest {

	private List<Tuple<Event, List<Fact>>> streamMethods;
	private Map<String, Set<IMethodName>> repoMethods;
	private List<Event> events;

	private PatternValidation sut;

	@Before
	public void setup() throws Exception {

		Episode ep = createEpisode(4, 1.0, "3", "4", "3>4");
		ep = createEpisode(3, 0.5, "6", "7", "6>7");
		ep = createEpisode(2, 0.3, "1", "3", "4");

		streamMethods = Lists.newLinkedList();
		streamMethods.add(Tuple.newTuple(enclCtx(2), Lists
				.newArrayList(new Fact(1), new Fact(3), new Fact(3),
						new Fact(4), new Fact(4))));
		streamMethods.add(Tuple.newTuple(enclCtx2(5), Lists.newArrayList(
				new Fact(6), new Fact(6), new Fact(7), new Fact(7))));
		streamMethods.add(Tuple.newTuple(enclCtx(2), Lists.newArrayList(
				new Fact(1), new Fact(3), new Fact(8), new Fact(4))));
		streamMethods.add(Tuple.newTuple(enclCtx(9), Lists.newArrayList(
				new Fact(6), new Fact(3), new Fact(7), new Fact(4))));

		repoMethods = Maps.newLinkedHashMap();
		repoMethods
				.put("Repository1", Sets.newHashSet(enclCtx(2).getMethod(),
						enclCtx(9).getMethod()));
		repoMethods
				.put("Repository2", Sets.newHashSet(enclCtx2(5).getMethod()));

		events = Lists.newArrayList(dummy(), firstCtx(1), inv(3), inv(4),
				inv(6), inv(7), inv(8), firstCtx(10), firstCtx(11),
				superCtx(12), firstCtx(13));

		sut = new PatternValidation();
	}

//	@Test
//	public void validationTest() throws Exception {
//
//		Map<Integer, Set<Triplet<Episode, Integer, Integer>>> actuals = sut
//				.validate(pattern, streamMethods, repoMethods, events,
//						valFactStream);
//		Map<Integer, Set<Triplet<Episode, Integer, Integer>>> expected = Maps
//				.newLinkedHashMap();
//		Set<Triplet<Episode, Integer, Integer>> expSet = Sets
//				.newLinkedHashSet();
//		expSet.add(new Triplet<Episode, Integer, Integer>(createEpisode(4, 1.0,
//				"3", "4", "3>4"), 1, 4));
//		expSet.add(new Triplet<Episode, Integer, Integer>(createEpisode(3, 0.5,
//				"6", "7", "6>7"), 2, 0));
//		expected.put(2, expSet);
//
//		expSet = Sets.newLinkedHashSet();
//		expSet.add(new Triplet<Episode, Integer, Integer>(createEpisode(2, 0.3,
//				"1", "3", "4"), 1, 0));
//		expected.put(3, expSet);
//
//		assertEquals(expected, actuals);
//	}

	private Episode createEpisode(int frequency, double entropy,
			String... strings) {
		Episode episode = new Episode();
		episode.addStringsOfFacts(strings);
		episode.setFrequency(frequency);
		episode.setEntropy(entropy);
		return episode;
	}

	private static Event inv(int i) {
		return Events.newInvocation(m(i));
	}

	private static Event firstCtx(int i) {
		return Events.newFirstContext(m(i));
	}

	private static Event superCtx(int i) {
		return Events.newSuperContext(m(i));
	}

	private static Event enclCtx(int i) {
		return Events.newContext(m(i));
	}

	private static Event enclCtx2(int i) {
		return Events.newContext(m2(i));
	}

	private static Event dummy() {
		return Events.newDummyEvent();
	}

	private static IMethodName m(int i) {
		if (i == 0) {
			return Names.getUnknownMethod();
		} else {
			return Names.newMethod("[T,P] [T,P].m" + i + "()");
		}
	}

	private static IMethodName m2(int i) {
		if (i == 0) {
			return Names.getUnknownMethod();
		} else {
			return Names.newMethod("[T2,P2] [T2,P2].m" + i + "()");
		}
	}
}
