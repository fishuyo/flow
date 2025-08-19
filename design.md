
# S.M.A.R.T Goals  (specific measurable achievable relevant time-bound)


- projectorRemote service steps
  - 1 finish setting up build system for a complete pipeline:
    - backend
      - coreIO
      - projectorIO
      - server
    - frontend
      - coreUI
      - projectorUI
      - client (1)
        - laminar entry + routing
        - vite app
  - 2 build ui 
    - unocss working, hot reload working, build working,
    - coreUI components
      - collapsibleList
      - consoleIO
      - buttonArray
      - dropdownMenu
    - ProjectorList data structure tbd -- rough debug version to build out ui
      - Projector(name, model, connectedToHost, projectorIO, consoleIO)
      - projectorIO (ProjectorCMD <-> ProjectorResponse)
      - projectorIO >> consoleIO
      - buttonArray --> ProjectorCMDs
    -



# Structure


- flow
  - backend
    - apps will be entry points with main function
        - server --> http server serving X frontend interfaces and communicating with backend services
          - perhaps runs services in same jvm as server or local remoting?

        - daemons --> run specific backend service modules

    - services
      - hidIO
      - ijsIO
      - phasespaceIO
      - openvrIO
      - oscIO
      - appManager
      - mappingManager
      - compiler
      - projectorIO
    - util
    - core?
  - frontend
    - app
    - components
      - sidebar
      - codeEditor
    - projectorRemote
    - deviceServer
    - launcher
    - multishell
  - protocol (shared)
    - 