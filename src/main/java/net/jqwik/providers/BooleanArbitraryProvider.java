package net.jqwik.providers;

import net.jqwik.api.*;
import net.jqwik.api.providers.*;

import java.util.*;
import java.util.function.*;

public class BooleanArbitraryProvider implements ArbitraryProvider {
	@Override
	public boolean canProvideFor(GenericType targetType) {
		return targetType.isAssignableFrom(Boolean.class);
	}

	@Override
	public Arbitrary<?> provideFor(GenericType targetType, Function<GenericType, Optional<Arbitrary<?>>> subtypeProvider) {
		return Arbitraries.of(true, false);
	}
}
