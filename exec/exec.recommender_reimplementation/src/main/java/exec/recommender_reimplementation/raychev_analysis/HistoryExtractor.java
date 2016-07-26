/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exec.recommender_reimplementation.raychev_analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.IParameterName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.pointsto.analysis.FieldSensitivity;
import cc.kave.commons.pointsto.analysis.PointsToAnalysis;
import cc.kave.commons.pointsto.analysis.PointsToContext;
import cc.kave.commons.pointsto.analysis.unification.UnificationAnalysis;

public class HistoryExtractor {

	public static Set<ConcreteHistory> extractHistories(Context context) {
		PointsToContext pointsToContext = performPointsToAnalysis(context);

		ISST sst = context.getSST();

		Set<ConcreteHistory> concreteHistorySet = new HashSet<>();

		for (IMethodDeclaration methodDecl : sst.getMethods()) {
			HistoryMap historyMap = new HistoryMap();
			methodDecl.accept(new RaychevAnalysisVisitor(pointsToContext),
					historyMap);

			for (AbstractHistory abstractHistory : historyMap.values()) {
				concreteHistorySet.addAll(abstractHistory.getHistorySet());
			}

		}

		return concreteHistorySet;
	}

	public static PointsToContext performPointsToAnalysis(Context context) {
		PointsToAnalysis pointsToAnalysis = new UnificationAnalysis(
				FieldSensitivity.FULL);
		return pointsToAnalysis.compute(context);
	}

	public static String getHistoryAsString(Set<ConcreteHistory> concreteHistories) {
		StringBuilder sb = new StringBuilder();
		for (ConcreteHistory concreteHistory : concreteHistories) {
			for (Iterator<Interaction> iterator = concreteHistory.getHistory()
					.iterator(); iterator.hasNext();) {
				Interaction interaction = iterator.next();
				String interactionString = buildInteractionString(interaction);
				sb.append(interactionString);
				if(iterator.hasNext()) sb.append(" ");	
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String buildInteractionString(Interaction interaction) {
		IMethodName methodName = interaction.getMethodName();
		int position = interaction.getPosition();
		StringBuilder sb = new StringBuilder();
		sb.append(methodName.getDeclaringType().getFullName());
		sb.append(".");
		sb.append(getNameForInteractionType(interaction.getInteractionType(),
				methodName));
		sb.append("(");
		sb.append(getParameterString(methodName));
		sb.append(")");
		sb.append(getReturnString(methodName));
		sb.append(":");
		sb.append(getPositionString(methodName, position));

		return sb.toString();
	}

	private static String getPositionString(IMethodName methodName, int position) {
		if (position == Interaction.RETURN)
			return "R";
		int numberOfParameters = methodName.isStatic() ? methodName
				.getParameters().size() : methodName.getParameters().size() + 1;
		return String.format("%1$s/%2$s", position, numberOfParameters);
	}

	private static char getReturnString(IMethodName methodName) {
		ITypeName returnType = methodName.getReturnType();
		if (returnType.isVoidType())
			return 'v';
		char firstChar = returnType.getName().charAt(0);
		if (returnType.isValueType())
			firstChar = Character.toLowerCase(firstChar);
		return firstChar;
	}

	private static String getParameterString(IMethodName methodName) {
		List<IParameterName> parameters = methodName.getParameters();
		StringBuilder sb = new StringBuilder();
		for (IParameterName parameterName : parameters) {
			char firstChar = parameterName.getValueType().getName().charAt(0);
			if (parameterName.getValueType().isValueType())
				firstChar = Character.toLowerCase(firstChar);
			sb.append(firstChar);
		}
		return sb.toString();
	}

	private static String getNameForInteractionType(
			InteractionType interactionType, IMethodName methodName) {
		String name = "";
		switch (interactionType) {
		case METHOD_CALL:
			name = methodName.getName();
			break;
		case PROPERTY_GET:
			name = "get" + methodName.getName();
			break;
		case PROPERTY_SET:
			name = "set" + methodName.getName();
			break;
		default:
			break;
		}
		return name;
	}

}
