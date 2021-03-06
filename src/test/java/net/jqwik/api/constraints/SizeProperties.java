package net.jqwik.api.constraints;

import net.jqwik.api.*;

import java.util.*;
import java.util.stream.*;

class SizeProperties {

	@Property
	boolean lists(@ForAll @Size(min = 2, max = 7) List<?> aValue) {
		return aValue.size() >= 2 && aValue.size() <= 7;
	}

	@Property
	boolean fixedSize(@ForAll @Size(5) List<?> aValue) {
		return aValue.size() == 5;
	}

	@Property
	boolean sets(@ForAll @Size(min = 2, max = 7) Set<?> aValue) {
		return aValue.size() >= 2 && aValue.size() <= 7;
	}

	@Property
	boolean arrays(@ForAll @Size(min = 2, max = 7) Object[] aValue) {
		return aValue.length >= 2 && aValue.length <= 7;
	}

	@Property
	boolean stream(@ForAll @Size(min = 2, max = 7) Stream<?> aValue) {
		long count = aValue.count();
		return count >= 2 && count <= 7;
	}
}
