### Content

* <a href="#intro">Introduction</a>
* <a href="#pre">Prerequisites</a>
* <a href="#starter">GridWars Starter Project</a>
* <a href="#bot">Create and upload your own bot</a>
* <a href="#faq">FAQ</a>

### Introduction <a name="intro" class="anchor"></a>
---

GridWars is a turn based game AI coding competition that takes place on a two-dimensional grid, on which two bots fight to gain the upper hand. You are going to programm your own bot AI and upload it to a match server, where it will compete against the bots of other competitors.

The following guide helps you to get started developing your own bot. To learn more about the game mechanics and rules, you can download the [slides of the introduction session](/files/gridwars-intro-2018.pdf).

### Prerequisites <a name="pre" class="anchor"></a>
---

#### Java

The bots are developed in Java. You need to download and install the latest JDK 8 ( *Java SE Development Kit* ) on your machine. You can download the installer for your operating system [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Choose the latest (64 bit) version for your OS, without the demos and samples. All operating systems that run Java are supported for the bot development. You are free to use Windows, Linux, or Mac.

The JDK installation should be straightforward, but you can also find installation help [here](https://www.java.com/en/download/help/download_options.xml) and you can always ask Dr. Google.

#### IDE

We strongly recommended to use a good IDE ( *Integrated Development Environment* ) for the development. We suggest to use the IntelliJ IDEA Community Edition that can be downloaded for free [here](https://www.jetbrains.com/idea/download/). To install it, just follow the instructions of the installation wizard.

You are also free to use your favourite IDE or editor (Eclipse, Xcode, VS Code, Vim, Notepad, ...), but it's usually more convenient to use an IDE that has good support for Java and the [Gradle](https://gradle.org/) build tool. 

When running IntelliJ IDEA for the first time after the installation, you are prompted with a customization dialog. Just select the UI theme that you prefer and then click on *"Skip Remaining and Set Defaults"* in the lower left use the defaults and start using the IDE. From here you should continue with the <a href="#starter">GridWars Starter Project section</a> below.

#### Using the console

Some of you may also prefer to use the console on Linux or Mac system to build and run the sources. You can easily do this as well, but you should make sure that you have the `JAVA_HOME` environment variable set to the correct folder and that the `java` executable is visible in your path environment.

On the console you will use the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper) directly to execute the build tasks.

### GridWars Starter Project <a name="starter" class="anchor"></a>
---

To get started you simply download the [GridWars starter project file](/files/gridwars-starter.zip). It contains two very simple example bots, an emulator that you can use to simulate the matches on your local computer, and all the tooling to produce the uploadable bot file.

After you downloaded the project file, unpack it into a folder on your disk that will be used as the project folder. You can also find some information about the content of the starter project in the `README.md` file in the root of the folder.

#### Setting up the IDE

1. When you start the IntelliJ IDE (which we installed above) you select *"Import Project"* and select the GridWars starter project folder that you have just unpacked.
1. On the next screen select *"Import project from external model"* and then *"Gradle"* below and click on "Next".
1. On the next screen tick the checkbox for *"Use auto-import"* and leave the rest on the default settings.
    * Make sure that the option *"Use default gradle wrapper (recommended)"* is selected.
    * Make sure that the *"Gradle JVM"* option is set to *"Use Project JDK"* and is pointing to the root folder of your JDK 8 installation. If not set, you might have to select the folder by yourself.
1. Click on *"Finish"* to conclude the project import. The IDE will open and run a first build of the project.
    * It is necessary to download some additional dependencies from the web, so it may take a few moments and you might be prompted by your firewall to allow network access to the Java executable, which you should grant.

#### Run the emulator from the IDE

To simulate matches between two bots on your local machine, you can use the GridWars emulator that is shipped with the starter project. You can use it to have different versions of your bot fight each other during development to see if the improvements you are making increases the strength of your bot AI.

1. In order to quickly execute the Gradle tasks, you should activate the Gradle tool window by selecting: *"View"* -> *"Tool Windows"* -> *"Gradle"*
    * This will open the Gradle task view on the right
1. Execute the *"run"* task using the Gradle tool view by selecting: *"Tasks"* -> *"application"* -> double click on *"run"*
1. After a short time you should see the emulator window appearing on your screen that shows two simple bots fight each other. You can pause the playback and increase/decrease the turn speed.

If the bots are writing output to `System.out` or `System.err`, it will be printed to the files `bot1.log` and `bot2.log` of the starter project folder.

#### Run the emulator from the console

As mentioned earlier, you can also execute the Gradle commands from the console. In this case you will use the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper) that is shipped with the starter project. You run the commands from the root of the starter project folder.

On Linux and Mac you need to make sure that the `gradlew` file has the execute flag (+x) set.

To run the GridWars emulator on Linux or Mac, simply execute the *"run"* task: `./gradlew run`

On Windows, you need to use the `gradlew.bat` executable instead: `gradlew.bat run`

### Create and upload your own bot  <a name="bot" class="anchor"></a>
---

After we have set up the starter project and saw two simple bots fighting each other, it's time to create your own bot:

1. Create a new Java class in the starter project. You can create it in the `gridwars.starter` package, or you may also choose a different package.
    * You can use one of the two example bots as a starting point. Just copy one of the classes and give it a new name.
    * The name of the class does not really matter, but the last part of the name will be shown in the web UI to identify your bot, so you may want to give it a fearsome name ;).
1. The bot class must implement the `cern.ais.gridwars.bot.PlayerBot` interface, which is the only requirement for the bot class.
1. To have your bot fight in the emulator, you modify the code of the `gridwars.starter.EmulatorRunner` class to use your new bot class as one of the bots.
1. That's it. Now you come up with some nice algorithms and keep improving your bot until it becomes the strongest on the fighting grid ;).

