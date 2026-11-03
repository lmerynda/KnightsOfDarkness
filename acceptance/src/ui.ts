import { spawnSync } from "node:child_process";
import { existsSync, readFileSync, statSync } from "node:fs";
import { createServer, request, type IncomingMessage, type ServerResponse } from "node:http";
import { dirname, extname, join, normalize, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const browserDirectory = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..", "browser");
const built = join(browserDirectory, "dist");

export type Ui = { url: string; stop: () => Promise<void> };

/** builds the browser's pages as a release would (npm install in browser/ first) */
export const buildUi = (): void => {
  const build = spawnSync("npm run build", { cwd: browserDirectory, stdio: "inherit", shell: true });
  if (build.status !== 0 || !existsSync(join(built, "index.html"))) {
    throw new Error(`Could not build the browser pages into ${built}`);
  }
};

const types: Record<string, string> = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript",
  ".css": "text/css",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".webp": "image/webp",
  ".woff2": "font/woff2",
  ".woff": "font/woff",
  ".json": "application/json",
  ".ico": "image/x-icon",
};

const passOn = (incoming: IncomingMessage, outgoing: ServerResponse, gameServer: URL): void => {
  // the pages and the game server share one origin, as behind production's single host, so no cross-origin check applies
  const { origin: _origin, ...headers } = incoming.headers;
  const forward = request(
    {
      host: gameServer.hostname,
      port: gameServer.port,
      method: incoming.method,
      path: (incoming.url ?? "/").replace(/^\/api/, "") || "/",
      headers: { ...headers, host: gameServer.host },
    },
    answer => {
      outgoing.writeHead(answer.statusCode ?? 502, answer.headers);
      answer.pipe(outgoing);
    },
  );
  // a page that navigates away drops its calls halfway, and the game server may answer to nobody
  forward.on("error", error => {
    if (outgoing.headersSent) outgoing.destroy();
    else outgoing.writeHead(502).end(String(error));
  });
  incoming.on("aborted", () => forward.destroy());
  incoming.pipe(forward);
};

/**
 * the built pages served the way production serves them: the files themselves, the app for any other path, and /api
 * handed to the given game server
 */
export const startUi = (gameServer: string): Promise<Ui> => {
  const target = new URL(gameServer);
  const server = createServer((incoming, outgoing) => {
    const path = decodeURIComponent(new URL(incoming.url ?? "/", "http://ui").pathname);
    if (path === "/api" || path.startsWith("/api/")) {
      passOn(incoming, outgoing, target);
      return;
    }
    const file = join(built, normalize(path));
    const found = file.startsWith(built) && existsSync(file) && statSync(file).isFile() ? file : join(built, "index.html");
    outgoing.writeHead(200, { "Content-Type": types[extname(found)] ?? "application/octet-stream" });
    outgoing.end(readFileSync(found));
  });
  return new Promise((ready, fail) => {
    server.once("error", fail);
    server.listen(0, "localhost", () => {
      const address = server.address();
      if (typeof address !== "object" || !address) {
        fail(new Error("The UI server has no port"));
        return;
      }
      ready({
        url: `http://localhost:${address.port}`,
        stop: () =>
          new Promise(done => {
            server.closeAllConnections();
            server.close(() => done());
          }),
      });
    });
  });
};
