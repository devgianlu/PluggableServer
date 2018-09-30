# PluggableServer
Did you ever want to create an awesome API but hadn't nowhere to host it because your free hosting service was already in use? Here it comes my solution: **PluggableServer**!

## How it works
This is designed to run on Heroku and Firebase, and it is not very adaptable. Since Heroku doesn't persist the data trough restarts, Firebase is used to save the data so that it can be downloaded each time.

This repository also includes an useful client to interact with the server.