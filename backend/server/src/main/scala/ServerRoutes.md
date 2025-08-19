

# server pekko-http route planning



/ -> index.html (SPA)
/assets/ -> getfromDir(dist/assets/)
<!-- /ws -->
/api (ws connections)
/api/projectors (ws to ProjectorManager actor?)


# todo

- server
  - implement ws route and handler
  - different ws types for different services? yeah...
  - write protocol and figure out what needs to be shared code in js land
- client
  - implement clientside projectorcommand controller thingy that knows the api ws language and streams to ui via laminar signals bitch learn it -- learn it -- don't be an ai.. to know is to know to teach --- confidence in what i do know -- enough to aknowledge unknown but confidently share...
  - finish ui and connect buttons

- test
  - make fake serverside testprojectors and hook em up


- same process construct flow..