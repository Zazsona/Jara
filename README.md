# Jara - A Discord Bot for you
Jara is a jack-of-all-trades Discord bot built for customisability in mind for developers, hosts, and users, so you can refine it to serve all your needs, or just use it out of the box. 

## Commands
- Games
- Toys
- Utility
- Audio
- Admin  
I'll expand this list later.

## Discord Users FAQ
Want to add Jara to your server? This is the place! 

**How do I add this to my server?**  
Sorry, but no live version is currently available. However, if you wish to host it for yourself, see the hosts FAQ below.  

**Can I disable commands, or limit them to certain users?**  
Yup. Commands can be disabled completely or limited to certain roles on the server.  

**How can I find out about the commands?**  
/Help will show you a list of commands you have access to. Use /Help [Command Name] to find out how to use it.  

**Can I make a suggestion / Submit a bug report?**  
Sure thing! Feedback is always appreciated. For now, please start a thread in issues [here](https://github.com/Zazsona/Jara/issues/new).

## Hosts FAQ
Whether you're thinking of hosting Jara for others or just yourself, a thanks from me.

**How can I get started?**  
Simply download a [release](https://github.com/Zazsona/Jara/releases) build, then run it and walk through the steps shown! This will also take you through setting up an account for the bot on Discord.

**How do I update?**
Download the latest [release](https://github.com/Zazsona/Jara/releases), then simply replace your old .jar. When you next run the bot it will prompt you to configure any new commands.

**What are the system requirements?**  
- OS: Windows/Linux (OS X might work, but is untested)
- Java Version: Java 8 or higher is recommended.
- CPU: 1GHz+ Single Core Processor (This will be higher based on the number of servers you're in)
- GPU: Yes. (If you can see this page, you're set)
- RAM: 512MB or higher.

**Can I limit the commands to reduce strain on my system?**  
Yup! You can disable specific commands or entire categories such as 'Games' or 'Audio'.

## Developers
Any help is always appreciated! For contributing to this base or making your own spin, here's a few pointers.

**How should I add new commands?**
- Select a category for your command and create the class file(s) in it's respective package. 
- Have the class extend from Command and implement the required methods.
- Index your command in the getRegister() method of the CommandRegister class
- Write your command (It will start in the run method)
- (Optional) Add help information in the Help class.

Note: The command will not be found unless you add it to the CommandRegister.