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
package cc.kave.episodes.mining.reader;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.episodes.Fact;

public class StreamParser {

	private File rootFolder;
	private FileReader reader;

	@Inject
	public StreamParser(@Named("events") File folder, FileReader reader) {
		assertTrue(folder.exists(), "Event stream folder does not exist");
		assertTrue(folder.isDirectory(), "Event stream is not a folder, but a file");
		this.rootFolder = folder;
		this.reader = reader;
	}

	public List<Fact> parseStream(int numbRepos) {
		List<Fact> stream = new LinkedList<Fact>();
		List<String> lines = reader.readFile(getStreamPath(numbRepos));
		
		for (String line : lines) {
			String[] eventTime = line.split(",");
			int eventID = Integer.parseInt(eventTime[0]);
			stream.add(new Fact(eventID));
		}
		return stream;
	}

	private File getStreamPath(int numbRepos) {
		File fileName = new File(rootFolder.getAbsolutePath() + "/" + numbRepos + "Repos/stream.txt");
		return fileName;
	}
}
