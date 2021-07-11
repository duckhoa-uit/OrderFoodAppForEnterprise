package com.example.orderfoodappforenterprise

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
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
import com.anychart.graphics.vector.Stroke
import com.example.orderfoodappforenterprise.model.DishIncome
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_analyze.*
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
                R.id.add_food -> Toast.makeText(applicationContext,"Add food", Toast.LENGTH_SHORT).show()
                R.id.home_page -> Toast.makeText(applicationContext,"Home page", Toast.LENGTH_SHORT).show()
                R.id.edit_profile -> Toast.makeText(applicationContext,"Edit profile", Toast.LENGTH_SHORT).show()
                R.id.sign_out -> Toast.makeText(applicationContext,"Sign out", Toast.LENGTH_SHORT).show()
                R.id.statistical -> Toast.makeText(applicationContext,"Statistical", Toast.LENGTH_SHORT).show()
            }
            true
        }
        menu_button_analyze.setOnClickListener {
            drawerLayout_analyze.openDrawer(GravityCompat.START)
        }

        //init data
        providerId = intent.getStringExtra("providerId").toString()
        dishesId = intent.getStringArrayListExtra("dishesId")!!
        cartesian = AnyChart.line()

        setInitData()
        loadDateToButton()
        loadChartForTheFirstTime()

        GlobalScope.launch {
            seriesData = async { reloadMonthChartSeries() }.await()
            delay(1000)
            seriesData.forEach {
                if (it.getValue("value").toString().toDouble() == 0.0){
                    seriesData.remove(it)
                }
            }
            loadCartesianSeriesData()
            loadingDialog.cancelLoadingDialog()
        }

        println("seriesData size: ${seriesData.size}")
        //event listener
        btnFromDate.setOnClickListener {
            showDatePickerDialog(btnFromDate, loadingDialog)
        }
        btnToDate.setOnClickListener {
            showDatePickerDialog(btnToDate, loadingDialog)
        }
    }

    suspend fun reloadLineChart() = coroutineScope{
        GlobalScope.launch {
            seriesData = async { reloadMonthChartSeries() }.await()
            delay(1000)
            seriesData.forEach {
                if (it.getValue("value").toString().toDouble() == 0.0){
                    seriesData.remove(it)
                }
            }
            loadCartesianSeriesData()
        }
    }

    fun showDatePickerDialog(btn: Button, loadingDialog: LoadingDialog){
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

    fun setInitData(){
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
        cartesian.removeAllSeries()
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
                        println(childBranch.child("time").value.toString())

                        seriesData.add(ValueDataEntry(childBranch.child("time").value.toString(), 0))
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
        val series: Line = cartesian.line(seriesData)
        series.name("Income")
        series.hovered().markers().enabled(true)
        series.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
    }
    private fun loadChartForTheFirstTime(){
        cartesian.xScroller(true);
        val xZoom = cartesian.xZoom()
        xZoom.setToPointsCount(6, false, null)
        xZoom.getStartRatio()
        xZoom.getEndRatio()
        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)
        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title("Analyze income from ${btnFromDate.text} to ${btnToDate.text}.")
        cartesian.yAxis(0).title("Income")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)
        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        lineChart.setChart(cartesian)

    }


}

