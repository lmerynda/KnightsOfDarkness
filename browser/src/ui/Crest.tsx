import React from "react";

// heraldic tinctures, muted to sit on the night stone
const tinctures = ["#8e2f2a", "#2e4a78", "#3e5e3a", "#5b3a6b", "#2c2f33", "#b89a5a", "#c9c4b8"];
const metals = new Set(["#b89a5a", "#c9c4b8"]);

const hash = (text: string): number => {
  let value = 2166136261;
  for (const character of text) {
    value ^= character.charCodeAt(0);
    value = Math.imul(value, 16777619);
  }
  return value >>> 0;
};

const shield = "M6 4h52v26c0 16-11 26-26 32C17 56 6 46 6 30z";

/**
 * every kingdom gets its own coat of arms, derived from its name so it is the same everywhere
 */
export const Crest: React.FC<{ name: string; size?: number }> = ({ name, size = 40 }) => {
  const seed = hash(name);
  const field = tinctures[seed % tinctures.length];
  // the rule of tincture: a metal is set against a colour
  const pool = metals.has(field) ? tinctures.filter(tincture => !metals.has(tincture)) : ["#b89a5a", "#c9c4b8"];
  const charge = pool[(seed >>> 4) % pool.length];
  const division = (seed >>> 8) % 6;
  const id = `crest-${seed}`;

  const ordinary = [
    <rect key="pale" x="25" y="0" width="14" height="64" fill={charge} />,
    <rect key="fess" x="0" y="22" width="64" height="13" fill={charge} />,
    <path key="bend" d="M0 8 8 0 64 56 56 64z" fill={charge} />,
    <path key="chevron" d="M4 46 32 18l28 28v-12L32 6 4 34z" fill={charge} />,
    <path key="quarterly" d="M0 0h32v32H0zM32 32h32v32H32z" fill={charge} />,
    <path key="cross" d="M27 0h10v64H27zM0 22h64v10H0z" fill={charge} />,
  ][division];

  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden="true">
      <defs>
        <clipPath id={id}>
          <path d={shield} />
        </clipPath>
      </defs>
      <g clipPath={`url(#${id})`}>
        <rect width="64" height="64" fill={field} />
        {ordinary}
      </g>
      <path d={shield} fill="none" stroke="#b89a5a" strokeWidth="2.5" />
      <path d={shield} fill="none" stroke="rgba(0,0,0,0.35)" strokeWidth="1" transform="translate(0 1)" />
    </svg>
  );
};
