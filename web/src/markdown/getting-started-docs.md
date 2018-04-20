### Introduction
---

TODO

### Prerequisites
---

#### Java

TODO

#### IDE

TODO

### GridWars Starter Package
---

TODO

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

<dt>Q: Can I use 3rd party Java libs?</dt>
<dd>A: Sorry, no. It's just you, your brain, your team and the standard Java API.</dd>

<dt>Q: Is there a size limit for the bot jar upload?</dt>
<dd>A: Yes, it's 5 MB. This is way more than enough when not using 3rd party libs. The size of your jar file is typically just a few kilobytes.</dd>

<dt>Q: Is there a memory limitation for the execution of a match?</dt>
<dd>A: Yes, the match JVM processes are running with 256 MB of heap space, shared by both bots and the execution engine.</dd>

<dt>Q: Is there a timeout per turn?</dt>
<dd>A: Yes, it's 50 milliseconds, which is plenty of time. All move commands that you add to the list within this time will be considered. After the timeout, no move commands will be accepted anymore, so it's good to make sure you add your move commands to the list asap.</dd>

<dt>Q: Is there a timeout for the bot initialisation?</dt>
<dd>A: Yes, it's 5 seconds.</dd>

<dt>Q: Is there a timout for the whole match?</dt>
<dd>A: Yes, it's 60 seconds. If the match takes longer, it will be considered as failed and will not go into the rank calculation.</dd>

<dt>Q: Can I output logs for my bot?</dt>
<dd>A: Yes, you can simply write to stdout and/or stderr (<code>System.out.println()</code>, <code>System.err.println()</code>). The output will be redirected to a file that you can download from the match view page. You can only download the output file of your own bot. Please note that the maximal total text that you are allowed to output is 5 MB. If you try to print more, any further output will simply be discarded.</dd>

<dt>Q: Can I use additional threads?</dt>
<dd>A: No.</dd>

<dt>Q: Can I read or write files on the file system?</dt>
<dd>A: Negative.</dd>

<dt>Q: Can I use reflections?</dt>
<dd>A: No. If you are getting caught trying, you will lose your current turn.</dd>

<dt>Q: Can I use threads?</dt>
<dd>A: Niet.</dd>

<dt>Q: Can I open network sockets or access external network resources?</dt>
<dd>A: Nein.</dd>

<dt>Q: Can I read system properties?</dt>
<dd>A: Non.</dd>

<dt>Q: Can I...</dt>
<dd>A: Probably not! ;)</dd>
</dl>
