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
package cc.kave.episodes.eventstream;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.organization.IAssemblyName;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.statistics.StreamStatistics;

import com.google.common.collect.Sets;

public class EventsFilter {

	private static StreamStatistics statistics = new StreamStatistics();
	// private static final String FRAMEWORKNAME = "mscorlib, 4.0.0.0";

	private static final double DELTA = 0.001;
	private static final double TIMEOUT = 0.5;
	private static double time = 0.0;
	private static boolean firstMethod = true;

	public static EventStream filterStream(List<Event> stream, int freqThresh) {
		List<Event> streamWithoutDublicates = removeMethodDublicates(stream);
		Map<Event, Integer> occurrences = statistics.getFrequencies(stream);
		EventStream es = new EventStream();

		for (Event e : streamWithoutDublicates) {
			if ((e.getKind() == EventKind.FIRST_DECLARATION)
					|| (e.getKind() == EventKind.METHOD_DECLARATION)) {
				es.addEvent(e);
				continue;
			}
			if (occurrences.get(e) >= freqThresh) {
				IAssemblyName asm = null;
				try {
					asm = e.getMethod().getDeclaringType().getAssembly();
				} catch (Exception e1) {
					continue;
				}
				// predefined types have always an unknown version, but come
				// from mscorlib, so they should be included
				if (!asm.getName().equals("mscorlib")
						&& asm.getVersion().isUnknown()) {
					continue;
				}
				es.addEvent(e);
			}
		}
		streamWithoutDublicates.clear();
		occurrences.clear();
		return es;
	}

	private static List<Event> removeMethodDublicates(List<Event> stream) {
		List<Event> results = new LinkedList<Event>();
		List<Event> method = new LinkedList<Event>();
		Set<IMethodName> observedMethods = Sets.newLinkedHashSet();
		Set<List<Event>> obsUnknownMethods = Sets.newLinkedHashSet();
		IMethodName currentMethod = null;

		for (Event event : stream) {
			if ((event.getKind() == EventKind.FIRST_DECLARATION)
					&& (currentMethod != null)) {
				if ((!observedMethods.contains(currentMethod))
						&& (!obsUnknownMethods.contains(method))) {
					results.addAll(method);
					if (currentMethod.equals(Names.getUnknownMethod())) {
						obsUnknownMethods.add(method);
					} else {
						observedMethods.add(currentMethod);
					}
				}
				method.clear();
				method = new LinkedList<Event>();
				currentMethod = null;
			} else if (event.getKind() == EventKind.METHOD_DECLARATION) {
				currentMethod = event.getMethod();
			}
			method.add(event);
		}
		if ((currentMethod != null)
				&& ((!observedMethods.contains(currentMethod)) && (!obsUnknownMethods
						.contains(method)))) {
			results.addAll(method);
		}
		observedMethods.clear();
		obsUnknownMethods.clear();
		return results;
	}

	public static String filterPartition(List<Event> partition,
			Map<Event, Integer> stream) {
		StringBuilder sb = new StringBuilder();
		time = 0.0;
		firstMethod = true;

		for (Event e : partition) {
			if (stream.keySet().contains(e)) {
				sb.append(addToPartitionStream(e, stream.get(e)));
			} else {
				if (e.getKind() == EventKind.METHOD_DECLARATION && !firstMethod) {
					time += TIMEOUT;
				}
			}
			firstMethod = false;
		}
		return sb.toString();
	}

	private static String addToPartitionStream(Event event, int eventId) {
		if ((event.getKind() == EventKind.METHOD_DECLARATION) && !firstMethod) {
			time += TIMEOUT;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(eventId);
		sb.append(',');
		sb.append(String.format("%.3f", time));
		sb.append('\n');

		time += DELTA;

		return sb.toString();
	}
}