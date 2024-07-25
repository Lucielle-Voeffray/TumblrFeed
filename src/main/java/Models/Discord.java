/*
    Discord bot allowing the creation of Tumblr feeds
    Copyright (C) 2024  Lucielle Voeffray

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    Contact:
        pro@lucielle.ch

*/


package Models;

import app.TumblrFeed.supervisor;
import com.tumblr.jumblr.types.Post;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import secrets.secrets;
import services.Error;
import services.LogType;

import java.util.ArrayList;
import java.util.List;

public class Discord implements supervisor {
    private final String token;
    private GatewayDiscordClient client;
    long guildId;

    public Discord() {
        token = secrets.DISCORD;
        client = null;
        guildId = secrets.ADMIN_SERVER;
    }

    public boolean connect() {
        boolean success = false;

        client = DiscordClientBuilder.create(token).build()
                .login()
                .block();

        if (client != null) {
            success = true;
        }

        return success;
    }

    @Override
    public void disconnect() {
        client.logout().subscribe();
    }

    @Override
    public void start() {
        client.on(ChatInputInteractionEvent.class, event -> {

            String userID = event.getInteraction().getUser().getId().asString();

            String serverID = event.getInteraction().getGuildId().get().asString();

            String channelID = event.getInteraction().getChannelId().asString();

            switch (event.getCommandName().toLowerCase()) {
                case "greet":
                    String name = event.getOption("name")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .get();

                    return event.reply(String.format(greet(name)));
                case "createsearch":
                    String search = event.getOption("search")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .get();

                    String searchName = event.getOption("nameyoursearch")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .get();

                    String message = createSearch(search, searchName, userID, serverID, channelID);

                    return event.reply(message);

                default:
                    return event.reply(supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 2").get(0).get("english"));
            }
        }).subscribe();
    }

    public String greet(String name) {
        String greetings = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 2").get(0).get("english");
        return String.format(greetings, name);
    }


    public void sendPosts(@NotNull List<Post> posts, @NotNull String channelID, @NotNull String searchName) {

        if (posts.get(0) != null) {
            Sql sql = supervisor.getSql();
            final String text = sql.select("SELECT english FROM t_text WHERE pk_text = 1").get(0).get("english");

            for (int i = posts.size(); i >= 0; i++) {
                int finalI = i;
                client.getChannelById(Snowflake.of(channelID))
                        .ofType(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage(String.format(text, searchName, posts.get(finalI).getPostUrl())))
                        .subscribe();
            }
        }
    }

    public String createSearch(String searchName, String toSearch, String userID, String channelID, String serverID) {

        String message = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 2").get(0).get("english");

        if (searchForIllegalTerms == 0) {

            int isSearchCreationSuccessful = supervisor.getSql().createSearch(toSearch, searchName, userID, serverID, channelID);

            if (isSearchCreationSuccessful == 0) {
                message = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 3").get(0).get("english");
            } else if (isSearchCreationSuccessful == 2) {
                message = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 5").get(0).get("english");
                Error.report(LogType.ERROR, "Discord.java", "createSearch", 0, message);
            } else if (isSearchCreationSuccessful == 3) {
                message = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 6").get(0).get("english");
                Error.report(LogType.ERROR, "Discord.java", "createSearch", 1, message);
            }
        } else {
            message = supervisor.getSql().select("SELECT english FROM t_text WHERE pk_text = 6").get(0).get("english");
        }

        return message;
    }

    public void build() {
        // Get our application's ID
        long applicationId = client.getRestClient().getApplicationId().block();

        // Build our command's definition
        //Build greet
        ApplicationCommandRequest greetCmdRequest = ApplicationCommandRequest.builder()
                .name("greet")
                .description("Greets You")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Your name")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        // Build search constructor
        ApplicationCommandOptionData searchName = ApplicationCommandOptionData.builder()
                .name("nameyoursearch")
                .description("This name will be used when you want to delete or modify it")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build();

        ApplicationCommandOptionData searchInput = ApplicationCommandOptionData.builder()
                .name("search")
                .description("What does the bot have to search on Tumblr")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build();

        List<ApplicationCommandOptionData> createSearchOption = new ArrayList<>();
        createSearchOption.add(searchName);
        createSearchOption.add(searchInput);

        ApplicationCommandRequest createSearchRequest = ApplicationCommandRequest.builder()
                .name("createsearch")
                .description("Creates a search on Tumblr")
                .addAllOptions(createSearchOption)
                .build();

        // Build search Deletion
        ApplicationCommandRequest createSearchDeletion = ApplicationCommandRequest.builder()
                .name("deletesearch")
                .description("[IRREVERSIBLE] Delete a search by it's name.")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("searchname")
                        .description("The name of the search you want to erase")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                )
                .build();

        // Build pausesearch
        ApplicationCommandRequest createSearchPause = ApplicationCommandRequest.builder()
                .name("pausesearch")
                .description("Pauses/Unpauses the search until reuse of this command")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("searchname")
                        .description("The name of the search you want to erase")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        // Build listmysearches
        ApplicationCommandRequest createListMySearches = ApplicationCommandRequest.builder()
                .name("listmysearches")
                .description("Lists all the searches you created in this server")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("scope")
                        .description("enter 'server' for all you created on the server or 'channel' for this channel")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        // Build listserversearches
        ApplicationCommandRequest createListServerSearches = ApplicationCommandRequest.builder()
                .name("listserversearches")
                .description("[Server Admin Only] Lists all the searches done by everyone in this server")
                .build();

        // Build deletemydata
        ApplicationCommandRequest createDeleteMyData = ApplicationCommandRequest.builder()
                .name("deletemydata")
                .description("[IRREVERSIBLE] Will delete all known data about you from my database")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("username")
                        .description("Confirm your choice by entering your username")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        // Build deleteserverdata
        ApplicationCommandRequest createDeleteServerData = ApplicationCommandRequest.builder()
                .name("deleteserverdata")
                .description("[Server Admin only] If you want to delete all data concerning your server")
                .build();

        // Build contactcreator
        ApplicationCommandRequest createContactCreator = ApplicationCommandRequest.builder()
                .name("contactcreator")
                .description("Will answer you with a way to contact the person responsible of the bot")
                .build();

        //App_admin commands (Guild commands on secrets.ADMIN_SERVER)
        // Build erasedatafromuser
        ApplicationCommandRequest createEraseDataFromUser = ApplicationCommandRequest.builder()
                .name("erasedatafromuser")
                .description("[APP_ADMIN ONLY] Will delete all data from a given user")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("username")
                        .description("Username of the user to delete")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .build()
                ).build();

