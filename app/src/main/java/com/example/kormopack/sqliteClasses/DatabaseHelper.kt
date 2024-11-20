import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import com.example.kormopack.brandRecycleClasses.FeedBrand
import com.example.kormopack.feedRecycleClasses.FeedSpecs
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "specifications.db"
        private const val DB_VERSION = 1
    }

    private val dbPath: String = mContext.getDatabasePath(DB_NAME).path
    private var mDatabase: SQLiteDatabase? = null

    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    private fun checkDatabase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        return try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
            checkDB?.close()
            checkDB != null
        } catch (e: Exception) {
            false
        }
    }

    @Throws(IOException::class)
    private fun copyDatabase() {
        val input: InputStream = mContext.assets.open(DB_NAME)
        val output: OutputStream = FileOutputStream(dbPath)
        val buffer = ByteArray(1024)
        var length: Int
        while (input.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
        output.flush()
        output.close()
        input.close()
    }

    @Throws(IOException::class)
    fun openDatabase() {
        val dbExist = checkDatabase()
        if (!dbExist) {
            this.readableDatabase
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized
    override fun close() {
        mDatabase?.close()
        super.close()
    }

    fun getAllBrands(): List<String> {
        val resList = mutableListOf<String>()

        val db = this.readableDatabase

        val createQuery = "SELECT * FROM feed_brands"

        val cursor = db.rawQuery(createQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val brandName = cursor.getString(1)
                resList.add(brandName)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return resList
    }

    fun insertFeedBrand(feedBrand: FeedBrand) {
        val db = this.writableDatabase
        Log.d("MyDBTest", db.toString())

        val brandLogo = when (feedBrand.name) {
            "Club 4 Paws" -> 1
            "Мяу" -> 2
            "OptiMeal" -> 3
            "Розумний Вибір/АТБ" -> 4
            "Sjobogardens" -> 5
            else -> {6}
        }

        val contentValues = ContentValues().apply {
            put("name", feedBrand.name)
            put("brand_logo", brandLogo)
            put("cardBackColor", feedBrand.cardBackColor)
            put("textColor", feedBrand.textColor)
        }

        try {
            val result = db.insert("feed_brands", null, contentValues)
            if (result == -1L) {
                Log.e("DatabaseError", "Failed to insert data. ContentValues: $contentValues")
            } else {
                Log.d("DatabaseSuccess", "Successfully inserted data")
            }
        } catch (e: Exception) {
            Log.e("DatabaseException", "Exception occurred: ${e.message}")
        } finally {
            db.close()
        }

    }

    fun insertFeedSpec(feedSpec: FeedSpecs, brand: String) {
        val db = this.writableDatabase

        val contentValues = ContentValues().apply {
            put("spec_num", feedSpec.spec_num)
            put("recipe_num", feedSpec.recipe_num)
            put("feed_name", feedSpec.feed_name)
            put("total_weight", feedSpec.total_weight)
            put("pieces_perc", feedSpec.pieces_perc)
            put("sauce_perc", feedSpec.sauce_perc)
            put("addition_one_perc", feedSpec.addition_one_perc)
            put("addition_two_perc", feedSpec.addition_two_perc)
            put("matrix_type", feedSpec.matrix_type)
            put("reg_data", feedSpec.reg_data)
        }

        val table = when(brand) {
            "Club 4 Paws" -> "specifications_clubs_four_paws"
            "Мяу" -> "specifications_myau"
            "OptiMeal" -> "specifications_opti_meal"
            "Розумний Вибір/АТБ" -> "specifications_smart_choice"
            "Sjobogardens" -> "specifications_sjobogardens"
            else -> "specifications_${brand.split(" ").joinToString("_").lowercase()}"
        }

        Log.d("MyBrandTest","$brand\n" +
                "specifications_${brand.split(" ")}".lowercase())

        try {
            val result = db.insert(table, null, contentValues)
            if (result == -1L) {
                Log.e("DatabaseError", "Failed to insert data. ContentValues: $contentValues")
            } else {
                Toast.makeText(mContext, "Додано нову специфікацію $brand", Toast.LENGTH_SHORT).show()
                Log.d("DatabaseSuccess", "Successfully inserted data")
            }
        } catch (e: Exception) {
            Log.e("DatabaseException", "Exception occurred: ${e.message}")
        } finally {
            db.close()
        }
    }

    fun createNewFeedBrandTable(brandName: String) {
        val strArray = brandName.split(" ")
        val tableName = "specifications_" + strArray.joinToString("_") { it.lowercase() }

        val createTableQuery = """
        CREATE TABLE "$tableName" (
            "spec_id" INTEGER,
            "spec_num" TEXT NOT NULL UNIQUE,
            "recipe_num" INTEGER NOT NULL DEFAULT 0,
            "feed_name" TEXT NOT NULL UNIQUE,
            "total_weight" INTEGER NOT NULL,
            "pieces_perc" INTEGER NOT NULL,
            "sauce_perc" INTEGER NOT NULL,
            "addition_one_perc" INTEGER NOT NULL DEFAULT 0,
            "addition_two_perc" INTEGER NOT NULL DEFAULT 0,
            "matrix_type" TEXT NOT NULL,
            "reg_data" TEXT NOT NULL,
            PRIMARY KEY("spec_id")
        );
    """.trimIndent()

        val db = this.writableDatabase
        try {
            db.execSQL(createTableQuery)
            Log.d("DatabaseSuccess", "Successfully created table: $tableName")
        } catch (e: Exception) {
            Log.e("DatabaseError", "Failed to create table: $tableName. Exception: ${e.message}")
        } finally {
            db.close()
        }
    }
}
