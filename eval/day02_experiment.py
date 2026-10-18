#!/usr/bin/env python3
"""Day 2 实验:同一批外链入口,在不同模型参数下各跑多次,对比稳定性、耗时和 token。

用法(先启动 server):
    python3 eval/day02_experiment.py --ids 4,9,40 --runs 3
结果写到 eval/results/day02-<时间>.md
"""
import argparse
import csv
import json
import statistics
import time
import urllib.request
from datetime import datetime
from pathlib import Path

API = "http://localhost:8090/api/judge/preview"
LABELS = Path(__file__).parent / "labels.csv"


def load_labels(site="cursivefontgenerator"):
    """人工标注:source_id -> 期望结论(SUITABLE / UNSUITABLE / UNCERTAIN)。"""
    if not LABELS.exists():
        return {}
    with LABELS.open(encoding="utf-8") as f:
        return {int(r["source_id"]): r["expected"] for r in csv.DictReader(f) if r["site"] == site}

# 每组配置只改一个变量,方便归因
CONFIGS = [
    {"name": "基线:不思考,配置默认温度", "thinking": False, "temperature": None, "nativeSchema": False},
    {"name": "不思考,temperature=0", "thinking": False, "temperature": 0.0, "nativeSchema": False},
    {"name": "不思考,temperature=1.2", "thinking": False, "temperature": 1.2, "nativeSchema": False},
    {"name": "开启思考", "thinking": True, "temperature": None, "nativeSchema": False},
    {"name": "不思考,原生 json_schema", "thinking": False, "temperature": None, "nativeSchema": True},
]


def call(source_id, cfg):
    body = {"sourceId": source_id, **{k: v for k, v in cfg.items() if k != "name"}}
    req = urllib.request.Request(API, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=300) as r:
            return json.load(r)
    except Exception as e:  # 失败也要记录,失败率本身就是实验结果
        return {"error": str(e)}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--ids", required=True, help="外链入口 id,逗号分隔")
    ap.add_argument("--runs", type=int, default=3)
    args = ap.parse_args()
    ids = [int(x) for x in args.ids.split(",")]
    labels = load_labels()

    rows = []
    for cfg in CONFIGS:
        for sid in ids:
            for i in range(args.runs):
                d = call(sid, cfg)
                rows.append({"cfg": cfg["name"], "id": sid, "run": i + 1, **d})
                verdict = (d.get("result") or {}).get("verdict") if "error" not in d else "ERR"
                print(f"{cfg['name']:<24} id={sid:<4} run={i + 1} verdict={verdict} ms={d.get('elapsedMs')}")

    lines = [f"# Day 2 实验结果 {datetime.now():%Y-%m-%d %H:%M}", "",
             f"入口 id:{args.ids};每组每个入口跑 {args.runs} 次。", "",
             "| 配置 | 结论一致率 | 与人工标注一致 | 失败次数 | 平均耗时 ms | 平均输入 token | 平均输出 token |",
             "|---|---:|---:|---:|---:|---:|---:|"]
    for cfg in CONFIGS:
        rs = [r for r in rows if r["cfg"] == cfg["name"]]
        ok = [r for r in rs if "error" not in r and r.get("result")]
        consistent = 0
        for sid in ids:
            verdicts = [r["result"].get("verdict") for r in ok if r["id"] == sid]
            if verdicts and len(set(map(str, verdicts))) == 1:
                consistent += 1
        avg = lambda k: round(statistics.mean([r[k] for r in ok if r.get(k) is not None]), 0) if ok else "-"
        labeled = [r for r in ok if r["id"] in labels]
        agree = sum(1 for r in labeled if r["result"].get("verdict") == labels[r["id"]])
        accuracy = f"{agree}/{len(labeled)}" if labeled else "-"
        lines.append(f"| {cfg['name']} | {consistent}/{len(ids)} | {accuracy} | {len(rs) - len(ok)} | "
                     f"{avg('elapsedMs')} | {avg('promptTokens')} | {avg('completionTokens')} |")

    lines += ["", "## 逐条结论", "", "| 配置 | id | 次 | verdict | 人工标注 | 首条理由 |", "|---|---|---|---|---|---|"]
    for r in rows:
        res = r.get("result") or {}
        reason = (res.get("reasons") or [r.get("error", "")])[0][:60].replace("|", "/")
        lines.append(f"| {r['cfg']} | {r['id']} | {r['run']} | {res.get('verdict', 'ERR')} | {labels.get(r['id'], '-')} | {reason} |")

    out = Path(__file__).parent / "results" / f"day02-{datetime.now():%Y%m%d-%H%M}.md"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"\n结果:{out}")


if __name__ == "__main__":
    main()
