### Introduction
---

GridWars is a turn based game AI coding competition that takes place on a two-dimensional grid on which two bots fight to gain the upper hand.

The following guide helps you to get started developing your own bot and have it compete against other players. To learn more about the game mechanics and rules, you can download the [slides of the introduction session](/files/gridwars-intro-2018.pdf).

### Prerequisites
---

#### Java

The bots are developed in Java. You need to download and install the latest JDK 8 ( *Java SE Development Kit* ) on your machine.

You can download the installer for your operating system [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Choose the latest (64 bit) version, without the demos and samples.

All operating systems that run Java are supported for the bot development. You are free to use Windows, Linux, or Mac.

#### IDE

For the development it is strongly recommended to use a good IDE ( *Integrated Development Environment* ). We suggest to use the IntelliJ IDEA Community Edition that can be downloaded [here](https://www.jetbrains.com/idea/download/). It provides all the features that we need.

You are also free to use your favourite IDE or editor (Eclipse, Xcode, VS Code, Vim, Notepad, ...), but it's usually more convenient to use an IDE that has good support for Java and the [Gradle](https://gradle.org/) build tool. 

### GridWars Starter Project
---

To get started you download the [GridWars starter project](/files/gridwars-starter.zip).

#### Setting up the IDE

TODO

#### Run from the IDE

TODO

#### Run from the console

TODO

### Create your own bot
---

TODO

#### Build the bot jar from the IDE

TODO

#### Build the bot jar from the console

TODO

#### Upload your bot

TODO

Play fair... and may the code be with you!

<pre>
                                    _.=+._
 \::::::::::::::::::::::::::.\`--._/[_/~|;\_.--'/.:::::::::\
  \::::::::::::::::::::::::::.`.  ` __`\.-.(  .'.:::::::::::\
  /::::::::::::::::::::::::::::.`-:.`'..`-'/\'.:::::::::::::/
 /::::::::::::::::::::::::::::::::.\ `--')/  ) ::::::::::::/
                                     `--'
</pre>

### FAQ
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

<dt>Q: Can my bot be statelful?</dt>
<dd>A: Yes, your bot will instantiated once and used for the time of the match. You can store state in class variables between the turns.</dd>

<dt>Q: Can I output logs for my bot?</dt>
<dd>A: Yes, you can simply write to stdout and/or stderr (<code>System.out.println()</code>, <code>System.err.println()</code>). The output will be redirected to a file that you can download from the match view page. You can only download the output file of your own bot. Please note that the maximal total text that you are allowed to output is 5 MB. If you try to print more, any further output will simply be discarded.</dd>

<dt>Q: Can I use 3rd party Java libs?</dt>
<dd>A: Sorry, no. It's just you, your brain, your team and the standard Java API.</dd>

<dt>Q: Can I use additional threads?</dt>
<dd>A: No.</dd>

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
