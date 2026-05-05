package com.gastro.nameet.database

import com.gastro.nameet.database.entity.PlaceEntity
import com.gastro.nameet.database.entity.FoodEntity

object SampleData {
    fun getSamplePlaces() = listOf(
        PlaceEntity(
            id = 1,
            name = "La Paz Batchoy",
            description = "Traditional Ilocano noodle soup",
            latitude = 10.3157,
            longitude = 123.8854,
            imageUrl = "https://example.com/lapaz.jpg",
            rating = 4.8f,
            category = "lunch",
            phone = "+63 32 XXX XXXX",
            address = "La Paz, Iloilo"
        ),
        PlaceEntity(
            id = 2,
            name = "Café Mary Grace",
            description = "Traditional Filipino ensaymadas and cakes",
            latitude = 14.5547,
            longitude = 121.0244,
            imageUrl = "https://example.com/marygrace.jpg",
            rating = 4.7f,
            category = "breakfast",
            phone = "+63 2 XXX XXXX",
            address = "Greenbelt, Makati"
        ),
        PlaceEntity(
            id = 3,
            name = "Aristocrat Restaurant",
            description = "Home of the best chicken barbecue in town",
            latitude = 14.5667,
            longitude = 120.9833,
            imageUrl = "https://example.com/aristocrat.jpg",
            rating = 4.5f,
            category = "dinner",
            phone = "+63 2 XXX XXXX",
            address = "Roxas Blvd, Manila"
        ),
        PlaceEntity(
            id = 4,
            name = "Manam Comfort Filipino",
            description = "Modern and classic Filipino comfort food",
            latitude = 14.5511,
            longitude = 121.0475,
            imageUrl = "https://example.com/manam.jpg",
            rating = 4.9f,
            category = "lunch",
            phone = "+63 2 XXX XXXX",
            address = "BGC, Taguig"
        ),
        PlaceEntity(
            id = 5,
            name = "Conti's Bakeshop",
            description = "Famous for Mango Bravo cake",
            latitude = 14.5350,
            longitude = 121.0310,
            imageUrl = "https://example.com/contis.jpg",
            rating = 4.6f,
            category = "dessert",
            phone = "+63 2 XXX XXXX",
            address = "Greenhills, San Juan"
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
