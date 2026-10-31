import React from "react";

export type IconName =
  | "gold"
  | "food"
  | "iron"
  | "tools"
  | "weapons"
  | "land"
  | "buildingPoints"
  | "people"
  | "turns"
  | "shield"
  | "eye"
  | "sword"
  | "warning"
  | "scroll";

const tint: Record<IconName, string> = {
  gold: "#d4b878",
  food: "#c9a85c",
  iron: "#9fb0bf",
  tools: "#b9a58a",
  weapons: "#c7ccd2",
  land: "#8fae73",
  buildingPoints: "#c9895a",
  people: "#d9cdb6",
  turns: "#e08a2e",
  shield: "#b89a5a",
  eye: "#9fb0bf",
  sword: "#c7ccd2",
  warning: "#e08a2e",
  scroll: "#d4b878",
};

const paths: Record<IconName, React.ReactNode> = {
  gold: (
    <>
      <ellipse cx="12" cy="17" rx="7" ry="2.6" />
      <path d="M5 17v-3.2c0 1.4 3.1 2.6 7 2.6s7-1.2 7-2.6V17" />
      <ellipse cx="12" cy="10.6" rx="7" ry="2.6" />
      <path d="M5 10.6V7.4C5 6 8.1 4.8 12 4.8S19 6 19 7.4v3.2" />
    </>
  ),
  food: (
    <>
      <path d="M12 21V8" />
      <path d="M12 12c-2.5 0-4.5-1.8-4.5-4.5 2.5 0 4.5 1.8 4.5 4.5zM12 12c2.5 0 4.5-1.8 4.5-4.5-2.5 0-4.5 1.8-4.5 4.5z" />
      <path d="M12 16.5c-2.5 0-4.5-1.8-4.5-4.5 2.5 0 4.5 1.8 4.5 4.5zM12 16.5c2.5 0 4.5-1.8 4.5-4.5-2.5 0-4.5 1.8-4.5 4.5z" />
      <path d="M12 7.5c-1.2-1-1.6-2.4-1-4 1.6.4 1.9 2.4 1 4zM12 7.5c1.2-1 1.6-2.4 1-4" />
    </>
  ),
  iron: (
    <>
      <path d="M3.5 16.5 6.5 9h11l3 7.5z" />
      <path d="M6.5 9 8 6.5h8L17.5 9" />
    </>
  ),
  tools: (
    <>
      <path d="m4 20 9.5-9.5" />
      <path d="M12 6.5 15.5 3l5.5 5.5-3.5 3.5-2-2-1.5 1.5-3-3 1.5-1.5z" />
    </>
  ),
  weapons: (
    <>
      <path d="m5 5 11 11M16 16l3 3M14 18l4-4" />
      <path d="m19 5-11 11M8 16l-3 3M10 18l-4-4" />
    </>
  ),
  land: (
    <>
      <path d="M2.5 19.5 9 9.5l3.5 5 2.5-3.5 6.5 8.5z" />
      <path d="M9 9.5V3.5l4 1.6-4 1.6" />
    </>
  ),
  buildingPoints: (
    <>
      <rect x="3" y="13" width="8" height="5" />
      <rect x="13" y="13" width="8" height="5" />
      <rect x="8" y="7" width="8" height="5" />
    </>
  ),
  people: (
    <>
      <circle cx="12" cy="7.5" r="3.5" />
      <path d="M4.5 20.5c.6-4.4 3.6-7 7.5-7s6.9 2.6 7.5 7" />
    </>
  ),
  turns: (
    <>
      <path d="M6 3h12M6 21h12" />
      <path d="M7.5 3c0 5 9 5 9 9s-9 4-9 9M16.5 3c0 5-9 5-9 9s9 4 9 9" />
    </>
  ),
  shield: <path d="M12 3 4.5 6v5.5c0 4.7 3.2 8.1 7.5 9.5 4.3-1.4 7.5-4.8 7.5-9.5V6z" />,
  eye: (
    <>
      <path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z" />
      <circle cx="12" cy="12" r="3" />
    </>
  ),
  sword: (
    <>
      <path d="M19 3 9 13l2 2L21 5V3z" />
      <path d="m7 13 4 4M5.5 18.5 8 16M4 20l1.5-1.5" />
    </>
  ),
  warning: (
    <>
      <path d="M12 3.5 2.5 20h19z" />
      <path d="M12 10v4.5M12 17.2v.3" />
    </>
  ),
  scroll: (
    <>
      <path d="M7 4h11a2 2 0 0 1 0 4h-1v10a2 2 0 0 1-2 2H6a2 2 0 0 1 0-4h1z" />
      <path d="M7 16V4a2 2 0 0 0-2 2v1h2M10 9h4M10 12h4" />
    </>
  ),
};

export const Icon: React.FC<{ name: IconName; size?: number; title?: string }> = ({ name, size = 18, title }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke={tint[name]}
    strokeWidth={1.6}
    strokeLinecap="round"
    strokeLinejoin="round"
    role={title ? "img" : undefined}
    aria-hidden={title ? undefined : true}
  >
    {title ? <title>{title}</title> : null}
    {paths[name]}
  </svg>
);