        // Build pauseuser
        ApplicationCommandRequest createPauseUser = ApplicationCommandRequest.builder()
                .name("pauseuser")
                .description("[APP_ADMIN ONLY] Will cut all the possibilities for this user to use the bot and pause ALL their searches")
                .build();

        // Build exitgracefully
        ApplicationCommandRequest createExitGracefully = ApplicationCommandRequest.builder()
                .name("exitgracefully")
                .description("[APP_ADMIN ONLY] Will stop the bot")
                .build();

        ApplicationCommandRequest[] commands = new ApplicationCommandRequest[]{greetCmdRequest, createListMySearches, createSearchPause, createSearchDeletion, createSearchRequest, createListServerSearches, createDeleteMyData, createDeleteServerData, createContactCreator};
        ApplicationCommandRequest[] appAdminCommands = new ApplicationCommandRequest[]{createEraseDataFromUser, createPauseUser, createExitGracefully};

        // Create guild command with discord

        //Create commands from the list
        for (ApplicationCommandRequest command : commands) {
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, guildId, command)
                    .subscribe();
        }

        // Create commands for App_Admin
        for (ApplicationCommandRequest command : appAdminCommands) {
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, guildId, command)
                    .subscribe();
        }

        // Create global command with Discord
        /*client.getRestClient().getApplicationService()
                .createGlobalApplicationCommand(applicationId, greetCmdRequest)
                .subscribe();*/
    }
}
