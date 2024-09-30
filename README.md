![Afterburner Logo](https://i.imgur.com/O6InVVP.png)

![java version](https://badge.ttsalpha.com/api?icon=oracle&label=java-version&status=11-21&color=green&iconColor=green)
[![license](https://badge.ttsalpha.com/api?label=license&status=GPL-3.0&color=pink)](https://www.gnu.org/licenses/why-not-lgpl.html)
![discord](https://badge.ttsalpha.com/api?icon=discord&label=discord&status=vengelis_&color=purple&iconColor=purple)

# Afterburner for Minecraft server deployment

Afterburner is a program that allows you to run a Minecraft server via a template system. This service is useful for an infrastructure that requires deploying servers on machines, via the copying of complete folders and files.
A program originally designed for deploying servers from the EternyUHC infrastructure, it has been tested in the field and is easily modular according to needs.

[![EternyUHC](https://badge.ttsalpha.com/api?label=EternyUHC&status=website&color=mint)](https://www.eternyuhc.fr/)
[![EternyUHC](https://badge.ttsalpha.com/api?label=EternyUHC&status=discord&color=teal)](https://discord.gg/eternyuhc)

# What is Afterburner used for ?

This program is a sub-layer to the program to be started (example: a minecraft server). That is to say:
- It intervenes before its execution. In this case, we do not start the final program directly but we start Afterburner. It will take care of copying the necessary data before the execution of the final program.
- It starts the final program and records all the logs returned by the console
- After stopping the final program, it can take care of executing new instructions before the final shutdown of Afterburner
- Everything can be re-executed as many times as desired thanks to the looping system

Here is a diagram to visually explain the life of the program:
```
> java -jar AfterburnerII.jar Dtemplate=myserver.yml
  | [do Starting]
  | - Copy the files needed for the 'myserver' template as specified in the template file
  | - Executing 'myserver'
  | - Post process after `myserver` shutted down
  | [goto Starting if is needed]
  | - End of job
```

# Afterburner, compatible with other programs?

Currently, Afterburner is not designed to work with anything other than a Java program. I plan to bring the ability to run something other than Java programs. A lot of things related to Minecraft (the use of the game name for example) are directly written live in the code. 
An update to modify all of these things must be done in the future to support something other than specifically a Minecraft server and more broadly something other than a Java program. In the current state, you can launch a Java program with Afterburner without necessarily it being a Minecraft server.

# Known Bugs/Things that aren't done being ported yet

- Correct handling of configuration version changes.
- Automatic adaptation of a configuration with incorrect YML syntax without potentially crashing afterburner.
- Support for inputs to the executable.
- Support for maven dependencies embedded in plugins or providers (only dependencies of the Afterburner program are supported, any new ones must be compiled in Afterburner and not in the Plugin or Provider).

# Configuration

All default configurations have explanations in them. You just have to open them and follow the instructions below each of the configuration nodes.

# API

API documentation should be available shortly.

# Credits

- [Vengelis_](https://github.com/Vengelis)
- [SiriHack](https://github.com/CyriacF) (small fix)


