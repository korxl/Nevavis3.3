# NevaVis — сервер онлайн-счётчика (необязательно)

Нужен, чтобы видеть, сколько людей играет с NevaVis, и чтобы значок «N» появлялся у ников других пользователей.
Без сервера значок виден только у вас, а счётчик показывает «Backend URL not set».

## Развернуть бесплатно на Cloudflare (можно с iPhone, в браузере)
1. Создайте аккаунт на dash.cloudflare.com.
2. Storage & Databases → **KV** → Create namespace, назовите `nevavis-users`.
3. Workers & Pages → **Create** → Create Worker → Deploy → **Edit code**, вставьте весь файл `worker.js`, Deploy.
4. В настройках воркера: **Bindings → Add → KV namespace**, Variable name: `USERS`, выберите `nevavis-users`. Deploy.
5. Скопируйте адрес воркера (вида `https://имя.ваш-аккаунт.workers.dev`).
6. В игре: NevaVis (Right Shift) → Settings → Online → вставьте адрес в поле (Ctrl+V).

Важно:
- Бесплатный лимит KV — 1000 записей в день: примерно 6–7 одновременных игроков (каждый пишет раз в 10 минут). Больше — платный план Cloudflare.
- Сервер доверяет присланному нику (проверки через Mojang нет), значок — косметика, подделать его может любой.
- Отправляются только ваш UUID и ник, и только если включено «Share presence».
