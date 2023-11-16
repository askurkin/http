package ru.otus.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ItemsWebApplication implements MyWebApplication {
	private String name;
	private ItemsDB items;

	private static final Logger logger = LogManager.getLogger(ItemsWebApplication.class);

	public ItemsWebApplication() throws SQLException {
		this.name = "Items Web Application";

		items = new ItemsDB("items2.db");
	}

	@Override
	public void execute(Request request, OutputStream output) throws IOException {
		logger.info(request.getMethod().toString());
		if (request.getMethod() == HttpMethod.GET) {
			output.write(("" +
					"HTTP/1.1 200 OK\r\n" +
					"Content-Type: application/json\r\n" +
					"Access-Control-Allow-Origin: *\r\n" +
					"\r\n" +
					items.getItemsJson()
			).getBytes(StandardCharsets.UTF_8));
		}
		if (request.getMethod() == HttpMethod.POST) {
			String json = request.getBody();
			items.addItemsJson(json);

			output.write(("" +
					"HTTP/1.1 200 OK\r\n" +
					"Content-Type: text/html\r\n" +
					"\r\n"
			).getBytes(StandardCharsets.UTF_8));
		}
	}
}
