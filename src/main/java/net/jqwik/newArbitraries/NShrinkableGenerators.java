package net.jqwik.newArbitraries;

import java.util.*;
import java.util.concurrent.atomic.*;

public class NShrinkableGenerators {

	public static <U> NShrinkableGenerator<U> choose(U[] values) {
		if (values.length == 0) {
			return fail("empty set of values");
		} else {
			return random -> choose(0, values.length - 1).map(i -> values[i]).next(random);
		}
	}

	public static NShrinkableGenerator<Integer> choose(int min, int max) {
		if (min == max) {
			return ignored -> new NShrinkableValue<>(min, ignore -> Collections.emptySet());
		} else {
			final int _min = Math.min(min, max);
			final int _max = Math.max(min, max);
			return random -> {
				int value = random.nextInt(Math.abs(_max - _min) + 1) + _min;
				return new NShrinkableValue<>(value, new NIntegerShrinker(min, max));
			};
		}
	}

	public static NShrinkableGenerator<Long> choose(long min, long max) {
		if (min == max) {
			return ignored -> NShrinkableValue.unshrinkable(min);
		} else {
			final long _min = Math.min(min, max);
			final long _max = Math.max(min, max);
			return random -> {
				final double d = random.nextDouble();
				long value = (long) ((d * _max) + ((1.0 - d) * _min) + d);
				return new NShrinkableValue<>(value, new NLongShrinker(min, max));
			};
		}
	}

	public static <T extends Enum<T>> NShrinkableGenerator<T> choose(Class<T> enumClass) {
		return random -> choose(enumClass.getEnumConstants()).next(random);
	}

	public static NShrinkableGenerator<Character> choose(char[] characters) {
		Character[] validCharacters = new Character[characters.length];
		for (int i = 0; i < characters.length; i++) {
			validCharacters[i] = characters[i];
		}
		return choose(validCharacters);
	}

	public static <T> NShrinkableGenerator<List<T>> list(NShrinkableGenerator<T> elementGenerator, int maxSize) {
		NShrinkableGenerator<Integer> lengthGenerator = choose(0, maxSize);
		return random -> {
			int listSize = lengthGenerator.next(random).value();
			List<T> list = new ArrayList<>();
			for (int j = 0; j < listSize; j++) {
				list.add(elementGenerator.next(random).value());
			}
			// TODO: Make lists shrinkable
			return new NShrinkableValue<>(list, ignore -> Collections.emptySet());
		};
	}


	public static NShrinkableGenerator<String> string(NShrinkableGenerator<Character> charGenerator, int maxLength) {
		NShrinkableGenerator<Integer> lengthGenerator = choose(0, maxLength);
		return random -> {
			int stringLength = lengthGenerator.next(random).value();
			final char[] chars = new char[stringLength];
			for (int j = 0; j < stringLength; j++) {
				chars[j] = charGenerator.next(random).value();
			}
			return new NShrinkableValue<>(new String(chars), new NStringShrinker());
		};
	}

	public static NShrinkableGenerator<Character> choose(char min, char max) {
		if (min == max) {
			return ignored -> NShrinkableValue.unshrinkable(min);
		} else {
			return random -> {
				NShrinkable<Integer> shrinkableInt = choose((int) min, (int) max).next(random);
				return shrinkableInt.map(anInt -> (char) anInt.intValue());
			};
		}
	}

	public static <T> NShrinkableGenerator<T> samples(T... samples) {
		AtomicInteger tryCount = new AtomicInteger(0);
		return ignored -> {
			if (tryCount.get() >= samples.length)
				tryCount.set(0);
			return NShrinkableValue.unshrinkable(samples[tryCount.getAndIncrement()]);
		};
	}

	public static <T> NShrinkableGenerator<T> fail(String message) {
		return ignored -> {
			throw new RuntimeException(message);
		};
	}

}