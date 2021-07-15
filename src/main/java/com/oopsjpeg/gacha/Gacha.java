package com.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oopsjpeg.gacha.command.Command;
import com.oopsjpeg.gacha.command.CommandManager;
import com.oopsjpeg.gacha.manager.MongoManager;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.ImageCache;
import com.oopsjpeg.gacha.object.json.CardJson;
import com.oopsjpeg.gacha.object.user.Player;
import com.oopsjpeg.gacha.util.BadSettingsException;
import com.oopsjpeg.gacha.util.CardFullRenderCache;
import com.oopsjpeg.gacha.util.CardImageCache;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Gacha
{
    public static final String SETTINGS_FILE = "gacha.properties";
    public static final ScheduledExecutorService SCHEDULER = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Gacha instance;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private GatewayDiscordClient client;
    private Settings settings;

    private MongoManager mongo;
    private CommandManager commands;

    private Map<String, Player> playerMap = new HashMap<>();
    private Map<Integer, Card> cardMap = new HashMap<>();

    private final ImageCache<Card> cardImageCache = new CardImageCache(this);
    private final ImageCache<Card> cardFullRenderCache = new CardFullRenderCache(this);

    public static void main(String[] args) throws BadSettingsException, IOException
    {
        //System.setProperty("user.timezone", "UTC");

        instance = new Gacha();
        instance.start();
    }

    public static Gacha getInstance()
    {
        return instance;
    }

    public void start() throws BadSettingsException, IOException
    {
        loadSettings();

        // Log in
        logger.info("Logging in");

        client = DiscordClient.create(settings.getToken()).gateway()
                .withEventDispatcher(dispatcher -> dispatcher.on(ReadyEvent.class).doOnNext(ev ->
                        {
                            logger.info("Creating MongoDB manager");
                            mongo = new MongoManager(this, settings.getMongoHost(), settings.getMongoDatabase());
                            logger.info("Creating command manager");
                            commands = new CommandManager(this, settings.getPrefix(), Command.values());
                            logger.info("Loading cards");
                            cardMap = fetchCards();
                            logger.info("Loaded " + cardMap.size() + " cards");
                            logger.info("Loading players");
                            playerMap = mongo.fetchPlayers();
                            logger.info("Registering commands into application");
                            commands.getCommands().forEach(c ->
                            {
                                RestClient restClient = ev.getClient().getRestClient();
                                long applicationId = restClient.getApplicationId().block();

                                logger.info("Registering command '" + c.getName() + "' on application");
                                //restClient.getApplicationService()
                                //        .createGuildApplicationCommand(applicationId, 401925683791790080L, c.app())
                                //        .block();
                            });

                            //Gacha.SCHEDULER.scheduleAtFixedRate(() -> ev.getClient().updatePresence(StatusUpdate.builder()
                            //                .game(ActivityUpdateRequest.builder()
                            //                        .name(Constants.GAMES[Util.RANDOM.nextInt(Constants.GAMES.length)])
                            //                        .build())
                            //                .build())
                            //                .block(),
                            //        0, 10, TimeUnit.MINUTES);

                            logger.info("Gacha is ready");
                        }))
                .login().block();

        client.onDisconnect().block();
    }

    private void loadSettings() throws BadSettingsException, IOException
    {
        File settingsFile = new File(SETTINGS_FILE);
        settings = new Settings(settingsFile);

        // Check if settings exist
        if (!settingsFile.exists())
        {
            // Store new settings
            settings.store();
            throw new BadSettingsException("Created new settings");
        }

        // Load settings
        settings.load();
        settings.validate();
    }

    public Logger getLogger()
    {
        return logger;
    }

    public GatewayDiscordClient getClient()
    {
        return client;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public MongoManager getMongo()
    {
        return mongo;
    }

    public Player registerPlayer(String id)
    {
        Player player = Player.create(this, id);
        player.getResources().addCrystals(5000);
        getMongo().savePlayer(player);
        playerMap.put(id, player);
        return player;
    }

    public Player registerPlayer(User user)
    {
        return registerPlayer(user.getId().asString());
    }

    public Player getPlayer(User user)
    {
        return getPlayer(user.getId().asString());
    }

    public Player getPlayer(String id)
    {
        return playerMap.getOrDefault(id, null);
    }

    public boolean hasPlayer(User user)
    {
        return playerMap.containsKey(user.getId().asString());
    }

    public List<Card> getCards()
    {
        return new LinkedList<>(cardMap.values());
    }

    public Card getCard(int id)
    {
        return cardMap.get(id);
    }

    public Map<Card, Integer> searchCards(String query)
    {
        Map<Card, Integer> results = new HashMap<>();
        String[] split = query.toLowerCase().split(" ");

        for (String term : split)
        {
            for (Card card : cardMap.values())
            {
                int matches = 0;
                // ID
                if (String.valueOf(card.getId()).equals(term))
                    matches++;
                // Name
                if (card.getName().toLowerCase().contains(term))
                    matches++;
                // Variant
                if (card.hasVariant() && card.getVariant().toLowerCase().contains(term))
                    matches++;

                if (matches > 0)
                    results.put(card, matches);
            }
        }
        return results;
    }

    public Card searchCard(String query)
    {
        return searchCards(query).entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public List<Card> getCardsForStar(int star)
    {
        return cardMap.values().stream()
                .filter(card -> card.getTier() == star)
                .collect(Collectors.toList());
    }

    public ImageCache<Card> getCardImageCache()
    {
        return cardImageCache;
    }

    public ImageCache<Card> getCardFullRenderCache()
    {
        return cardFullRenderCache;
    }

    public Map<Integer, Card> fetchCards()
    {
        try (FileReader fr = new FileReader(settings.getDataFolder() + "//cards.json"))
        {
            Map<Integer, Card> cards = new HashMap<>();
            CardJson[] jsons = GSON.fromJson(fr, CardJson[].class);
            for (int i = 0; i < jsons.length; i++)
                cards.put(i, new Card(this, jsons[i], i));
            return cards;
        }
        catch (IOException error)
        {
            error.printStackTrace();
        }
        return null;
    }
}
