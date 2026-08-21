package com.fbi.criminal_justice_system.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

public final class BackgroundTask {

	private BackgroundTask() {
	}

	public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
		Task<T> task = new Task<>() {
			@Override
			protected T call() {
				return work.get();
			}
		};

		task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
		task.setOnFailed(event -> onError.accept(unwrap(task.getException())));
		Thread thread = new Thread(task, "cjs-api-call");
		thread.setDaemon(true);
		thread.start();
	}

	private static Throwable unwrap(Throwable throwable) {
		if (throwable != null && throwable.getCause() instanceof ApiException apiException) {
			return apiException;
		}
		return throwable == null ? new ApiException("Error desconocido.", null) : throwable;
	}
}
