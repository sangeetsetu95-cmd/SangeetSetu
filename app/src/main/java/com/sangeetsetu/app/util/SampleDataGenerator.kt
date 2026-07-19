package com.sangeetsetu.app.util

import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Random
import java.util.UUID

object SampleDataGenerator {

    private val firstNamesMale = listOf("Rahul", "Amit", "Sandeep", "Vijay", "Rajesh", "Sunil", "Pankaj", "Deepak", "Manoj", "Anil", "Vikram", "Suresh", "Ramesh", "Karan", "Arjun")
    private val firstNamesFemale = listOf("Priya", "Anjali", "Suman", "Kavita", "Pooja", "Ritu", "Neha", "Shweta", "Meena", "Aarti", "Sneha", "Kiran", "Jyoti", "Sapna", "Deepa")
    private val lastNames = listOf("Sharma", "Verma", "Gupta", "Singh", "Yadav", "Mishra", "Pandey", "Joshi", "Choudhary", "Mehta", "Trivedi", "Agrawal", "Soni", "Patel", "Khan")
    
    private val cities = listOf("Mathura", "Vrindavan", "Lucknow", "Jaipur", "Indore", "Bhopal", "Patna", "Delhi", "Mumbai", "Pune", "Ahmedabad", "Chandigarh", "Kanpur", "Varanasi", "Agra")
    private val states = mapOf(
        "Mathura" to "Uttar Pradesh", "Vrindavan" to "Uttar Pradesh",
        "Lucknow" to "Uttar Pradesh", "Kanpur" to "Uttar Pradesh", "Varanasi" to "Uttar Pradesh", "Agra" to "Uttar Pradesh",
        "Jaipur" to "Rajasthan", "Udaipur" to "Rajasthan",
        "Indore" to "Madhya Pradesh", "Bhopal" to "Madhya Pradesh", "Gwalior" to "Madhya Pradesh",
        "Patna" to "Bihar", "Delhi" to "Delhi", "Mumbai" to "Maharashtra", "Pune" to "Maharashtra",
        "Ahmedabad" to "Gujarat", "Chandigarh" to "Punjab"
    )

    private val categoryDetails = mapOf(
        "Singer (Male)" to "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=400",
        "Singer (Female)" to "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=400",
        "Tabla" to "https://images.unsplash.com/photo-1619983081563-430f63602796?q=80&w=400",
        "Harmonium" to "https://images.unsplash.com/photo-1605646194273-51984252f4c9?q=80&w=400",
        "Keyboard" to "https://images.unsplash.com/photo-1512733596533-7b00ccf8ebaf?q=80&w=400",
        "Narrator (Katha)" to "https://images.unsplash.com/photo-1583089892943-e02e5b017b6a?q=80&w=400",
        "Guitar" to "https://images.unsplash.com/photo-1525201548942-d8b8bb097fb3?q=80&w=400",
        "Dholak" to "https://images.unsplash.com/photo-1599839618126-d934325a7695?q=80&w=400",
        "DJ" to "https://images.unsplash.com/photo-1571266028243-3716f02d7d29?q=80&w=400",
        "Photographer" to "https://images.unsplash.com/photo-1492691523567-6172152e7eb6?q=80&w=400",
        "Sound System" to "https://images.unsplash.com/photo-1598653222000-6b7b7a552625?q=80&w=400",
        "Bhajan Mandali" to "https://images.unsplash.com/photo-1514334331481-b849301017e4?q=80&w=400",
        "Flute" to "https://images.unsplash.com/photo-1605333396915-47ed6b68a00e?q=80&w=400",
        "Orchestra" to "https://images.unsplash.com/photo-1465847899034-d174fc546f00?q=80&w=400",
        "Dance Group" to "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=400"
    )

    suspend fun generateAllSampleData() {
        val db = FirebaseFirestore.getInstance()
        val random = Random()
        // First ensure categories exist
        for ((name, imageUrl) in categoryDetails) {
            val query = db.collection("categories").whereEqualTo("name", name).get().await()
            if (query.isEmpty) {
                val docRef = db.collection("categories").document()
                val category = Category(
                    id = docRef.id,
                    name = name,
                    imageUrl = imageUrl,
                    isActive = true
                )
                docRef.set(category).await()
            }
        }

        // Generate Artists
        for ((category, imageUrl) in categoryDetails) {
            val count = (6..10).random()
            for (i in 1..count) {
                val isFemale = category.contains("Female") || (category == "Anchor" && random.nextBoolean()) || (category == "Dance Group" && random.nextBoolean())
                val firstName = if (isFemale) firstNamesFemale.random() else firstNamesMale.random()
                val lastName = lastNames.random()
                val fullName = "$firstName $lastName"
                
                val city = cities.random()
                val state = states[city] ?: "India"
                
                val expValue = (1..20).random()
                val rating = (38..50).random().toDouble() / 10.0
                
                val phone = "9" + (100000000 + random.nextInt(900000000)).toString()
                
                val artist = User(
                    uid = UUID.randomUUID().toString(),
                    name = fullName,
                    email = "${firstName.lowercase()}.${lastName.lowercase()}${random.nextInt(1000)}@sangeetsetu.com",
                    phone = phone,
                    whatsapp = phone,
                    city = city,
                    district = city,
                    state = state,
                    experience = expValue.toString(),
                    aboutMe = "नमस्ते! मैं एक अनुभवी $category कलाकार हूँ। मुझे इस क्षेत्र में $expValue साल का अनुभव है और मैं आपकी खुशी के अवसरों को और भी यादगार बनाने के लिए 'संगीत सेतु' के माध्यम से अपनी सेवाएं प्रदान कर रहा हूँ।",
                    photoUrl = imageUrl,
                    userType = "Artist",
                    category = category,
                    isVerified = random.nextDouble() > 0.3,
                    status = "active",
                    rating = rating,
                    reviewsCount = (15..800).random(),
                    isPremium = random.nextDouble() > 0.7,
                    isAvailable = random.nextDouble() > 0.1,
                    totalBookings = (5..200).random(),
                    createdAt = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * (1..365).random()),
                    profileCompleted = true,
                    registrationCompleted = true,
                    approvalStatus = "APPROVED",
                    languages = listOf("Hindi", "English", "Punjabi").shuffled().take((1..3).random())
                )
                
                // Save to 'users' collection for app compatibility
                db.collection("users").document(artist.uid).set(artist).await()
                
                // Save to 'artists' collection as explicitly requested
                db.collection("artists").document(artist.uid).set(artist).await()
            }
        }
    }
}
