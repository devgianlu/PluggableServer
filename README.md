# PluggableServer
Did you ever want to create an awesome API but hadn't nowhere to host it because your free hosting service was already in use? Here it comes my solution: **PluggableServer**!

## How it works
You can run multiple Undertow-based projects on one server, by simply overriding [BaseComponent](https://github.com/devgianlu/PluggableServer/blob/master/api/src/com/gianlu/pluggableserver/api/BaseComponent.java) and annotating it with [PluggableComponent](https://github.com/devgianlu/PluggableServer/blob/master/api/src/com/gianlu/pluggableserver/api/PluggableComponent.java). Add your project to the classpath and you're done!