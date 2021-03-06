package net.jqwik.api.constraints;

import net.jqwik.api.*;

import java.util.*;

class AnnotatedParameterTypesProperties {

	@Property
	boolean withNullInParameterType(@ForAll List<@WithNull(1.0) String> aValue) {
		return aValue.stream().allMatch(Objects::isNull);
	}

	@Property(tries = 20)
	boolean fixedSizedListWithFixedLengthString(
		@ForAll @Size(3) List<@StringLength(5) @Chars({'a', 'b', 'c'}) String> aStringList
	) {
		return aStringList.size() == 3
			&& aStringList.stream().allMatch(s -> s.length() == 5);
	}

}
