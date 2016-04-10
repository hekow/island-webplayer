package fr.unice.polytech.qgl.qbd.utils;

import eu.ace_design.island.bot.IExplorerRaid;
import eu.ace_design.island.runner.Runner;
import fr.unice.polytech.qgl.playersocket.SocketPlayer;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static eu.ace_design.island.runner.Runner.run;

/**
 * PolyTech Nice / SI3 / Module POO-Java
 * Annee 2015 - qbd - Lab 3
 * Package fr.unice.polytech.qgl.qbd
 *
 * @author Flavian Jacquot
 * @version 23/11/2015
 * @since 1.8.0_60
 */
public class ChampionshipRunner {
    int weekNumber;
    private Runner runner;
    private File mapFile;
    private long seed;
    private int startX;
    private int startY;
    private String direction;
    private int actionPoints;
    private int mens;
    private HashMap<String, Integer> contrats;
    private String outputPath;
    private String weekId ="";
    private int numberOfContractsDone;
    private File log;
    private HashMap<String, Integer> collectedRessources;
    private int timeOut = 2000;
    private IExplorerRaid aClass;

    public ChampionshipRunner(int weekNumber) {

        numberOfContractsDone=-1;
        this.weekNumber = weekNumber;
        if(weekNumber<10)
            weekId="0"+weekId;
        weekId += weekNumber+"";
        final String mapPath = "https://raw.githubusercontent.com/mosser/QGL-15-16/master/championships/week" + weekId + "/_map.json";
        final String scalaPath = "https://raw.githubusercontent.com/mosser/QGL-15-16/master/arena/src/main/scala/championships/Week" + weekId + ".scala";
        final String scalaGeneralPath = "https://raw.githubusercontent.com/mosser/QGL-15-16/master/arena/src/main/scala/library/Islands.scala";


        String path = "maps/" + weekId + "/";


        //Apply
        File mapDirectory = new File(path);
        if (!mapDirectory.exists()) {
            mapDirectory.mkdir();

        }
        File mapFile = new File(path + "map.json");
        if(!mapFile.exists())
            download(mapPath, mapFile.getAbsolutePath());

        outputPath = path + "output/";
        File resultDir = new File(outputPath);
        if (!resultDir.exists()) {
            resultDir.mkdir();
        }



        if (!mapDirectory.exists()) {
            System.out.println("Map !exists Downloading ...");
            download(mapPath, mapFile.getPath());
        }

        this.mapFile = mapFile;

        File confFile = new File(path + "Week" + weekId + ".scala");
        String seedFile = "maps/Islands.scala";

        if (!confFile.exists()) {
            System.out.println("Scala conf file !exists Downloading ...");
            //Download scala declaration
            download(scalaPath, confFile.getPath());

            //Get seed
            download(scalaGeneralPath, seedFile);

        }
        contrats = new HashMap<>();
        parseScalaConf(confFile);

        //now get seed
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(seedFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String ligne :
                lines)
            if (ligne.contains("val s" + weekId)) {
                String seed = ligne.split("=")[1];
                //System.out.println("Setting seed:<" + seed + ">");
                seed = seed.replace("L", "");
                seed = seed.replace(" ", "");
                //System.out.println("Setting seed2:<" + seed + ">");

                // for(Character c:text)
                {

                }
                try {
                    this.seed = Long.decode(seed);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("char values");
                    System.out.println("Setting seed3:<" + seed + ">");

                    BigInteger bi = new BigInteger(seed.substring(2), 16);
                    System.out.println(bi);
                    System.out.println(Long.MAX_VALUE);
                    this.seed = bi.longValue();

                }

                break;
            }
    }

    //http://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
    private void download(String url, String to) {
        URL website = null;
        try {
            website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseScalaConf(File file) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            if (line.contains("val crew")) {
                String[] split = line.split(" ");
                this.mens = Integer.valueOf(split[split.length - 1]);
            } else if (line.contains("val budget")) {
                String[] split = line.split(" ");
                this.actionPoints = Integer.valueOf(split[split.length - 1]);
            } else if (line.contains("val plane")) {
                String[] split = line.split("\\(|\\)");
                String[] values = split[1].split(",");

                this.startX = Integer.valueOf(values[0].trim());
                this.startY = Integer.valueOf(values[1].trim());
                direction = values[2].split("\\.")[1];
            } else if (line.contains("val objectives")) {
                String[] split = line.split("\\(\\(");
                String contractListString = split[1].replaceAll("\\(|\\)|,|Set", " ");
                String[] contractListTmp = contractListString.split(" ");
                ArrayList<String> contractList = new ArrayList<>();
                for (String aContract :
                        contractListTmp) {
                    if (!aContract.trim().isEmpty()) {
                        contractList.add(aContract);
                    }
                }
                System.out.println("Setting contract list " + contractListString);
                for (int i = 0; i < contractList.size(); i += 2) {
                    this.contrats.put(contractList.get(i).trim(), Integer.valueOf(contractList.get(i + 1).trim()));
                }
            }
        }

    }

