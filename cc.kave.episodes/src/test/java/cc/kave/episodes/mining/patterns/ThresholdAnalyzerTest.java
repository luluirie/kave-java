package cc.kave.episodes.mining.patterns;

import static cc.recommenders.io.LoggerUtils.assertLogContains;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EpisodeType;
import cc.recommenders.io.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ThresholdAnalyzerTest {

	@Mock
	private EpisodeParser parser;
	@Mock
	private PatternFilter filter;

	private static final int FREQUENCY = 200;
	private static final double ENTROPY = 0.5;

	private Map<Integer, Set<Episode>> episodes;

	private ThresholdAnalyzer sut;

	@Before
	public void setup() throws Exception {
		Logger.reset();
		Logger.setCapturing(true);

		MockitoAnnotations.initMocks(this);

		episodes = Maps.newLinkedHashMap();

		Set<Episode> twoNodes = Sets.newLinkedHashSet();
		twoNodes.add(createEpisode(200, 1.0, "1", "2", "1>2"));
		twoNodes.add(createEpisode(220, 1.0, "1", "2", "2>1"));
		twoNodes.add(createEpisode(240, 0.5, "1", "2"));
		episodes.put(2, twoNodes);

		Set<Episode> threNodes = Sets.newLinkedHashSet();
		threNodes.add(createEpisode(200, 0.5, "7", "8", "34", "34>7", "34>8"));
		threNodes.add(createEpisode(220, 0.7, "7", "8", "34", "7>8"));
		threNodes.add(createEpisode(240, 0.5, "7", "8", "34", "7>8", "34>8"));
		threNodes.add(createEpisode(200, 0.7, "7", "8", "34", "7>8", "7>34"));
		threNodes.add(createEpisode(220, 0.5, "7", "8", "34", "7>8", "7>34",
				"8>34"));
		threNodes.add(createEpisode(240, 0.7, "7", "8", "34", "7>34", "7>8",
				"34>8"));
		threNodes.add(createEpisode(200, 0.7, "7", "8", "34", "34>7", "34>8",
				"7>8"));
		threNodes.add(createEpisode(220, 0.5, "7", "8", "34", "8>7", "34>7"));
		threNodes.add(createEpisode(240, 0.5, "7", "8", "34", "34>8", "34>7",
				"8>7"));
		episodes.put(3, threNodes);

		sut = new ThresholdAnalyzer(parser, filter);

		when(parser.parser(anyInt())).thenReturn(episodes);
		when(
				filter.filter(eq(EpisodeType.GENERAL), anyMap(), anyInt(),
						anyDouble())).thenReturn(episodes);
	}

	@After
	public void teardown() {
		Logger.reset();
	}

	@Test
	public void oneDim() throws Exception {
		sut.EntDim(FREQUENCY);

		assertLogContains(0, "\tEntropy threshold analyses!");
		assertLogContains(1, "\tFrequency\tEntropy\t#Patterns");
		assertLogContains(2, "\t200\t0.00\t12");

		verify(parser).parser(anyInt());
		verify(filter, times(606)).filter(eq(EpisodeType.GENERAL),
				eq(episodes), anyInt(), anyDouble());
	}

	@Test
	public void twoDim() throws Exception {
		sut.EntFreqDim(FREQUENCY);

		assertLogContains(0, "\tFrequency-entropy analyzes!");
		assertLogContains(1, "\tFrequency\tEntropy\t#Patterns");
		assertLogContains(2, "\t200\t0.00\t12");

		verify(parser, times(2)).parser(anyInt());
		verify(filter, times(303)).filter(eq(EpisodeType.GENERAL),
				eq(episodes), anyInt(), anyDouble());
	}

	@Test
	public void histogram() throws Exception {
		sut.createHistogram(EpisodeType.GENERAL, FREQUENCY, ENTROPY);

		assertLogContains(0, "\tHistogram for GENERAL-configuration:");
		assertLogContains(1, "\tEntropy threshold = 0.5");
		assertLogContains(2, "\tFrequency\t#Patterns");
		assertLogContains(3, "\t200\t12");

		verify(parser).parser(anyInt());
		verify(filter, times(9)).filter(eq(EpisodeType.GENERAL), eq(episodes),
				anyInt(), anyDouble());
	}

	private Episode createEpisode(int freq, double bdmeas, String... strings) {
		Episode episode = new Episode();
		episode.setFrequency(freq);
		episode.setEntropy(bdmeas);
		for (String fact : strings) {
			episode.addFact(fact);
		}
		return episode;
	}
}