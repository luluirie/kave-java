package cc.kave.commons.utils.visitor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.names.csharp.CsTypeName;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.impl.SSTUtil;
import cc.kave.commons.model.ssts.impl.blocks.IfElseBlock;
import cc.kave.commons.model.ssts.impl.visitor.inlining.InliningIStatementVisitor;

public class InliningUtil {
	public static final TypeName RESULT_TYPE = CsTypeName.newTypeName("int");
	public static final TypeName GOT_RESULT_TYPE = CsTypeName.newTypeName("boolean");
	public static final String RESULT_NAME = "$result_";
	public static final String RESULT_FLAG = "$gotResult_";

	public static void visit(List<IStatement> body, List<IStatement> blockBody, InliningContext context) {
		context.enterBlock();
		int index = 0;
		for (IStatement statement : body) {
			statement.accept(new InliningIStatementVisitor(), context);
			if (context.isGuardNeeded() && body.subList(index + 1, body.size()).size() > 0) {
				IfElseBlock block = new IfElseBlock();
				block.setCondition(SSTUtil.referenceExprToVariable(RESULT_FLAG + context.getResultName()));
				context.setGuardNeeded(false);
				visit(body.subList(index + 1, body.size()), block.getThen(), context);
				context.addStatement(block);
				break;
			}
			index++;
		}
		context.leaveBlock(blockBody);
	}

	public static IStatement visit(IStatement statement, InliningContext context) {
		List<IStatement> body = new ArrayList<>();
		visit(Lists.newArrayList(statement), body, context);
		return body.get(0);
	}

	public static void visitScope(List<IStatement> body, List<IStatement> newBody, InliningContext context) {
		context.enterScope(body);
		int index = 0;
		for (IStatement statement : body) {
			statement.accept(new InliningIStatementVisitor(), context);
			if (context.isGuardNeeded() && body.subList(index + 1, body.size()).size() > 0) {
				IfElseBlock block = new IfElseBlock();
				block.setCondition(SSTUtil.referenceExprToVariable(RESULT_FLAG + context.getResultName()));
				context.setGuardNeeded(false);
				visit(body.subList(index + 1, body.size()), block.getThen(), context);
				context.addStatement(block);
				break;
			}
			index++;
		}
		newBody.addAll(context.getBody());
		context.leaveScope();
	}
}
