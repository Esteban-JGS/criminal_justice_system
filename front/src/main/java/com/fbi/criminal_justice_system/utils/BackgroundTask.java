package com.fbi.criminal_justice_system.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

/**
 * Ejecuta una llamada al web service fuera del hilo de JavaFX.
 *
 * <p>
 * JavaFX dibuja la ventana en un solo hilo: si ahí se hace una petición HTTP,
 * la interfaz queda congelada hasta que el servidor responda. {@link Task}
 * corre el trabajo aparte y devuelve los callbacks al hilo de FX, donde sí se
 * pueden tocar los controles.
 *
 * <pre>
 * BackgroundTask.run(() -&gt; criminalService.getAll(), criminales -&gt; tabla.setItems(...),
 * 		error -&gt; lblError.setText(error.getMessage()));
 * </pre>
 */
public final class BackgroundTask {

	private BackgroundTask() {
	}

	/**
	 * @param work
	 *            lo que se ejecuta en segundo plano (la llamada al WS)
	 * @param onSuccess
	 *            recibe el resultado, ya en el hilo de JavaFX
	 * @param onError
	 *            recibe la excepción, ya en el hilo de JavaFX
	 */
	public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
		Task<T> task = new Task<>() {
			@Override
			protected T call() {
				return work.get();
			}
		};

		task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
		task.setOnFailed(event -> onError.accept(unwrap(task.getException())));

		// Hilo daemon: si el usuario cierra la ventana, la JVM no se queda esperándolo.
		Thread thread = new Thread(task, "cjs-api-call");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Task envuelve las excepciones; nos interesa la original (normalmente
	 * ApiException).
	 */
	private static Throwable unwrap(Throwable throwable) {
		if (throwable != null && throwable.getCause() instanceof ApiException apiException) {
			return apiException;
		}
		return throwable == null ? new ApiException("Error desconocido.", null) : throwable;
	}
}
