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
package cc.kave.commons.pointsto.analysis.inclusion;

import static cc.kave.commons.pointsto.analysis.utils.SSTBuilder.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cc.kave.commons.model.names.IMemberName;
import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.IParameterName;
import cc.kave.commons.model.names.IPropertyName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.pointsto.analysis.exceptions.UnexpectedNameException;
import cc.kave.commons.pointsto.analysis.inclusion.allocations.AllocationSite;
import cc.kave.commons.pointsto.analysis.inclusion.allocations.ArrayEntryAllocationSite;
import cc.kave.commons.pointsto.analysis.inclusion.allocations.OutParameterAllocationSite;
import cc.kave.commons.pointsto.analysis.inclusion.allocations.UndefinedMemberAllocationSite;
import cc.kave.commons.pointsto.analysis.inclusion.annotations.ContextAnnotation;
import cc.kave.commons.pointsto.analysis.inclusion.annotations.InclusionAnnotation;
import cc.kave.commons.pointsto.analysis.names.DistinctMemberName;
import cc.kave.commons.pointsto.analysis.names.DistinctMemberNameFactory;
import cc.kave.commons.pointsto.analysis.references.DistinctMethodParameterReference;
import cc.kave.commons.pointsto.analysis.references.DistinctPropertyParameterReference;
import cc.kave.commons.pointsto.analysis.references.DistinctReference;
import cc.kave.commons.pointsto.analysis.utils.LanguageOptions;
import cc.kave.commons.pointsto.extraction.DeclarationMapper;

public final class DeclarationLambdaStore {

	private final LanguageOptions languageOptions = LanguageOptions.getInstance();
	private final DistinctMemberNameFactory nameFactory = new DistinctMemberNameFactory();

	private final Function<DistinctReference, SetVariable> variableResolver;
	private final SetVariableFactory variableFactory;

	private final ConstraintResolver constraintResolver;
	private final DeclarationMapper declMapper;

	private final Map<DistinctMemberName, LambdaTerm> declarationLambdas = new HashMap<>();

	public DeclarationLambdaStore(Function<DistinctReference, SetVariable> variableResolver,
			SetVariableFactory variableFactory, ConstraintResolver constraintResolver, DeclarationMapper declMapper) {
		this.variableResolver = variableResolver;
		this.variableFactory = variableFactory;
		this.constraintResolver = constraintResolver;
		this.declMapper = declMapper;
	}

	public DeclarationLambdaStore(DeclarationLambdaStore other,
			Function<DistinctReference, SetVariable> variableProvider, ConstraintResolver constraintResolver) {
		this(variableProvider, other.variableFactory, constraintResolver, other.declMapper);
		declarationLambdas.putAll(other.declarationLambdas);
	}

	public SetVariableFactory getVariableFactory() {
		return variableFactory;
	}

	public LambdaTerm getDeclarationLambda(IMemberName member) {
		DistinctMemberName name = nameFactory.create(member);
		LambdaTerm lambda = declarationLambdas.get(name);
		if (lambda == null) {
			if (member instanceof IMethodName) {
				lambda = createDeclarationLambda((IMethodName) member);
			} else if (member instanceof IPropertyName) {
				lambda = createDeclarationLambda((IPropertyName) member);
			} else {
				throw new UnexpectedNameException(member);
			}
			declarationLambdas.put(name, lambda);
		}

		return lambda;
	}

	private LambdaTerm createDeclarationLambda(IMethodName method) {
		List<IParameterName> formalParameters = method.getParameters();
		List<SetVariable> variables = new ArrayList<>(formalParameters.size() + 2);

		boolean isMethodWithoutDefinition = declMapper.get(method) == null;

		if (!method.isExtensionMethod()) {
			if (method.isStatic()) {
				variables.add(ConstructedTerm.BOTTOM);
			} else {
				variables.add(variableResolver.apply(new DistinctMethodParameterReference(
						parameter(languageOptions.getThisName(), method.getDeclaringType()), method)));
			}
		}

		for (IParameterName parameter : formalParameters) {
			DistinctReference parameterDistRef = new DistinctMethodParameterReference(parameter, method);
			SetVariable parameterVar = variableResolver.apply(parameterDistRef);
			variables.add(parameterVar);

			if (parameter.isOutput() && (isMethodWithoutDefinition || parameter.getValueType().isStructType())) {
				// methods without a definition require an object for their out-parameters; struct out-parameters are
				// allocated even for methods which have a definition as they already have a location on method entry
				// (although they remain uninitialized)
				allocateOutParameter(method, parameter, parameterVar);
			}
		}

		ITypeName returnType = method.getReturnType();
		if (!returnType.isVoidType()) {
			SetVariable returnVar = variableFactory.createReferenceVariable();
			// methods without a definition require an object to return
			if (isMethodWithoutDefinition) {
				allocateReturnObject(method, returnVar, returnType);
			}
			variables.add(returnVar);
		}

		return LambdaTerm.newMethodLambda(variables, formalParameters, returnType);
	}

	private LambdaTerm createDeclarationLambda(IPropertyName property) {
		SetVariable thisVar = variableResolver.apply(new DistinctPropertyParameterReference(
				languageOptions.getThisName(), property.getDeclaringType(), property));
		SetVariable setParameterVar = variableResolver
				.apply(new DistinctPropertyParameterReference(languageOptions, property));
		SetVariable returnVar = variableFactory.createReferenceVariable();
		// properties without a definition require an object to return
		if (declMapper.get(property) == null) {
			allocateReturnObject(property, returnVar, property.getValueType());
		}

		List<SetVariable> variables = Arrays.asList(thisVar, setParameterVar, returnVar);
		return LambdaTerm.newPropertyLambda(variables);
	}

	private void allocateReturnObject(IMemberName member, SetVariable returnVar, ITypeName type) {
		AllocationSite allocationSite = new UndefinedMemberAllocationSite(member, type);
		RefTerm returnObject = new RefTerm(allocationSite, variableFactory.createObjectVariable());
		constraintResolver.addConstraint(returnObject, returnVar, InclusionAnnotation.EMPTY, ContextAnnotation.EMPTY);

		if (type.isArrayType()) {
			allocateArrayEntry(allocationSite, returnVar);
		}
	}

	private void allocateOutParameter(IMemberName member, IParameterName parameter, SetVariable parameterVar) {
		AllocationSite allocationSite = new OutParameterAllocationSite(member, parameter);
		RefTerm paramObject = new RefTerm(allocationSite, variableFactory.createObjectVariable());
		constraintResolver.addConstraint(paramObject, parameterVar, InclusionAnnotation.EMPTY, ContextAnnotation.EMPTY);

		ITypeName type = parameter.getValueType();
		if (type.isArrayType()) {
			allocateArrayEntry(allocationSite, parameterVar);
		}
	}

	private void allocateArrayEntry(AllocationSite arrayAllocationSite, SetVariable returnVar) {
		// provide one initialized array entry
		RefTerm arrayEntry = new RefTerm(new ArrayEntryAllocationSite(arrayAllocationSite),
				variableFactory.createObjectVariable());
		SetVariable temp = variableFactory.createProjectionVariable();
		Projection projection = new Projection(RefTerm.class, RefTerm.WRITE_INDEX, temp);

		// array ⊆ proj
		constraintResolver.addConstraint(returnVar, projection, InclusionAnnotation.EMPTY, ContextAnnotation.EMPTY);
		// src ⊆ temp
		constraintResolver.addConstraint(arrayEntry, temp, InclusionAnnotation.EMPTY, ContextAnnotation.EMPTY);
	}

}
