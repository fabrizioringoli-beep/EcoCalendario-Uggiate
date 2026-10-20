# EcoCalendario Uggiate con Ronago — prototipo Android

App Android nativa in Kotlin con stile cartoon leggero, calendario, notifiche alle 19:00, ecocentri e ricerca materiali.

**Attenzione:** le date quindicinali di carta, secco e plastica NON sono state importate e verificate dal PDF ufficiale. L’app mostra esplicitamente l’avviso di consultare il calendario del Comune; non va considerata una fonte completa per questi ritiri. Gli orari ecocentri vanno ricontrollati prima dell’uso.

## Compilare senza PC
1. Da telefono crea un repository GitHub e carica i file di questo ZIP, mantenendo le cartelle.
2. Apri **Actions** nel repository e seleziona **Crea APK Android** → **Run workflow** (oppure avvia il workflow con un push sul ramo main).
3. Quando il workflow termina, apri il risultato e scarica l’artifact **EcoCalendario-APK**.
4. Estrai `app-debug.apk` e installalo sul telefono autorizzando l’installazione da quella fonte.

Il debug APK non è firmato per distribuzione Play Store. La compilazione non è stata testata in questa sessione.
