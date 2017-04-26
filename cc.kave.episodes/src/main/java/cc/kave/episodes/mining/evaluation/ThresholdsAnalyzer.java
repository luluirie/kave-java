package cc.kave.episodes.mining.evaluation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.episodes.io.EpisodesParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.RepositoriesParser;
import cc.kave.episodes.io.ValidationDataIO;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EpisodeType;
import cc.kave.episodes.model.Threshold;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Fact;
import cc.kave.episodes.postprocessor.EpisodesFilter;
import cc.recommenders.datastructures.Tuple;
import cc.recommenders.io.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ThresholdsAnalyzer {

	private EventStreamIo eventStream;
	private EpisodesParser episodeParser;
	private EpisodesFilter episodeFilter;

	private RepositoriesParser repoParser;
	private ValidationDataIO validationIo;

	private PatternsValidation patternsValidation;

	@Inject
	public ThresholdsAnalyzer(EventStreamIo eventStream, EpisodesParser parser,
			EpisodesFilter filter, RepositoriesParser reposParser,
			ValidationDataIO validationIo, PatternsValidation validation) {
		this.eventStream = eventStream;
		this.episodeParser = parser;
		this.episodeFilter = filter;
		this.repoParser = reposParser;
		this.validationIo = validationIo;
		this.patternsValidation = validation;
	}

	public void analyze(EpisodeType type, int frequency, int foldNum,
			int freqThresh, double entThresh) throws Exception {
		Logger.log("Reading repositories - enclosing method declarations mapper!");
		repoParser.generateReposEvents();
		Map<String, Set<ITypeName>> repoCtxMapper = repoParser
				.getRepoTypesMapper();
		Logger.log("Reading events ...");
		List<Event> trainEvents = eventStream.readMapping(frequency, foldNum);
		Logger.log("Reading training stream ...");
		List<Tuple<Event, List<Fact>>> streamContexts = eventStream
				.parseStream(frequency, foldNum);
		Logger.log("Reading validation data ...");
		List<Event> valData = validationIo.read(frequency, foldNum);
		Map<Event, Integer> eventsMap = mergeEventsToMap(trainEvents, valData);
		List<Event> eventsList = Lists.newArrayList(eventsMap.keySet());
		List<List<Fact>> valStream = validationIo.streamOfFacts(valData,
				eventsMap);

		Map<Integer, Set<Episode>> episodes = episodeParser.parse(type,
				frequency, foldNum);
		String title = "Frequency\tEntropy\tNumGens\tNumSpecs\tFraction";
		if (type == EpisodeType.GENERAL) {
			title += "\tNumPartials";
		}
		Logger.log("\t%s", title);

		if ((freqThresh > 0) && (entThresh == 0.0)) {
			Set<Double> entropies = getEntropies(episodes, freqThresh);
			Logger.log("Number of entropy thresholds: %d", entropies.size());
			for (double entropy : entropies) {
				Map<Integer, Set<Episode>> patterns = episodeFilter.filter(
						type, episodes, freqThresh, entropy);
				Map<Integer, Set<Triplet<Episode, Integer, Integer>>> validations = patternsValidation
						.validate(patterns, streamContexts, repoCtxMapper,
								eventsList, valStream);
				Threshold threshItem = getThreshResults(validations);
				threshItem.setFrequency(freqThresh);
				threshItem.setEntropy(entropy);
				printInfo(type, threshItem, patterns);
			}
		} else if ((freqThresh == 0) && (entThresh > 0.0)) {
			Set<Integer> frequencies = getFrequencies(episodes, entThresh);
			Logger.log("Number of frequency thresholds: %d", frequencies.size());
			for (int freq : frequencies) {
				Map<Integer, Set<Episode>> patterns = episodeFilter.filter(
						type, episodes, freq, entThresh);
				Map<Integer, Set<Triplet<Episode, Integer, Integer>>> validations = patternsValidation
						.validate(patterns, streamContexts, repoCtxMapper,
								eventsList, valStream);
				Threshold threshItem = getThreshResults(validations);
				threshItem.setFrequency(freq);
				threshItem.setEntropy(entThresh);
				printInfo(type, threshItem, patterns);
			}
		}
	}

	private Set<Integer> getFrequencies(Map<Integer, Set<Episode>> episodes,
			double entThresh) {
		SortedSet<Integer> frequencies = new TreeSet<Integer>();

		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			for (Episode episode : entry.getValue()) {
				if (episode.getEntropy() >= entThresh) {
					frequencies.add(episode.getFrequency());
				}
			}
		}
		
		
//		int frequency = 400;
//		while (frequency < 2964) {
//			frequencies.add(frequency);
//			frequency += 10;
//		}
		
		
		return frequencies;
	}

	private Set<Double> getEntropies(Map<Integer, Set<Episode>> episodes,
			int frequency) {
		SortedSet<Double> entropies = new TreeSet<Double>();

		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			for (Episode episode : entry.getValue()) {
				if (episode.getFrequency() >= frequency) {
					double entropy = episode.getEntropy();
					double roundEnt = Math.floor(entropy * 100) / 100;
					entropies.add(roundEnt);
				}
			}
		}
		return entropies;
	}

	private void printInfo(EpisodeType type, Threshold item,
			Map<Integer, Set<Episode>> patterns) {
		String data = "\t" + item.getFrequency();
		data += "\t" + item.getEntropy();
		data += "\t" + item.getNoGenPatterns();
		data += "\t" + item.getNoSpecPatterns();
		data += "\t" + Math.floor(item.getFraction() * 1000) / 1000;
		if (type == EpisodeType.GENERAL) {
			data += "\t" + partialsCounter(patterns);
		}
		Logger.log("%s", data);
	}

	private Threshold getThreshResults(
			Map<Integer, Set<Triplet<Episode, Integer, Integer>>> patterns) {
		Threshold item = new Threshold();

		for (Map.Entry<Integer, Set<Triplet<Episode, Integer, Integer>>> entry : patterns
				.entrySet()) {
			Set<Triplet<Episode, Integer, Integer>> episodeSet = entry
					.getValue();
			for (Triplet<Episode, Integer, Integer> triplet : episodeSet) {

				if ((triplet.getThird() == 0) && (triplet.getSecond() < 2)) {
					item.addSpecPattern();
				} else {
					item.addGenPattern();
				}
			}
		}
		return item;
	}

	private boolean isPartial(Episode episode) {
		Set<Fact> events = episode.getEvents();
		Set<Fact> relations = episode.getRelations();
		int numRels = relations.size();

		if ((numRels != 0) && (numRels < maxRels(events.size()))) {
			return true;
		}
		return false;
	}

	private int maxRels(int numEvents) {
		if (numEvents < 3) {
			return 1;
		} else {
			return (numEvents - 1) + maxRels(numEvents - 1);
		}
	}

	private int partialsCounter(Map<Integer, Set<Episode>> episodes) {
		int counter = 0;

		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			if (entry.getKey() < 3) {
				continue;
			}
			for (Episode episode : entry.getValue()) {
				if (isPartial(episode)) {
					counter++;
				}
			}
		}
		return counter;
	}

	private Map<Event, Integer> mergeEventsToMap(List<Event> lista,
			List<Event> listb) {
		Map<Event, Integer> events = Maps.newLinkedHashMap();
		int id = 0;

		for (Event event : lista) {
			events.put(event, id);
			id++;
		}
		for (Event event : listb) {
			if (!events.containsKey(event)) {
				events.put(event, id);
				id++;
			}
		}
		return events;
	}
}