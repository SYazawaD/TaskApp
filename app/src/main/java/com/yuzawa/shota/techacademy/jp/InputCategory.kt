package com.yuzawa.shota.techacademy.jp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import io.realm.Realm

import kotlinx.android.synthetic.main.activity_input_category.*
import kotlinx.android.synthetic.main.content_input_category.*

class InputCategory : AppCompatActivity() {
    private lateinit var mRealm3: Realm
    private var mCategory2: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_category)
        setSupportActionBar(toolbar)

        category_add_button.setOnClickListener{
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            mCategory2 = realm.where(Category::class.java).equalTo("id", taskId).findFirst()

            if (mCategory2 == null) {
                // 新規作成の場合
                mCategory2 = Category()

                val taskRealmResults = realm.where(Category::class.java).findAll()

                val identifier: Int =
                    if (taskRealmResults.max("id") != null) {
                        taskRealmResults.max("id")!!.toInt() + 1
                    } else {
                        0
                    }
                mCategory2!!.id = identifier
            }

        val category = category_add_text.text.toString()

            mCategory2!!.category_for = category

            realm.copyToRealmOrUpdate(mCategory2!!)
            realm.commitTransaction()

            realm.close()

            //タスク作成画面に戻る
            finish()
//            val intent = Intent(this, InputActivity::class.java)
//            startActivity(intent)
        }
    }
 }

