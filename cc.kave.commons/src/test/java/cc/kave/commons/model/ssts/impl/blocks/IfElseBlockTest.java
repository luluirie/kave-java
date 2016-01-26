package cc.kave.commons.model.ssts.impl.blocks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.impl.SSTBaseTest;
import cc.kave.commons.model.ssts.impl.SSTTestHelper;
import cc.kave.commons.model.ssts.impl.blocks.IfElseBlock;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.UnknownExpression;
import cc.kave.commons.model.ssts.impl.statements.ContinueStatement;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;

import com.google.common.collect.Lists;

public class IfElseBlockTest extends SSTBaseTest {
	@Test
	public void testDefaultValue() {
		IfElseBlock sut = new IfElseBlock();

		assertThat(new UnknownExpression(), equalTo(sut.getCondition()));
		assertThat(new ArrayList<IStatement>(), equalTo(sut.getThen()));
		assertThat(new ArrayList<IStatement>(), equalTo(sut.getElse()));
		assertThat(0, not(equalTo(sut.hashCode())));
		assertThat(1, not(equalTo(sut.hashCode())));
	}

	@Test
	public void testSettingValues() {
		IfElseBlock sut = new IfElseBlock();
		sut.setCondition(new ConstantValueExpression());
		sut.getThen().add(new ReturnStatement());
		sut.getElse().add(new ContinueStatement());

		assertThat(new ConstantValueExpression(), equalTo(sut.getCondition()));
		assertThat(Lists.newArrayList(new ReturnStatement()), equalTo(sut.getThen()));
		assertThat(Lists.newArrayList(new ContinueStatement()), equalTo(sut.getElse()));
	}
	
	@Test
	public void testChildrenIdentity() {
		IfElseBlock sut = new IfElseBlock();
		sut.setCondition(new ConstantValueExpression());
		sut.getThen().add(new ReturnStatement());
		sut.getElse().add(new ContinueStatement());

		assertChildren(sut, sut.getCondition(), sut.getThen().get(0), sut.getElse().get(0));
	}

	@Test
	public void testEqualityDefault() {
		IfElseBlock a = new IfElseBlock();
		IfElseBlock b = new IfElseBlock();

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityReallyTheSame() {
		IfElseBlock a = new IfElseBlock();
		IfElseBlock b = new IfElseBlock();
		a.setCondition(new ConstantValueExpression());
		a.getElse().add(new ContinueStatement());
		a.getThen().add(new ReturnStatement());
		b.setCondition(new ConstantValueExpression());
		b.getElse().add(new ContinueStatement());
		b.getThen().add(new ReturnStatement());

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityDifferentCondition() {
		IfElseBlock a = new IfElseBlock();
		IfElseBlock b = new IfElseBlock();
		a.setCondition(new ConstantValueExpression());

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testEqualityDifferentThen() {
		IfElseBlock a = new IfElseBlock();
		IfElseBlock b = new IfElseBlock();
		a.getThen().add(new ReturnStatement());

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testEqualityDifferentElse() {
		IfElseBlock a = new IfElseBlock();
		IfElseBlock b = new IfElseBlock();
		a.getElse().add(new ContinueStatement());

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testVisitorIsImplemented() {
		IfElseBlock sut = new IfElseBlock();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void testVisitorWithReturnIsImplemented() {
		// TODO : Visitor Test
	}
}