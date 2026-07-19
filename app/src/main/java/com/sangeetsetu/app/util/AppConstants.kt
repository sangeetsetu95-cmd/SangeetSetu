package com.sangeetsetu.app.util

data class CategoryInfo(
    val name: String,
    val imageUrl: String
)

object AppConstants {
    const val APP_VERSION = "2.0.26 Luxury Edition"
    const val DEVELOPER_TEAM = "Sangeet Setu Team"

    val defaultCategories = listOf(
        CategoryInfo("Singers", "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=400"),
        CategoryInfo("Female Singers", "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=400"),
        CategoryInfo("Male Singers", "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=400"),
        CategoryInfo("Harmonium Players", "https://images.unsplash.com/photo-1605646194273-51984252f4c9?q=80&w=400"),
        CategoryInfo("Keyboard Players", "https://images.unsplash.com/photo-1512733596533-7b00ccf8ebaf?q=80&w=400"),
        CategoryInfo("Tabla Players", "https://images.unsplash.com/photo-1619983081563-430f63602796?q=80&w=400"),
        CategoryInfo("Dholak Players", "https://images.unsplash.com/photo-1599839618126-d934325a7695?q=80&w=400"),
        CategoryInfo("Guitarists", "https://images.unsplash.com/photo-1525201548942-d8b8bb097fb3?q=80&w=400"),
        CategoryInfo("DJs", "https://images.unsplash.com/photo-1571266028243-3716f02d7d29?q=80&w=400"),
        CategoryInfo("Bands", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=400"),
        CategoryInfo("Orchestra", "https://images.unsplash.com/photo-1465847899034-d174fc93fc77?q=80&w=400"),
        CategoryInfo("Katha Vachak", "https://images.unsplash.com/photo-1583089892943-e02e5b017b6a?q=80&w=400"),
        CategoryInfo("Bhajan Mandali", "https://images.unsplash.com/photo-1514334331481-b849301017e4?q=80&w=400"),
        CategoryInfo("Sound Service", "https://images.unsplash.com/photo-1598653222000-6b7b7a552625?q=80&w=400"),
        CategoryInfo("Event Organizer", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=400"),
        CategoryInfo("Dancers", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=400"),
        CategoryInfo("Photographers", "https://images.unsplash.com/photo-1492691523567-6172152e7eb6?q=80&w=400")
    )
}
