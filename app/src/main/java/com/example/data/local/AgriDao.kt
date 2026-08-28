package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgriDao {

    // User Profile
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE user_profiles SET ecoPoints = :points WHERE id = :userId")
    suspend fun updateUserEcoPoints(userId: String, points: Int)

    // Notifications
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    // Equipment
    @Query("SELECT * FROM equipment_items")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(item: EquipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEquipment(items: List<EquipmentEntity>)

    @Update
    suspend fun updateEquipment(item: EquipmentEntity)

    @Query("SELECT * FROM equipment_items WHERE id = :id")
    suspend fun getEquipmentById(id: String): EquipmentEntity?

    @Query("UPDATE equipment_items SET isRentedCurrently = :isRented, currentRenterName = :renterName, daysRemaining = :days WHERE id = :id")
    suspend fun updateEquipmentRentalStatus(id: String, isRented: Boolean, renterName: String, days: Int)

    // Produce
    @Query("SELECT * FROM produce_items")
    fun getAllProduce(): Flow<List<ProduceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduce(item: ProduceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProduce(items: List<ProduceEntity>)

    @Query("UPDATE produce_items SET availableStock = :newStock WHERE id = :id")
    suspend fun updateProduceStock(id: String, newStock: Int)

    // Compost
    @Query("SELECT * FROM compost_items")
    fun getAllCompost(): Flow<List<CompostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompost(item: CompostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCompost(items: List<CompostEntity>)

    @Query("UPDATE compost_items SET volumeAvailable = :newVolume WHERE id = :id")
    suspend fun updateCompostVolume(id: String, newVolume: Int)

    // Waste Requests
    @Query("SELECT * FROM waste_requests ORDER BY createdAt DESC, pickupDate DESC")
    fun getAllWasteRequests(): Flow<List<WasteRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteRequest(request: WasteRequestEntity)

    @Update
    suspend fun updateWasteRequest(request: WasteRequestEntity)

    @Query("UPDATE waste_requests SET status = :status WHERE id = :id")
    suspend fun updateWasteRequestStatus(id: String, status: String)

    @Query("DELETE FROM waste_requests WHERE id = :id")
    suspend fun deleteWasteRequest(id: String)

    // Rental Contracts
    @Query("SELECT * FROM rental_contracts ORDER BY startDate DESC")
    fun getAllContracts(): Flow<List<RentalContractEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: RentalContractEntity)

    @Update
    suspend fun updateContract(contract: RentalContractEntity)

    @Query("UPDATE rental_contracts SET status = :status WHERE id = :id")
    suspend fun updateContractStatus(id: String, status: String)

    // Payments
    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity)

    // Forum
    @Query("SELECT * FROM forum_posts")
    fun getAllForumPosts(): Flow<List<ForumPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllForumPosts(posts: List<ForumPostEntity>)
}
