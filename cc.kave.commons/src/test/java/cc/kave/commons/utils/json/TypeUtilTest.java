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
package cc.kave.commons.utils.json;

import org.junit.Assert;
import org.junit.Test;

public class TypeUtilTest {

	@Test
	public void testSSTTypeToJaveTypeName() {
		String type = "[SST:Expressions.Simple.UnknownExpression]";
		String expected = "KaVE.Commons.Model.SSTs.Impl.Expressions.Simple.UnknownExpression, KaVE.Commons";
		String actual = TypeUtil.fromSerializedNames(type);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testContextToJavaTypeName() {
		String type = "\"KaVE.Commons.Model.Events.CompletionEvents.Context, KaVE.Commons\"";
		String expected = "\"cc.kave.commons.model.events.completionevents.Context\"";
		String actual = TypeUtil.fromSerializedNames(type);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testSSTType() {
		String type = "[SST:Declarations.FieldDeclaration]";
		String expected = "KaVE.Commons.Model.SSTs.Impl.Declarations.FieldDeclaration, KaVE.Commons";
		String actual = TypeUtil.fromSerializedNames(type);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testJavaTypeNameToCsharp() {
		String type = "\"cc.kave.commons.model.events.completionevents.Context\"";
		String expected = "\"KaVE.Commons.Model.Events.CompletionEvents.Context, KaVE.Commons\"";
		String actual = TypeUtil.toSerializedNames(type);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void typeAnnotation_events_CompletionEvents() {
		String java = "\"cc.kave.commons.model.events.completionevents.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.CompletionEvents.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	@Test
	public void typeAnnotation_events_TestRunEvents() {
		String java = "\"cc.kave.commons.model.events.testrunevents.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.TestRunEvents.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	@Test
	public void typeAnnotation_events_UserProfiles() {
		String java = "\"cc.kave.commons.model.events.userprofiles.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.UserProfiles.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	@Test
	public void typeAnnotation_events_VersionControlEvents() {
		String java = "\"cc.kave.commons.model.events.versioncontrolevents.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.VersionControlEvents.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	@Test
	public void typeAnnotation_events_VisualStudio() {
		String java = "\"cc.kave.commons.model.events.visualstudio.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.VisualStudio.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	@Test
	public void typeAnnotation_events_Base() {
		String java = "\"cc.kave.commons.model.events.Foo\"";
		String csharp = "\"KaVE.Commons.Model.Events.Foo, KaVE.Commons\"";
		assertTypeAnnotationConversion(java, csharp);
	}

	private static void assertTypeAnnotationConversion(String java, String csharp) {
		Assert.assertEquals(csharp, TypeUtil.toSerializedNames(java));
		Assert.assertEquals(java, TypeUtil.fromSerializedNames(csharp));
	}
}