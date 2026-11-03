Ты — агент в репозитории Minecraft Java 1.21.4 Fabric-мода NevaVis (JDK 21, Gradle + Loom, Yarn 1.21.4+build.8, Loader 0.16.10, Fabric API 0.119.4+1.21.4).

ЗАДАЧА: собрать рабочий JAR и починить ошибки компиляции.

1. Запусти `./gradlew build` (если нет wrapper — `gradle wrapper --gradle-version 8.12`, потом сборка). Либо запусти workflow .github/workflows/build.yml.
2. Если сборка падает — прочитай ошибки и исправь ТОЛЬКО то, что сломано. Код писали без доступа к Minecraft-исходникам, поэтому чаще всего ошибки в названиях Yarn: сверяйся с реальными классами через `./gradlew genSources` и поиск по исходникам. Подозрительные места: SkinTextures (пакет и конструктор, mixin/CapeMixin), PlayerSkinDrawer.draw (hud/elements/TargetHud), ClientWorld.getSkyColor/getCloudsColor, PlayerEntity.getDisplayName, PlayerListHud.getPlayerName, InGameHud.renderCrosshair, HeldItemRenderer.applySwingOffset, LivingEntity.getHandSwingDuration, Style.withFont(Identifier).
3. Миксины: если инъекция не находит метод — исправь имя метода/сигнатуру, не удаляй функцию. Файл nevavis.mixins.json держи с "defaultRequire": 0.
4. Повторяй сборку, пока `build/libs/nevavis-*.jar` не появится без ошибок. Ничего не удаляй и не упрощай без необходимости, не добавляй новых функций.
5. Закоммить исправления в ветку `fix/build` и открой Pull Request. В описании перечисли каждое исправление (файл, было → стало).
6. Положи готовый JAR в GitHub Release (или artifact `NevaVis-jar`) и дай ссылку.

ПРАВИЛА: не пиши, что сборка успешна, пока не запускал её и не видел зелёный результат; приведи последние строки лога сборки. Если не удаётся исправить — покажи точный текст ошибки и файл/строку. Не добавляй читы (видимость сквозь стены, обход античитов, эксплойты серверов).
