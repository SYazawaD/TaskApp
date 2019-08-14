package com.yuzawa.shota.techacademy.jp

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.content_input.*

const val EXTRA_TASK = "jp.techacademy.taro.kirameki.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private lateinit var mRealm2: Realm
    private lateinit var mRealm3: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // fab
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }
        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }
        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

//課題追加箇所　start
        //検索ボタン実行時
        category_search_on.setOnClickListener(){
            reloadListView2()
        }
        //全カテゴリ検索ボタン実行時
        category_search_reset.setOnClickListener(){
            reloadListView()
        }
//課題追加箇所　end

        reloadListView()
    }

    private fun reloadListView() {

        // Realmの設定
        mRealm = Realm.getDefaultInstance()

//課題追加箇所　start
        // Spinner設定用
        var spinnerItems = arrayListOf<String>()
        var spinnerItems2 = listOf<String>()
        val taskRealmResults2
                = mRealm.where(Category::class.java).findAll().sort("category_for", Sort.ASCENDING)

        if (taskRealmResults2.size != 0) {
            //カテゴリが1件でもある場合→Realmからカテゴリを取得
            var min:Int = taskRealmResults2.min("id").toString().toInt() //最小id取得
            var max:Int = taskRealmResults2.max("id").toString().toInt() //最大id取得

            //Categoryクラスのカテゴリ一覧を取得して、ArrayListに登録
            var i: Int = min
            while (i <= max) {
                var taskRealmResultsRecord = mRealm.where(Category::class.java).equalTo("id", i).findFirst()
                if (taskRealmResultsRecord!!.category_for.isNotEmpty()) {
                    spinnerItems.add(taskRealmResultsRecord!!.category_for)
                }
                i = i + 1
            }
        }else{
            //カテゴリが1件もない場合→下記メッセージを表示
            spinnerItems.add("カテゴリを追加してください")
        }
        //重複削除
        spinnerItems2 = (spinnerItems).distinct()

        //adapterの設定
        var adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, spinnerItems2)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category_search.adapter = adapter
//課題追加箇所　end

        //ListViewの表示設定
        val taskRealmResults = mRealm.where(Task::class.java)
            .findAll().sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
        mRealm.close()
    }

//課題追加箇所　start
    private fun reloadListView2() {
        mRealm = Realm.getDefaultInstance()
        var ids = category_search.getSelectedItemPosition();
        var taskRealmResults = mRealm.where(Category::class.java).equalTo("id", ids).findFirst()
        if (taskRealmResults?.category_for != null) {
            var categorySet = taskRealmResults.category_for
            var taskRealmResults3 = mRealm.where(Task::class.java).equalTo("category.category_for", categorySet).findAll()

            // 上記の結果を、TaskList としてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults3)
            // TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter
            // 表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()
            mRealm.close()
        }
    }
//課題追加箇所　end

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
}