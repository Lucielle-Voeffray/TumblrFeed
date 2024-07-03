/*
    Discord bot allowing the creation of Tumblr feeds
    Copyright (C) 2024  Lucielle Voeffray

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

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

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import secrets.secrets;

import java.util.ArrayList;
import java.util.List;

public class Discord {
    private final String token;

    private GatewayDiscordClient client;
    private Tumblr tumblr;
    private Sql sql;

    public Discord() {
        token = secrets.DISCORD;
        client = null;
        tumblr = null;
        sql = null;
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

    public void start() {

        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equalsIgnoreCase("greet")) {
                return event.reply(String.format("Hallo du lieb %s", event.getOption("name")
                        .flatMap(ApplicationCommandInteractionOption::getValue)
                        .map(ApplicationCommandInteractionOptionValue::asString)
                        .get()));
            } else {
                return event.reply("Something went wrong :smiling_face_with_tear:");
            }
        }).subscribe();


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

        ApplicationCommandRequest[] commands = new ApplicationCommandRequest[]{greetCmdRequest, createListMySearches, createSearchPause, createSearchDeletion, createSearchRequest, createListServerSearches, createDeleteMyData};

        // Create guild command with discord
        long guildId = secrets.ADMIN_SERVER; //Your admin/test server (Will give access to the app_admin commands (You'll also need to be marked as app_admin in the database)).

        //Create commands from the list
        for (ApplicationCommandRequest command : commands) {
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, guildId, command)
                    .subscribe();
        }

        // Create global command with Discord
        /*client.getRestClient().getApplicationService()
                .createGlobalApplicationCommand(applicationId, greetCmdRequest)
                .subscribe();*/
    }

    public Tumblr getTumblr() {
        return tumblr;
    }

    public void setTumblr(Tumblr tumblr) {
        this.tumblr = tumblr;
    }

    public Sql getSql() {
        return sql;
    }

    public void setSql(Sql sql) {
        this.sql = sql;
    }
}
