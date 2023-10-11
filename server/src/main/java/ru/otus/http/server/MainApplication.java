package ru.otus.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApplication {
	public static final int PORT = 8189;
	private static final Logger logger = LogManager.getLogger(MainApplication.class);
	public static ExecutorService serv;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			Map<String, MyWebApplication> router = new HashMap<>();
			router.put("/calculator", new CalculatorWebApplication());
			router.put("/greetings", new GreetingsWebApplication());

			logger.info("Сервер запущен, порт: " + PORT);

			serv = Executors.newFixedThreadPool(4);

			while (true) {
				Socket socket = serverSocket.accept();
				serv.execute(() -> {
					try {
						clientConnect(socket, router);
					} catch (IOException e) {
						logger.error(e.getStackTrace());
						e.printStackTrace();
						throw new RuntimeException(e);
					} finally {
						try {
							if (!socket.isClosed()) {
								socket.close();
							}
						} catch (IOException e) {
							logger.error(e.getStackTrace());
							throw new RuntimeException(e);
						}
					}
				});

			}

		} catch (IOException e) {
			logger.error(e.getStackTrace());
			e.printStackTrace();
		}
	}

	public static void clientConnect(Socket socket, Map<String, MyWebApplication> router) throws IOException {
		logger.info("Клиент подключился");

		byte[] buffer = new byte[2048];
		int n = socket.getInputStream().read(buffer);
		String rawRequest = new String(buffer, 0, n);
		Request request = new Request(rawRequest);
		request.show();
		boolean executed = false;
		for (Map.Entry<String, MyWebApplication> e : router.entrySet()) {
			if (request.getUri().startsWith(e.getKey())) {
				e.getValue().execute(request, socket.getOutputStream());
				executed = true;
				break;
			}
		}
		if (!executed) {
			socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Unknown application</h1></body></html>").getBytes(StandardCharsets.UTF_8));
		}
	}
}
