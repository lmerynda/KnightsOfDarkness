import React, { useEffect, useId, useLayoutEffect, useRef, useState } from "react";

const gap = 6;
const margin = 8;

/**
 * the rules live here, out of the way: open on hover or focus, and on a tap for fingers
 */
const useTip = () => {
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);
  const [pinned, setPinned] = useState(false);
  const anchor = useRef<HTMLElement | null>(null);
  const bubble = useRef<HTMLSpanElement | null>(null);
  const [place, setPlace] = useState<{ top: number; left: number } | null>(null);
  const id = useId();
  const open = hovered || focused || pinned;

  useLayoutEffect(() => {
    if (!open || !anchor.current || !bubble.current) {
      setPlace(null);
      return;
    }
    const at = anchor.current.getBoundingClientRect();
    const size = bubble.current.getBoundingClientRect();
    const below = at.bottom + gap;
    const top = below + size.height > window.innerHeight - margin ? Math.max(margin, at.top - gap - size.height) : below;
    const left = Math.min(Math.max(margin, at.left + at.width / 2 - size.width / 2), window.innerWidth - margin - size.width);
    setPlace({ top, left });
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const close = (event: Event) => {
      if (event instanceof KeyboardEvent && event.key !== "Escape") return;
      if (event.type === "pointerdown" && anchor.current?.contains(event.target as Node)) return;
      setPinned(false);
      setHovered(false);
      if (event instanceof KeyboardEvent) anchor.current?.blur();
    };
    const hide = () => {
      setPinned(false);
      setHovered(false);
    };
    document.addEventListener("pointerdown", close);
    document.addEventListener("keydown", close);
    window.addEventListener("scroll", hide, true);
    return () => {
      document.removeEventListener("pointerdown", close);
      document.removeEventListener("keydown", close);
      window.removeEventListener("scroll", hide, true);
    };
  }, [open]);

  const triggers = {
    "aria-describedby": open ? id : undefined,
    onPointerEnter: (event: React.PointerEvent) => event.pointerType === "mouse" && setHovered(true),
    onPointerLeave: (event: React.PointerEvent) => event.pointerType === "mouse" && setHovered(false),
    onFocus: () => setFocused(true),
    onBlur: () => setFocused(false),
    onClick: (event: React.MouseEvent) => {
      event.preventDefault();
      event.stopPropagation();
      if (!pinned) {
        setPinned(true);
        return;
      }
      setPinned(false);
      setHovered(false);
      setFocused(false);
    },
  };

  const shown = (text: React.ReactNode) =>
    open ? (
      <span
        ref={bubble}
        id={id}
        role="tooltip"
        className="tip-bubble"
        style={place ? { top: place.top, left: place.left } : { visibility: "hidden", top: 0, left: 0 }}
      >
        {text}
      </span>
    ) : null;

  return { anchor, triggers, shown };
};

/** a small mark beside a heading that holds the rules of what is under it */
export const Tip: React.FC<{ about: string; children: React.ReactNode }> = ({ about, children }) => {
  const { anchor, triggers, shown } = useTip();
  return (
    <>
      <button
        ref={element => {
          anchor.current = element;
        }}
        type="button"
        className="tip-mark"
        aria-label={`About ${about}`}
        {...triggers}
      >
        ?
      </button>
      {shown(children)}
    </>
  );
};

/** a word that explains itself */
export const Term: React.FC<{ tip: React.ReactNode; children: React.ReactNode }> = ({ tip, children }) => {
  const { anchor, triggers, shown } = useTip();
  return (
    <>
      <span
        ref={element => {
          anchor.current = element;
        }}
        className="term"
        tabIndex={0}
        role="button"
        {...triggers}
      >
        {children}
      </span>
      {shown(tip)}
    </>
  );
};