You should also check out the JavaDocs of the API classes in the `cern.ais.gridwars.api` packages to get familiar with the programming interface. The JavaDocs are available in the `api` folder of the starter package (simply open the `index.html` in a browser) and can also be [read online](/static/api/index.html).

#### Build the bot jar file from the IDE

When you are ready to upload your first bot to the match server, you create the uploadable jar file using the following steps:

1. Fill in the fully qualified class name (package + class name) of your bot class in the `build.gradle` file in the root of the starter project in the section for the manifest attributes for the `Bot-Class-Name` header:
    * For example: `'Bot-Class-Name': 'gridwars.starter.MyAwesomeBot'`
    * This information is used by the match server to know which class in the jar file is your bot, as the jar file may contain multiple classes.
1. Execute the *"jar"* Gradle task using the Gradle tool view: *"Tasks"* -> *"build"* -> double click on *"jar"*
    * The resulting uploadable bot jar file will be located at `build/libs/gridwars-bot.jar` in the starter project folder.

#### Build the bot jar file from the console

To build the bot jar file from the console, you first need to follow step 1. above about configuring the fully qualified class name in the `build.gradle` file. After that, you run the *"jar"* task from the console:

Linux/Mac: `./gradlew jar`

Windows: `gradlew.bat jar`

The resulting uploadable bot jar file will be located at `build/libs/gridwars-bot.jar` in the starter project folder.

#### Upload your bot

Now that you have your bot jar file, go to the [team page](/team) and use the upload function to upload the bot jar file that we created above. The match server will run a few validations on the jar file and your bot class. If everything looks fine, it will generate the matches against all other existing bots. You can refresh the team page to see the match results as they become available.

You can upload a new bot file as often as you want. It will replace your currently active bot and schedule new matches. At the end of the competition you will be informed about the time when the bot upload will be closed. Make sure to upload your final version of the bot before this time. The last bot you upload will be used in the final playoffs.

That's it! Have fun developing your bot and good luck!

Play fair... and may the code be with you ;)

<pre>
                                    _.=+._
 \::::::::::::::::::::::::::.\`--._/[_/~|;\_.--'/.:::::::::\
  \::::::::::::::::::::::::::.`.  ` __`\.-.(  .'.:::::::::::\
  /::::::::::::::::::::::::::::.`-:.`'..`-'/\'.:::::::::::::/
 /::::::::::::::::::::::::::::::::.\ `--')/  ) ::::::::::::/
                                     `--'
</pre>

### FAQ <a name="faq" class="anchor"></a>
---

<dl>
<dt>Q: Can I use other programming languages?</dt>
<dd>A: No, you have to use Java 8.</dd>

<dt>Q: Is there a size limit for the bot jar upload?</dt>
<dd>A: Yes, it's 5 MB. This is way more than enough when not using 3rd party libs. The size of your jar file is typically just a few kilobytes.</dd>

<dt>Q: Is there a memory limitation for the execution of a match?</dt>
<dd>A: Yes, the match JVM processes are running with 256 MB of heap space, shared by both bots and the execution engine.</dd>

<dt>Q: Is there a maximun number of turns?</dt>
<dd>A: Yes, it's 2000. If the turns reach this limit, the bot with the most units in total wins. If both bots have the same amount, the match is a draw.</dd>

<dt>Q: Is there a timeout per turn?</dt>
<dd>A: Yes, it's 50 milliseconds, which should be plenty of time. All move commands that you add to the list within this time will be evaluated. After the timeout, no move commands will be accepted anymore, so it's a good practive to add your moves to the list asap.</dd>

<dt>Q: Is there a timeout for the bot initialisation?</dt>
<dd>A: Yes, it's 3 seconds. If the bot fails to initialise (when instantiated) within the allowed time, it will idle for the rest of the match and (very likely) lose.</dd>

<dt>Q: Is there a timout for the whole match?</dt>
<dd>A: Yes, it's 60 seconds. If the match takes longer, it will be considered as failed and will not go into the rank calculation.</dd>

<dt>Q: How are the scores calculated?</dt>
<dd>A: A win gives 3 points, a draw 1 point and a loss no points. The score is the sum of the points.</dd>

<dt>Q: Can the bot be stateful?</dt>
<dd>A: Yes, your bot will instantiated once and used during the whole match. You can store state in class variables between the turns.</dd>

<dt>Q: Can the bot log output?</dt>
<dd>A: Yes, you can simply write to stdout and/or stderr (<code>System.out.println()</code>, <code>System.err.println()</code>). The output will be redirected to a file that you can download from the match view page. You can only download the output file of your own bot. Please note that the maximal total text that you are allowed to output is 5 MB. If you try to print more, any further output will simply be discarded.</dd>

<dt>Q: How is the bot ranking calculated?</dt>
<dd>A: TODO explain...</dd>

<dt>Q: Can I use 3rd party Java libs?</dt>
<dd>A: Sorry, no. It's just you, your brain, your team and the standard Java API.</dd>

<dt>Q: Can I read or write files on the file system?</dt>
<dd>A: Negative.</dd>

<dt>Q: Can I use Java reflection?</dt>
<dd>A: Nē. If you are getting caught trying, you will lose your current turn.</dd>

<dt>Q: Can I use threads?</dt>
<dd>A: Niet.</dd>

<dt>Q: Can I open network sockets or access external network resources?</dt>
<dd>A: Nein.</dd>

<dt>Q: Can I read system properties or environment variables?</dt>
<dd>A: Non.</dd>

<dt>Q: Can I...</dt>
<dd>A: Probably not! ;)</dd>
</dl>
