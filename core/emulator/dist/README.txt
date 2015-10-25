         .    .        .      .             . .     .        .          .          .
  .    .          .                 .                    .                .    . 
           .               A long time ago in a galaxy far, far away...   .
    *         .               .           .               .        .             .
              .      .            .                 .                         +      .
      .   .      .         .         .   . :::::+::::...      .          .         .
   .          .         .      .    ..::.:::+++++:::+++++:+::.    .     .
                                 .:.  ..:+:..+|||+..::|+|+||++|:.             .     .
                     .   .    :::....:::::::::++||||O||O#OO|OOO|+|:.    .
     .   .      .      .    .:..:..::+||OO#|#|OOO+|O||####OO###O+:+|+               .
                          .:...:+||O####O##||+|OO|||O#####O#O||OO|++||:     .    .
.          .             ..::||+++|+++++|+::|+++++O#O|OO|||+++..:OOOOO|+  .         .
   .   .      .   .     +++||++:.:++:..+#|. ::::++|+++||++O##O+:.++|||#O+    .
         .           . ++++++++...:+:+:.:+: ::..+|OO++O|########|++++||##+            .
.          .       .  :::+++|O+||+::++++:::+:::+++::+|+O###########OO|:+OO       .  .
              .       +:+++|OO+|||O:+:::::.. .||O#OOO||O||#@###@######:+|O|  .
          .          ::+:++|+|O+|||++|++|:::+O#######O######O@############O
     +             . ++++: .+OO###O++++++|OO++|O#@@@####@##################+         .
               .     ::::::::::::::::::::++|O+..+#|O@@@@#@###O|O#O##@#OO####     .
          .        . :. .:.:. .:.:.: +.::::::::  . +#:#@:#@@@#O||O#O@:###:#| .      .
                                    `. .:.:.:.:. . :.:.:%::%%%:::::%::::%:::
         .      .                                      `.:.:.:.:   :.:.:.:.  .   .
                    .                                                                .
  .            .
         .          .                                                       .   .
 .                                                                                    .
             .        .                                                           .
             .     .                                                           .    *  .
    .      .     .                                                        .
                       .   A terrible civil war burns throughout the  .        .     .
                          galaxy: a rag-tag group of freedom fighters   .  .
              .       .  has risen from beneath the dark shadow of the            .
         .        .     evil monster the Galactic Empire has become.                  .
  .        .             Imperial  forces  have  instituted  a reign of   .      .
        *             terror,  and every  weapon in its arsenal has  been
   .               . turned upon the Rebels  and  their  allies:  tyranny, .   .
            .       oppression, vast fleets, overwhelming armies, and fear.        .  .
      .  .      .  Fear  keeps  the  individual systems in line,  and is the   .
                  prime motivator of the New Order.             .
   .        .      Outnumbered and outgunned,  the Rebellion burns across the   .    .
         .      vast reaches of space and a thousand-thousand worlds, with only     .
             . their great courage - and the mystical power known as the Force -
   .         flaming a fire of hope.                                    .
                This is a  galaxy  of wondrous aliens,  bizarre monsters,  strange   .
          . Droids, powerful weapons, great heroes, and terrible villains.  It is a
    +      galaxy of fantastic worlds,  magical devices, vast fleets, awesome machi-  .
          nery, terrible conflict, and unending hope.              .         .          .   . 
.        .          .    .    .            .            .                   .       .
               .               ..       .       .   .             .           +          .   
                     .              .       .              +     .      .    .          .  
.        .               .       .     .            .        .                 .          .   
   .           .        .                     .        .            .          .     .      .    
             .               .    .          .              .   .         .
    _______ .______       __   _______     ____    __    ____  ___      .______      +   _______.
   /  _____||   _  \   . |  | |       \    \   \ ./  \  /   / /   \     |   _  \        /       |
  |  |  __  |  |_)  |    |  | |  .--.  |  . \   \/    \/   / /  ^  \  . |  |_)  |      |   (----`
  |  | |_ | |      / .   |  | |  |  |  |     \            / /  /_\  \   |      /   .    \   \    
  |  |__| | |  |\  \----.|  | |  '--'  |      \    /\    / /  _____  \  |  |\  \----.----)   | . 
   \______| | _| `._____||__| |_______/   .    \__/  \__/ /__/     \__\ | _| `._____|_______/    .
     .             .             .         .               .                 . 
.        .               .       .     .            .    .       *        .        .        .     

                                         Episode N

                            B A T T L E   F O R   D U B N A

## Distribution Content:

/build.gradle (build file. See Build section for details.)
/src/** (Example source code)
/bin/emulator (run script for Mac/Linux)
/bin/emulator.bat (run script for Windows)
/gradlew (build script runner script for Mac/Linux)
/gradlew.bat (build script runner script for Windows)
/gradle/** (technical files. Do not touch)
/lib/** (necessary dependencies. Do not touch)

## Build

There is two ways to work with sources given. With or without IDE.
Although IDE work is recommended, as it facilitates all operations, fully command-line build is also possible 
(for people w/o IDE, and low bandwidth, and console/notepad maniacs).

This section will first explain importing project in IDE, and then console build.

### Eclipse / IntelliJ IDEA

1. Open project in your favorite IDE (IntelliJ idea recommended, Eclipse - tested)
1.1. IntelliJ IDEA. File -> Open -> (Select build.gradle file) -> Click Ok for default settings -> enjoy.
1.1. Go to File -> Import -> Gradle -> Gradle Project -> Browse -> (navigate to the root folder of your project, then click Build Model).
2. All necessary project dependencies are bundled, but Gradle will spend some time for downloading distribution
   and configuring project for first run. It should happen only once.

### Console build
1. Given project can be run from command line.
You have to have JDK 1.7+ and JAVA_HOME environmental variable set. Or you can have java in your PATH.
Consult web if you have problems at this step, there are plenty guides and tutorials how to setup java environment on your operating system.
2. First run of gradlew scripts **will** take some time! Something around 60MB will be downloaded.
3. After download you will have following commands.
  1. To build jar file: ./gradlew jar
  2. To clean build directory: ./gradlew clean
  3. To run emulator tool: ./gradlew run
  4. To list all tasks available: ./gradlew tasks
4. All build results goes under build directory.


Play fair and
May the Code be with you!

                                    _.=+._
 \::::::::::::::::::::::::::.\`--._/[_/~|;\_.--'/.:::::::::\
  \::::::::::::::::::::::::::.`.  ` __`\.-.(  .'.:::::::::::\
  /::::::::::::::::::::::::::::.`-:.`'..`-'/\'.:::::::::::::/
 /::::::::::::::::::::::::::::::::.\ `--')/  ) ::::::::::::/
                                     `--'
