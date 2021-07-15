package com.oopsjpeg.gacha.manager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.user.Player;
import com.oopsjpeg.gacha.object.json.PlayerJson;
import discord4j.common.util.Snowflake;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Manages all MongoDB interactions.
 * Created by oopsjpeg on 2/3/2019.
 */
public class MongoManager
{
    private final Gacha gacha;
    private final MongoDatabase database;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MongoManager(Gacha gacha, String hostname, String database)
    {
        this.gacha = gacha;

        ConnectionString connection = new ConnectionString(hostname);
        // Add BSON/POJO translator
        CodecRegistry pojoRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        // Add default codec for Java types
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoRegistry);
        // Create settings
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .codecRegistry(registry)
                .build();

        this.database = MongoClients.create(settings).getDatabase(database);
    }

    public MongoCollection<PlayerJson> getPlayerCollection()
    {
        return database.getCollection("players", PlayerJson.class);
    }

    public HashMap<String, Player> fetchPlayers()
    {
        HashMap<String, Player> playerMap = new HashMap<>();
        // Add each user into the map by ID
        getPlayerCollection().find().forEach((Consumer<PlayerJson>) data ->
        {
            Player player = new Player(gacha, data);
            logger.info("Fetched player data for ID " + data.id);
            playerMap.put(player.getId(), player);
        });
        return playerMap;
    }

    public void savePlayer(Player player)
    {
        savePlayer(player.getData());
    }

    public void savePlayer(PlayerJson data)
    {
        logger.info("Saving player data for ID " + data.id);
        getPlayerCollection().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
}
