package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLog
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.Pledge
import com.example.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        Member::class,
        Donation::class,
        Expense::class,
        AuditLog::class,
        Transaction::class,
        Pledge::class,
        CommitteeMember::class
    ],
    version = 5,
    exportSchema = false
)
abstract class SocietyDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun donationDao(): DonationDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun transactionDao(): TransactionDao
    abstract fun pledgeDao(): PledgeDao
    abstract fun committeeMemberDao(): CommitteeMemberDao

    companion object {
        @Volatile
        private var INSTANCE: SocietyDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ): SocietyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SocietyDatabase::class.java,
                    "society_management_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInMemoryDatabase(context: Context): SocietyDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                SocietyDatabase::class.java
            ).allowMainThreadQueries().build()
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(database: SocietyDatabase) {
                // Database starts completely clean with 0 records. All default data removed per user request.
            }
        }
    }
}
