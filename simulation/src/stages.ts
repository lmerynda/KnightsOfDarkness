import { spawn, spawnSync } from "node:child_process";
import { mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { parseArgs } from "node:util";
import { chromium } from "playwright-core";
import { World } from "./client.ts";
import { startServer } from "./server.ts";
import { coalitionWarlord, foundCoalition, guardian } from "./coalition.ts";
import { economist, raider, turtle, warlord } from "./strategies.ts";
import { runWorld } from "./world.ts";

const usage = `Play one world and photograph the real browser UI at several turns, to see how the game looks as it unfolds.

  node src/stages.ts [--turns 12,30,60] [--out stages]

Needs \`npm install\` in browser/ for the Vite dev server, and an installed Chrome. The UI runs on port 3001.`;

const { values } = parseArgs({
  options: {
    turns: { type: "string", default: "12,30,60" },
    out: { type: "string", default: "stages" },
    help: { type: "boolean", default: false },
  },
});
if (values.help) {
  console.log(usage);
  process.exit(0);
}

const browserDirectory = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..", "browser");
const stages = values.turns.split(",").map(Number);
const lastTurn = Math.max(...stages);
const uiPort = 3001;
const ui = `http://localhost:${uiPort}`;
const pages = ["overview", "build", "train", "war", "spies", "market", "carriers", "alliance", "letters", "chronicle", "rankings"];
mkdirSync(values.out, { recursive: true });

const answers = async (url: string): Promise<boolean> => {
  try {
    await fetch(url);
    return true;
  } catch {
    return false;
  }
};
// our own dev server, never one left running and pointed at another backend
if (await answers(ui)) throw new Error(`Port ${uiPort} is taken, stop whatever runs there first`);

const server = await startServer();
const vite = spawn(`npm run dev -- --port ${uiPort} --strictPort`, {
  cwd: browserDirectory,
  shell: true,
  env: { ...process.env, VITE_GAME_API: server.url },
  stdio: "ignore",
});
const browser = await chromium.launch({ channel: "chrome" });

const signIn = async (kingdomName: string): Promise<string> => {
  const email = `${kingdomName.toLowerCase().replace(/[^a-z0-9]+/g, "-")}@simulation.test`;
  const response = await fetch(`${server.url}/auth/authenticate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password: "simulation-password" }),
  });
  return ((await response.json()) as { token: string }).token;
};

const photograph = async (kingdomName: string, label: string, only: string[], viewport = { width: 1440, height: 900 }): Promise<void> => {
  const context = await browser.newContext({ viewport, deviceScaleFactor: 1 });
  const page = await context.newPage();
  await page.goto(`${ui}/`);
  const token = await signIn(kingdomName);
  await page.evaluate(value => localStorage.setItem("authToken", value), token);
  for (const name of only) {
    await page.goto(`${ui}/kingdom/${name}`);
    await page.waitForTimeout(1200);
    await page.screenshot({ path: join(values.out, `${label}-${name}.png`), fullPage: true });
  }
  await context.close();
};

try {
  for (let attempt = 0; !(await answers(ui)); attempt++) {
    if (attempt > 120) throw new Error("The UI never started");
    await new Promise(wait => setTimeout(wait, 500));
  }
  const world = new World(server.url);
  // two rival coalitions: Order strikes together and guards its own, Horde plays every kingdom for itself
  const order = await foundCoalition(world, "Order", council => [coalitionWarlord(council), economist, guardian(council), coalitionWarlord(council)]);
  const horde = await foundCoalition(world, "Horde", () => [warlord(), economist, turtle, raider]);
  const rulers = [...order.rulers, ...horde.rulers];
  const [warlordName, economistName, turtleName] = order.rulers.map(ruler => ruler.player.name);

  console.log("photographing the gates and a newcomer");
  const visitor = await browser.newContext({ viewport: { width: 1440, height: 900 }, deviceScaleFactor: 1 });
  const gate = await visitor.newPage();
  for (const route of ["", "sign-in", "register"]) {
    await gate.goto(`${ui}/${route}`);
    await gate.waitForTimeout(1200);
    await gate.screenshot({ path: join(values.out, `t00-gate-${route || "landing"}.png`), fullPage: true });
  }
  await visitor.close();
  await photograph(economistName, "t00-newcomer", pages);
  await runWorld(world, rulers, lastTurn, {
    afterTurn: async turn => {
      if (!stages.includes(turn)) return;
      console.log(`photographing turn ${turn}`);
      const stage = `t${String(turn).padStart(2, "0")}`;
      await photograph(warlordName, `${stage}-warlord`, turn === lastTurn ? pages : ["overview", "build", "train", "war", "spies", "rankings"]);
      if (turn !== lastTurn) return;
      await photograph(economistName, `${stage}-economist`, ["overview", "war", "alliance", "chronicle"]);
      await photograph(turtleName, `${stage}-guardian`, ["overview", "war"]);
      await photograph(warlordName, `${stage}-phone`, ["overview", "war"], { width: 390, height: 844 });
    },
  });
  console.log(`Screenshots in ${resolve(values.out)}`);
} finally {
  await browser.close();
  // npm starts vite as a grandchild, so the whole tree goes, and before this process exits
  if (process.platform === "win32") spawnSync("taskkill", ["/pid", String(vite.pid), "/T", "/F"], { stdio: "ignore" });
  else vite.kill();
  await server.stop();
}
