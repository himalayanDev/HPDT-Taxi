package code_setup.net_.fcm_import android.annotation.SuppressLintimport android.app.Notificationimport android.app.NotificationChannelimport android.app.NotificationManagerimport android.app.PendingIntentimport android.content.ContentResolverimport android.content.Contextimport android.content.Intentimport android.graphics.BitmapFactoryimport android.graphics.Colorimport android.media.AudioAttributesimport android.media.RingtoneManagerimport android.net.Uriimport android.os.Buildimport android.os.SystemClockimport androidx.core.app.NotificationCompatimport android.util.Logimport code_setup.app_core.BaseApplicationimport code_setup.app_models.other_.NotificationModelimport code_setup.app_models.other_.event.CustomEventimport code_setup.app_models.other_.event.EVENTSimport code_setup.app_util.CommonValuesimport code_setup.app_util.DateUtilizerimport code_setup.app_util.Prefsimport code_setup.db_.AppDatabaseimport code_setup.db_.tour_notification.Notifiimport code_setup.ui_.home.views.HomeActivityimport com.electrovese.setup.Rimport com.google.firebase.messaging.FirebaseMessagingServiceimport com.google.firebase.messaging.RemoteMessageimport com.google.gson.Gsonimport com.google.gson.reflect.TypeTokenimport org.greenrobot.eventbus.EventBusimport org.json.JSONExceptionimport org.json.JSONObjectimport java.text.SimpleDateFormatimport java.util.*import javax.annotation.Nullable/** * Created by Electrovese on 17/2019. */public class MyFirebaseMessagingService : FirebaseMessagingService() {    private var notificationId: Int = System.currentTimeMillis().toInt()    private var notificationModel: NotificationModel = NotificationModel()    private val TAG = "Myfcm Service  HPDT TAXI USER"    private val notificationCount = 0    internal var notificationDesc = ""    internal var notificationModelArrayList = ArrayList<NotificationModel>()    var notificationList: MutableList<NotificationModel> = mutableListOf<NotificationModel>()    private val CHANNEL_ID = "com.hpdt.taxi"    private var mManager: NotificationManager? = null    override fun onMessageReceived(remoteMessage: RemoteMessage) {// Handle data payload of FCM messages.        Log.d(TAG, "FCM Data Message: " + remoteMessage.data)        val params = remoteMessage.data        Log.d(TAG, "FCM Message Id: " + remoteMessage.messageId!!)//        Log.d(TAG, "FCM Notification Message: " + remoteMessage.notification!!)        val data = JSONObject(params as Map<*, *>)        val msg = ""        Log.e("JSON_OBJECT", data.toString())        val date = System.currentTimeMillis()        val sdf = SimpleDateFormat("dd MMM yyyy")        val dateString = sdf.format(date)        val c = Calendar.getInstance()        val dateformat = SimpleDateFormat("hh:mm aa")        val datetime = dateformat.format(c.getTime())        Log.d(TAG, "Message data : " + remoteMessage.data)        try {            val messageText = data.getString("title")            val message = try {                data.getString("body")            } catch (e: Exception) {            }            try {                notificationId = data.getInt("notification_id").toInt()            } catch (e: Exception) {            }            val action_code = data.getString("action_code")            val title = data.getString("title")            var datxa = ""            try {                datxa = data.getString("data")            } catch (e: Exception) {                e.printStackTrace()            }            val appointment_id = "1"            notificationModel = NotificationModel(                datxa,                action_code,                appointment_id,                messageText,                message as String?,                datetime,                title            )            notificationList = loadSharedPreferencesLogList()            if (notificationList != null && notificationList.size > 0) {                notificationList.add(0, notificationModel)            } else {                notificationList.add(notificationModel)            }            saveSharedPreferencesLogList(notificationList)            Log.d("notification Json ", "  ==>  " + Gson().toJson(notificationModel))            /*send notification data to popup on Home  screen*/            if (action_code.equals("NEW_JOB"))                EventBus.getDefault().postSticky(                    CustomEvent<Any>(                        EVENTS.EVENT_NEW_JOB,                        notificationModel                    )                )            /*-------SAVE NOTIFICATION TO DATABASE-------------*///            var notificationDataBase = AppDatabase.getAppDatabase(this).notificationDao()//            var nData = Notifi()//            nData.notificationId = ""+notificationId // System.currentTimeMillis().toString()//            nData.notifictaionTime = DateUtilizer.getCurrentDate().time//            nData.notificatcionData = Gson().toJson(notificationModel)//            notificationDataBase.insertAll(nData)////            Log.d(TAG, "Notification DATABASE " + Gson().toJson(notificationDataBase.all))        } catch (e: JSONException) {            e.printStackTrace()        }//      Log.d(TAG, "Notification is " + Prefs.getBoolean(CommonValues.NOTIFICATION_STATUS, true))        showNotification(notificationModel)    }    private fun showNotification(notificationModel: NotificationModel) {        val intent = Intent(this@MyFirebaseMessagingService as Context, HomeActivity::class.java)        if (notificationModel.action_code.equals("NEW_JOB")) {            notificationId = CommonValues.NEW_JOB_MOTIFICATION_ID            intent.putExtra(CommonValues.IS_FROM_NOTIFICATION, true)            intent.putExtra(CommonValues.TOUR_DATA, notificationModel)            intent.putExtra(CommonValues.NOTIFICATION_ID, notificationId)        }        val contentIntent =            PendingIntent.getActivity(                this@MyFirebaseMessagingService as Context,                0,                intent,                PendingIntent.FLAG_UPDATE_CURRENT            )        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {            @SuppressLint("WrongConstant") val androidChannel = NotificationChannel(                CHANNEL_ID, notificationModel.title, NotificationCompat.PRIORITY_HIGH            )            // Sets whether notifications posted to this channel should display notification lights            androidChannel.enableLights(true)            // Sets whether notification posted to this channel should vibrate.            androidChannel.enableVibration(true)            // Sets the notification light color for notifications posted to this channel            androidChannel.lightColor = Color.GREEN            // Creating an Audio Attribute            val audioAttributes = AudioAttributes.Builder()                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)                .setUsage(AudioAttributes.USAGE_ALARM)                .build()            if (notificationModel.action_code.equals("NEW_JOB")) {                androidChannel.setVibrationPattern(                    longArrayOf(                        100,                        200,                        300,                        400,                        500,                        400,                        300,                        200,                        400                    )                )                androidChannel.enableVibration(true)                androidChannel.setSound(                    Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + R.raw.short_cute_tone),                    audioAttributes                )                try {                    val notification =                        Uri.parse("android.resource://" + packageName + "/" + R.raw.short_cute_tone)                    val r = RingtoneManager.getRingtone(applicationContext, notification)                    r.play()                } catch (e: Exception) {                    e.printStackTrace()                }            }            // Sets whether notifications posted to this channel appear on the lockscreen or not            androidChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE            getManager().createNotificationChannel(androidChannel)            val nb = Notification.Builder(BaseApplication.instance, CHANNEL_ID)                .setContentTitle(notificationModel.title)                .setContentText(notificationModel.body)                .setSmallIcon(getNotificationIcon())                //.setStyle(new Notification.BigTextStyle().bigText(body))                .setAutoCancel(true).setContentIntent(contentIntent)            getManager().notify(notificationId, nb.build())        } else {            try {                val inboxStyle = NotificationCompat.BigTextStyle()                val notificationBuilder =                    NotificationCompat.Builder(BaseApplication.instance)                        .setLargeIcon(                            BitmapFactory.decodeResource(                                BaseApplication.instance.resources,                                R.mipmap.ic_launcher                            )                        )                        .setSmallIcon(getNotificationIcon())                        .setStyle(                            androidx.core.app.NotificationCompat.BigTextStyle().bigText(                                notificationModel.body                            )                        ).setPriority(NotificationManager.IMPORTANCE_HIGH)                        .setContentTitle(notificationModel.title)                        .setContentText(notificationModel.body).setTicker(notificationModel.body)                        .setContentIntent(contentIntent)                        .setStyle(inboxStyle)                        .setLights(-0x89fe6d, 300, 1000)                        .setAutoCancel(true).setVibrate(longArrayOf(1000, 1000))                if (notificationModel.action_code.equals("NEW_JOB")) {                    notificationBuilder.setVibrate(                        longArrayOf(                            100,                            200,                            300,                            400,                            500,                            400,                            300,                            200,                            400                        )                    )                    notificationBuilder.setSound(                        Uri.parse(                            ContentResolver.SCHEME_ANDROID_RESOURCE                                    + "://" + getPackageName() + "/raw/short_cute_tone"                        )                    )                }                val notificationManager =                    BaseApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager                notificationManager.notify(                    notificationId                    /*System.currentTimeMillis().toInt()*/ /* ID of notification */,                    notificationBuilder.build()                )            } catch (se: SecurityException) {                se.printStackTrace()            }        }    }    private fun getManager(): NotificationManager {        if (mManager == null) {            mManager =                BaseApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager        }        return mManager as NotificationManager    }    private fun getNotificationIcon(): Int {        val useWhiteIcon =            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP        return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher    }    fun loadSharedPreferencesLogList(): ArrayList<NotificationModel> {        var callLog = ArrayList<NotificationModel>()        val gson = Gson()        val json = Prefs.getString("NotificationArrayList", "")        if (json!!.isEmpty()) {            callLog = ArrayList()        } else {            val type = object : TypeToken<List<NotificationModel>>() {            }.type            callLog = gson.fromJson(json, type)        }        return callLog    }    fun saveSharedPreferencesLogList(callLog: List<NotificationModel>) {        val gson = Gson()        val json = gson.toJson(callLog)        Prefs.putString("NotificationArrayList", json)    }}