// Converts generated PNGs (see prompts.md) into the web-sized WebP files served from public/art.
// Usage: node art/process.mjs <directory with generated pngs>
// Needs sharp: npm install --no-save sharp
import { readdirSync, mkdirSync } from "node:fs";
import { join, basename, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import sharp from "sharp";

const scenes = ["hero", "overview", "war", "marketplace", "alliance", "rankings", "messages", "spies", "transfers", "login", "aftermath", "victory"];
const source = process.argv[2];
if (!source) {
  console.error("Usage: node art/process.mjs <directory with generated pngs>");
  process.exit(1);
}

const output = join(dirname(fileURLToPath(import.meta.url)), "..", "public", "art");
mkdirSync(output, { recursive: true });

for (const file of readdirSync(source).filter(name => name.endsWith(".png"))) {
  const name = basename(file, ".png");
  const isScene = scenes.includes(name);
  const target = join(output, `${name}.webp`);
  await sharp(join(source, file))
    .resize(isScene ? { width: 1600 } : { width: 480, height: 480, fit: "cover" })
    .webp({ quality: isScene ? 72 : 78 })
    .toFile(target);
  console.log(target);
}
