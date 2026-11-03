// NevaVis presence backend — Cloudflare Worker + KV (привязка с именем USERS).
// POST /ping   {"uuid": "...", "name": "..."}  -> отметить игрока онлайн (TTL 25 мин)
// GET  /online                                 -> {"count": N, "players": [{"uuid","name"}]}
const UUID = /^[0-9a-fA-F-]{32,36}$/;
const NAME = /^[A-Za-z0-9_]{1,16}$/;
const TTL = 25 * 60;

const json = (data, status = 200) =>
  new Response(JSON.stringify(data), {
    status,
    headers: { "content-type": "application/json", "access-control-allow-origin": "*" },
  });

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return json({});
    if (url.pathname === "/ping" && request.method === "POST") {
      let body;
      try { body = await request.json(); } catch { return json({ error: "bad json" }, 400); }
      if (!UUID.test(body.uuid || "") || !NAME.test(body.name || "")) return json({ error: "bad data" }, 400);
      await env.USERS.put("u:" + body.uuid.toLowerCase(), "1", { expirationTtl: TTL, metadata: { name: body.name } });
      return json({ ok: true });
    }
    if (url.pathname === "/online" && request.method === "GET") {
      const list = await env.USERS.list({ prefix: "u:" });
      const players = list.keys
        .filter((k) => k.metadata && k.metadata.name)
        .map((k) => ({ uuid: k.name.slice(2), name: k.metadata.name }));
      return json({ count: players.length, players });
    }
    return json({ service: "nevavis-presence" });
  },
};
