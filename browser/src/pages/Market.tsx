import React, { useCallback, useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { MarketOffer, MarketResource } from "../api/types";
import { marketResources } from "../api/types";
import { useGame } from "../game/useGame";
import { art, marketResourceLabel } from "../game/lore";
import { Icon } from "../ui/Icon";
import { CountField, Banner, Panel, Tabs } from "../ui/parts";
import { formatNumber, plural } from "../ui/format";

const OfferRow: React.FC<{ offer: MarketOffer; own: boolean; ally: boolean; onDone: () => void }> = ({ offer, own, ally, onDone }) => {
  const { kingdom, act } = useGame();
  const [count, setCount] = useState(0);
  const affordable = Math.min(offer.count, Math.floor(kingdom.resources.gold / offer.price));
  const label = marketResourceLabel[offer.resource].toLowerCase();

  return (
    <tr className={own ? "mine" : undefined}>
      <td>{own ? `${offer.sellerName} (you)` : ally ? `${offer.sellerName} (ally)` : offer.sellerName}</td>
      <td className="number">{formatNumber(offer.count)}</td>
      <td className="number">{formatNumber(offer.price)}</td>
      <td className="number muted narrow-hidden">{formatNumber(offer.count * offer.price)}</td>
      <td className="market-buy">
        {own ? (
          <button
            className="button quiet small"
            type="button"
            onClick={async () => {
              await act(
                () => api.withdrawOffer(offer.id),
                result => ({ text: result.success ? `Your ${label} came back from the market` : result.message, failure: !result.success }),
              );
              onDone();
            }}
          >
            Withdraw
          </button>
        ) : (
          <div className="row">
            <div style={{ width: 140 }}>
              <CountField label={`Amount of ${label} to buy`} hideLabel value={count} onChange={setCount} max={affordable} />
            </div>
            <button
              className="button small"
              type="button"
              disabled={count <= 0}
              onClick={async () => {
                await act(
                  () => api.buyOffer(offer.id, count),
                  result => ({
                    text:
                      result.count > 0
                        ? `Bought ${formatNumber(result.count)} ${label} for ${formatNumber(result.totalCost)} gold`
                        : "Not enough gold for any of it",
                    failure: result.count === 0,
                  }),
                );
                setCount(0);
                onDone();
              }}
            >
              Buy{count > 0 ? ` for ${formatNumber(count * offer.price)}` : ""}
            </button>
          </div>
        )}
      </td>
    </tr>
  );
};

const SellPanel: React.FC<{ resource: MarketResource; onDone: () => void }> = ({ resource, onDone }) => {
  const { kingdom, config, act } = useGame();
  const [count, setCount] = useState(0);
  const [price, setPrice] = useState(config.market.minPrice * 4);
  const owned = kingdom.resources[resource];
  const label = marketResourceLabel[resource].toLowerCase();

  return (
    <Panel
      title={`Sell ${label}`}
      help={`Goods you offer leave your stores until someone buys them or you withdraw them. Up to ${config.market.maxKingdomOffers} offers of each good.`}
    >
      <div className="row">
        <div style={{ width: 160 }}>
          <CountField label={`Amount, ${formatNumber(owned)} in store`} value={count} onChange={setCount} max={owned} />
        </div>
        <div style={{ width: 130 }}>
          <CountField label={`Gold each, at least ${config.market.minPrice}`} value={price} onChange={setPrice} />
        </div>
        <button
          className="button"
          type="button"
          disabled={count <= 0 || price < config.market.minPrice}
          onClick={async () => {
            const result = await act(
              () => api.postOffer(resource, count, price),
              outcome => ({ text: outcome.message, failure: !outcome.success }),
            );
            if (result?.success) setCount(0);
            onDone();
          }}
        >
          Offer for {formatNumber(count * price)} gold
        </button>
      </div>
    </Panel>
  );
};

export const Market: React.FC = () => {
  const { kingdom, config } = useGame();
  const [resource, setResource] = useState<MarketResource>("food");
  const [offers, setOffers] = useState<MarketOffer[] | null>(null);
  const [alliesOffers, setAlliesOffers] = useState<MarketOffer[]>([]);
  const allied = kingdom.allianceName !== null;

  const load = useCallback(() => {
    api
      .offers(resource)
      .then(list => setOffers([...list].sort((a, b) => a.price - b.price)))
      .catch(() => setOffers([]));
    if (allied)
      api
        .alliesOffers()
        .then(setAlliesOffers)
        .catch(() => setAlliesOffers([]));
  }, [resource, allied]);

  useEffect(() => {
    setOffers(null);
    load();
  }, [load]);

  const ownOffers = kingdom.marketOffers.filter(offer => offer.resource === resource);
  const ownIds = new Set(ownOffers.map(offer => offer.id));
  // the market lists only the cheapest offers; ours and our allies' always show, so a dear piece can carry gold between allies
  const listed = offers ?? [];
  const extra = [...ownOffers, ...alliesOffers.filter(offer => offer.resource === resource)].filter(
    offer => !listed.some(cheap => cheap.id === offer.id),
  );
  const shown = [...listed, ...extra].sort((a, b) => a.price - b.price);
  const allyNames = new Set(alliesOffers.map(offer => offer.sellerName));

  return (
    <>
      <Banner image={art("marketplace")} title="Market" />
      <Tabs
        value={resource}
        onChange={setResource}
        tabs={marketResources.map(name => ({
          value: name,
          label: (
            <>
              <Icon name={name} size={16} /> {marketResourceLabel[name]}{" "}
              <span className="muted">{formatNumber(kingdom.resources[name])}</span>
            </>
          ),
        }))}
      />
      <div className="stack">
        <Panel
          title={`${marketResourceLabel[resource]} for sale`}
          lede={`${formatNumber(kingdom.resources.gold)} gold in the treasury.`}
          help={`Prices are per piece, in gold, and you may buy part of an offer. Only the ${config.market.visibleMarketOffersCap} cheapest offers of each good are listed${allied ? ", besides every offer of your allies" : ""}.`}
        >
          {offers === null ? (
            <p className="muted">Asking the merchants</p>
          ) : shown.length === 0 ? (
            <p className="muted">Nobody sells {marketResourceLabel[resource].toLowerCase()}.</p>
          ) : (
            <div className="table-scroll">
              <table className="ledger">
                <thead>
                  <tr>
                    <th>Seller</th>
                    <th className="number">Amount</th>
                    <th className="number">Price each</th>
                    <th className="number narrow-hidden">All of it</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {shown.map(offer => (
                    <OfferRow
                      key={offer.id}
                      offer={offer}
                      own={ownIds.has(offer.id) || offer.sellerName === kingdom.name}
                      ally={allyNames.has(offer.sellerName)}
                      onDone={load}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Panel>
        <SellPanel resource={resource} onDone={load} />
        {kingdom.marketOffers.length > ownOffers.length ? (
          <p className="muted">
            {plural(kingdom.marketOffers.length - ownOffers.length, "more offer", "more offers")} of yours under other goods.
          </p>
        ) : null}
      </div>
    </>
  );
};
