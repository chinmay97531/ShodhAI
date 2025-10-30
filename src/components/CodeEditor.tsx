"use client";

import { useId } from "react";

export interface CodeEditorProps {
  value: string;
  language: string;
  onChange: (value: string) => void;
}

const LANGUAGE_LABELS: Record<string, string> = {
  python: "Python",
  cpp: "C++",
  java: "Java",
  javascript: "JavaScript"
};

export function CodeEditor({ value, language, onChange }: CodeEditorProps) {
  const textAreaId = useId();
  const label = LANGUAGE_LABELS[language] ?? language;

  return (
    <div className="flex h-full flex-col overflow-hidden rounded-xl border border-slate-800 bg-slate-950/80">
      <div className="flex items-center justify-between border-b border-slate-800 px-4 py-2 text-xs uppercase tracking-wide text-slate-400">
        <span>{label} Editor</span>
        <span>{value.length} chars</span>
      </div>
      <label htmlFor={textAreaId} className="sr-only">
        {label} editor
      </label>
      <textarea
        id={textAreaId}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        spellCheck={false}
        className="h-full flex-1 resize-none bg-transparent px-4 py-3 font-mono text-sm text-slate-100 outline-none"
        placeholder={`Write your ${label} solution here...`}
      />
    </div>
  );
}
