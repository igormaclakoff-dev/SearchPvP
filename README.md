<div align="center">

# ⚔️ SearchPvP

**Ранговый поиск PvP-поединков от студии AuraCode** — открытый плагин для Paper 1.21.x.

Матчмейкинг по уровню брони, система ELO-рейтинга, сезоны с наградами топ-3 и красивый GUI прямо из `config.yml`.

![Версия](https://img.shields.io/badge/version-1.0.0-blue)
![Paper](https://img.shields.io/badge/Paper-1.21.x-white)
![Лицензия](https://img.shields.io/badge/license-MIT-green)

</div>

---

## ✨ Возможности

- **Меню игрока** — `/pvp`: личная статистика (рейтинг, ранг, победы, K/D, серия побед, сезон) в виде головы и кнопка запуска поиска.
- **Матчмейкинг по броне** — в поиск попадают только игроки в полном сете брони (незерит или алмаз).
- **Защита от смены брони** — если во время поиска изменить/снять броню, показываются предупреждающие титулы; поиск отменяется, если броня не восстановлена за 10 секунд.
- **BOSS-BAR и анонсы** — ищущий игрок видит прогресс-бар, сервер получает уведомление о начале поиска.
- **Отмена поиска** — командой `/pvp cancel` или повторным кликом по кнопке в меню.
- **Арена** — случайная телепортация бойцов на расстояние 2000–5000 блоков, с заморозкой на время отсчёта.
- **Гибкая команда PvP-режима** — админ задаёт команду включения боя (`%name%` или `<name>`).
- **ELO-рейтинг** — классическая формула с настраиваемым K-фактором, стартовый рейтинг.
- **Сезоны** — таймер сезона; в последние сутки обратный отсчёт тикает по часам и минутам.
- **Награды топ-3** — описания и команды выдачи (бесконечно) для 1–3 места, выдаются автоматически в конце сезона и показываются в `/top`.
- **Топ игроков** — `/top`: пагинированная таблица лидеров по рейтингу с блоком наград сезона.
- **Хранение в YAML** — игроки и сезон сохраняются в `playerdata.yml` и `season.yml`.
- **Полная настройка** — вся текстовая часть в `messages.yml` с поддержкой MiniMessage (градиенты, HEX).

---

## 🎮 Команды

### Игрок

| Команда | Описание | Право |
|---|---|---|
| `/pvp` | Открыть меню арен | `searchpvp.use` |
| `/pvp cancel` | Отменить поиск противника | `searchpvp.use` |
| `/top` | Таблица лидеров сезона | `searchpvp.use` |

**Алиасы:** `/ranked`, `/arenapvp` → `/pvp`; `/pvptop`, `/leaderboard` → `/top`.

### Администрация

| Команда | Описание | Право |
|---|---|---|
| `/pvpadmin help` | Справка по командам | `searchpvp.admin` |
| `/pvpadmin reload` | Перезагрузить конфиги | `searchpvp.command.reload` |
| `/pvpadmin setpvpcommand <команда>` | Задать команду включения PvP-режима | `searchpvp.command.setpvpcommand` |
| `/pvpadmin season info` | Инфо о сезоне и таймер | `searchpvp.command.season` |
| `/pvpadmin season set <номер>` | Установить номер сезона (таймер заново) | `searchpvp.command.season` |
| `/pvpadmin season reset` | Новый сезон со сбросом рейтинга | `searchpvp.command.season` |
| `/pvpadmin season finish` | Завершить сезон сейчас (анонс + награды) | `searchpvp.command.season` |
| `/pvpadmin forcestop <игрок>` | Остановить поиск/бой игрока | `searchpvp.command.forcestop` |
| `/pvpadmin forcesearch <игрок>` | Принудительно запустить поиск | `searchpvp.command.forcesearch` |
| `/pvpadmin setelo <игрок> <число>` | Установить рейтинг игрока | `searchpvp.command.setelo` |
| `/pvpadmin reward <1-3> info` | Показать награду за место | `searchpvp.command.reward` |
| `/pvpadmin reward <1-3> line add <текст>` | Добавить строку описания награды | `searchpvp.command.reward` |
| `/pvpadmin reward <1-3> line remove <№>` / `clear` | Удалить/очистить строки | `searchpvp.command.reward` |
| `/pvpadmin reward <1-3> command add <команда>` | Добавить команду выдачи | `searchpvp.command.reward` |
| `/pvpadmin reward <1-3> command remove <№>` / `clear` | Удалить/очистить команды | `searchpvp.command.reward` |

**Алиасы:** `/spvpadmin`, `/searchpvp` → `/pvpadmin`.

> Все команды поддерживают **Tab-дополнение** (подкоманды, имена игроков, номера мест).

---

## 🔐 Права

| Право | Описание | По умолчанию |
|---|---|---|
| `searchpvp.use` | Доступ к `/pvp`, `/top` и матчмейкингу | все (`true`) |
| `searchpvp.admin` | Доступ ко всем подкомандам `/pvpadmin` | `op` |
| `searchpvp.command.reload` | `/pvpadmin reload` | `op` |
| `searchpvp.command.setpvpcommand` | `/pvpadmin setpvpcommand` | `op` |
| `searchpvp.command.season` | `/pvpadmin season ...` | `op` |
| `searchpvp.command.forcestop` | `/pvpadmin forcestop` | `op` |
| `searchpvp.command.forcesearch` | `/pvpadmin forcesearch` | `op` |
| `searchpvp.command.setelo` | `/pvpadmin setelo` | `op` |
| `searchpvp.command.reward` | `/pvpadmin reward ...` | `op` |

Все права `searchpvp.command.*` являются дочерними для `searchpvp.admin`.

---

## 📦 Установка

1. Скачайте `SearchPvP-1.0.0.jar` (см. раздел «Сборка» или Releases).
2. Положите jar в папку `plugins/` вашего Paper-сервера.
3. Перезапустите сервер — плагин создаст `plugins/SearchPvP/config.yml`, `messages.yml`, `playerdata.yml` и `season.yml`.
4. Настройте команду включения PvP-режима: `/pvpadmin setpvpcommand <команда>` (например `pvp give %name%`).

**Требования:** Paper 1.21.x, Java 21+.

---

## 🔨 Сборка из исходников

```bash
mvn clean package
```

Готовый jar появится в `target/SearchPvP-1.0.0.jar`.

---

## ⚙️ Конфигурация (`config.yml`)

```yaml
settings:
  # Команда (от консоли), включающая PvP-режим игроку. %name% или <name> — ник.
  placement-command: "pvp give %name%"

arena:
  world: "world"          # мир для арен
  min-distance: 2000      # минимальное расстояние от центра мира
  max-distance: 5000      # максимальное расстояние
  same-location: true     # телепортировать бойцов рядом
  spawn-offset: 5         # смещение между бойцами (при same-location)

matchmaking:
  interval-ticks: 20      # частота обработки очереди
  armor-change-grace-seconds: 10   # время на восстановление брони

elo:
  starting-elo: 1000      # стартовый рейтинг
  k-factor: 32            # коэффициент K

season:
  length-days: 30         # длительность сезона в днях

match:
  countdown-seconds: 3    # отсчёт перед боем
  return-to-origin: true  # вернуть игроков на исходное место
  keep-inventory: true    # сохранять инвентарь в бою

# Награды топ-3 сезона (выдаются автоматически в конце сезона)
season-rewards:
  1:
    lines: []             # строки описания (видны в /top и в анонсе)
    commands: []          # команды выдачи (%name%, %elo%, %place%)
  2:
    lines: []
    commands: []
  3:
    lines: []
    commands: []
```

Все текстовые сообщения лежат в `messages.yml` и поддерживают **MiniMessage** (градиенты, HEX, `<bold>` и т.д.).

---

## 🧭 Как это работает

1. Игрок надевает полный сет брони (незерит/алмаз) и открывает `/pvp`.
2. Нажимает «Начать поиск» — попадает в очередь, видит boss-bar, сервер получает анонс.
3. Если во время поиска изменить броню — появляются титулы-предупреждения (10 секунд на восстановление), иначе поиск отменяется.
4. При нахождении соперника того же уровня брони оба телепортируются на арену в 2000–5000 блоках.
5. После отсчёта включается PvP-режим, бой идёт до победы.
6. По итогу боя пересчитывается ELO, обновляется статистика и объявляется результат на весь сервер.
7. Когда сезон заканчивается, объявляются победители топ-3 и выдаются настроенные награды, после чего стартует новый сезон.

---

## 📄 Лицензия

Проект распространяется под лицензией [MIT](LICENSE).

Сделано студией **AuraCode** (IgMacla) с ❤️.
