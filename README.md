**Paper 1.13.2 and above now contains built-in [Velocity support](https://velocity.readthedocs.io/en/latest/users/player-info-forwarding.html#paper)!**
As a result, this plugin is now completely redundant and unsupported.

---

# VelocityConnect

A plugin for Paper 1.13+ servers, enabling modern forwarding with [Velocity](https://github.com/astei/velocity).

**Note: Currently requires a Paper server that includes [this patch in Spigot-Server-Patches](https://gist.github.com/md678685/580dd7685d28c2e850fdd41c1d1376db)**,
which comments out the contents of the `PacketLoginInCustomPayload` handler in `net.minecraft.server.LoginListener`. The patch is likely outdated, so you'll want to do this yourself. Without this patch, players will be kicked from the server during login.

Tested against a version of Paper 1.13.1 with the patch linked above and ProtocolLib [v4.4.0-SNAPSHOT-b415](http://ci.dmulloy2.net/job/ProtocolLib/415/), running in offline mode.

## Disclaimer

Use at your own risk. This isn't guaranteed to be efficient, stable or even functional.
