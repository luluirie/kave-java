/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.episodes.repositories;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.naming.types.organization.IAssemblyName;
import cc.kave.episodes.mining.reader.ReposParser;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.statistics.StreamStatistics;

public class FrameworksDistribution {

	private File rootFolder;
	private ReposParser repos;
	private StreamStatistics statistics;

	@Inject
	public FrameworksDistribution(@Named("statistics") File folder, ReposParser repos, StreamStatistics statistics) {
		assertTrue(folder.exists(), "Statistics folder does not exist");
		assertTrue(folder.isDirectory(), "Statistics is not a folder, but a file");
		this.rootFolder = folder;
		this.repos = repos;
		this.statistics = statistics;
	}

	public void getDistribution(int numbRepos) throws IOException {

		List<Event> allEvents = repos.learningStream(numbRepos);
		Map<Event, Integer> eventsFreqs = statistics.getFrequencies(allEvents);
		Frameworks distribution = getEventsAndTypes(eventsFreqs);
		storeFrameworks(distribution, numbRepos);
	}

	private Frameworks getEventsAndTypes(Map<Event, Integer> frequencies) throws IOException {
		Frameworks frameworks = new Frameworks();

		for (Map.Entry<Event, Integer> entry : frequencies.entrySet()) {
			if (entry.getValue() == 1) {
				continue;
			}
			IAssemblyName asm = entry.getKey().getMethod().getDeclaringType().getAssembly();
			String frameworkName = asm.getIdentifier();
			String typeName = entry.getKey().getMethod().getDeclaringType().getFullName();
			if (asm.getVersion().isUnknown()) {
				continue;
			}
			// String methodName = entry.getKey().getMethod().getName();

			if (frameworks.events.containsKey(frameworkName)) {
				frameworks.events.get(frameworkName).put(entry.getKey(), entry.getValue());

				if (!frameworks.types.get(frameworkName).contains(typeName)) {
					frameworks.types.get(frameworkName).add(typeName);
				}
			} else {
				Map<Event, Integer> newFrameworkEvent = Maps.newHashMap();
				newFrameworkEvent.put(entry.getKey(), entry.getValue());
				frameworks.events.put(frameworkName, newFrameworkEvent);

				frameworks.types.put(frameworkName, Lists.newArrayList(typeName));
			}
		}
		return frameworks;
	}

	private class Frameworks {
		private Map<String, Map<Event, Integer>> events = Maps.newHashMap();
		private Map<String, List<String>> types = Maps.newHashMap();
	}

	private void storeFrameworks(Frameworks distribution, int numOfRepos) throws IOException {
		StringBuilder eventsBuilder = new StringBuilder();
		StringBuilder typesBuilder = new StringBuilder();

		for (Map.Entry<String, Map<Event, Integer>> framework : distribution.events.entrySet()) {

			String frameworkName = framework.getKey();
			eventsBuilder.append(frameworkName + "\t" + framework.getValue().size() + "\t");

			int freqTotal = 0;
			for (Map.Entry<Event, Integer> events : framework.getValue().entrySet()) {
				freqTotal += events.getValue();
			}
			eventsBuilder.append(freqTotal);
			eventsBuilder.append("\n");

			typesBuilder.append(frameworkName + "\t" + distribution.types.get(frameworkName).size() + "\n");
		}
		FilePaths paths = getPath(numOfRepos);
		FileUtils.writeStringToFile(new File(paths.eventsPath), eventsBuilder.toString());
		FileUtils.writeStringToFile(new File(paths.typesPath), typesBuilder.toString());
	}

	private FilePaths getPath(int numOfRepos) {
		File pathName = new File(rootFolder.getAbsolutePath() + "/" + numOfRepos + "Repos");
		if (!pathName.isDirectory()) {
			pathName.mkdir();
		}
		FilePaths paths = new FilePaths();
		paths.eventsPath = pathName.getAbsolutePath() + "/eventsPerFramework" + numOfRepos + ".txt";
		paths.typesPath = pathName.getAbsolutePath() + "/typesPerFramework" + numOfRepos + ".txt";

		return paths;
	}

	private class FilePaths {
		private String eventsPath = "";
		private String typesPath = "";
	}
}
