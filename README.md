# UniversalVotes
A Bukkit plugin that connects reward points for voting on serverlists across multiple Minecraft servers.

## Permissions
| Permission | Explanation |
|------------|-------------|
| `universalvotes.vote` | Allows a player to execute the `vote` command |
| `universalvotes.admin` | Allows a player to use admin commands |
| `universalvotes.placesigns` | Allows a player to place reward signs |
| `universalvotes.buy` | Allows a player to use reward signs, spending their vote points |

## Commands
Look them up by issuing the `vote help` command.

## Building
```bash
mvn initialize
mvn clean process-resources package
```
