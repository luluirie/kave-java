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
package cc.kave.episodes.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import cc.kave.commons.model.episodes.Event;
import cc.kave.commons.model.episodes.Events;
import cc.kave.commons.model.episodes.Fact;
import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.model.names.csharp.MethodName;
import cc.kave.commons.model.names.csharp.TypeName;
import cc.kave.episodes.model.Episode;
import cc.recommenders.datastructures.Tuple;

public class PatternExtractorTest {

	private List<List<Fact>> stream = new LinkedList<>();
	private List<Event> events = new LinkedList<>();
	
	private PatternExtractor sut;
	
	@Before 
	public void setup() {
		
		List<Fact> method = new LinkedList<Fact>();
 		method.add(new Fact(1));	//FM
		method.add(new Fact(2));	//SM
		method.add(new Fact(3));	//EM1
		method.add(new Fact(4));	//I1
		method.add(new Fact(5));	//I2
		method.add(new Fact(6));	//I3
		stream.add(method);
		
		method = new LinkedList<Fact>();
		method.add(new Fact(1));
		method.add(new Fact(7));	//EM2
		method.add(new Fact(6));	
		method.add(new Fact(4));	
		stream.add(method);
		
		method = new LinkedList<Fact>();
		method.add(new Fact(2));
		method.add(new Fact(3));
		method.add(new Fact(6));	
		stream.add(method);
		
		method = new LinkedList<Fact>();
		method.add(new Fact(3));	
		method.add(new Fact(5));	
		method.add(new Fact(4));
		method.add(new Fact(6));
		stream.add(method);
		
		method = new LinkedList<Fact>();
		method.add(new Fact(1));
		method.add(new Fact(8));	//EM4
		method.add(new Fact(5));
		method.add(new Fact(9));	//I4
		method.add(new Fact(4));
		method.add(new Fact(6));
		stream.add(method);
		
		events.add(Events.newDummyEvent());
		events.add(Events.newFirstContext(m(1, 11)));
		events.add(Events.newSuperContext(m(1, 21)));
		events.add(Events.newContext(m(1, 31)));
		events.add(Events.newInvocation(m(1, 1)));
		events.add(Events.newInvocation(m(1, 2)));
		events.add(Events.newInvocation(m(1, 3)));
		events.add(Events.newContext(m(2, 32)));
		events.add(Events.newContext(m(5, 34)));
		events.add(Events.newInvocation(m(5, 4)));
		
		sut = new PatternExtractor();
	}
	
	@Test
	public void invocations() throws Exception {
		Episode episode = new Episode();
		episode.addFact(new Fact(4));
		episode.addFact(new Fact(5));
		episode.addFact(new Fact(6));
		
		Set<IMethodName> methodNames = Sets.newLinkedHashSet();
		methodNames.add(m(1, 31));
		methodNames.add(m(5, 34));
		
		Tuple<Set<IMethodName>, Integer> expected = Tuple.newTuple(methodNames, 3);
		
		Tuple<Set<IMethodName>, Integer> actuals = sut.getMethodsFromCode(episode, stream, events, false);
		
		assertEquals(expected.getFirst(), actuals.getFirst());
		assertEquals(expected.getSecond(), actuals.getSecond());
	}
	
	@Test
	public void invocationsWithOrderRelations() throws Exception {
		Episode episode = new Episode();
		episode.addFact(new Fact(4));
		episode.addFact(new Fact(5));
		episode.addFact(new Fact(6));
		episode.addFact(new Fact("4>5"));
		episode.addFact(new Fact("4>6"));
		episode.addFact(new Fact("5>6"));
		
		Set<IMethodName> methodsName = Sets.newHashSet(m(1, 31));
		Tuple<Set<IMethodName>, Integer> expected = Tuple.newTuple(methodsName, 1);
		
		Tuple<Set<IMethodName>, Integer> actuals = sut.getMethodsFromCode(episode, stream, events, true);
		
		assertEquals(expected.getFirst(), actuals.getFirst());
		assertEquals(expected.getSecond(), actuals.getSecond());
	}
	
	@Test
	public void firstDeclaration() throws Exception {
		Episode episode = new Episode();
		episode.addFact(new Fact(1));
		episode.addFact(new Fact(4));
		episode.addFact(new Fact(6));
		
		Set<IMethodName> methodNames = Sets.newLinkedHashSet();
		methodNames.add(m(1, 31));
		methodNames.add(m(2, 32));
		methodNames.add(m(5, 34));
		
		Tuple<Set<IMethodName>, Integer> expected = Tuple.newTuple(methodNames, 3);
		
		Tuple<Set<IMethodName>, Integer> actuals = sut.getMethodsFromCode(episode, stream, events, false);
		
		assertEquals(expected.getFirst(), actuals.getFirst());
		assertEquals(expected.getSecond(), actuals.getSecond());
	}
	
	@Test
	public void noOccurrences() throws Exception {
		Episode episode = new Episode();
		episode.addFact(new Fact(4));
		episode.addFact(new Fact(5));
		episode.addFact(new Fact(6));
		episode.addFact(new Fact(11));
		
		Tuple<Set<IMethodName>, Integer> actuals = sut.getMethodsFromCode(episode, stream, events, false);
		
		assertTrue(actuals.getFirst().isEmpty());
		assertTrue(actuals.getSecond() == 0);
	}
	
	private IMethodName m(int typeNum, int methodNum) {
		return MethodName.newMethodName(String.format("[R,P] [%s].m%d()", t(typeNum), methodNum));
	}

	private ITypeName t(int typeNum) {
		return TypeName.newTypeName(String.format("T%d,P", typeNum));
	}
}