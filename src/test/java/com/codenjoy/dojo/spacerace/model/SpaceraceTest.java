package com.codenjoy.dojo.spacerace.model;

import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.EventListener;
import com.codenjoy.dojo.services.PrinterFactory;
import com.codenjoy.dojo.services.PrinterFactoryImpl;
import com.codenjoy.dojo.spacerace.services.Events;
import com.codenjoy.dojo.utils.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: sanja
 * Date: 17.12.13
 * Time: 4:47
 */
public class SpaceraceTest {

    public final static BulletCharger UNLIMITED_CHARGER = new BulletCharger(1000, 1);
    private Spacerace game;
    private BulletCharger charger = UNLIMITED_CHARGER;
    private Hero hero;
    private Dice dice;
    private EventListener listener;
    private Player player;
    private PrinterFactory printer = new PrinterFactoryImpl();

    @Before
    public void setup() {
        dice = mock(Dice.class);
    }

    private void dice(int...ints) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int i : ints) {
            when = when.thenReturn(i);
        }
    }

    private void diceNew(int...ints) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));

        if(ints.length == 0){ // we work just with stones
            when = when.thenReturn(-1);
        }

        if(ints.length == 1){ // we work just with stones
            when = when.thenReturn(-1, -1, -1, -1, ints[0], -1);
        }

        if(ints.length == 2){ // we work with stones and bombs
            when = when.thenReturn(-1, -1, -1, -1, ints[0], ints[1], -1);
        }

        if(ints.length == 4){ // we work stones, bombs and bulletPacks
            when = when.thenReturn(ints[2], ints[3], ints[0], ints[1], -1);
        }
    }



    private void givenFl(String board) {
        LevelImpl level = new LevelImpl(board);
        Hero hero = level.getHero(charger).get(0);

        game = new Spacerace(level, dice,
                charger.getTicksToRecharge(),
                charger.getBulletsCount());
        listener = mock(EventListener.class);
        player = new Player(listener);
        game.newGame(player);
        player.hero = hero;
        hero.init(game);
        this.hero = game.getHeroes().get(0);
    }

    private void assertE(String expected) {
        assertEquals(TestUtils.injectN(expected),
                printer.getPrinter(game.reader(), player).print());
    }

    // есть карта со мной
    @Test
    public void shouldFieldAtStart() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        assertE("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    // я могу двигаться
    @Test
    public void shouldFieldICanMove() {
        //given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");
        //when
        diceNew(); // выключаем генерацию каменей и мин
        hero.up();
        game.tick();

        //then
        assertE("☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        hero.right();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼  ☺☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        hero.down();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼  ☺☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        hero.left();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    // появляется новый комень
    @Test
    public void shouldNewStone() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼☺  ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        diceNew(1);
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼ 0 ☼" +
                "☼   ☼" +
                "☼☺  ☼" +
                "☼   ☼" +
                "☼   ☼");
        //When
        diceNew(2);
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼  0☼" +
                "☼   ☼" +
                "☼☺  ☼" +
                "☼ 0 ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldStoneMove() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");

        //When
        diceNew(0);
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼0  ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");
        //When
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼0  ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");
    }

    @Test
    public void shouldStoneAppearsEvery3seconds() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");

        //When
        diceNew(1); // камень в первой колонке, мины нет, камень во второй колонке
        game.tick();
        game.tick();
        game.tick();
        diceNew(2);
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼  0☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ 0 ☼" +
                "☼ ☺ ☼");
    }

    @Test
    public void shouldHeroShoot() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //When
        diceNew();
        hero.recharge();
        hero.act();
        game.tick();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼ * ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldBulletOutOfTheBoard() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        diceNew();
        hero.recharge();
        hero.act();
        game.tick();


        //Then
        assertE("☼   ☼" +
                "☼ * ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        game.tick();
        game.tick();

           //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldStoneIsDestroyedByBullet() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");
        //When
        diceNew(1);
        hero.recharge();
        hero.act();
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼ 0 ☼" +
                "☼ * ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");

        //When
        game.tick();

        //Then
        assertE("☼ x ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");

    }

    @Test
    public void shouldStoneIsDestroyedByBullet2() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //When
        diceNew(1);
        hero.recharge();
        hero.act();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼ * ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //When
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼ * ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //When
        game.tick();

        //Then
        assertE("☼ x ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //When
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

    }

    // проявляем новую мину
    @Test
    public void shouldNewBomb() {
        //given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        diceNew(-1, 1); // камень не появляем, мину появляем, тоже на 3-м тике
        game.tick();
        game.tick();
        game.tick();


        //then
        assertE("☼ ♣ ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldNewBombAtRandomPlace() {
        //given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼  ☺☼");

        //when
        diceNew(-1, 0);
        game.tick();
        game.tick();
        game.tick();

        //then
        assertE("☼♣  ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼  ☺☼");

        //when
        diceNew(-1, 2);
        game.tick();
        game.tick();
        game.tick();

        //then
        assertE("☼  ♣☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼♣  ☼" +
                "☼  ☺☼");
    }

    @Test
    public void shouldNewBombAndNewStoneAtNewPlace() {
        //given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        //when
        diceNew(0, 2);
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        //then
        assertE("☼   ☼" +
                "☼0 ♣☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldBombRemovedWhenOutsideBorder() {
        //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☺  ☼");

        //When
        diceNew(-1, 2);
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼  ♣☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☺  ☼");

        //When
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☺ ♣☼");

        //When
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☺  ☼");
    }

    @Test
    public void shouldBombDestroyedByBullet() {
        // given

        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");


        diceNew(-1, 1);
        hero.recharge();
        hero.act();
        game.tick();
        game.tick();
        game.tick();


        assertE("☼ ♣ ☼" +
                "☼ * ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");

        game.tick();

        assertE("☼xxx☼" +
                "☼xxx☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼");
    }

    @Test
    public void shouldBombDestroyedByBullet2() {
        // given

        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");


        diceNew(-1, 1);
        hero.recharge();
        hero.act();
        game.tick();
        game.tick();


        assertE("☼   ☼" +
                "☼ * ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        game.tick();

        assertE("☼xxx☼" +
                "☼xxx☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");

        game.tick();

        assertE("☼   ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldNewBombAndNewStoneAtNewPlace2() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        diceNew(1, 3);
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼ 0 ♣☼" +
                "☼    ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");
    }

    @Test
    public void shouldBombDestroyedByBulletNew() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼");

        diceNew(0, 2);
        hero.recharge();
        game.tick();
        game.tick();
        hero.act();
        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼0 ♣ ☼" +
                "☼    ☼" +
                "☼  * ☼" +
                "☼    ☼" +
                "☼  ☺ ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼ xxx☼" +
                "☼0xxx☼" +
                "☼ xxx☼" +
                "☼    ☼" +
                "☼  ☺ ☼");

    }

    @Test
    public void shouldBombDestroyHero() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        diceNew(0, 2);
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼0 ♣ ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼ xxx☼" +
                "☼0xxx☼" +
                "☼ xxx☼" +
                "☼    ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼0 + ☼" +
                "☼    ☼");
    }

    @Test
    public void shouldBombDestroyHeroAndResurrectionHero() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        diceNew(0, 2);
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼0 ♣ ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼ xxx☼" +
                "☼0xxx☼" +
                "☼ xxx☼" +
                "☼    ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼0 + ☼" +
                "☼    ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼0   ☼");
    }

    @Test
    public void shouldBombDestroyHeroRightAndResurrectHero() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        diceNew(0, 3);

        game.tick();
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼0  ♣☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼  xxx" +
                "☼0 xxx" +
                "☼  xxx" +
                "☼    ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼0 + ☼" +
                "☼    ☼");
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼0   ☼");
    }

    @Test
    public void shouldStoneDestroyHero() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        diceNew(2);
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼  0 ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        game.tick();
        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  0 ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        game.tick();

        // then
        assertE("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  + ☼" +
                "☼    ☼");
    }

    @Test
    public void shouldCountScores() {
        // given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        assertEquals(0, player.getScore());
        player.event(Events.DESTROY_BOMB);

        assertEquals(1, player.getScore());
        player.event(Events.DESTROY_STONE);
        player.event(Events.DESTROY_STONE);

        assertEquals(3, player.getScore());
        player.event(Events.DESTROY_ENEMY);

        assertEquals(4, player.getScore());
        player.event(Events.LOOSE);

        assertEquals(0, player.getScore());
        assertEquals(4, player.getMaxScore());
    }

    @Test
    public void shouldBulletChargerOnField() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

        diceNew(-1, -1, 1, 1);
        game.tick();
        game.tick();

        // then
        assertE("☼ 7  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼  ☺ ☼" +
                "☼    ☼");

    }

    @Test
    public void shouldHeroPickUpBulletPack() {
        // given
        givenFl("☼    ☼" +
                "☼    ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        diceNew(-1, -1, 1, 1);
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        // then
        assertE("☼ 7  ☼" +
                "☼    ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        hero.up();
        game.tick();

        assertE("☼ 7  ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        hero.up();
        game.tick();

        assertE("☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");
    }

    @Test
    public void shouldNewBulletPackAfterHeroGetOldBulletPack() {
        // given
        givenFl("☼    ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        diceNew(-1, -1, 0, 1);
        game.tick();

        // then
        assertE("☼7   ☼" +
                "☼ ☺  ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        hero.left();
        game.tick();

        // then
        assertE("☼7   ☼" +
                "☼☺   ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");

        hero.up();
        game.tick();
        diceNew(-1, -1, 3, 0);
        game.tick();

        // then
        assertE("☼☺   ☼" +
                "☼   7☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼");
    }

    @Test
    public void shouldHeroShootAfterRecharge() {
        //Given
        givenFl("☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when

        diceNew(-1, -1, 0, 0);
        game.tick();

        //Given
        assertE("☼   ☼" +
                "☼7☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when
        hero.act();
        game.tick();

        //Given
        assertE("☼ * ☼" +       // у него в начале теста есть одна пуля, выплюнем ее
                "☼7☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when
        hero.act();
        game.tick();

        //Given
        assertE("☼   ☼" +
                "☼7☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        hero.left();
        game.tick();

        //Given
        assertE("☼   ☼" +
                "☼☺  ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

      //When
        hero.act();
        game.tick();

        //Then
        assertE("☼*  ☼" +
                "☼☺  ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    @Test
    public void shouldNoBulletsAfterFireWithEmptyBulletCharger() {
       //Given
        givenFl("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when
        diceNew(-1, -1, -1, -1);
        hero.act();
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when
        diceNew(-1, -1, 1, 0);
        game.tick();

        //Then
        assertE("☼   ☼" +
                "☼ 7 ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼");

        //when
        hero.up();
        game.tick();

        //then
        assertE("☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        //When
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();
        hero.act();
        game.tick();

        //Given
        assertE("☼   ☼" +   // todo по подстетам пуля должна быть (последняя), говорит, что нету
                "☼ ☺ ☼" +   
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");

        hero.act();
        game.tick();

        //Given
        assertE("☼   ☼" +
                "☼ ☺ ☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼   ☼");
    }

    // появление на поле магазина патронов
    // итераторы
    // инструкция
    // золото/здоровье
    // лазер
    // выстрелы не каждый тик (?)
    // плюшка, которая позволяет стрелать каждый тик, пока она действует (время или количество снарядов)
    // узнать причину загадочного ексепшна при вылете игры и исправить
    // написать нормального бота

/*
    некорорые тесты сломались из-за акт(когда есть возможность выстрела - сказать, что нет патронов)
    если есть пули - более агрессивная политика
    если падает сверху мина - считать, куда уклоняться (сейчас вправо)
    как можно меньше времени проводить в самых верхних рядах
    уклонения от пуль
    стрелять в других игроков
    потестить все позиции мин рядом
    если меня убивают - вылетает эксепшин (вроде пофиксил)
    проверить баг полета навстречу камню
    вынести в настройки кол-во пэков
    переделать дайс и все тесты
    рефакторинг!!!!

    */
}
