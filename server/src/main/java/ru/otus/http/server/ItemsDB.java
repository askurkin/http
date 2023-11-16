package ru.otus.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ItemsDB {
	private List<Item> items;
	Connection connection;
	private static final Logger logger = LogManager.getLogger(ItemsDB.class);

	public ItemsDB(String dbname) throws SQLException {
		items = new ArrayList<>();

		connection = DriverManager.getConnection("jdbc:sqlite:server\\" + dbname);
	}

	public List<Item> getItems() {
		getItemsFromDB(items, connection);
		return items;
	}

	public String getItemsJson() {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		return gson.toJson(getItems());
	}

	public void addItemsJson(String json) {
		Gson gson = new Gson();
		Item item = gson.fromJson(json, Item.class);

		saveItemToDB(item, connection);
	}

	private void getItemsFromDB(List<Item> items, Connection con) {
		String sqlLoad = "SELECT ID, TITLE FROM ITEMS";
		items.clear();
		try (Statement statement = con.createStatement();
			 ResultSet rs = statement.executeQuery(sqlLoad)) {
			while (rs.next()) {
				Item item = new Item(
						rs.getLong("ID"),
						rs.getString("TITLE"));
				items.add(item);
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("no such table: ITEMS")) {
				logger.debug("no such table: ITEMS");
				String sqlCreate = "CREATE TABLE ITEMS ( ID INTEGER, TITLE TEXT(100) );";
				try (Statement statement = con.createStatement()) {
					statement.execute("CREATE TABLE ITEMS ( ID INTEGER, TITLE TEXT(100) );");
					logger.info("ITEMS create");
				} catch (SQLException ex2) {
					logger.error(ex2.getMessage());
					throw new RuntimeException(ex2.getMessage());
				}
			} else {
				logger.error(ex.getMessage());
				throw new RuntimeException(ex.getMessage());
			}
		}
	}

	private void saveItemToDB(Item item, Connection con) {
		String sqlInsert = "INSERT INTO ITEMS( ID, TITLE ) VALUES ( ?, ? ) ";
		try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
			ps.setLong(1, item.getId());
			ps.setString(2, item.getTitle());
			ps.executeUpdate();
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
}
