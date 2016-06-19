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
package exec.recommender_reimplementation.tokenization;

import java.util.List;

import cc.kave.commons.model.events.completionevents.Context;

public class TokenExtractor {

	public static List<String> extractTokenStream(Context context) {
		TokenizationContext tokenizationContext = new TokenizationContext(new TokenizationSettings(true));
		
		context.getSST().accept(new TokenizationVisitor(context.getTypeShape()), tokenizationContext);
		
		return tokenizationContext.getTokenStream();
	}
}
