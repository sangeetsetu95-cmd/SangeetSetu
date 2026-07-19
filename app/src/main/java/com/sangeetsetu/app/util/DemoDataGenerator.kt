package com.sangeetsetu.app.util

import android.util.Log
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.LocationData
import com.sangeetsetu.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DemoDataGenerator {
    private val db = FirebaseFirestore.getInstance()

    private val categories = listOf(
        "कथावाचक", "भजन गायक", "कीर्तन मंडली", "गायक", "डांसर", 
        "संगीत", "DJ", "एंकर", "ढोलक वादक", "तबला वादक", 
        "हारमोनियम वादक", "कीबोर्ड वादक", "शहनाई वादक", "साउंड इंजीनियर"
    )

    private val names = listOf(
        "Pandit Rahul Sharma", "Acharya Deepali Ji", "Sant Kabir Das Mandali",
        "Sadhvi Rashmi", "Kavi Deepak", "Swar Sagar Group", "DJ Vicky",
        "Master Shridhar", "Meera Bhajan Mandali", "Ustad Zakir",
        "Sadhvi Pragya", "Pandit Vishwas", "Radhe Radhe Mandali", "Sufi Brothers"
    )

    private val descriptions = listOf(
        "Expert with over 10 years of experience in traditional performances.",
        "Renowned artist known for soul-stirring devotional music.",
        "A group dedicated to preserving our rich cultural heritage.",
        "Award-winning performer available for weddings and festivals.",
        "Specialist in classical and semi-classical music genres.",
        "Spreading peace and spirituality through katha and bhajan."
    )

    private val images = listOf(
        "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=400",
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=400",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=400",
        "https://images.unsplash.com/photo-1527980965255-d3b416303d12?q=80&w=400",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=400",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=400"
    )

    suspend fun generateDemoArtists() {
        Log.d("DemoData", "Starting Demo Artist Generation...")
        val usersCollection = db.collection("users")
        
        var count = 0
        for (state in LocationData.states) {
            val districts = LocationData.stateDistricts[state] ?: listOf("Main District")
            
            // Create 3 artists per state to ensure variety
            repeat(3) { i ->
                val category = categories.random()
                val district = districts.random()
                val artistName = "${names.random()} (${state.take(3)})"
                val uid = "demo_artist_${state.replace(" ", "_").replace("-", "_")}_$i"
                
                val artist = User(
                    uid = uid,
                    name = artistName,
                    email = "${uid}@sangeetsetu.demo",
                    state = state,
                    district = district,
                    city = district,
                    category = category,
                    aboutMe = descriptions.random(),
                    photoUrl = images.random(),
                    userType = "Artist",
                    status = "active",
                    isVerified = (0..1).random() == 1,
                    rating = (40..50).random() / 10.0,
                    totalBookings = (10..200).random(),
                    registrationCompleted = true,
                    profileCompleted = true,
                    approvalStatus = "APPROVED"
                )
                
                usersCollection.document(uid).set(artist).await()
                count++
            }
        }
        Log.d("DemoData", "Finished! Generated $count demo artists across all states.")
    }

    suspend fun generateDemoCategories() {
        val catCollection = db.collection("categories")
        val categoryImages = mapOf(
            "कथावाचक" to "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?q=80&w=200",
            "भजन गायक" to "https://images.unsplash.com/photo-1544923246-77307dd654ca?q=80&w=200",
            "गायक" to "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=200",
            "DJ" to "https://images.unsplash.com/photo-1571266028243-e4733b0f0bb1?q=80&w=200",
            "डांसर" to "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=200",
            "संगीत" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=200"
        )

        categories.forEach { catName ->
            val id = catName.replace(" ", "_").lowercase()
            val category = Category(
                id = id,
                name = catName,
                imageUrl = categoryImages[catName] ?: "https://images.unsplash.com/photo-1514525253344-f8500071367c?q=80&w=200",
                isActive = true
            )
            catCollection.document(id).set(category).await()
        }
    }
}
