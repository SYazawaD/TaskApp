package com.yuzawa.shota.techacademy.jp

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Category : RealmObject(), Serializable {
    var category_for: String = ""   // カテゴリ
    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}