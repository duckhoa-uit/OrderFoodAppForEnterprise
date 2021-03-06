package com.example.orderfoodappforenterprise.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.example.orderfoodappforenterprise.LoadingDialog
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.model.DishIncome
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_analyze.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class AnalyzeActivity : AppCompatActivity() {
    private var year = arrayOf(2022, 2021, 2020)
    private var monthOptions = arrayOf("All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private var dayOptions = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28)
    lateinit var providerId: String
    lateinit var dishesId: ArrayList<String>

    private var dishIncomes = mutableListOf<DishIncome>()
    var seriesData: MutableList<DataEntry> = ArrayList()

    private lateinit var cartesian: Cartesian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze)

        val loadingDialog = LoadingDialog(this)
        loadingDialog.startLoadingDialog()

        //init navigation bar
        navView_analyze.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_page -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.add_food -> startActivity(Intent(this, AddFoodActivity::class.java))
                R.id.edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
                R.id.inbox -> startActivity(Intent(this, ChatActivity::class.java))
                R.id.sign_out -> {
                    Firebase.auth.signOut()
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    Toast.makeText(applicationContext, "Sign out", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        menu_button_analyze.setOnClickListener {
            drawerLayout_analyze.openDrawer(GravityCompat.START)
        }

        //init data
//        providerId = intent.getStringExtra("providerId").toString()
//        dishesId = intent.getStringArrayListExtra("dishesId")!!

        providerId = MainActivity.KotlinConstantClass.PROVIDER_ID
        dishesId = MainActivity.KotlinConstantClass.DISHES_ID

        println("MainActivity.KotlinConstantClass.PROVIDER_ID: " + MainActivity.KotlinConstantClass.PROVIDER_ID)
        println("MainActivity.KotlinConstantClass.DISHES_ID size: " + MainActivity.KotlinConstantClass.DISHES_ID.size)
        setInitData()
        loadDateToButton()
        loadChartForTheFirstTime()

        GlobalScope.launch(Dispatchers.Default) {
            val data =
                withContext(Dispatchers.Default) { reloadMonthChartSeries() }
            delay(1000)
            if (seriesData.size >= 1){
                seriesData.removeIf {
                    it.getValue("value").toString().toDouble() == 0.0
                }
            }
            loadCartesianSeriesData()
            loadingDialog.cancelLoadingDialog()
        }

        //event listener
        btnFromDate.setOnClickListener {
            showDatePickerDialog(btnFromDate, loadingDialog)
        }
        btnToDate.setOnClickListener {
            showDatePickerDialog(btnToDate, loadingDialog)
        }
    }

    private suspend fun reloadLineChart() = coroutineScope{
        GlobalScope.launch {
            seriesData.removeAll(seriesData)
            val data = async { reloadMonthChartSeries() }.await()
            delay(1000)
            if (seriesData.size >= 1){
                seriesData.removeAll {
                    it.getValue("value").toString().toDouble() == 0.0
                }
            }
            loadCartesianSeriesData()
        }
    }

    private fun showDatePickerDialog(btn: Button, loadingDialog: LoadingDialog){
        val arr = btn.text.toString().split("/")
        val day = arr[0].toInt()
        val month = arr[1].toInt()
        val year = arr[2].toInt()
        val dateSetListener =
            OnDateSetListener { _, mYear, monthOfYear, dayOfMonth ->
                loadingDialog.showLoading()
                GlobalScope.launch {
                    val currentDate = "${dayOfMonth}/${monthOfYear + 1}/${mYear}"
                    btn.text = currentDate

                    //reload chart
                    val reloadChart = async { reloadLineChart() }.await()
                    delay(1000)
                    loadingDialog.cancelLoadingDialog()
                }
            }

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener, year, month - 1, day
        )
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF8526"))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF8526"))
    }

    fun loadDateToButton(){
        val c = Calendar.getInstance()

        val today = Date()
        c.time = today
        var year = c.get(Calendar.YEAR)
        var month = c.get(Calendar.MONTH) + 1
        var day = c.get(Calendar.DAY_OF_MONTH)
        val currentDate = "${day}/${month}/${year}"
        btnToDate.text = currentDate

        val oneMonthAgo = Date(today.time - (1000 * 60 * 60 * 24) * 20 - (1000 * 60 * 60 * 24) * 10)
        c.time = oneMonthAgo
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH) + 1
        day = c.get(Calendar.DAY_OF_MONTH)
        val oneMonthAgoDate = "${day}/${month}/${year}"
        btnFromDate.text = oneMonthAgoDate
    }

    private fun setInitData(){
        dishesId.forEach {
            dishIncomes.add(DishIncome(
                it,
                0.0,
                0.0,
                0.0,
                0.0,
                0,
                0,
                0
            ))
        }

    }

    fun loadDishes(){
        val dbRef = FirebaseDatabase.getInstance().getReference("Product")

        dbRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                dishesId.removeAll(dishesId)
                dishIncomes.removeAll(dishIncomes)

                for(childBranch in snapshot.children){
                    if (childBranch.child("provider").value.toString() == providerId){
                        dishIncomes.add(
                            DishIncome(
                                childBranch.child("id").value.toString(),
                                0.0,
                                0.0,
                                0.0,
                                0.0,
                                0,
                                0,
                                0,
                            )
                        )
                        dishesId.add(
                            childBranch.child("id").value.toString()
                        )
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun resetIncomes(){
        for(income in dishIncomes){
            income.totalIncome = 0.0
            income.SInCome = 0.0
            income.MIncome = 0.0
            income.LIncome = 0.0
            income.amountS = 0
            income.amountM = 0
            income.amountL = 0
        }
    }

    private suspend fun reloadMonthChartSeries(): MutableList<DataEntry>  = coroutineScope{
        resetIncomes()
        //remove series
        seriesData.removeAll(seriesData)

        //get date end and start
        var dateArr = btnFromDate.text.split("/")
        val fromDate = LocalDate.of(dateArr[2].toString().toInt(), dateArr[1].toString().toInt(), dateArr[0].toString().toInt()).toString()
        dateArr = btnToDate.text.split("/")
        val toDate = LocalDate.of(dateArr[2].toString().toInt(), dateArr[1].toString().toInt(), dateArr[0].toString().toInt()).toString()

        val dbRef = FirebaseDatabase.getInstance().getReference("Bill").orderByChild("time").startAt(fromDate).endAt(toDate)

        GlobalScope.launch {
            dbRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(childBranch in snapshot.children){
                        if (seriesData.find { it.getValue("x") == childBranch.child("time").value.toString() } == null){
                            seriesData.add(ValueDataEntry(childBranch.child("time").value.toString(), 0))
                        }
                    }
                    for(childBranch in snapshot.children){
                        println(childBranch.child("time").value.toString())
                        //check if bill is purchased
                        if (childBranch.child("status").value.toString() == "done"){
                            //get products reference of one bill
                            val ref = childBranch.child("products").ref

                            GlobalScope.launch(Dispatchers.IO) {
                                val income =  async { loadBillProductItems(ref, childBranch.child("time").value.toString()) }
                                income.await()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        println("reloadMonthChartSeries, size: ${seriesData.size}")
        seriesData.forEach {
            println("${it.getValue("x")}: ${it.getValue("value")}")
        }
        return@coroutineScope seriesData
    }

    private suspend fun loadBillProductItems(ref: DatabaseReference, date: String): Double = coroutineScope {
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(productChildBranch in snapshot.children){
                    //get dishId
                    val dishId = productChildBranch.child("id").value.toString()
                    if (dishesId.contains(dishId)){
                        val index = dishesId.indexOf(dishId)
                        val amount = productChildBranch.child("amount").value.toString().toInt()

                        when(productChildBranch.child("id").value.toString()){
                            "S" -> dishIncomes[index].amountS += amount
                            "M" -> dishIncomes[index].amountM += amount
                            "L" -> dishIncomes[index].amountL += amount
                        }
                        seriesData.forEach {
                            if (it.getValue("x") == date){
                                it.setValue("value", it.getValue("value").toString().toDouble() + productChildBranch.child("unitPrice").value.toString().toDouble())
                            }
                        }
                        dishIncomes[index].totalIncome += productChildBranch.child("unitPrice").value.toString().toFloat()
                        println("dishId $dishId, income: ${dishIncomes.sumOf { d -> d.totalIncome }}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return@coroutineScope dishIncomes.sumOf { d -> d.totalIncome }
    }

    private fun resetChart() {
        cartesian.removeAllSeries()
    }

    private fun loadCartesianSeriesData(){
        cartesian.removeAllSeries()
        val series: Line = cartesian.line(seriesData)
        series.name("Income")
        series.hovered().markers().enabled(true)
        series.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series.selected().markers().enabled(true)
        series.selected().markers().fill("#FF8526")
        series.selected().markers().stroke("#FF8526")
        series.selected().markers().type(MarkerType.CIRCLE).size(5)
        series.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
        series.stroke("2.5 #FF8526")

        reloadChartTitle()
    }
    private fun loadChartForTheFirstTime(){
        cartesian = AnyChart.line()

        cartesian.xScroller(true);
        val xZoom = cartesian.xZoom()
        xZoom.setToPointsCount(6, false, null)
        xZoom.getStartRatio()
        xZoom.getEndRatio()

        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        //custome tooltip
        val tooltip = cartesian.tooltip()
        tooltip.positionMode(TooltipPositionMode.POINT)
        tooltip.background().fill("#FF8526")
        tooltip.titleFormat("Date: {%x}")
        tooltip.valuePrefix("$")
        tooltip.fontSize(15)

        cartesian.title("Analyze total income from ${btnFromDate.text} to ${btnToDate.text}.").title().fontColor("#000000")
        cartesian.title().fontSize("20px")

        val yAxis = cartesian.yAxis(0)
        yAxis.title("Income ($)").title().fontColor("#000000")
        yAxis.title().fontSize("17px")
        yAxis.labels().fontColor("#000000")
        yAxis.labels().fontSize("14px")
        yAxis.labels().format("\${%value}")

        val xAxis = cartesian.xAxis(0)
        xAxis.labels().padding(5.0, 5.0, 5.0, 5.0)
        xAxis.labels().fontColor("#000000")
        xAxis.labels().fontSize("14px")

        cartesian.legend().enabled(true)
        cartesian.legend()
        cartesian.legend().selectable(false)
        cartesian.legend().fontSize(17.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        lineChart.setChart(cartesian)
    }

    fun reloadChartTitle(){
        cartesian.title("Analyze total income from ${btnFromDate.text} to ${btnToDate.text}.")
    }

}