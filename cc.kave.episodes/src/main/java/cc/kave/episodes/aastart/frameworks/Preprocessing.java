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
package cc.kave.episodes.aastart.frameworks;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.episodes.Event;
import cc.kave.episodes.export.EventStreamIo;
import cc.kave.episodes.export.EventsFilter;
import cc.kave.episodes.model.EventStream;
import cc.recommenders.io.Directory;

public class Preprocessing {

	private Directory contextsDir;
	private File eventsFolder;
	private ReductionByRepos repos;

	@Inject
	public Preprocessing(@Named("contexts") Directory directory, @Named("events") File folder, ReductionByRepos repos) {
		assertTrue(folder.exists(), "Contexts folder does not exist");
		assertTrue(folder.isDirectory(), "Contexts is not a folder, but a file");
		this.contextsDir = directory;
		this.eventsFolder = folder;
		this.repos = repos;
	}
	private static final int NUMOFREPOS = 10;
	private static final int REMFREQS = 1;

	public void generate() throws ZipException, IOException {
		List<Event> allEvents = repos.select(contextsDir, NUMOFREPOS);
		EventStream complStream = EventsFilter.filterStream(allEvents, REMFREQS);
		EventStreamIo.write(complStream, getStreamPath(), getMappingPath());
	}

	private Set<String> findZips() {
		Set<String> zips = contextsDir.findFiles(new Predicate<String>() {

			@Override
			public boolean apply(String arg0) {
				return arg0.endsWith(".zip");
			}
		});
		return zips;
	}

	private String getStreamPath() {
		File streamFile = new File(eventsFolder.getAbsolutePath() + "/eventStream.txt");
		return streamFile.getAbsolutePath();
	}

	private String getMappingPath() {
		File mappingFile = new File(eventsFolder.getAbsolutePath() + "/eventMapping.txt");
		return mappingFile.getAbsolutePath();
	}
}