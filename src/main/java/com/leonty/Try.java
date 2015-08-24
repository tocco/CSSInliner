package com.leonty;

abstract class Try<T> {

	public abstract T get();

	public abstract Throwable failure();

	public abstract boolean isDefined();

	public static <T> Try<T> success(T value) {
		return new Success<T>(value);
	}

	public static <T> Try<T> failure(Throwable e) {
		return new Failure<T>(e);
	}

	private static class Success<T> extends Try<T> {
		private final T value;

		public Success(T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public boolean isDefined() {
			return true;
		}

		@Override
		public String toString() {
			return "Success(" + value + ")";
		}

		@Override
		public Throwable failure() {
			throw new RuntimeException(
					"The result is success. Cannot get a failure.");
		}
	}

	private static class Failure<T> extends Try<T> {
		public final Throwable error;

		public Failure(Throwable error) {
			this.error = error;
		}

		@Override
		public T get() {
			throw new RuntimeException("Try thrown an exception.", error);
		}

		@Override
		public boolean isDefined() {
			return false;
		}

		@Override
		public String toString() {
			return "Failure(" + error + ")";
		}

		@Override
		public Throwable failure() {
			return error;
		}
	}
}