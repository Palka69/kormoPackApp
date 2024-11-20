package com.example.kormopack.navFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kormopack.databinding.FragmentPersonalCabinetBinding
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import com.example.kormopack.NIGHT
import com.example.kormopack.PREF_NAME
import com.example.kormopack.PREF_PIB
import com.example.kormopack.R
import com.example.kormopack.activityCallbackInterfaces.DrawerCallback
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.authorizationFragment.GoogleAccountViewModel
import com.example.kormopack.utils.NetworkChangeReceiver
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.GeneralSecurityException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

class PersonalCabinetFragment : Fragment() {
    private var _binding: FragmentPersonalCabinetBinding? = null
    private val binding get() = _binding!!
    private var account: GoogleSignInAccount? = null

    private val APPLICATION_NAME = "Google Sheets API Android Quickstart"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
    private val TAG = "Korm28"
    private var job: Job? = null
    private var drawerCallback: DrawerCallback? = null
    private var toolbarCallback: ToolbarCallback? = null

    private var cachedValues: List<List<Any>>? = null

    private val sharedViewModel: GoogleAccountViewModel by activityViewModels()

    private lateinit var userName: String

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    private var dayHour = 0f
    private var nightHour = 0f

    private var hourMap = mutableMapOf<Int, String>()
    private var bonusMap = mutableMapOf<Int, Float>()
    private var workDays = mutableListOf<MutableList<Any>>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DrawerCallback) {
            drawerCallback = context
        } else {
            throw RuntimeException("$context must implement DrawerCallback")
        }
        if (context is ToolbarCallback) {
            toolbarCallback = context
        } else {
            throw RuntimeException("$context must implement ToolbarCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        drawerCallback = null
        toolbarCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalCabinetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(networkChangeReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        job?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerCallback?.unlockDrawer()
        toolbarCallback?.changeToolbarColor(0)
        toolbarCallback?.showToolbar()

        userName = activity?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.getString(PREF_PIB, "NULL").toString()
        drawerCallback?.renameDrawerUser(userName ?: "NULL")

        sharedViewModel.sharedAccount.observe(viewLifecycleOwner) { sharedAccount ->
            Log.d("AccountGTest", "1 $account")

            handleSignInResult(sharedAccount)
        }
        account = GoogleSignIn.getLastSignedInAccount(requireContext())

        networkChangeReceiver = NetworkChangeReceiver (requireContext()) {
            if (networkChangeReceiver.isNetworkAvailable()) {
                fetchDataFromApi()
            }
        }
    }

    private fun PIBexit() {
        if (binding.dayCard.findViewById<PieChart>(R.id.day_chart).centerText == "0г") {
            Toast.makeText(requireContext(), "Графік для даного ПІБ не знайдено.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
        credential.selectedAccount = account.account

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
                val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()

                val spreadsheetId = "1kinvYJDC8WS4kV8v_VFFljNeb0i3wuxZdAR4BfCemSQ"
                val range = "Class!A1:F"
                val response = service.spreadsheets().values()[spreadsheetId, range].execute()
                val values = response.getValues()

                withContext(Dispatchers.Main) {
                    if (values == null || values.isEmpty()) {
                        Log.d(TAG, "No data found.")
                    } else {
                        val text = "$values and my pib is ${activity?.getSharedPreferences(
                            PREF_NAME,
                            Context.MODE_PRIVATE)?.getString(PREF_PIB, "нема")}"
                        if (isAdded && _binding != null) {
                            Log.d("resLog", "$values")
                        } else {
                            Log.d("TTTt", "Ex")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error 1: $e")
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, "Error 2: $e")
            }
        }
    }

    private fun initCalendar() {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        Log.d("TestOfDays", "Start Date: ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}")

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        Log.d("TestOfDays", "End Date: ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}")

        workDays = initWorkDay(cachedValues)

        binding.calendar.addDecorator(DayColorDecorator(startDate, endDate))
        binding.calendar.addDecorator(NightColorDecorator(startDate, endDate))

        initDayChart()
    }


    inner class DayColorDecorator(
        private val startDate: CalendarDay,
        private val endDate: CalendarDay,
    ) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {

            if (cachedValues != null && (day.isAfter(startDate)
                        && day.isBefore(endDate) || day == startDate || day == endDate)) {

                for ((index, list) in workDays.withIndex()) {
                    if (list.size > 0 && list[0] == day.day && list[1] == "День") {
                        hourMap.put(list[0].toString().toInt(), list[3].toString())
                        Log.d("BonusTest", "$list")
                        Log.d("BonusTest", "День: ${day.day} - ${list[2].toString().toFloat()}")
                        bonusMap.put(day.day, list[2].toString().toFloat())
                        return true
                    }
                }
            }

            binding.calendarCard.visibility = View.VISIBLE
            binding.dayCard.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
            Log.d("DayChartTest", "${binding.dayCard.findViewById<PieChart>(R.id.day_chart)}")
            return false
        }

        override fun decorate(view: DayViewFacade) {
            ContextCompat.getDrawable(requireContext(), R.drawable.green_background)?.let {
                view.setBackgroundDrawable(it)
            }
        }
    }

    inner class NightColorDecorator(
        private val startDate: CalendarDay,
        private val endDate: CalendarDay,
    ) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {

            Log.d("TestV", "${cachedValues}")

            if (cachedValues != null && (day.isAfter(startDate)
                        && day.isBefore(endDate) || day == startDate || day == endDate)) {
                for ((index, list) in workDays.withIndex()) {
                    if (list.size > 0 && list[0] == day.day && list[1] == "Ніч") {
                        hourMap.put(list[0].toString().toInt(), list[3].toString())
                        Log.d("BonusTest", "Ніч: ${day.day} - ${list[2].toString().toFloat()}")
                        bonusMap.put(day.day, list[2].toString().toFloat())
                        return true
                    }
                }
            }

            binding.calendarCard.visibility = View.VISIBLE
            binding.dayCard.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
            Log.d("DayChartTest", "${binding.dayCard.findViewById<PieChart>(R.id.day_chart)}")
            return false
        }

        override fun decorate(view: DayViewFacade) {

            ContextCompat.getDrawable(requireContext(), R.drawable.blue_background)?.let {
                view.setBackgroundDrawable(it)
            }
        }
    }

    private fun soDecorate(
        list: List<Any>,
        day: CalendarDay,
        index: Int,
        cachedValues: List<List<Any>>
    ): Boolean {

        if (list.size != 0) {
            if (list[8].toString().contains(pibReg(userName))) {
                if (list[0] == "") {
                    Log.d("VVVssd", "${cachedValues[index - 1][0]} ${list[8]} ${list[26]}")
                    if (day.day == stringToCalendarDay(cachedValues[index - 1][0].toString())?.day) {
                        hourMap.put(day.day, list[1].toString())
                        bonusMap.put(day.day, list[26].toString().replace(",", ".").toFloat())
                        return true
                        Log.d("dateTestsss", "true")
                    }
                } else {
                    Log.d("VVVssd", "${list[0]} ${list[8]} ${list[26]}")
                    if (day.day == stringToCalendarDay(list[0].toString())?.day) {
                        hourMap.put(day.day, list[1].toString())
                        bonusMap.put(day.day, list[26].toString().replace(",", ".").toFloat())
                        return true
                        Log.d("dateTestsss", "true")
                    }
                }
            }
        }

        return false
    }

    private fun fetchDataFromApi() {

        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
        credential.selectedAccount = account?.account

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
                val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()

                val spreadsheetId = "1ErfWPiMF3bXh97FFfB1i7Eb6exdSG50I38EvTTdph7M"

                val calendar = Calendar.getInstance()
                val listMonth = getMyMonth(calendar.get(Calendar.MONTH) + 1)
                var currentMonth = ""
                val currentYear = calendar.get(Calendar.YEAR).toString()

                for (month in listMonth) {
                    if (sheetExists(service, spreadsheetId, "$month $currentYear")) {
                        currentMonth = month
                        break
                    }
                }

                val range = "$currentMonth $currentYear!A2:AA"
                val response = service.spreadsheets().values()[spreadsheetId, range].execute()
                val values = response.getValues()

                cachedValues = values

                withContext(Dispatchers.Main) {

                    Log.d("resLog", "Values2: $values")

                    initCalendar()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error fetching data: $e")
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, "Security error: $e")
            }
        }
    }

    private fun getMyMonth(get: Int): List<String> {
        val monthMap = mapOf<Int, String>(1 to "січень",2 to "лютий",3 to "березень",4 to "квітень",5 to "травень",6 to "червень",
            7 to "липень",8 to "серпень",9 to "вересень",10 to "жовтень",11 to "листопад",12 to "грудень")

        val myMonth = monthMap[get]

        return if (myMonth != null) {
            listOf(
                myMonth,
                myMonth.uppercase(),
                myMonth.replaceFirstChar { it.uppercaseChar() }
            )
        } else {
            emptyList()
        }
    }

    private fun sheetExists(service: Sheets, spreadsheetId: String, sheetName: String): Boolean {
        return try {
            val spreadsheet = service.spreadsheets().get(spreadsheetId).execute()
            val sheetNames = spreadsheet.sheets.map { it.properties.title }
            sheetNames.contains(sheetName)
        } catch (e: IOException) {
            Log.e(TAG, "Error checking if sheet exists: ${e.message}", e)
            false
        }
    }
    inner class IntegerValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    private fun initDayChart() {
        binding.dayChart.isRotationEnabled = false
        val lastDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        Log.d("lastOfMonth", "$lastDay")
        Log.d("sharedNight", "1 ${drawerCallback?.getSharPref()?.getBoolean(NIGHT, false)}")
        if (drawerCallback?.getSharPref()?.contains(NIGHT) == false) {
            drawerCallback?.getSharPref().also {
                it?.edit { putBoolean(NIGHT, false) }
            }
        }
        Log.d("sharedNight", "2 ${drawerCallback?.getSharPref()?.getBoolean(NIGHT, false)}")
        if (drawerCallback?.getSharPref()?.getBoolean(NIGHT, false) == true) {
            nightHour += 8
            drawerCallback?.getSharPref().also {
                it?.edit { putBoolean(NIGHT, false) }
            }
        }

        dayHour = 0f
        nightHour = 0f
        for ((index, list) in workDays.withIndex()) {
            if (list.size > 0) {
                if (list[1] == "День") {
                    dayHour += list[3].toString().toInt()
                } else {
                    nightHour += list[3].toString().toInt()
                }
            }
        }

        var bonus = 0f
        for (value in bonusMap.values) {
            if (value >= 1f) bonus += (round(value * 100) / 100)
        }
        binding.dayChart.centerText = "${(dayHour + nightHour).toInt()}г"
        val entry = ArrayList<PieEntry>()
        entry.add(PieEntry(nightHour, "Нічні"))
        entry.add(PieEntry(dayHour, "Денні"))
        val dataSet = PieDataSet(entry, null)
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.kormotech_light_blue),
            ContextCompat.getColor(requireContext(), R.color.kormotech_green),
        )
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(IntegerValueFormatter())
        binding.dayChart.data = pieData
        binding.dayChart.description.textSize = 19f
        binding.dayChart.holeRadius = 34f
        binding.dayChart.transparentCircleRadius = 38f
        binding.dayChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.kormoTech_blue)
        binding.dayChart.description.text = "Бонус: $bonus%"
        binding.dayChart.setCenterTextSize(25f)
        dataSet.valueTextSize = 18f
        dataSet.sliceSpace = 1f
        binding.dayChart.notifyDataSetChanged()
        binding.dayChart.invalidate()
        PIBexit()
    }

    private fun pibReg(username: String): String {
        val userList = username.split(" ")
        Log.d("PibTest", "${userList[0]} ${userList[1][0]}.")
        return "${userList[0]} ${userList[1][0]}."
    }

    private fun stringToCalendarDay(dateString: String): CalendarDay? {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun initWorkDay(cash: List<List<Any>>?): MutableList<MutableList<Any>> {
        val resList = mutableListOf<MutableList<Any>>()

        for ((index, list) in cash!!.withIndex()) {
            if (index >= 65) {
                break
            }
            val tempList = mutableListOf<Any>()
            if (list[8].toString().contains(pibReg(userName))) {
                if (list[0].toString().isNotEmpty()) {
                    Log.d("adadsdaast4", "${list[0]} --- ${list[1]}")
                    tempList.add(getDayOfMonth(list[0].toString()))
                    tempList.add(list[1].toString())

                    // Check if the value is a valid number
                    Log.d("BonusTestTwo", list[25].toString())
                    val bonusValue = list[25].toString().trim().replace(",", ".")
                    Log.d("BonusTestTwo", "1 $bonusValue")
                    val bonus = try {
                        bonusValue.toDouble() / 100f
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Invalid float format: $bonusValue", e)
                        0f
                    }
                    Log.d("BonusTestTwo", "2 $bonus")
                    tempList.add(bonus)
                    tempList.add(12)
                } else {
                    val lastDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                    tempList.add(getDayOfMonth(cash[index - 1][0].toString()))
                    tempList.add(list[1].toString())

                    // Check if the value is a valid number
                    val bonusValue = list[25].toString().replace(",", ".")
                    val bonus = try {
                        bonusValue.toDouble() / 100f
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Invalid float format: $bonusValue", e)
                        0f
                    }
                    tempList.add(bonus)
                    if (tempList[0] != lastDay) {
                        tempList.add(12)
                    } else {
                        tempList.add(4)
                    }
                }
            }
            resList.add(tempList)
        }

        return resList
    }


    private fun getDayOfMonth(dateString: String): Int {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val date = dateFormat.parse(dateString)

        return date?.let {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.DAY_OF_MONTH)
        } ?: 1
    }
}