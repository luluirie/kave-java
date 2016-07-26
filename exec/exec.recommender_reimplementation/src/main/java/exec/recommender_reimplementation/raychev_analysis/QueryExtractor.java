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

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.ISST;
import exec.recommender_reimplementation.java_printer.JavaPrintingContext;
import exec.recommender_reimplementation.java_printer.RaychevQueryPrinter;
import exec.recommender_reimplementation.java_printer.RaychevQueryPrinter.InvalidJavaCodeException;

public class QueryExtractor {

	public static String createJavaCodeForQuery(CompletionEvent completionEvent) {
		return createJavaCodeForQuery(completionEvent.getContext());
	}
	
	public static String createJavaCodeForQuery(Context context) {
		ISST sst = context.getSST();

		JavaPrintingContext printingContext = new JavaPrintingContext();
		RaychevQueryPrinter printer = new RaychevQueryPrinter();

		try {
			sst.accept(printer, printingContext);

		} catch (InvalidJavaCodeException e) {
			return "";
		}
		
		return printingContext.toString();
	}
}
