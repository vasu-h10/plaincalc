package org.vasu.plaincalc

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var display: TextView
    private var expression = ""
    private var evaluated = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(245,247,250)
        window.navigationBarColor = Color.rgb(245,247,250)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(22),dp(16),dp(16)); setBackgroundColor(Color.rgb(245,247,250)) }
        root.addView(TextView(this).apply { text="PlainCalc"; textSize=23f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.rgb(35,45,65)); gravity=Gravity.CENTER_VERTICAL }, LinearLayout.LayoutParams(-1,dp(48)))
        display = TextView(this).apply { text="0"; textSize=36f; setTextColor(Color.rgb(20,28,45)); gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL; maxLines=2; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        root.addView(display, LinearLayout.LayoutParams(-1,0,1f))
        val grid=GridLayout(this).apply { columnCount=4; rowCount=5 }
        listOf("AC","±","%","÷","7","8","9","×","4","5","6","−","1","2","3","+","⌫","0",".","=").forEach { key ->
            val op=key in listOf("÷","×","−","+","=")
            val b=Button(this).apply {
                text=key; textSize=20f; isAllCaps=false
                setTextColor(if(op) Color.WHITE else Color.rgb(35,45,65))
                backgroundTintList=ColorStateList.valueOf(if(op) Color.rgb(45,104,220) else if(key in listOf("AC","±","%","⌫")) Color.rgb(220,229,242) else Color.WHITE)
                setOnClickListener { press(key) }
            }
            grid.addView(b, GridLayout.LayoutParams().apply { width=0; height=dp(66); columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(dp(3),dp(3),dp(3),dp(3)) })
        }
        root.addView(grid,LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(root)
    }
    private fun press(k:String) {
        when(k) {
            "AC" -> { expression=""; display.text="0"; evaluated=false }
            "⌫" -> { expression=expression.dropLast(1); display.text=expression.ifEmpty{"0"} }
            "±" -> { val n=display.text.toString().replace("−","-").toDoubleOrNull(); if(n!=null){ expression=(-n).toString().removeSuffix(".0"); display.text=expression } }
            "%" -> { val n=display.text.toString().replace("−","-").toDoubleOrNull(); if(n!=null){ expression=(n/100.0).toString().removeSuffix(".0"); display.text=expression } }
            "=" -> try { val v=Parser(expression).parse(); expression=if(v%1.0==0.0) v.toLong().toString() else v.toString(); display.text=expression.replace("-","−"); evaluated=true } catch(_:Exception) { display.text="Error"; expression="" }
            else -> { if(evaluated && k.firstOrNull()?.isDigit()==true) expression=""; evaluated=false; expression+=when(k){"÷"->"/";"×"->"*";"−"->"-";else->k}; display.text=expression.replace("*","×").replace("/","÷").replace("-","−").ifEmpty{"0"} }
        }
    }
    private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
    private class Parser(private val s:String) {
        var p=0
        fun parse():Double { val v=expr(); ws(); if(p!=s.length||!v.isFinite()) error("bad"); return v }
        fun ws(){while(p<s.length&&s[p].isWhitespace())p++}
        fun eat(c:Char):Boolean { if(p<s.length&&s[p]==c){p++;return true};return false }
        fun expr():Double { var x=term(); while(true){ws(); if(eat('+'))x+=term() else if(eat('-'))x-=term() else return x} }
        fun term():Double { var x=factor(); while(true){ws(); if(eat('*'))x*=factor() else if(eat('/')){val y=factor();if(y==0.0)error("zero");x/=y}else return x} }
        fun factor():Double { ws(); if(eat('+'))return factor(); if(eat('-'))return -factor(); if(eat('(')){val x=expr();ws();if(!eat(')'))error("paren");return x}; val st=p;var dot=false;while(p<s.length&&(s[p].isDigit()||(s[p]=='.'&&!dot))){if(s[p]=='.')dot=true;p++};if(st==p)error("number");return s.substring(st,p).toDouble() }
    }
}
