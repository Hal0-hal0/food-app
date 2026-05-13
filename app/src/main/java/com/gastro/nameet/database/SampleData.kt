package com.gastro.nameet.database

import com.gastro.nameet.database.entity.PlaceEntity
import com.gastro.nameet.database.entity.FoodEntity

object SampleData {
    fun getSamplePlaces() = listOf(
        PlaceEntity(
            id = 1,
            name = "Netong's Original La Paz Batchoy",
            description = "The most famous batchoy in Iloilo, located inside La Paz Market.",
            latitude = 10.7048,
            longitude = 122.5604,
            imageUrl = "https://images.unsplash.com/photo-1596797038558-b8231157c5a1?w=800",
            rating = 4.8f,
            category = "Batchoy",
            phone = "+63 33 396 2189",
            address = "La Paz Public Market, Iloilo City"
        ),
        PlaceEntity(
            id = 2,
            name = "Kap Isko's Pancit Molo",
            description = "Authentic Molo soup with handmade dumplings.",
            latitude = 10.7003,
            longitude = 122.5492,
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800",
            rating = 4.7f,
            category = "Pancit Molo",
            phone = "+63 33 337 2355",
            address = "Molo, Iloilo City"
        ),
        PlaceEntity(
            id = 3,
            name = "Breakthrough Restaurant",
            description = "Best seafood and local Ilonggo dishes by the sea.",
            latitude = 10.6698,
            longitude = 122.5398,
            imageUrl = "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800",
            rating = 4.9f,
            category = "Seafood",
            phone = "+63 33 337 3027",
            address = "Villa Arevalo, Iloilo City"
        ),
        PlaceEntity(
            id = 4,
            name = "Pat-Pat's Kansi",
            description = "A sour and savory beef soup, an Ilonggo favorite.",
            latitude = 10.7155,
            longitude = 122.5582,
            imageUrl = "https://images.unsplash.com/photo-1547928576-a4a33237ce35?w=800",
            rating = 4.6f,
            category = "Kansi",
            phone = "+63 33 320 2259",
            address = "Jaro, Iloilo City"
        ),
        PlaceEntity(
            id = 5,
            name = "Roberto's Siopao",
            description = "The legendary giant siopao of Iloilo.",
            latitude = 10.6942,
            longitude = 122.5714,
            imageUrl = "https://images.unsplash.com/photo-1526233595104-bd9b04c025ef?w=800",
            rating = 4.8f,
            category = "Snacks",
            phone = "+63 33 335 0413",
            address = "Calle Real, Iloilo City"
        )
    )

    fun getSampleFoods() = listOf(
        FoodEntity(
            placeId = 1,
            name = "Special Batchoy",
            description = "Noodle soup made with pork offal, crushed pork cracklings, chicken stock, beef loin and round noodles.",
            category = "lunch"
        ),
        FoodEntity(
            placeId = 2,
            name = "Ensaymada",
            description = "Sweet Filipino pastry topped with butter, sugar, and grated cheese.",
            category = "breakfast"
        ),
        FoodEntity(
            placeId = 3,
            name = "Chicken Barbecue",
            description = "Aristocrat's signature chicken barbecue served with java rice.",
            category = "dinner"
        )
    )
}
