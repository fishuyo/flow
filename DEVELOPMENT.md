# Development Workflow

This document describes the streamlined development workflow for Flow.

## Overview

Flow uses a hybrid setup:
- **Frontend**: Scala.js + Vite for rapid development with hot reload
- **Backend**: Scala + Pekko HTTP server

## Key Improvements

### 1. Dynamic Scala.js Path Resolution

No more hardcoded Scala version paths! The build system now automatically finds the correct Scala.js output directory regardless of:
- Scala version (e.g., `scala-3.3.5`, `scala-3.4.0`)
- Build mode (`client-fastopt` for dev, `client-opt` for production)

**Files changed:**
- `frontend/client/vite-plugin-scalajs-resolver.js` - New plugin that dynamically resolves paths
- `frontend/client/main.js` - Now uses `scalajs:main.js` import
- `frontend/client/uno.config.js` - Uses glob pattern `target/scala-*/client-*/**/*.js`

### 2. Unified Build Workflow

SBT tasks now integrate with Vite build:

**New SBT tasks:**
- `viteBuild` - Builds the frontend client with Vite (includes Scala.js compilation)
- `serverDev` - Alias: builds client + starts server
- `serverBuild` - Alias: builds client only
- `clientBuild` - Alias: compiles Scala.js only

**Usage:**
```bash
# Build client and start server (one command!)
sbt serverDev

# Or step by step:
sbt "project server" viteBuild reStart
```

## Development Workflows

### Frontend Development (Client-side only)

```bash
cd frontend/client
npm run dev
```

This starts Vite dev server on port 3000 with:
- Hot module replacement (HMR)
- Automatic Scala.js recompilation via `@scala-js/vite-plugin-scalajs`
- Proxy to backend API at `http://localhost:8080`

### Backend Development (Server-side)

```bash
# Option 1: Build client then start server
sbt serverDev

# Option 2: Manual steps
sbt "project server" viteBuild reStart
```

The `viteBuild` task:
1. Compiles Scala.js (`client / Compile / fastOptJS`)
2. Runs `npm run build` in `frontend/client`
3. Outputs to `frontend/client/dist` (served by Pekko HTTP)

### Full Stack Development

**Terminal 1** - Frontend dev server:
```bash
cd frontend/client
npm run dev
```

**Terminal 2** - Backend server:
```bash
sbt "project server" reStart
```

Access:
- Frontend dev: http://localhost:3000 (Vite HMR)
- Backend API: http://localhost:8080

## File Structure

```
flow/
├── frontend/
│   └── client/
│       ├── vite.config.js          # Vite config with Scala.js plugin
│       ├── vite-plugin-scalajs-resolver.js  # Dynamic path resolver
│       ├── main.js                  # Entry point (uses scalajs:main.js)
│       ├── uno.config.js            # UnoCSS config (dynamic paths)
│       └── dist/                    # Production build output
├── backend/
│   └── server/                      # Pekko HTTP server
└── build.sbt                        # SBT build config with viteBuild task
```

## How It Works

### Dynamic Path Resolution

The `vite-plugin-scalajs-resolver.js` plugin:
1. Scans `target/scala-*/` directories
2. Finds `client-fastopt` (dev) or `client-opt` (production)
3. Resolves `scalajs:main.js` import to the actual path
4. Works regardless of Scala version

### Vite Build Integration

The `viteBuild` SBT task:
1. Ensures Scala.js is compiled first (`dependsOn fastOptJS`)
2. Runs `npm run build` in the client directory
3. Outputs optimized production build to `dist/`

### Server Integration

The Pekko HTTP server serves files from `frontend/client/dist`:
- Static assets from `dist/assets/`
- `index.html` for SPA routing
- API endpoints at `/api/*`

## Troubleshooting

### "Could not find Scala.js output directory"

This is normal in dev mode before first compilation. The resolver will find it once Scala.js compiles.

### Vite build fails

Make sure:
1. Scala.js has been compiled: `sbt "project client" fastOptJS`
2. Node modules are installed: `cd frontend/client && npm install`
3. Check `frontend/client/dist` exists after build

### Server can't find dist files

Ensure:
1. `viteBuild` task completed successfully
2. `frontend/client/dist` directory exists
3. Check `backend/server/src/main/resources/application.conf` for correct `project.root` path

## Future Improvements

Potential enhancements:
- SBT plugin for Vite (better integration)
- Watch mode for `viteBuild` task
- Automatic rebuild on Scala.js changes
- Unified dev command that runs both frontend and backend
