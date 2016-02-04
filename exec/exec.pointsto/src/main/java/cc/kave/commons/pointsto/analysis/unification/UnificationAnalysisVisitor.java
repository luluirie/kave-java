/**
 * Copyright 2016 Simon Reuß
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cc.kave.commons.pointsto.analysis.unification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.csharp.MethodName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IIndexAccessExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ILambdaExpression;
import cc.kave.commons.model.ssts.expressions.simple.IReferenceExpression;
import cc.kave.commons.model.ssts.references.IAssignableReference;
import cc.kave.commons.model.ssts.references.IEventReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IMethodReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IUnknownReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.commons.model.ssts.statements.IReturnStatement;
import cc.kave.commons.pointsto.LanguageOptions;
import cc.kave.commons.pointsto.SSTBuilder;
import cc.kave.commons.pointsto.analysis.FailSafeNodeVisitor;
import cc.kave.commons.pointsto.analysis.ScopingVisitor;
import cc.kave.commons.pointsto.analysis.exceptions.MissingVariableException;
import cc.kave.commons.pointsto.analysis.exceptions.UndeclaredVariableException;
import cc.kave.commons.pointsto.analysis.unification.locations.FunctionLocation;
import cc.kave.commons.pointsto.analysis.unification.locations.ReferenceLocation;

public class UnificationAnalysisVisitor extends ScopingVisitor<UnificationAnalysisVisitorContext, Void> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnificationAnalysisVisitor.class);

	private LanguageOptions languageOptions = LanguageOptions.getInstance();

	private ReferenceAssignmentHandler referenceAssignmentHandler = new ReferenceAssignmentHandler();
	private MethodParameterVisitor parameterVisitor = new MethodParameterVisitor();

	@Override
	public Void visit(ISST sst, UnificationAnalysisVisitorContext context) {
		super.visit(sst, context);
		context.finalize();
		return null;
	}

	@Override
	public Void visit(IPropertyDeclaration decl, UnificationAnalysisVisitorContext context) {
		context.setCurrentMember(decl.getName());
		return super.visit(decl, context);
	}

	@Override
	public Void visit(IMethodDeclaration decl, UnificationAnalysisVisitorContext context) {
		context.setCurrentMember(decl.getName());
		return super.visit(decl, context);
	}

	@Override
	public Void visit(IAssignment stmt, UnificationAnalysisVisitorContext context) {
		context.setLastAssignment(stmt);

		IAssignableReference destRef = stmt.getReference();
		IAssignableExpression srcExpr = stmt.getExpression();

		if (destRef instanceof IUnknownReference) {
			LOGGER.error("Ignoring assignment to an unknown reference");
			return null;
		}

		try {
			if (srcExpr instanceof IReferenceExpression) {
				IReference srcRef = ((IReferenceExpression) srcExpr).getReference();

				if (srcRef instanceof IUnknownReference) {
					LOGGER.error("Ignoring an assignment with an unknown reference");
				} else {
					referenceAssignmentHandler.setContext(context);
					referenceAssignmentHandler.process(destRef, srcRef);
				}
			} else if (srcExpr instanceof IIndexAccessExpression) {
				referenceAssignmentHandler.setContext(context);
				referenceAssignmentHandler.process(destRef,
						SSTBuilder.indexAccessReference((IIndexAccessExpression) srcExpr));
			} else {
				// visit expressions (invocation, lambda, ...)
				return super.visit(stmt, context);
			}

		} catch (MissingVariableException | UndeclaredVariableException ex) {
			LOGGER.error("Failed to process an assignment: {}", ex.getMessage());
		}

		return null;
	}

	@Override
	public Void visit(IInvocationExpression entity, UnificationAnalysisVisitorContext context) {
		IMethodName method = entity.getMethodName();

		IAssignableReference destRef = context.getDestinationForExpr(entity);

		// TODO replace with isUnknown once fixed
		if (method.equals(MethodName.UNKNOWN_NAME)) {
			// assume that unknown methods return a newly allocated object
			if (destRef != null) {
				context.allocate(destRef);
			}
			return super.visit(entity, context);
		}

		List<ReferenceLocation> parameterLocations;
		ReferenceLocation returnLocation;

		if (method.isConstructor() && destRef != null) {
			context.allocate(destRef);
		}

		if (languageOptions.isDelegateInvocation(method)) {
			// TODO detect a bug where a delegate stored in a property of the class is invoked by using 'this' as the
			// receiver
			if (entity.getReference().getIdentifier().equals(languageOptions.getThisName())) {
				LOGGER.error("Skipping malformed delegate invocation");
				return null;
			}
			FunctionLocation functionLocation = context.invokeDelegate(entity);
			parameterLocations = functionLocation.getParameterLocations();
			returnLocation = functionLocation.getReturnLocation();
		} else {
			parameterLocations = context.getMethodParameterLocations(method);
			returnLocation = context.getMethodReturnLocation(method);
		}

		List<ISimpleExpression> parameters = entity.getParameters();
		int numberOfFormalParameters = method.getParameters().size();
		if (numberOfFormalParameters == 0 && parameters.size() > 0) {
			LOGGER.error("Attempted to invoke method {}.{} which expects zero parameters with {} actual parameters",
					method.getDeclaringType().getName(), method.getName(), parameters.size());
		} else {
			for (int i = 0; i < parameters.size(); ++i) {
				ISimpleExpression parameterExpr = parameters.get(i);
				if (parameterExpr instanceof IReferenceExpression) {
					IReference parameterRef = ((IReferenceExpression) parameterExpr).getReference();

					// due to parameter arrays the number of actual parameters can be greater than the number for formal
					// parameters
					int formalParameterIndex = Math.min(i, numberOfFormalParameters - 1);
					ReferenceLocation formalParameterLocation = parameterLocations.get(formalParameterIndex);

					parameterRef.accept(parameterVisitor, new ContextReferencePair(context, formalParameterLocation));
				}
			}
		}

		if (destRef != null && !method.getReturnType().isVoidType()) {
			context.storeReturn(destRef, returnLocation);
		}

		return super.visit(entity, context);
	}

	@Override
	public Void visit(ILambdaExpression expr, UnificationAnalysisVisitorContext context) {
		IAssignableReference destRef = context.getDestinationForExpr(expr);
		context.enterLambda(expr);
		try {
			super.visit(expr, context);
			if (destRef != null) {
				context.storeFunction(destRef, expr);
			}
		} finally {
			context.leaveLambda();
		}
		return null;
	}

	@Override
	public Void visit(IReturnStatement stmt, UnificationAnalysisVisitorContext context) {
		ISimpleExpression expr = stmt.getExpression();
		if (expr instanceof IReferenceExpression) {
			IReference ref = ((IReferenceExpression) expr).getReference();
			context.registerReturnReference(ref);
		}

		return null;
	}

	private static class ContextReferencePair {
		public UnificationAnalysisVisitorContext analysis;
		public ReferenceLocation parameterLocation;

		public ContextReferencePair(UnificationAnalysisVisitorContext context, ReferenceLocation parameterLocaction) {
			this.analysis = context;
			this.parameterLocation = parameterLocaction;
		}

	}

	private static class MethodParameterVisitor extends FailSafeNodeVisitor<ContextReferencePair, Void> {

		private static final Logger LOGGER = LoggerFactory.getLogger(MethodParameterVisitor.class);

		@Override
		public Void visit(IVariableReference varRef, ContextReferencePair context) {
			context.analysis.registerParameterReference(context.parameterLocation, varRef);
			return null;
		}

		@Override
		public Void visit(IFieldReference fieldRef, ContextReferencePair context) {
			context.analysis.registerParameterReference(context.parameterLocation, fieldRef);
			return null;
		}

		@Override
		public Void visit(IPropertyReference propertyRef, ContextReferencePair context) {
			context.analysis.registerParameterReference(context.parameterLocation, propertyRef);
			return null;
		}

		@Override
		public Void visit(IMethodReference methodRef, ContextReferencePair context) {
			context.analysis.registerParameterReference(context.parameterLocation, methodRef);
			return null;
		}

		@Override
		public Void visit(IEventReference eventRef, ContextReferencePair context) {
			LOGGER.info("Ignoring event reference");
			return null;
		}

		@Override
		public Void visit(IUnknownReference unknownRef, ContextReferencePair context) {
			LOGGER.error("Ignoring an unknown reference");
			return null;
		}
	}
}