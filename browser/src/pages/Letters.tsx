import React, { useCallback, useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { Letter } from "../api/types";
import { useGame } from "../game/useGame";
import { art } from "../game/lore";
import { Crest } from "../ui/Crest";
import { Banner, Panel, Tabs } from "../ui/parts";
import { ago } from "../ui/format";

const allianceRecipient = "__alliance__";
const maxLength = 2000;

const Compose: React.FC<{ onSent: () => void; replyTo: string | null }> = ({ onSent, replyTo }) => {
  const { kingdom, rankings, act } = useGame();
  const [recipient, setRecipient] = useState("");
  const [body, setBody] = useState("");

  useEffect(() => {
    if (replyTo) setRecipient(replyTo);
  }, [replyTo]);

  const send = async (): Promise<void> => {
    const result = await act(
      () => (recipient === allianceRecipient ? api.sendAllianceLetter(body) : api.sendLetter(recipient, body)),
      outcome => ({ text: outcome.message, failure: !outcome.success }),
    );
    if (result?.success) {
      setBody("");
      onSent();
    }
  };

  return (
    <Panel title="Write a letter">
      <div className="stack">
        <label className="field">
          <span>To</span>
          <select className="select" value={recipient} onChange={event => setRecipient(event.target.value)}>
            <option value="">Choose who receives it</option>
            {kingdom.allianceName ? <option value={allianceRecipient}>Everyone in {kingdom.allianceName}</option> : null}
            {(rankings?.kingdoms ?? [])
              .filter(rank => rank.kingdomName !== kingdom.name)
              .map(rank => (
                <option key={rank.kingdomName} value={rank.kingdomName}>
                  {rank.kingdomName}
                  {rank.allianceName ? `, ${rank.allianceName}` : ""}
                </option>
              ))}
          </select>
        </label>
        <label className="field">
          <span>
            Letter, {body.length} of {maxLength} characters
          </span>
          <textarea className="textarea" value={body} maxLength={maxLength} onChange={event => setBody(event.target.value)} />
        </label>
        <div className="row">
          <button className="button" type="button" disabled={!recipient || !body.trim()} onClick={send}>
            Seal and send
          </button>
        </div>
      </div>
    </Panel>
  );
};

const LetterItem: React.FC<{ letter: Letter; incoming: boolean; onOpen: (letter: Letter) => void; onReply: (sender: string) => void }> = ({
  letter,
  incoming,
  onOpen,
  onReply,
}) => {
  const [open, setOpen] = useState(false);
  const other = incoming ? letter.sender : letter.recipient;
  const unread = incoming && !letter.isRead;

  return (
    <li className={`letter ${unread ? "unread" : ""}`}>
      <button
        type="button"
        className="letter-head"
        aria-expanded={open}
        onClick={() => {
          setOpen(!open);
          if (unread) onOpen(letter);
        }}
      >
        <Crest name={other} size={30} />
        <span className="letter-who">
          <strong>{incoming ? other : `To ${other}`}</strong>
          {letter.toAlliance ? <span className="muted"> to the alliance</span> : null}
        </span>
        <span className="letter-preview muted">{letter.body}</span>
        <span className="muted">{ago(letter.date)}</span>
      </button>
      {open ? (
        <div className="letter-body">
          <p>{letter.body}</p>
          {incoming ? (
            <button className="button quiet small" type="button" onClick={() => onReply(letter.sender)}>
              Reply to {letter.sender}
            </button>
          ) : null}
        </div>
      ) : null}
    </li>
  );
};

export const Letters: React.FC = () => {
  const { refreshCounters } = useGame();
  const [box, setBox] = useState<"inbox" | "sent">("inbox");
  const [letters, setLetters] = useState<Letter[] | null>(null);
  const [replyTo, setReplyTo] = useState<string | null>(null);

  const load = useCallback(() => {
    (box === "inbox" ? api.inbox() : api.sentLetters()).then(setLetters).catch(() => setLetters([]));
  }, [box]);

  useEffect(() => {
    setLetters(null);
    load();
  }, [load]);

  const markRead = async (letter: Letter): Promise<void> => {
    await api.markLetterRead(letter.id).catch(() => undefined);
    setLetters(current => current?.map(item => (item.id === letter.id ? { ...item, isRead: true } : item)) ?? null);
    refreshCounters();
  };

  return (
    <>
      <Banner image={art("messages")} title="Letters">
        Words travel faster than armies. Make pacts, ask for help, or warn a rival off.
      </Banner>
      <div className="two-columns letters-layout">
        <div>
          <Tabs
            value={box}
            onChange={setBox}
            tabs={[
              { value: "inbox", label: "Received" },
              { value: "sent", label: "Sent" },
            ]}
          />
          {letters === null ? (
            <p className="muted">Opening the satchel</p>
          ) : letters.length === 0 ? (
            <p className="empty">
              {box === "inbox" ? "No letters yet. Write first; most alliances start with a letter." : "You have not sent any letters."}
            </p>
          ) : (
            <ul className="letters">
              {letters.map(letter => (
                <LetterItem
                  key={letter.id}
                  letter={letter}
                  incoming={box === "inbox"}
                  onOpen={markRead}
                  onReply={sender => setReplyTo(sender)}
                />
              ))}
            </ul>
          )}
        </div>
        <Compose onSent={() => (box === "sent" ? load() : undefined)} replyTo={replyTo} />
      </div>
    </>
  );
};
