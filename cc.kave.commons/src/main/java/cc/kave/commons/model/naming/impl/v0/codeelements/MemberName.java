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
package cc.kave.commons.model.naming.impl.v0.codeelements;

import cc.kave.commons.model.naming.codeelements.IMemberName;
import cc.kave.commons.model.naming.impl.csharp.CsNameUtils;
import cc.kave.commons.model.naming.impl.v0.BaseName;
import cc.kave.commons.model.naming.impl.v0.types.TypeName;
import cc.kave.commons.model.naming.types.ITypeName;

public abstract class MemberName extends BaseName implements IMemberName {

	public final String STATIC_MODIFIER = "static";

	protected MemberName(String identifier) {
		super(identifier);
	}

	public String getModifiers() {
		return identifier.substring(0, identifier.indexOf('['));
	}

	@Override
	public ITypeName getDeclaringType() {
		int endOfValueType = CsNameUtils.endOfNextTypeIdentifier(identifier, 0);
		int startOfDeclaringType = CsNameUtils.startOfNextTypeIdentifier(identifier, endOfValueType) + 1;
		int endOfDeclaringType = CsNameUtils.endOfNextTypeIdentifier(identifier, endOfValueType) - 1;
		return TypeName.newTypeName(identifier.substring(startOfDeclaringType, endOfDeclaringType));
	}

	@Override
	public ITypeName getValueType() {
		int start = identifier.indexOf("[");
		int end = CsNameUtils.getClosingBracketIndex(identifier, start);
		return TypeName.newTypeName(identifier.substring(start + 1, end - 1));
	}

	@Override
	public boolean isStatic() {
		return getModifiers().contains(STATIC_MODIFIER);
	}

	@Override
	public String getName() {
		return identifier.substring(identifier.lastIndexOf('.') + 1);
	}

	@Override
	public String getFullName() {
		// TODO Auto-generated method stub
		return null;
	}
}