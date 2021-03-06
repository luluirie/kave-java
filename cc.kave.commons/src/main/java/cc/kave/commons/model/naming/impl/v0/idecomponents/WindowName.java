/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.commons.model.naming.impl.v0.idecomponents;

import cc.kave.commons.model.naming.idecomponents.IWindowName;
import cc.kave.commons.model.naming.impl.v0.BaseName;
import cc.recommenders.exceptions.ValidationException;

public class WindowName extends BaseName implements IWindowName {

	public WindowName() {
		this(UNKNOWN_NAME_IDENTIFIER);
	}

	public WindowName(String identifier) {
		super(identifier);
		if (!isUnknown() && !identifier.contains(" ")) {
			throw new ValidationException("must contain space");
		}
	}

	@Override
	public String getType() {
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : identifier.substring(0, identifier.indexOf(' '));
	}

	@Override
	public String getCaption() {
		int startOfWindowCaption = getType().length() + 1;
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : identifier.substring(startOfWindowCaption);
	}

	@Override
	public boolean isUnknown() {
		return UNKNOWN_NAME_IDENTIFIER.equals(identifier);
	}
}