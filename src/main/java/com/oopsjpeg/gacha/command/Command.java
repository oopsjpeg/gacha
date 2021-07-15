package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.Player;
import com.oopsjpeg.gacha.object.user.PlayerCard;
import com.oopsjpeg.gacha.object.Banner;
import com.oopsjpeg.gacha.util.PagedList;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Command interface.
 * Created by oopsjpeg on 1/30/2019.
 */
public enum Command
{
    HELP("help", "View helpful information about Gacha Battle.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    User self = call.getClient().getSelf().block();

                    return new CommandResult(call).setEmbed(e -> e
                            .setTitle("Gacha Battle Commands")
                            .setDescription(Arrays.stream(Command.values())
                                    // Sort commands by name
                                    .sorted(Comparator.comparing(Command::getName))
                                    // Format and list the commands
                                    .map(c -> "`" + call.format(c) + "` - " + c.getDescription())
                                    .collect(Collectors.joining("\n"))));
                }
            },
    PROFILE("profile", "View a player's profile.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Player player = gacha.getPlayer(user);
                    List<PlayerCard> cards = player.getCards();

                    PlayerCard displayCard = player.hasFavoriteCard() ? player.getFavoriteCard() : player.getBestCard();

                    CommandResult result = new CommandResult(call).setEmbed(e ->
                    {
                        e.setAuthor(user.getUsername() + " (" + Util.stars(player.getTier()) + ")", null, user.getAvatarUrl());
                        e.setColor(Util.getColor(user, channel));
                        // Display Card Thumbnail
                        if (displayCard != null) e.setThumbnail("attachment://" + displayCard.getId() + ".png");
                        // Description
                        e.setDescription(player.getDescription());
                        // Resources
                        e.addField("Resources", Util.sticker("Crystals", Util.crystals(player.getResources().getCrystals()))
                                + "\n" + Util.sticker("Zenith Cores", Util.zenithCores(player.getResources().getZenithCores()))
                                + "\n" + Util.sticker("Violet Runes", Util.violetRunes(player.getResources().getVioletRunes())), false);
                        // Cards
                        int cardsOwned = cards.size();
                        int cardsTotal = gacha.getCards().size();
                        float percentOwned = (float) cardsOwned / cardsTotal;
                        e.addField("Cards", Util.comma(cardsOwned) + " / " + Util.comma(cardsTotal) + " (" + Util.percent(percentOwned) + ")", true);
                        // Timelies
                        List<String> timelies = new ArrayList<>();
                        timelies.add(player.hasDaily() ? "**Daily** is available." : "**Daily** is available in " + player.timeUntilDaily());
                        timelies.add(player.hasWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + player.timeUntilWeekly());
                        e.addField("Timelies", String.join("\n", timelies), false);
                    });

                    if (displayCard != null)
                        try (InputStream is = gacha.getCardImageCache().retrieveAsStream(displayCard.getCard()))
                        {
                            result.setFile(displayCard.getId() + ".png", gacha.getCardImageCache().retrieveAsStream(displayCard.getCard()));
                        }
                        catch (IOException error)
                        {
                            return CommandResult.failure(call, error);
                        }

                    return result;
                }
            },
    REGISTER("register", "Register a new Gacha Battle profile.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();

                    if (gacha.hasPlayer(user))
                        return CommandResult.failure(call, "You already registered a profile.");

                    gacha.getMongo().savePlayer(gacha.registerPlayer(user));

                    return new CommandResult(call).setEmbed(e -> e
                            .setTitle("You're now registered to play Gacha Battle!")
                            .setDescription("Check your profile with `" + call.format(PROFILE) + "`."
                                    + "\nPull a new card with `" + call.format(PULL) + "`."
                                    + "\nCollect a daily reward with `" + call.format(DAILY) + "`."
                                    + "\n\nHave fun!")
                            .setColor(Util.getColor(user, channel)));
                }
            },
    DESCRIPTION("description", "Update your profile description.")
            {
                private static final int MAX_LENGTH = 200;

                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Player player = gacha.getPlayer(user);

                    if (!call.hasArguments())
                    {
                        if (!player.hasDescription())
                            return CommandResult.failure(call, "You don't have a profile description set.");
                        return CommandResult.info(call, player.getDescription());
                    }

                    if (call.getArgument(0).equalsIgnoreCase("clear"))
                    {
                        if (!player.hasDescription())
                            return CommandResult.failure(call, "You don't have a profile description set.");
                        player.clearDescription();
                        gacha.getMongo().savePlayer(player);
                        return CommandResult.success(call, "Your profile description has been cleared.");
                    }

                    String description = call.getArgumentsRaw();
                    if (description.length() > MAX_LENGTH)
                        return CommandResult.failure(call, "Your profile description can't be longer than **" + MAX_LENGTH + "** characters.");

                    player.setDescription(description);
                    gacha.getMongo().savePlayer(player);

                    return CommandResult.success(call, "Your profile description has been set.\n\n" + description);
                }
            },
    FAVORITE("favorite", "Set your favorite card to show on your profile.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Player player = gacha.getPlayer(user);

                    if (!player.hasCards())
                        return CommandResult.failure(call, "You don't have any cards.");

                    try
                    {
                        if (!call.hasArguments())
                        {
                            if (!player.hasFavoriteCard())
                                return CommandResult.failure(call, "You don't have a favorite card set.");
                            return CommandResult.card(call, player.getFavoriteCard(), null, null);
                        }

                        if (call.getArgument(0).equalsIgnoreCase("clear"))
                        {
                            if (!player.hasFavoriteCard())
                                return CommandResult.failure(call, "You don't have a favorite card set.");
                            player.clearFavoriteCard();
                            gacha.getMongo().savePlayer(player);
                            return CommandResult.success(call, "Your favorite card has been cleared.");
                        }

                        PlayerCard card = player.searchCard(call.getArgumentsRaw());
                        if (card == null)
                            return CommandResult.failure(call, "You either don't have that card, or it doesn't exist.");

                        player.setFavoriteCard(card);
                        gacha.getMongo().savePlayer(player);

                        return CommandResult.card(call, card, "Your favorite card has been set to **" + card.getName() + "**"
                                + (card.getCard().hasVariant() ? " " + card.getCard().getVariant() : "") + ".", null);
                    }
                    catch (IOException error)
                    {
                        return CommandResult.failure(call, error);
                    }
                }
            },
    CARD("card", "View one of your cards.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Player player = gacha.getPlayer(user);

                    if (!player.hasCards())
                        return CommandResult.failure(call, "You don't have any cards.");

                    // Find the card by ID or name
                    PlayerCard card = player.searchCard(call.getArgumentsRaw());
                    if (card == null)
                        return CommandResult.failure(call, "You either don't have that card, or it doesn't exist.");

                    try
                    {
                        return CommandResult.card(call, card, null, null);
                    }
                    catch (IOException error)
                    {
                        return CommandResult.failure(call, error);
                    }
                }
            },
    CARDS("cards", "View your cards.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Player player = gacha.getPlayer(user);

                    if (!player.hasCards())
                        return CommandResult.failure(call, "You don't have any cards.");

                    PagedList<PlayerCard> cards = new PagedList<>(player.getCards(), 10);
                    // Sort cards by star, then by name
                    cards.sort(Comparator.comparingInt(PlayerCard::getTier).reversed().thenComparing(PlayerCard::getName));

                    int cardsOwned = cards.size();

                    // Show all cards
                    if (call.getArgument(0).equalsIgnoreCase("all"))
                    {
                        try
                        {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            baos.write((user.getUsername() + "'s Cards (" + Util.comma(cardsOwned) + ")"
                                    + "\n" + cards.getOriginal().stream().map(PlayerCard::formatRaw)).getBytes(StandardCharsets.UTF_8));
                            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                            CommandResult result = new CommandResult(call)
                                    .setContent("Viewing all of " + Util.formatUsername(user) + "'s cards.")
                                    .setFile("Cards_" + Util.toFileName(LocalDateTime.now()) + ".txt", bais);

                            baos.close();
                            bais.close();

                            return result;
                        }
                        catch (IOException error)
                        {
                            return CommandResult.failure(call, error);
                        }
                    }

                    AtomicInteger page = new AtomicInteger(1);

                    if (Util.isDigits(call.getLastArgument()))
                        page.set(Integer.parseInt(call.getLastArgument()));

                    if (page.get() <= 0 || page.get() > cards.pages())
                        return CommandResult.failure(call, "There's only " + cards.pages() + " page(s).");

                    List<PlayerCard> pagedCards = cards.get();
                    return new CommandResult(call).setEmbed(e -> e
                            .setAuthor(user.getUsername() + "'s Cards (" + Util.comma(cardsOwned) + ")", null, user.getAvatarUrl())
                            .setDescription(pagedCards.stream().map(PlayerCard::format).collect(Collectors.joining("\n")))
                            .setFooter("Page " + page.get() + " / " + cards.pages(), null));
                }
            },
    PULL("pull", "Pull a card.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Player player = gacha.getPlayer(user);
                    Banner banner = Banner.STANDARD; // TODO Add selection when more banners are available

                    if (player.getResources().getCrystals() < banner.getCost())
                        return CommandResult.failure(call, "You need **" + Util.crystals(banner.getCost()) + "** to pull from **" + banner.getName() + "**.");

                    Card card = banner.get(player);
                    PlayerCard playerCard;
                    String message = null;
                    player.getResources().subCrystals(banner.getCost());

                    if (player.hasCard(card))
                    {
                        playerCard = player.getCard(card);

                        int violetRunes = 25 * card.getTier();
                        int xp = (int) (160 + (playerCard.getMaxXp() * 0.4f));

                        player.getResources().addVioletRunes(violetRunes);
                        playerCard.addXp(xp);
                        playerCard.handleXp();

                        message = "**Re-Pull**"
                                + "\n" + "**`+" + Util.violetRunes(violetRunes) + "`**"
                                + "\n" + "**`+" + Util.xp(xp) + "`**";
                    }
                    else
                    {
                        playerCard = PlayerCard.create(player, card);
                    }

                    player.addCard(playerCard);
                    gacha.getMongo().savePlayer(player);

                    try
                    {
                        return CommandResult.card(call, playerCard, "Pulled **" + card.getName() + "** from **" + banner.getName() + "**.", message);
                    }
                    catch (IOException error)
                    {
                        return CommandResult.failure(call, error);
                    }
                }
            },
    DAILY("daily", "Collect your daily reward.")
            {
                private static final int CRYSTALS = 1000;

                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Player player = gacha.getPlayer(user);

                    if (!player.hasDaily())
                        return CommandResult.failure(call, "Your **Daily** is available in " + player.timeUntilDaily() + ".");

                    int amount = CRYSTALS;
                    player.getResources().addCrystals(amount);
                    player.setDailyDate(LocalDateTime.now());
                    gacha.getMongo().savePlayer(player);

                    return CommandResult.success(call, "+**`" + Util.crystals(amount) + "`** from **Daily**.");
                }
            },
    WEEKLY("weekly", "Collect your weekly reward.")
            {
                private static final int CRYSTALS = 5000;

                @Override
                public CommandResult execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Player player = gacha.getPlayer(user);

                    if (!player.hasWeekly())
                        return CommandResult.failure(call, "Your **Weekly** is available in " + player.timeUntilWeekly() + ".");

                    int amount = CRYSTALS;
                    player.getResources().addCrystals(amount);
                    player.setWeeklyDate(LocalDateTime.now());
                    gacha.getMongo().savePlayer(player);

                    return CommandResult.success(call, "+**`" + Util.crystals(amount) + "`** from **Weekly**.");
                }
            },
    GIVE_CRYSTALS("givecrystals", "Give a player crystals.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    User user = call.getUser();
                    User target = call.getClient().getUserById(Snowflake.of(call.getArgument(0))).block();
                    Player targetData = call.getGacha().getPlayer(target);
                    int crystals = Integer.parseInt(call.getArgument(1));

                    targetData.getResources().addCrystals(crystals);
                    call.getGacha().getMongo().savePlayer(targetData);

                    return new CommandResult(call)
                            .setEmbed(e -> e
                                    .setDescription(Util.formatUsername(target) + " has received **" + Util.comma(crystals) + "** crystals."));
                }
            },
    BACKUP("backup", "Create a MongoDB back-up.")
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    User user = call.getUser();

                    //call.getGacha().getMongo().backup();

                    return new CommandResult(call).setContent("Created backup at `" + LocalDateTime.now() + "`.");
                }
            },
    COUNTHEARTS("counthearts", null)
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    GatewayDiscordClient client = call.getClient();
                    List<Message> hearts = client.getChannelById(Snowflake.of(856984021078769695L))
                            .cast(MessageChannel.class).block()
                            .getMessagesAfter(Snowflake.of(857010045081878598L))
                            .collectList().block();
                    hearts.stream()
                            .sorted(Comparator.comparingInt(m -> m.getReactions().isEmpty() ? 0 : m.getReactions().get(0).getCount()))
                            .forEach(m ->
                            {
                                User submitter = m.getAuthor().get();
                                int count = m.getReactions().isEmpty() ? 0 : m.getReactions().get(0).getCount();

                                System.out.println(submitter.getUsername() + "#" + submitter.getDiscriminator() + " : " + (count - 1) + " votes");
                            });
                    return CommandResult.success(call, "check console");
                }
            },
    TESTCARD("testcard", null)
            {
                @Override
                public CommandResult execute(CommandCall call)
                {
                    Player player = call.getGacha().getPlayer(call.getUser());
                    Card card = call.getGacha().searchCard(call.getArgumentsRaw());
                    try
                    {
                        return CommandResult.card(call, PlayerCard.create(player, card), null, null);
                    }
                    catch (IOException e)
                    {
                        return CommandResult.failure(call, "didnt work");
                    }
                }
            };

    static
    {
        //HELP.aliases = new String[]{"?", "about"};

        //PROFILE.aliases = new String[]{"account", "bal", "balance"};
        PROFILE.registeredOnly = true;
        //DESCRIPTION.aliases = new String[]{"desc", "bio"};
        DESCRIPTION.registeredOnly = true;

        //CARD.aliases = new String[]{"show", "summon", "sum"};
        CARD.registeredOnly = true;
        CARDS.registeredOnly = true;
        //PULL.aliases = new String[]{"gacha"};
        PULL.registeredOnly = true;
        //FORGE.aliases = new String[]{"craft", "combine"};
        //FORGE.registeredOnly = true;

        DAILY.registeredOnly = true;
        WEEKLY.registeredOnly = true;

        //TEST_CARD.registeredOnly = true;
        //TEST_CARD.developerOnly = true;
        GIVE_CRYSTALS.developerOnly = true;
        BACKUP.developerOnly = true;
    }

    private final String name;
    private final String description;
    private boolean developerOnly;
    private boolean registeredOnly;

    Command(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public abstract CommandResult execute(CommandCall call);

    public CommandResult tryExecute(CommandCall call)
    {
        User user = call.getUser();

        // Developer only
        if (developerOnly && !user.equals(call.getClient().getApplicationInfo().block().getOwner().block()))
            return CommandResult.failure(call, "You're not a developer.");
        // Registered only
        if (registeredOnly && !call.getGacha().hasPlayer(user))
            return CommandResult.failure(call, "You're not registered yet. Use `" + call.format(Command.REGISTER) + "` to create a profile.");

        return execute(call);
    }

    public boolean canExecute(CommandCall call)
    {
        Gacha gacha = call.getGacha();
        User user = call.getUser();
        GatewayDiscordClient client = gacha.getClient();
        User owner = client.getApplicationInfo().block().getOwner().block();

        return (!registeredOnly || gacha.hasPlayer(user)) && (!developerOnly || user.equals(owner));
    }

    public ApplicationCommandRequest app()
    {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .options(getOptions())
                .build();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<ApplicationCommandOptionData> getOptions()
    {
        return Collections.emptyList();
    }

    public boolean isDeveloperOnly()
    {
        return developerOnly;
    }

    public boolean isRegisteredOnly()
    {
        return registeredOnly;
    }
}
