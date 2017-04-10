package net.jqwik.execution.properties;

import javaslang.test.*;
import net.jqwik.api.*;
import net.jqwik.api.properties.*;
import net.jqwik.api.properties.Property;
import net.jqwik.descriptor.*;
import org.junit.platform.engine.*;

import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

public class CheckedPropertyFactoryTests {

	private CheckedPropertyFactory factory = new CheckedPropertyFactory();

	@Example
	void simple() {
		PropertyMethodDescriptor descriptor = createDescriptor("prop");
		ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());

		assertThat(property.propertyName).isEqualTo("prop");

		assertThat(property.forAllParameters).size().isEqualTo(2);
		assertThat(property.forAllParameters.get(0).getType()).isEqualTo(int.class);
		assertThat(property.forAllParameters.get(1).getType()).isEqualTo(String.class);

		Object[] args = { 1, "test" };
		assertThat(property.assumeFunction.apply(args)).isTrue();
		assertThat(property.forAllFunction.apply(args)).isTrue();
		assertThat(property.forAllFunction.apply(new Object[] { 2, "test" })).isFalse();

		assertThat(property.randomSeed).isEqualTo(Long.MIN_VALUE);
		assertThat(property.tries).isEqualTo(Checkable.DEFAULT_TRIES);
	}

	@Example
	void withUnboundParams() {
		PropertyMethodDescriptor descriptor = createDescriptor("propWithUnboundParams");
		ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());

		assertThat(property.forAllParameters).size().isEqualTo(2);
		assertThat(property.forAllParameters.get(0).getType()).isEqualTo(int.class);
		assertThat(property.forAllParameters.get(0).getDeclaredAnnotation(ForAll.class)).isNotNull();
		assertThat(property.forAllParameters.get(1).getType()).isEqualTo(String.class);
		assertThat(property.forAllParameters.get(1).getDeclaredAnnotation(ForAll.class)).isNotNull();
	}

	@Example
	void withTries() {
		PropertyMethodDescriptor descriptor = createDescriptor("propWithTries");
		ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());
		assertThat(property.tries).isEqualTo(42);
	}

	@Example
	void withSeed() {
		PropertyMethodDescriptor descriptor = createDescriptor("propWithSeed");
		ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());
		assertThat(property.randomSeed).isEqualTo(4242);
	}

	@Group
	class Assumptions {

		@Example
		void unknown() {
			PropertyMethodDescriptor descriptor = createDescriptor("propWithUnknownAssumption");
			CheckedProperty property = factory.fromDescriptor(descriptor, new PropertyExamples());

			assertThat(property.check().getTestExecutionResult().getStatus()).isEqualTo(TestExecutionResult.Status.ABORTED);
		}

		@Example
		void findByMethodName() {
			PropertyMethodDescriptor descriptor = createDescriptor("propWithAssumption");
			ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());

			assertThat(property.assumeFunction.apply(new Object[] { 4, "test" })).isTrue();
			assertThat(property.assumeFunction.apply(new Object[] { 5, "test" })).isFalse();
		}

		@Example
		void findByValue() {
			PropertyMethodDescriptor descriptor = createDescriptor("propWithNamedAssumption");
			ExecutingCheckedProperty property = (ExecutingCheckedProperty) factory.fromDescriptor(descriptor, new PropertyExamples());

			assertThat(property.assumeFunction.apply(new Object[] { 4, 5 })).isTrue();
			assertThat(property.assumeFunction.apply(new Object[] { 5, 4 })).isFalse();
		}

		@Example
		void wrongNumberOfArguments() {
			PropertyMethodDescriptor descriptor = createDescriptor("propWithWrongNumberOfArguments");
			CheckedProperty property = factory.fromDescriptor(descriptor, new PropertyExamples());

			assertThat(property.check().getTestExecutionResult().getStatus()).isEqualTo(TestExecutionResult.Status.ABORTED);
		}

		@Example
		void nonAssignableArguments() {
			PropertyMethodDescriptor descriptor = createDescriptor("propWithNonAssignableArguments");
			CheckedProperty property = factory.fromDescriptor(descriptor, new PropertyExamples());

			assertThat(property.check().getTestExecutionResult().getStatus()).isEqualTo(TestExecutionResult.Status.ABORTED);
		}

	}

	private PropertyMethodDescriptor createDescriptor(String methodName) {
		UniqueId uniqueId = UniqueId.root("test", "test");
		Method method = TestHelper.getMethod(PropertyExamples.class, methodName);
		return new PropertyMethodDescriptor(uniqueId, method, PropertyExamples.class);
	}

	private static class PropertyExamples {

		@Property
		boolean prop(@ForAll int anInt, @ForAll String aString) {
			return anInt == 1 && aString.equals("test");
		}

		@Property
		boolean propWithUnboundParams(int otherInt, @ForAll int anInt, @ForAll String aString, String otherString) {
			return true;
		}

		@Property(tries = 42)
		boolean propWithTries(@ForAll int anInt, @ForAll String aString) {
			return true;
		}

		@Property(seed = 4242L)
		boolean propWithSeed(@ForAll int anInt, @ForAll String aString) {
			return true;
		}

		@Property
		@Assume("unknownAssumption")
		boolean propWithUnknownAssumption(@ForAll int length, @ForAll String aString) {
			return true;
		}

		@Property
		@Assume("lengthOfString")
		boolean propWithAssumption(@ForAll int length, @ForAll String aString) {
			return true;
		}

		@Assumption
		boolean lengthOfString(int length, String aString) {
			return aString.length() == length;
		}

		@Property
		@Assume("secondIsHigher")
		boolean propWithNamedAssumption(@ForAll int first, @ForAll int second) {
			return true;
		}

		@Assumption("secondIsHigher")
		boolean anyName(int first, int second) {
			return first < second;
		}

		@Property
		@Assume("wrongNumberOfArguments")
		boolean propWithWrongNumberOfArguments(@ForAll int first, @ForAll int second) {
			return true;
		}

		@Assumption
		boolean wrongNumberOfArguments(int first, int second, int third) {
			return true;
		}

		@Property
		@Assume("otherArguments")
		boolean propWithNonAssignableArguments(@ForAll int first, @ForAll int second) {
			return true;
		}

		@Assumption
		boolean otherArguments(int first, String second) {
			return true;
		}
	}
}