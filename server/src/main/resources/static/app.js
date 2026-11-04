async function api(path, opts = {}) {
  const res = await fetch(path, opts);
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error((data && data.error) || res.status + " " + res.statusText);
  return data;
}
function esc(s) { return String(s ?? "").replace(/[&<>"]/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c])); }
function nav(active) {
  const items = [["sources.html","外链库"],["sites.html","站点配置"]];
  document.body.insertAdjacentHTML("afterbegin",
    "<nav><strong>linkops-lite</strong>" + items.map(([h,t]) => `<a href="${h}" class="${h===active?"on":""}">${t}</a>`).join("") + "</nav>");
}
