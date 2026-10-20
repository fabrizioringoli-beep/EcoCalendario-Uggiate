package it.ecocalendario.uggiate

import android.Manifest
import android.app.*
import android.os.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

object Data {
    val it = Locale.ITALIAN
    val types = listOf("Umido" to "🥕", "Vetro e lattine" to "🍾", "Carta" to "📦", "Secco" to "🗑️", "Plastica" to "🧴", "Pannolini e PSA" to "👶")
    // La raccolta quindicinale va verificata sulle date ufficiali prima di attivarla.
    fun collections(day: LocalDate): List<String> = when(day.dayOfWeek) {
        DayOfWeek.MONDAY -> listOf("Pannolini e PSA")
        DayOfWeek.TUESDAY -> listOf("Umido")
        DayOfWeek.WEDNESDAY -> listOf("Vetro e lattine")
        DayOfWeek.THURSDAY -> listOf("Carta / Secco: controlla calendario ufficiale")
        DayOfWeek.FRIDAY -> listOf("Umido", "Plastica: controlla calendario ufficiale")
        else -> emptyList()
    }
    val official = "https://comune.uggiateconronago.co.it/notizie/3543923/calendario-raccolta-rifiuti-2026"
}

class MainActivity : Activity() {
    private lateinit var body: LinearLayout
    private val green = Color.rgb(4,117,70)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = green
        window.navigationBarColor = green
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 12)
        ReminderReceiver.schedule(this)
        home()
    }
    private fun bg(color:Int, radius:Float=22f): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius=radius*resources.displayMetrics.density }
    private fun text(s:String, size:Float=17f, bold:Boolean=false): TextView = TextView(this).apply { text=s; textSize=size; setTextColor(Color.rgb(35,49,39)); if(bold) setTypeface(null,Typeface.BOLD); setPadding(8,9,8,9) }
    private fun screen(title:String) {
        val scroll=ScrollView(this).apply { setBackgroundColor(Color.rgb(238,249,226)) }
        body=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,18,18,36) }
        scroll.addView(body)
        setContentView(scroll)
        val head=text("🌿  $title",26f,true).apply { setTextColor(green) }
        body.addView(head)
    }
    private fun card(title:String, subtitle:String, emoji:String, action:()->Unit) {
        val b=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(17,12,17,12); background=bg(Color.WHITE); elevation=5f }
        b.addView(text("$emoji   $title",20f,true)); b.addView(text(subtitle,14f));
        val p=LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=14; topMargin=5 }
        body.addView(b,p); b.setOnClickListener { action() }
    }
    private fun home() {
        screen("EcoCalendario")
        body.addView(text("🏡 Uggiate con Ronago\nUn paese più pulito!",20f,true))
        val tomorrow=LocalDate.now().plusDays(1)
        card("Domani: ${tomorrow.format(DateTimeFormatter.ofPattern("EEEE d MMMM",Data.it))}", Data.collections(tomorrow).joinToString(" • ").ifEmpty { "Nessuna raccolta ordinaria" },"🔔") { calendar() }
        card("Calendario", "Scopri i prossimi giorni di raccolta", "🗓️") { calendar() }
        card("Notifiche", "Promemoria alle 19:00 della sera precedente", "🔔") { notifications() }
        card("Ecocentri", "Ronago e Uggiate: orari e indicazioni", "♻️") { centers() }
        card("Dove lo butto?", "Cerca come differenziare i materiali", "🔎") { search() }
        body.addView(text("⚠️ Le raccolte quindicinali richiedono conferma sul calendario ufficiale.",14f))
    }
    private fun back() { card("← Torna alla home", "", "🏠") { home() } }
    private fun officialLink() { card("Apri il calendario del Comune", "Controlla le date quindicinali e le variazioni", "🌐") { startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse(Data.official))) } }
    private fun calendar() {
        screen("Calendario raccolta")
        val start=LocalDate.now()
        for(i in 0..20) { val d=start.plusDays(i.toLong()); card(d.format(DateTimeFormatter.ofPattern("EEEE d MMMM",Data.it)).replaceFirstChar { it.uppercase() },Data.collections(d).joinToString(" • ").ifEmpty { "Nessuna raccolta ordinaria" },"📅") {} }
        officialLink(); back()
    }
    private fun notifications() {
        screen("Notifiche")
        body.addView(text("🔔 Ogni sera alle 19:00 ti ricordiamo la raccolta del giorno dopo.\n\nEsporre i rifiuti non prima delle 20:30 e comunque entro le 5:00.",18f))
        card("Attiva promemoria", "Programma la prossima notifica sul telefono", "⏰") { ReminderReceiver.schedule(this); Toast.makeText(this,"Promemoria programmato",Toast.LENGTH_SHORT).show() }
        body.addView(text("Nota: per carta, secco e plastica la frequenza quindicinale non è ancora verificata: l'avviso rimanda al calendario ufficiale.",14f))
        back()
    }
    private fun centers() {
        screen("Ecocentri")
        card("Ronago – Via Selvamara", "Lunedì 9:30–11:30\nGiovedì 9:30–11:30\nSabato 9:00–12:00 e 14:00–16:00", "♻️") {}
        val winter=LocalDate.now().monthValue in listOf(10,11,12,1,2,3)
        val hours=if(winter) "Lunedì 14:00–17:00\nMartedì 13:00–17:00\nMercoledì 8:00–12:00\nVenerdì 13:00–17:00\nSabato 8:00–12:00 e 13:00–17:00" else "Lunedì 14:00–17:00\nMartedì 14:00–18:00\nMercoledì 8:00–12:00\nVenerdì 14:00–18:00\nSabato 8:00–12:00 e 14:00–18:00"
        card("Uggiate – Piazzale Terragni",(if(winter) "Orario invernale (ottobre–marzo)\n" else "Orario estivo (aprile–settembre)\n")+hours,"🌳") {}
        body.addView(text("Porta la tessera sanitaria. Verifica eventuali chiusure festive sul sito comunale.",15f)); officialLink(); back()
    }
    private fun search() {
        screen("Dove lo butto?")
        val edit=EditText(this).apply { hint="Cerca: bottiglia, carta, lattina..."; setSingleLine(true); background=bg(Color.WHITE); setPadding(20,20,20,20) }
        body.addView(edit)
        val result=text("",19f,true); body.addView(result)
        val entries=mapOf("bottiglia di plastica" to "Plastica 🧴", "bottiglia di vetro" to "Vetro e lattine 🍾", "lattina" to "Vetro e lattine 🍾", "giornale" to "Carta 📦", "scatola" to "Carta 📦", "avanzi" to "Umido 🥕", "buccia" to "Umido 🥕", "pannolino" to "Pannolini e PSA 👶", "flacone" to "Plastica 🧴")
        edit.addTextChangedListener(object:android.text.TextWatcher { override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}; override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) { val q=s.toString().lowercase().trim(); result.text=if(q.isBlank()) "Inserisci un materiale" else entries.filter { it.key.contains(q) }.entries.joinToString("\n") { "${it.key}: ${it.value}" }.ifEmpty { "Materiale non presente nel catalogo iniziale: verifica le istruzioni comunali." } }; override fun afterTextChanged(s:android.text.Editable?){} })
        body.addView(text("Catalogo iniziale indicativo: verifica sempre le istruzioni locali.",14f)); officialLink(); back()
    }
}

