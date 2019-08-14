package com.yuzawa.shota.techacademy.jp

import android.app.*
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.support.v4.app.NotificationCompat.getCategory
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.Sort
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null
    private lateinit var mRealm2: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView3()
        }
    }
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString =
                    mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

// 課題追加箇所　start
        // Spinnerの設定
        reloadListView3()
// 課題追加箇所　end

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        var intent = intent
        var taskId = intent.getIntExtra(EXTRA_TASK, -1)
        var realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
// 課題追加箇所　start
            //カテゴリのidを取得
            var ida = mTask!!.category?.id
            if (ida != null) {
                category_edit_text.setSelection(ida.toInt())
            }
// 課題追加箇所　end
            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString =
                mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
            realm.close()
        }
// 課題追加箇所　start
        input_category_button.setOnClickListener {
            val intent = Intent(this, InputCategory::class.java)
            startActivity(intent)
        }
        category_edit_text.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
// 課題追加箇所　end
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()

// カテゴリ設定箇所 start
        // 選択されているアイテムのIndexを取得
        mRealm2 = Realm.getDefaultInstance()
        var ids = category_edit_text.getSelectedItemPosition();
        var category = mRealm2.where(Category::class.java).equalTo("id", ids).findFirst()
        mRealm2.close()
// カテゴリ設定箇所 end

        mTask!!.title = title
        mTask!!.category  = category
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)

    }
// 課題追加箇所　start
    private fun reloadListView3(){
        // Realmの設定
        mRealm2 = Realm.getDefaultInstance()
        mRealm2.addChangeListener(mRealmListener)

        var spinnerItems = arrayListOf<String>()
        var spinnerItems2 = listOf<String>()
        var taskRealmResults = mRealm2.where(Category::class.java).findAll().sort("category_for", Sort.ASCENDING)

        if (taskRealmResults.size != 0)
        {
            //カテゴリが1件でもある場合
            done_button.setEnabled(true)
            var min: Int = taskRealmResults.min("id").toString().toInt() //最小id取得
            var max: Int = taskRealmResults.max("id").toString().toInt() //最大id取得

            var i: Int = min
            while (i <= max) {
                var taskRealmResultsRecord = mRealm2.where(Category::class.java).equalTo("id", i).findFirst()
                if (taskRealmResultsRecord!!.category_for.isNotEmpty()) {
                    spinnerItems.add(taskRealmResultsRecord!!.category_for)
                }
                i = i + 1
            }
        }else
        {
            //カテゴリが1件もない場合
            spinnerItems.add("カテゴリを追加してください")
            done_button.setEnabled(false)
        }
        spinnerItems2 = (spinnerItems).distinct()

        mRealm2.close()

        var adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, spinnerItems2)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category_edit_text.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm2.close()
     }
// 課題追加箇所　end
}
