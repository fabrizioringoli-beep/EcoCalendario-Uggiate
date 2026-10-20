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
            val message="Domani: ${types.joinToString(", ")}. Prepara i rifiuti e consulta le indicazioni comunali."
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
