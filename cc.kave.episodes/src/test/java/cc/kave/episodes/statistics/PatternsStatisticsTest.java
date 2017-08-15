package cc.kave.episodes.statistics;

import static cc.recommenders.io.LoggerUtils.assertLogContains;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cc.kave.episodes.io.EpisodeReader;
import cc.kave.episodes.mining.patterns.ParallelPatterns;
import cc.kave.episodes.mining.patterns.PartialPatterns;
import cc.kave.episodes.mining.patterns.SequentialPatterns;
import cc.kave.episodes.model.Episode;
import cc.recommenders.io.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PatternsStatisticsTest {

	private static final int FREQUENCY = 2;
	private static final double ENTROPY = 0.5;

	@Mock
	private EpisodeReader episodeReader;

	private Map<Integer, Set<Episode>> episodes;

	private PartialPatterns partials;
	private SequentialPatterns sequentials;
	private ParallelPatterns parallels;

	private PatternsStatistics sut;

	@Before
	public void setup() {
		Logger.reset();
		Logger.setCapturing(true);

		MockitoAnnotations.initMocks(this);

		episodes = Maps.newLinkedHashMap();
		Set<Episode> level = Sets.newLinkedHashSet();
		level.add(createEpisode(2, 0.3, "1"));
		level.add(createEpisode(3, 0.5, "2"));
		episodes.put(1, level);

		level = Sets.newLinkedHashSet();
		level.add(createEpisode(4, 0.6, "1", "2"));
		level.add(createEpisode(5, 1.0, "1", "2", "1>2"));
		level.add(createEpisode(8, 1.0, "1", "2", "2>1"));
		episodes.put(2, level);

		level = Sets.newLinkedHashSet();
		level.add(createEpisode(3, 0.5, "1", "2", "3"));
		level.add(createEpisode(3, 0.6, "1", "2", "3", "1>2"));
		level.add(createEpisode(4, 0.7, "1", "2", "3", "2>1", "2>3"));
		level.add(createEpisode(5, 0.8, "1", "2", "3", "1>3"));
		level.add(createEpisode(4, 1.0, "1", "2", "3", "1>2", "1>3", "2>3"));
		level.add(createEpisode(4, 1.0, "1", "2", "3", "2>1", "2>3", "1>3"));
		level.add(createEpisode(6, 1.0, "1", "2", "3", "3>1", "3>2", "1>2"));
		episodes.put(3, level);

		partials = new PartialPatterns();
		sequentials = new SequentialPatterns();
		parallels = new ParallelPatterns();
		
		when(episodeReader.read(anyInt())).thenReturn(episodes);

		sut = new PatternsStatistics(episodeReader, partials, sequentials,
				parallels);
	}
	
	@After
	public void teardown() {
		Logger.reset();
	}
	
	@Test
	public void logger() {
		sut.numPatterns(FREQUENCY, FREQUENCY, ENTROPY);
		
		verify(episodeReader).read(anyInt());
		
		assertLogContains(0, "Number of patterns learned by partial configuration: 2");
		assertLogContains(1, "Number of patterns learned by sequential configuration: 5");
		assertLogContains(2, "Number of patterns learned by parallel configuration: 2");
	}

	private Episode createEpisode(int frequency, double entropy,
			String... strings) {
		Episode episode = new Episode();
		episode.setFrequency(frequency);
		episode.setEntropy(entropy);
		episode.addStringsOfFacts(strings);

		return episode;
	}
}
