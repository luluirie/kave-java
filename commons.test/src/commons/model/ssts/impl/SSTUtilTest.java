package commons.model.ssts.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.csharp.CsMethodName;
import cc.kave.commons.model.names.csharp.CsTypeName;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.impl.SSTUtil;
import cc.kave.commons.model.ssts.impl.blocks.LockBlock;
import cc.kave.commons.model.ssts.impl.expressions.assignable.ComposedExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ReferenceExpression;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.statements.ExpressionStatement;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;
import cc.kave.commons.model.ssts.impl.statements.VariableDeclaration;
import cc.kave.commons.model.ssts.references.IVariableReference;

import com.google.common.collect.Lists;

public class SSTUtilTest {
	@Test
	public void testDeclare() {
		VariableDeclaration actual = (VariableDeclaration) SSTUtil.declare("a", CsTypeName.UNKNOWN_NAME);
		VariableDeclaration expected = new VariableDeclaration();
		expected.setType(CsTypeName.UNKNOWN_NAME);
		expected.setReference(ref("a"));

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testReturn() {
		ReturnStatement actual = (ReturnStatement) SSTUtil.returnStatement(new ConstantValueExpression());
		ReturnStatement expected = new ReturnStatement();
		expected.setExpression(new ConstantValueExpression());

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testReturnVariable() {
		ReturnStatement actual = (ReturnStatement) SSTUtil.returnVariable("a");
		ReturnStatement expected = new ReturnStatement();
		ReferenceExpression refExpr = new ReferenceExpression();
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("a");
		refExpr.setReference(varRef);
		expected.setExpression(refExpr);

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testReferenceExprToVariable() {
		ReferenceExpression actual = (ReferenceExpression) SSTUtil.referenceExprToVariable("a");
		ReferenceExpression expected = new ReferenceExpression();
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("a");
		expected.setReference(varRef);

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testComposedExpression() {
		ComposedExpression actual = (ComposedExpression) SSTUtil.composedExpression("a", "b");
		ComposedExpression expected = new ComposedExpression();
		expected.setReferences(Lists.newArrayList(ref("a"), ref("b")));

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testSettingValues() {
		InvocationExpression a = (InvocationExpression) SSTUtil.invocationExpression("a1", getMethod("A2"), Lists
				.newArrayList(refs("a3")).iterator());
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("a1");

		assertThat(varRef, equalTo(a.getReference()));
		assertThat(getMethod("A2"), equalTo(a.getMethodName()));
		assertThat(Lists.newArrayList(refs("a3")), equalTo(a.getParameters()));
	}

	@Test
	public void testInvocationExpressionStatic() {
		InvocationExpression a = (InvocationExpression) SSTUtil.invocationExpression(getStaticMethod("B2"), Lists
				.newArrayList(refs("c2")).iterator());

		assertThat(new VariableReference(), equalTo(a.getReference()));
		assertThat(getStaticMethod("B2"), equalTo(a.getMethodName()));
		assertThat(Lists.newArrayList(refs("c2")), equalTo(a.getParameters()));
	}

	@Test
	public void testInvocationExpressionNonStatic() {
		InvocationExpression a = (InvocationExpression) SSTUtil.invocationExpression("a1", getMethod("B1"), Lists
				.newArrayList(refs("c1")).iterator());
		assertThat(SSTUtil.variableReference("a1"), equalTo(a.getReference()));
		assertThat(getMethod("B1"), equalTo(a.getMethodName()));
		assertThat(Lists.newArrayList(refs("c1")), equalTo(a.getParameters()));
	}

	@Test
	public void testInvocationStatementStatic() {
		ExpressionStatement actual = (ExpressionStatement) SSTUtil.invocationStatement(getStaticMethod("B2"), Lists
				.newArrayList(refs("c2")).iterator());
		ExpressionStatement expected = new ExpressionStatement();
		InvocationExpression expr = new InvocationExpression();
		expr.setMethodName(getStaticMethod("B2"));
		expr.setParameters(Lists.newArrayList(varRefExpr("c2")));
		expected.setExpression(expr);

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testInvocationStatementNonStatic() {
		ExpressionStatement actual = (ExpressionStatement) SSTUtil.invocationStatement("a", getMethod("B2"), Lists
				.newArrayList(refs("c2")).iterator());
		ExpressionStatement expected = new ExpressionStatement();
		InvocationExpression expr = new InvocationExpression();
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("a");
		expr.setReference(varRef);
		expr.setMethodName(getMethod("B2"));
		expr.setParameters(Lists.newArrayList(varRefExpr("c2")));
		expected.setExpression(expr);

		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testLockBlock() {
		LockBlock actual = (LockBlock) SSTUtil.lockBlock("a");
		LockBlock expected = new LockBlock();
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("a");
		expected.setReference(varRef);

		assertThat(expected, equalTo(actual));
	}

	/*
	 * @Test(expected = AssertionError.class) public void
	 * testCustomConstructorNonStaticAssert() {
	 * SSTUtil.invocationExpression("a1", getStaticMethod("B1"),
	 * Lists.newArrayList(refs("c1")).iterator()); }
	 * 
	 * @Test(expected = AssertionError.class) public void
	 * testCustomConstructorStaticAssert() {
	 * SSTUtil.invocationExpression(getMethod("B2"),
	 * Lists.newArrayList(refs("c2")).iterator()); }
	 */

	private static IVariableReference ref(String id) {
		VariableReference ref = new VariableReference();
		ref.setIdentifier(id);
		return ref;
	}

	private static MethodName getMethod(String simpleName) {
		String methodName = "[System.String, mscore, 4.0.0.0] [System.String, mscore, 4.0.0.0]." + simpleName + "()";
		return CsMethodName.newMethodName(methodName);
	}

	private static MethodName getStaticMethod(String simpleName) {
		String methodName = "static [System.String, mscore, 4.0.0.0] [System.String, mscore, 4.0.0.0]" + simpleName
				+ "()";
		return CsMethodName.newMethodName(methodName);
	}

	private static ISimpleExpression varRefExpr(String id) {
		ReferenceExpression refExpr = new ReferenceExpression();
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier("id");
		refExpr.setReference(varRef);
		return refExpr;
	}

	private static ISimpleExpression[] refs(String... ids) {
		ISimpleExpression[] output = new ISimpleExpression[ids.length];
		for (int i = 0; i < ids.length; i++) {
			ReferenceExpression ref = new ReferenceExpression();
			VariableReference varRef = new VariableReference();
			varRef.setIdentifier("id");
			ref.setReference(varRef);
			output[i] = ref;
		}
		return output;
	}
}