package com.rlee.discordbots.rpbot.dice;

import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.regitstry.AliasRegistry;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RollCalculatorTest {
    @Mock private TextChannel channel; // For now, only testing TextChannel not MessageChannel
    @Mock private Message message;
    @Mock private User author;
    @Mock private Member authorMember;
    @Mock private User selfUser;
    @Mock private MessageAction messageAction;
    @Mock private ThreadLocalRandom threadLocalRandom;
    @Mock private Guild guild;
    @Mock private RPGame game;
    @Mock private RollConfig rollConfig;
    @Mock private AliasRegistry aliasRegistry;
    @Mock private ProfileRegistry profileRegistry;
    @Mock private CharProfile authorProfile;
    @Mock private CharProfile otherProfile;

    private RollCalculator rollCalculator;

    private final String authorMention = "@author";

    private void mockPrinter(String content) {
        when(author.getAsMention()).thenReturn(authorMention);
        when(channel.sendMessage(content)).thenReturn(messageAction);
        doNothing().when(messageAction).queue();
    }

    private void verifyPrint(String content) {
            verify(channel).sendMessage(content);
    }

    private void mockRandInt(int range) {
        when(threadLocalRandom.nextInt(range)).thenReturn(range - 1);
    }

    @BeforeEach
    public void beforeEach() {
        rollCalculator = new RollCalculator();

        try (MockedStatic<RPBot> rpBot = mockStatic(RPBot.class)) {
            rpBot.when(RPBot::selfUser).thenReturn(selfUser);
        }

        when(message.getAuthor()).thenReturn(author);
    }

    @Test
    public void whenNoArgsGivenDefaultIsUsed() {
        mockRandInt(20);
        String expectedOutput = String.format("%s rolled **%d**.", authorMention, 20);
        mockPrinter(expectedOutput);
        try (MockedStatic<ThreadLocalRandom> threadLocalRandomClass = mockStatic(ThreadLocalRandom.class)) {
            threadLocalRandomClass.when(ThreadLocalRandom::current).thenReturn(threadLocalRandom);
            rollCalculator.compute("", channel, message);
        }
        verifyPrint(expectedOutput);
    }

    @Test
    public void whenDiceGivenDiceUsed() {
        when(channel.getGuild()).thenReturn(guild);
        when(game.getRollConfig()).thenReturn(rollConfig);
        when(rollConfig.getRollAttribute()).thenReturn(false);
        when(game.getProfileRegistry()).thenReturn(profileRegistry);
        when(message.getMember()).thenReturn(authorMember);
        when(game.getAliasRegistry()).thenReturn(aliasRegistry);

        String expression = "d10 + d5";

        mockRandInt(10);
        mockRandInt(5);
        String expectedOutput = String.format("%s rolled **%d**. (%d + %d)", authorMention, 15, 10, 5);
        mockPrinter(expectedOutput);
        try (MockedStatic<ThreadLocalRandom> threadLocalRandomClass = mockStatic(ThreadLocalRandom.class);
             MockedStatic<RPBot> rpBot = mockStatic(RPBot.class)) {
            threadLocalRandomClass.when(ThreadLocalRandom::current).thenReturn(threadLocalRandom);
            rpBot.when(() -> RPBot.getGame(guild)).thenReturn(game);

            rollCalculator.compute(expression, channel, message);
        }
        verifyPrint(expectedOutput);
    }
}
