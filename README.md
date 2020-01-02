# PluggableServer

[![Build Status](https://travis-ci.com/devgianlu/PluggableServer.svg?branch=master)](https://travis-ci.com/devgianlu/PluggableServer)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c3611b6ba1c446d0ab012046b12d033e)](https://www.codacy.com/manual/devgianlu/PluggableServer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=devgianlu/PluggableServer&amp;utm_campaign=Badge_Grade)
[![time tracker](https://wakatime.com/badge/github/devgianlu/PluggableServer.svg)](https://wakatime.com/badge/github/devgianlu/PluggableServer)
[![Donate Bitcoin](https://img.shields.io/badge/donate-bitcoin-orange.svg)](https://gianlu.xyz/donate/)

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

Did you ever want to create an awesome API but hadn't nowhere to host it because your free hosting service was already in use? Here it comes my solution: **PluggableServer**!

## How it works
This is designed to run on Heroku and Firebase, and it is not very adaptable. Since Heroku doesn't persist the data trough restarts, Firebase is used to save the data so that it can be downloaded each time.

This repository also includes an useful client to interact with the server.
