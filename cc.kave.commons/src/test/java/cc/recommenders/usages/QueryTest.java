/**
 * Copyright (c) 2011-2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package cc.recommenders.usages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import cc.recommenders.names.CoReMethodName;
import cc.recommenders.names.ICoReMethodName;
import cc.recommenders.names.ICoReTypeName;
import cc.recommenders.testutils.ToStringAssert;

public class QueryTest {

	private Query sut;

	@Before
	public void sut() {
		sut = new Query();
	}

	@Test
	public void typeCanBeSet() {
		ICoReTypeName expected = mock(ICoReTypeName.class);
		sut.setType(expected);
		ICoReTypeName actual = sut.getType();
		assertEquals(expected, actual);
	}

	@Test
	public void classContextCanBeSet() {
		ICoReTypeName expected = mock(ICoReTypeName.class);
		sut.setClassContext(expected);
		ICoReTypeName actual = sut.getClassContext();
		assertEquals(expected, actual);
	}

	@Test
	public void methodContextCanBeSet() {
		ICoReMethodName expected = mock(ICoReMethodName.class);
		sut.setMethodContext(expected);
		ICoReMethodName actual = sut.getMethodContext();
		assertEquals(expected, actual);
	}

	@Test
	public void definitionCanBeSet() {
		DefinitionSite expected = mock(DefinitionSite.class);
		sut.setDefinition(expected);
		DefinitionSite actual = sut.getDefinitionSite();
		assertEquals(expected, actual);
	}

	@Test
	public void callSitesAreEmptyByDefault() {
		Set<CallSite> actuals = sut.getAllCallsites();
		assertEquals(Sets.newLinkedHashSet(), actuals);
	}

	@Test
	public void callSitesCanBeAdded() {
		CallSite site = createReceiverCallSite();
		Set<CallSite> expecteds = Sets.newHashSet(site);

		sut.addCallSite(site);
		Set<CallSite> actuals = sut.getAllCallsites();

		assertEquals(expecteds, actuals);
	}

	@Test
	public void successfullyAddingCallSitesReturnsTrue() {
		boolean success = sut.addCallSite(createReceiverCallSite());
		assertTrue(success);
	}

	@Test
	public void equalCallSitesCannotBeAddedTwice() {
		CallSite site = createReceiverCallSite();
		sut.addCallSite(site);
		boolean success = sut.addCallSite(site);
		assertFalse(success);

		Set<CallSite> actuals = sut.getAllCallsites();
		assertEquals(1, actuals.size());
	}

	@Test
	public void callSitesCanBeReset() {
		sut.addCallSite(createReceiverCallSite());
		sut.resetCallsites();
		Set<CallSite> actuals = sut.getAllCallsites();
		Set<CallSite> expecteds = Sets.newLinkedHashSet();
		assertEquals(expecteds, actuals);
	}

	@Test
	public void callSitesCanBeSetAtOnce() {
		Set<CallSite> expecteds = Sets.newHashSet();
		expecteds.add(createReceiverCallSite());
		expecteds.add(createParameterCallSite());
		expecteds.add(createReceiverCallSite());

		sut.setAllCallsites(expecteds);
		Set<CallSite> actuals = sut.getAllCallsites();
		assertEquals(expecteds, actuals);
	}

	@Test
	public void settingMultipleCallSitesResets() {
		Set<CallSite> expecteds = Sets.newHashSet();
		expecteds.add(createReceiverCallSite());
		expecteds.add(createParameterCallSite());
		expecteds.add(createReceiverCallSite());

		sut.addCallSite(createParameterCallSite());
		sut.setAllCallsites(expecteds);

		Set<CallSite> actuals = sut.getAllCallsites();
		assertEquals(expecteds, actuals);
	}

	@Test
	public void receiverCallSitesAreCorrectlyFiltered() {
		CallSite r1 = createReceiverCallSite();
		CallSite r2 = createReceiverCallSite();
		CallSite p1 = createParameterCallSite();
		CallSite p2 = createParameterCallSite();

		sut.setAllCallsites(Sets.newHashSet(r1, p1, r2, p2));
		Set<CallSite> actuals = sut.getReceiverCallsites();

		Set<CallSite> expecteds = Sets.newHashSet(r1, r2);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void paramCallSitesAreCorrectlyFiltered() {
		CallSite r1 = createReceiverCallSite();
		CallSite r2 = createReceiverCallSite();
		CallSite p1 = createParameterCallSite();
		CallSite p2 = createParameterCallSite();

		sut.setAllCallsites(Sets.newHashSet(r1, p1, r2, p2));
		Set<CallSite> actuals = sut.getParameterCallsites();

		Set<CallSite> expecteds = Sets.newHashSet(p1, p2);
		assertEquals(expecteds, actuals);
	}

	@Test
	public void creationOfUsageCopyCreatesClone() {
		Usage expected = createUsage();
		Query actual = Query.createAsCopyFrom(expected);

		assertNotSame(expected, actual);
		assertNotSame(expected.getAllCallsites(), actual.getAllCallsites());
	}

	@Test
	public void creationOfUsageCopiesWorks() {
		Usage expected = createUsage();
		Query actual = Query.createAsCopyFrom(expected);

		assertEquals(expected.getType(), actual.getType());
		assertEquals(expected.getClassContext(), actual.getClassContext());
		assertEquals(expected.getMethodContext(), actual.getMethodContext());
		assertEquals(expected.getDefinitionSite(), actual.getDefinitionSite());
		assertEquals(expected.getAllCallsites(), actual.getAllCallsites());
	}

	@Test
	public void equality_default() {
		assertEquals(new Query(), new Query());
	}

	@Test
	public void equality_notEqualToNoUsage() {
		assertNotEquals(new Query(), new NoUsage());
	}

	@Test
	public void toStringIsImplemented() {
		ToStringAssert.assertToStringUtils(new Query());
	}

	private static CallSite createReceiverCallSite() {
		ICoReMethodName m = CoReMethodName.get("LType.receiverMethod()V");
		CallSite site = CallSites.createReceiverCallSite(m);
		return site;
	}

	private static CallSite createParameterCallSite() {
		ICoReMethodName m = CoReMethodName.get("LType.paramMethod(LParam;)V");
		CallSite site = CallSites.createParameterCallSite(m, 1);
		return site;
	}

	private static Usage createUsage() {
		Query q = new Query();
		q.setType(mock(ICoReTypeName.class));
		q.setClassContext(mock(ICoReTypeName.class));
		q.setMethodContext(mock(ICoReMethodName.class));
		q.setDefinition(mock(DefinitionSite.class));
		q.addCallSite(mock(CallSite.class));
		q.addCallSite(mock(CallSite.class));
		return q;
	}
}