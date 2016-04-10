package fr.unice.polytech.qgl.playersocket;

import fr.unice.polytech.qgl.qbd.utils.ChampionshipRunner;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * PolyTech Nice / SI3 / Module POO-Java
 * Annee 2015 - IslandWorking - Lab 3
 * Package fr.unice.polytech.qgl.playersocket
 *
 * @author Flavian Jacquot
 * @version 09/04/2016
 * @since 1.8.0_60
 */
public class SocketPlayerTest {
    @Test
    public void test()
    {
        ChampionshipRunner runner = new ChampionshipRunner(1);
        runner.setTimeOut(Integer.MAX_VALUE);
        runner.setClass(new SocketPlayer());
        runner.runWith();
        runner.fire(false);
    }

}