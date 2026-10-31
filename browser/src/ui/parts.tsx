import React, { useId } from "react";
import { formatNumber } from "./format";
import type { IconName } from "./Icon";
import { Icon } from "./Icon";

export const Figure: React.FC<{ icon: IconName; value: React.ReactNode; title?: string; size?: number }> = ({
  icon,
  value,
  title,
  size,
}) => (
  <span className="figure" title={title}>
    <Icon name={icon} size={size} />
    <span>{value}</span>
  </span>
);

export const Banner: React.FC<{ image: string; title: string; children?: React.ReactNode }> = ({ image, title, children }) => (
  <header className="banner" style={{ backgroundImage: `url(${image})` }}>
    <div>
      <h1>{title}</h1>
      {children ? <p>{children}</p> : null}
    </div>
  </header>
);

export const Panel: React.FC<{ title?: string; lede?: React.ReactNode; children: React.ReactNode; className?: string }> = ({
  title,
  lede,
  children,
  className,
}) => (
  <section className={`panel ${className ?? ""}`}>
    {title ? <h2>{title}</h2> : null}
    {lede ? <p className="lede">{lede}</p> : null}
    {children}
  </section>
);

/**
 * a whole number field with a button that fills in the most the player can afford
 */
export const AmountField: React.FC<{
  value: number;
  onChange: (value: number) => void;
  max?: number;
  label: string;
  hideLabel?: boolean;
}> = ({ value, onChange, max, label, hideLabel }) => {
  const id = useId();
  return (
    <div className="field">
      <label htmlFor={id} className={hideLabel ? "visually-hidden" : undefined}>
        {label}
      </label>
      <span className="amount">
        <input
          id={id}
          className="input"
          type="number"
          inputMode="numeric"
          min={0}
          value={value === 0 ? "" : value}
          placeholder="0"
          onChange={event => onChange(Math.max(0, Math.floor(Number(event.target.value) || 0)))}
        />
        {max === undefined ? null : (
          <button
            type="button"
            className="max"
            onClick={() => onChange(Math.max(0, max))}
            title={`Fill in ${formatNumber(max)}`}
            aria-label={`Fill in the most, ${formatNumber(max)}`}
          >
            Max
          </button>
        )}
      </span>
    </div>
  );
};

export const Meter: React.FC<{ value: number; max: number; label: string }> = ({ value, max, label }) => {
  const ratio = max <= 0 ? 1 : Math.min(1, value / max);
  return (
    <div
      className={`meter ${ratio >= 1 ? "full" : ""}`}
      role="meter"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-label={label}
    >
      <span style={{ width: `${ratio * 100}%` }} />
    </div>
  );
};

export const Choice = <T extends string>({
  options,
  value,
  onChange,
  label,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (value: T) => void;
  label: string;
}): React.ReactElement => (
  <div className="choice" role="group" aria-label={label}>
    {options.map(option => (
      <button key={option.value} type="button" aria-pressed={option.value === value} onClick={() => onChange(option.value)}>
        {option.label}
      </button>
    ))}
  </div>
);

export const Tabs = <T extends string>({
  tabs,
  value,
  onChange,
}: {
  tabs: { value: T; label: React.ReactNode }[];
  value: T;
  onChange: (value: T) => void;
}): React.ReactElement => (
  <div className="tabs" role="tablist">
    {tabs.map(tab => (
      <button key={tab.value} type="button" role="tab" aria-selected={tab.value === value} onClick={() => onChange(tab.value)}>
        {tab.label}
      </button>
    ))}
  </div>
);