    public void runWith() {
        System.out.println(this.toString());
        PrintStream old = System.out;


        try {
            log = new File(outputPath+"logRunner.txt");
            System.setOut(new PrintStream(log));
            double cheat=1.0f;
            runner = new Runner(aClass.getClass());
            runner.exploring(mapFile)
                    .withSeed(seed)
                    .startingAt(startX, startY, direction)
                    .backBefore((int)Math.floor(actionPoints*cheat))
                    .withCrew(mens)
                    .storingInto(outputPath)
                    .withTimeout(timeOut);

            for (String s : this.contrats.keySet()) {
                runner.collecting(contrats.get(s), s);
            }
            System.setOut(old);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fire(boolean replaceOut)
    {
        PrintStream old = System.out;

        log = new File(outputPath+"logRunner.txt");
        try {
            if(replaceOut)
                System.setOut(new PrintStream(log));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        runner.fire();
        System.setOut(old);
    }

    @Override
    public String toString() {
        return "ChampionshipRunner{" +
                "mapFile=" + mapFile +
                ", seed=" + seed +
                ", startX=" + startX +
                ", startY=" + startY +
                ", direction='" + direction + '\'' +
                ", actionPoints=" + actionPoints +
                ", mens=" + mens +
                ", contrats=" + contrats +
                ", outputPath='" + outputPath + '\'' +
                ", weekId=" + weekId +
                '}';
    }

    public int getNumberOfContractsDone()
    {
        if(numberOfContractsDone==-1)
        {
            numberOfContractsDone++;
            parseNumberOfContractsDone();
        }
        return numberOfContractsDone;
    }

    private void parseNumberOfContractsDone()
    {
        try {
            List<String> lines = Files.readAllLines(Paths.get(log.getAbsolutePath()));
            collectedRessources= new HashMap<>();
            for (String line:lines)
            {
                if(line.startsWith("  - "))
                {
                    String[] splited = line.split(": ");
                    String res = splited[0].substring(4);
                    int value = Integer.parseInt(splited[1]);
                    collectedRessources.put(res,value);
                }
            }
            for(String s:collectedRessources.keySet())
            {

                System.out.print("Collected - "+s);

                if(contrats.containsKey(s))
                {
                    System.out.println("\t"+collectedRessources.get(s)*100/contrats.get(s)+"%");
                    if(collectedRessources.get(s) >= contrats.get(s))
                    {
                        numberOfContractsDone++;
                    }
                }
                else
                    System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void printLinks() {
        System.out.println("Runner log" + toLink(log.getAbsolutePath()));
        System.out.println("json log" + toLink(new File(outputPath).getAbsolutePath()+"/log.json"));
        System.out.println("map file " + toLink(new File(outputPath).getAbsolutePath()+"/map.svg"));


    }
    private String toLink(String s)
    {
        System.out.println("Path"+s);
        Path path = Paths.get(s);
        Path cur = Paths.get("").toAbsolutePath();
        System.out.println(cur);

        String relativePath = cur.relativize(path).toString();

        StringBuilder sb = new StringBuilder();
        StringBuilder insideLink = new StringBuilder();
        if(relativePath.endsWith("svg"))
        {

            insideLink.append("<img src=\"")
                    .append(relativePath).append("\" alt=\"map ")
                    .append(relativePath)
                    .append("\" height=\"100\" width=\"100\">\"");

        }
        else
            insideLink.append(relativePath);

        sb.append("<a href=\"")
                .append(relativePath)
                .append("\">")
                .append(insideLink.toString())
                .append("</a>");
        return sb.toString();
    }

    public static void remplaceHTMLChar() throws IOException {
        String path = "Test Results - ITRunPreviousChampionships.html";
        List<String> lines = Files.readAllLines(Paths.get(path));
        List<String> unexcaped = new ArrayList<>();
        for(String s:lines)
        {
            unexcaped.add(StringEscapeUtils.unescapeHtml4(s));
        }
        //lines.forEach(name -> name=StringEscapeUtils.unescapeHtml4(name));

        Files.write(Paths.get("Resultats des championnats.html"), unexcaped);
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setClass(IExplorerRaid aClass) {
        this.aClass = aClass;
    }
}