class ReminderReceiver: android.content.BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        if(intent.action==Intent.ACTION_BOOT_COMPLETED) { schedule(context); return }
        val tomorrow=LocalDate.now().plusDays(1)
        val types=Data.collections(tomorrow)
        if(types.isNotEmpty()) {
            val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel="raccolta"
            if(Build.VERSION.SDK_INT>=26) manager.createNotificationChannel(NotificationChannel(channel,"Promemoria raccolta",NotificationManager.IMPORTANCE_DEFAULT))
            val message="Domani: ${types.joinToString(", ")}. Prepara i rifiuti; esponili non prima delle 20:30."
            val n=Notification.Builder(context,channel).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("♻️ EcoCalendario – domani").setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setAutoCancel(true).build()
            try { manager.notify(101,n) } catch(_:SecurityException) {}
        }
        schedule(context)
    }
    companion object {
        fun schedule(context:Context) {
            val next=LocalDateTime.of(LocalDate.now(),LocalTime.of(19,0)).let { if(it.isAfter(LocalDateTime.now())) it else it.plusDays(1) }
            val millis=next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val pi=PendingIntent.getBroadcast(context,1,Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alarm=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,millis,pi)
        }
    }
}
class BootReceiver:android.content.BroadcastReceiver() { override fun onReceive(context:Context,intent:Intent) { if(intent.action==Intent.ACTION_BOOT_COMPLETED) ReminderReceiver.schedule(context) } }
