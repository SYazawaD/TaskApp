package com.yuzawa.shota.techacademy.jp

import io.realm.RealmList
import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task : RealmObject() {
    var title: String = ""      // タイトル
    var category: Category? = null  //カテゴリ
//    var category: String = ""   // カテゴリ
    var contents: String = ""   // 内容
    var date: Date = Date()     // 日時

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}