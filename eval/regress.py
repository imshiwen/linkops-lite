#!/usr/bin/env python3
"""研判回归:用人工标注集比较不同提示词版本。

    python3 eval/regress.py merge                     把 to-label.csv 里填了 expected 的行并入 labels.csv
    python3 eval/regress.py run --versions v1,v2      每个版本把全部标注入口跑一遍(先启动 server)
    python3 eval/regress.py run --versions v2 --runs 2

参数沿用服务默认值(关闭思考、temperature=0),只变提示词版本,方便归因。
结果写到 eval/results/regress-<时间>.md
"""
import argparse
import csv
import json
import urllib.request
from collections import Counter
from datetime import date, datetime
from pathlib import Path

HERE = Path(__file__).parent
API = "http://localhost:8090/api/judge/preview"
LABELS = HERE / "labels.csv"
TO_LABEL = HERE / "to-label.csv"
VERDICTS = ["SUITABLE", "UNSUITABLE", "UNCERTAIN"]
SITE = "cursivefontgenerator"


def load_labels():
    with LABELS.open(encoding="utf-8") as f:
        return {int(r["source_id"]): r for r in csv.DictReader(f) if r["site"] == SITE}


def merge():
    labels = load_labels()
    with TO_LABEL.open(encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    added, pending = 0, []
    with LABELS.open("a", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        for r in rows:
            exp = r["expected"].strip().upper()
            if not exp:
                pending.append(r)
                continue
            if exp not in VERDICTS:
                raise SystemExit(f"id {r['source_id']} 的 expected 只能是 {VERDICTS}")
            if int(r["source_id"]) in labels:
                continue
            w.writerow([r["source_id"], SITE, exp, date.today().isoformat(), r["note"] or r["bucket"]])
            added += 1
    with TO_LABEL.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["source_id", "bucket", "url", "list_description", "expected", "note"])
        w.writeheader()
        w.writerows(pending)
    print(f"并入 {added} 条;待标注剩 {len(pending)} 条")


def call(source_id, version):
    body = {"sourceId": source_id, "site": SITE, "promptVersion": version}
    req = urllib.request.Request(API, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=300) as r:
            return json.load(r)
    except Exception as e:  # 失败本身也是结果
        return {"error": str(e)}


def run(versions, runs):
    labels = load_labels()
    ids = sorted(labels)
    results = {}  # (version, id) -> [response...]
    for v in versions:
        for sid in ids:
            for i in range(runs):
                d = call(sid, v)
                results.setdefault((v, sid), []).append(d)
                got = (d.get("result") or {}).get("verdict", "ERR")
                mark = "✓" if got == labels[sid]["expected"] else "✗"
                print(f"{v:<4} id={sid:<4} run={i + 1} {got:<10} {mark} attempts={d.get('attempts')}")

    def verdict(d):
        return (d.get("result") or {}).get("verdict", "ERR") if "error" not in d else "ERR"

    lines = [f"# 研判回归 {datetime.now():%Y-%m-%d %H:%M}", "",
             f"标注 {len(ids)} 条;版本 {', '.join(versions)};每条跑 {runs} 次;关闭思考、temperature=0。", "",
             "| 版本 | 准确率 | 稳定性 | 重试>1 次 | 失败 | 平均输入 token | 平均输出 token | 平均耗时 ms |",
             "|---|---:|---:|---:|---:|---:|---:|---:|"]
    for v in versions:
        all_d = [d for sid in ids for d in results[(v, sid)]]
        ok = [d for d in all_d if "error" not in d and d.get("result")]
        correct = sum(1 for sid in ids for d in results[(v, sid)] if verdict(d) == labels[sid]["expected"])
        stable = sum(1 for sid in ids if len({verdict(d) for d in results[(v, sid)]}) == 1)
        retried = sum(1 for d in ok if (d.get("attempts") or 1) > 1)
        avg = lambda k: round(sum(d[k] for d in ok) / len(ok)) if ok else "-"
        lines.append(f"| {v} | {correct}/{len(all_d)} | {stable}/{len(ids)} | {retried} | {len(all_d) - len(ok)} | "
                     f"{avg('promptTokens')} | {avg('completionTokens')} | {avg('elapsedMs')} |")

    for v in versions:
        cm = Counter((labels[sid]["expected"], verdict(d)) for sid in ids for d in results[(v, sid)])
        lines += ["", f"## 混淆矩阵 {v}(行=人工标注,列=模型)", "",
                  "| | " + " | ".join(VERDICTS + ["ERR"]) + " |", "|---|" + "---:|" * 4]
        for e in VERDICTS:
            lines.append(f"| {e} | " + " | ".join(str(cm[(e, g)]) for g in VERDICTS + ["ERR"]) + " |")

    lines += ["", "## 逐条", "", "| id | 人工标注 | " + " | ".join(versions) + " | 备注 |",
              "|---|---|" + "---|" * len(versions) + "---|"]
    for sid in ids:
        cells = []
        for v in versions:
            vs = [verdict(d) for d in results[(v, sid)]]
            cells.append("/".join(vs) + ("" if all(x == labels[sid]["expected"] for x in vs) else " ✗"))
        lines.append(f"| {sid} | {labels[sid]['expected']} | " + " | ".join(cells) + f" | {labels[sid]['note']} |")

    lines += ["", "## 判错的理由(便于改 prompt)", ""]
    for v in versions:
        for sid in ids:
            for d in results[(v, sid)]:
                if verdict(d) != labels[sid]["expected"]:
                    reasons = (d.get("result") or {}).get("reasons") or [d.get("error", "")]
                    lines.append(f"- {v} · id {sid} · 判 {verdict(d)}(应 {labels[sid]['expected']}):{reasons[0]}")

    out = HERE / "results" / f"regress-{datetime.now():%Y%m%d-%H%M}.md"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"\n结果:{out}")


def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("merge")
    r = sub.add_parser("run")
    r.add_argument("--versions", default="v1")
    r.add_argument("--runs", type=int, default=1)
    a = ap.parse_args()
    if a.cmd == "merge":
        merge()
    else:
        run(a.versions.split(","), a.runs)


if __name__ == "__main__":
    main()
