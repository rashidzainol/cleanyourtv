# Esolat JAKIM

Waktu solat for Malaysia on the TV: today's Subuh, Syuruk, Zohor, Asar, Maghrib and Isyak for the
JAKIM zone you choose, full screen, with the next prayer highlighted and a countdown to it. Each
prayer has its Arabic and Malay name. For the [Clean Your TV](../../README.md) launcher it declares
a [module card](../../docs/modules.md), so once you answer **Show card** its tile reads "Esolat
JAKIM" with a green glow; on any other Android TV home it is an ordinary app.

- **Data** comes from [MPT](https://mpt.i906.my), a community mirror of JAKIM's official e-solat
  times (no account or key). Each zone's times were verified to carry the matching JAKIM zone code.
- **The zone** is chosen on the TV: press OK on the times screen to open settings, pick negeri,
  then zon (SGR01 Hulu Langat, WLY01 Kuala Lumpur & Putrajaya, …, 56 zones). It is kept in the app,
  so no rebuild is needed to move.
- **The azan reminder** can be turned off in the same settings. When it is on, a small card and a
  notification appear when a prayer time arrives, and the app reschedules itself after each prayer,
  at boot and across month changes.
- **Times are shown in Malaysia time** whatever the TV's clock setting says.

## Permissions

- `INTERNET` — fetches the month's prayer times from MPT (mpt.i906.my), JAKIM's e-solat data; the
  last successful fetch is cached so the screen still works offline.
- `RECEIVE_BOOT_COMPLETED` — reschedules the prayer reminders after a reboot or an app update.
- `SCHEDULE_EXACT_ALARM` — fires the reminder at the exact prayer minute.
- `SYSTEM_ALERT_WINDOW` — shows the small azan card over what is playing (only with reminders on).

No telemetry, no ads, no other network access.
