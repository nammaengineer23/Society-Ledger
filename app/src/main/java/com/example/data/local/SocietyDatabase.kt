package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLog
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Donor
import com.example.data.model.Expense
import com.example.data.model.MaintenanceCharge
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
        CommitteeMember::class,
        MaintenanceCharge::class,
        Donor::class
    ],
    version = 8,
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
    abstract fun maintenanceChargeDao(): MaintenanceChargeDao
    abstract fun donorDao(): DonorDao

    companion object {
        @Volatile
        private var INSTANCE: SocietyDatabase? = null

        /**
         * Adds the maintenance ledger without touching existing member, donation,
         * expense, audit, transaction, pledge, or committee records.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS maintenance_charges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        flatNo TEXT NOT NULL,
                        memberId INTEGER NOT NULL,
                        memberName TEXT NOT NULL,
                        billingPeriod TEXT NOT NULL,
                        amount REAL NOT NULL,
                        dueDate INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        amountPaid REAL NOT NULL,
                        paymentMode TEXT NOT NULL,
                        paymentReference TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        recordedById INTEGER NOT NULL,
                        recordedByName TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_maintenance_charges_flatNo_billingPeriod ON maintenance_charges(flatNo, billingPeriod)"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS donors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        email TEXT NOT NULL,
                        societyId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_donors_name_phone_societyId ON donors(name, phone, societyId)")
                db.execSQL("""
                    INSERT INTO donors (name, phone, email, societyId, createdAt, updatedAt)
                    SELECT DISTINCT m.name, m.phone, m.email,
                           COALESCE(cm.societyId, ''), m.createdAt, m.createdAt
                    FROM members m
                    LEFT JOIN committee_members cm
                      ON lower(cm.name) = lower(m.name) AND cm.role = 'Resident Member'
                    WHERE m.role = 'Resident Member' AND trim(m.name) != ''
                """.trimIndent())
                // These rows were created by the old donation flow, not by committee management.
                db.execSQL("DELETE FROM committee_members WHERE role = 'Resident Member'")
                db.execSQL("DELETE FROM members WHERE role = 'Resident Member'")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN committeeMemberId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_committeeMemberId ON transactions(committeeMemberId)")
            }
        }

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
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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
