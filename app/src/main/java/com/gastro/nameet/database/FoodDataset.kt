package com.gastro.nameet.database

import com.gastro.nameet.model.Specialty

object FoodDataset {

    /** Category labels keyed by categoryId */
    val categories = mapOf(
        1 to "Soups",
        2 to "Comfort Food",
        3 to "Seafood & Grilled",
        4 to "Rice Delicacies",
        5 to "Dumplings & Noodles",
        6 to "Pasalubong"
    )

    fun getSpecialties(): List<Specialty> = listOf(
        Specialty(
            id = 1,
            name = "Laswa",
            description = "A hearty Ilonggo vegetable soup made with a medley of native greens like alugbati, kalabasa, and okra, simmered with shrimp or small fish for a savory broth. The vegetables are cooked simply with minimal seasoning, letting the natural sweetness of the ingredients shine through. It is a staple everyday dish that reflects the rural agricultural roots of Iloilo's countryside communities.",
            history = "Laswa has been a cornerstone of rural Ilonggo cooking for generations, originating from farming households in the provinces surrounding Iloilo City. It was traditionally prepared by farmers' wives using whatever vegetables were freshly harvested from their backyard gardens, making it a practical and nutritious meal. The dish represents the Ilonggo value of simplicity and sustainability, and it remains a beloved comfort food served in homes and local eateries across Western Visayas.",
            imageUrl = "https://assets.unileversolutions.com/recipes-v3/175481-default.jpg?im=AspectCrop=(720,459);Resize=(720,459)",
            categoryId = 1
        ),
        Specialty(
            id = 2,
            name = "Tinuom nga Manok",
            description = "A traditional Ilonggo chicken dish where native free-range chicken is wrapped in banana leaves and slow-cooked with lemongrass, ginger, and other indigenous herbs and spices. The banana leaf wrapping locks in the aromatic flavors and keeps the meat incredibly tender and juicy. This unique cooking method creates a fragrant, earthy dish that is distinctly Ilonggo and unlike any other Filipino chicken preparation.",
            history = "Tinuom nga Manok originates from the rural countryside of Iloilo, where native chickens roam freely and banana leaves are abundant. The cooking technique of wrapping meat in banana leaves dates back to pre-colonial Filipino culinary traditions. It was a festive dish reserved for celebrations and gatherings, symbolizing hospitality and the Ilonggo tradition of sharing good food with family and community.",
            imageUrl = "https://i0.wp.com/www.angsarap.net/wp-content/uploads/2023/06/Tinuom-na-Manok.jpg",
            categoryId = 1
        ),
        Specialty(
            id = 3,
            name = "Batchoy",
            description = "An iconic Ilonggo noodle soup made with a rich pork bone broth, topped with miki noodles, sliced pork, liver, and finished with crushed chicharon (pork cracklings) and fresh spring onions. Each bowl is a deeply savory experience, with the umami-rich broth serving as the foundation and the crispy chicharon adding a satisfying textural contrast. It is often served with a raw egg cracked on top for extra richness.",
            history = "La Paz Batchoy was invented in the 1930s at the La Paz Public Market in Iloilo City, making it one of the most historically significant dishes in the Philippines. Federico Guillergan Sr. is widely credited with creating the dish, which quickly became a local sensation and eventually spread nationwide. Today, it is considered the signature dish of Iloilo and a must-try for every visitor, with the La Paz Market still home to the most authentic versions.",
            imageUrl = "https://assets.unileversolutions.com/recipes-v3/110752-default.jpg?im=AspectCrop=(720,459);Resize=(720,459)",
            categoryId = 2
        ),
        Specialty(
            id = 4,
            name = "KBL (Kadios, Baboy, Langka)",
            description = "A uniquely Ilonggo sour soup made from three key native ingredients: kadios (pigeon peas), baboy (pork), and langka (unripe jackfruit). The tangy, slightly sour broth is achieved by adding batuan fruit or vinegar, creating a complex flavor profile that is both hearty and refreshing. The combination of tender pork, nutty pigeon peas, and fibrous jackfruit makes it a nutritionally complete and deeply satisfying meal.",
            history = "KBL is a traditional Ilonggo comfort dish that has been passed down through generations, with roots in the indigenous farming communities of Western Visayas. The dish utilizes ingredients that are native and abundant in the region, making it an economical yet flavorful meal for rural families. It holds a special place in Ilonggo food culture as a dish that brings together the land's natural bounty, and it is often served during rainy days and family gatherings.",
            imageUrl = "https://images.yummy.ph/yummy/uploads/2023/10/kblrecipe-small-2.jpg",
            categoryId = 2
        ),
        Specialty(
            id = 5,
            name = "Linagpang",
            description = "A coastal Ilonggo dish where freshly grilled fish, typically bangus (milkfish), is simmered in a tomato-based broth with onions, ginger, and green chili peppers. The grilling process imparts a distinct smoky flavor that elevates the simple soup base, creating a layered and complex taste experience. It is best enjoyed piping hot with steamed rice on cool evenings.",
            history = "Linagpang is a native dish from the coastal communities of Iloilo province, where fishing has been a way of life for centuries. Fishermen's families developed this recipe as a way to transform their daily catch into a warm, nourishing meal after long hours at sea. The dish represents the intimate connection between Ilonggo coastal culture and its culinary traditions, and it remains a popular nighttime comfort food in seaside communities.",
            imageUrl = "https://i0.wp.com/www.angsarap.net/wp-content/uploads/2017/02/Linagpang-na-Bangus.jpg?w=1080&ssl=1",
            categoryId = 3
        ),
        Specialty(
            id = 6,
            name = "Chicken Binakol",
            description = "A fragrant and nourishing Visayan chicken soup cooked in fresh coconut water with strips of tender young coconut meat, lemongrass, and green papaya. The coconut water creates a naturally sweet and light broth that perfectly complements the tender chicken, while the lemongrass adds a citrusy aroma. This dish is considered a healing soup, often served to those recovering from illness or simply seeking comfort on a rainy day.",
            history = "Chicken Binakol is a beloved dish across Western Visayas, with deep roots in the region's coconut-rich agricultural heritage. The name 'binakol' comes from 'bakol,' the Hiligaynon word for coconut shell, referencing the traditional method of serving the soup inside a whole coconut. It has been a centerpiece of Ilonggo home cooking for generations, celebrating the abundance of coconut palms that dot the landscape of the islands.",
            imageUrl = "https://i0.wp.com/www.angsarap.net/wp-content/uploads/2012/01/chicken-binakol.jpg?w=800&ssl=1",
            categoryId = 3
        ),
        Specialty(
            id = 7,
            name = "Puto Manapla",
            description = "Soft, fluffy steamed rice cakes from the town of Manapla in Negros Occidental, closely associated with Ilonggo food culture. These delicate white puto are made from galapong (ground rice paste) and have a slightly sweet, pillowy texture that melts in the mouth. They are traditionally paired with dinuguan (pork blood stew) for a classic sweet-savory combination that is iconic in Visayan cuisine.",
            history = "Puto Manapla takes its name from the municipality of Manapla, where local families have been perfecting the recipe for decades. The town became famous for these rice cakes as travelers passing through would stop specifically to buy them, turning puto-making into a cottage industry. Today, Puto Manapla is one of the most popular pasalubong (travel gifts) for visitors to the region, representing the shared culinary heritage between Iloilo and neighboring Negros.",
            imageUrl = "https://miro.medium.com/v2/resize:fit:2000/format:webp/1*FWR-U--WQ6M8ALNFgngouQ.jpeg",
            categoryId = 4
        ),
        Specialty(
            id = 8,
            name = "Baye-Baye",
            description = "A traditional Ilonggo rice delicacy made from freshly pounded sticky rice (malagkit), shaped by hand into flat oval cakes and wrapped in banana leaves. The simple preparation highlights the natural sweetness and chewy texture of glutinous rice, sometimes enhanced with coconut milk or sugar. Each piece is lovingly hand-shaped, making baye-baye as much a labor of love as it is a delicious treat.",
            history = "Baye-Baye has been an Ilonggo delicacy for centuries, with its origins traced to the rice-farming communities of Iloilo province. The name comes from the Hiligaynon word 'baye,' meaning to pound or flatten, describing the traditional method of preparation using a wooden mortar and pestle. It is closely tied to harvest celebrations and has cultural significance as a food offering during town fiestas, symbolizing abundance and gratitude for a bountiful rice harvest.",
            imageUrl = "https://cdn.shortpixel.ai/spai/q_+w_975+to_webp+ret_img/rezelkealoha.com/wp-content/uploads/2023/11/Baye-Baye-Recipe-22.jpg",
            categoryId = 4
        ),
        Specialty(
            id = 9,
            name = "Pancit Molo",
            description = "Often called the 'Wonton Soup of the Philippines,' Pancit Molo is a flavorful dumpling soup featuring delicate wonton wrappers stuffed with a savory mixture of ground pork, shrimp, and vegetables, simmered in a rich chicken broth. The dumplings are folded into small bundles and cooked until tender, creating a comforting and elegant soup. It is garnished with fried garlic, spring onions, and sometimes toasted shallots for added depth.",
            history = "Pancit Molo is named after the historic district of Molo in Iloilo City, which was once a thriving center of Chinese-Filipino culture and commerce. The dish emerged from the blending of Chinese wonton soup traditions with local Ilonggo flavors during the Spanish colonial period. Molo was known as the 'Athens of the Philippines' for its cultured residents, and this refined soup reflects that sophistication, becoming one of Iloilo's most celebrated culinary exports.",
            imageUrl = "https://www.kawalingpinoy.com/wp-content/uploads/2015/10/pancit-molo-1-1152x1536.jpg",
            categoryId = 5
        ),
        Specialty(
            id = 10,
            name = "Biscocho",
            description = "A beloved Ilonggo bread treat made by twice-baking bread slices until golden and crunchy, then generously coating them with butter and sugar. The result is a perfectly crispy, sweet, and buttery snack that shatters with each bite. Biscocho comes in several varieties including plain butter, garlic, and the special version dipped in chocolate, making it a versatile treat enjoyed with coffee or as a standalone snack.",
            history = "Biscocho originated in the bakery houses of Iloilo City during the Spanish colonial era, when local bakers adapted European biscotti-making techniques using local ingredients and preferences. The tradition of biscocho-making became a thriving industry in Iloilo, with families passing down recipes through generations. Today, it is the most iconic pasalubong (souvenir gift) from Iloilo, with brands like Biscocho Haus and Tiburcio's becoming household names across the Philippines.",
            imageUrl = "https://www.foxyfolksy.com/wp-content/uploads/2019/08/how-to-make-biscocho.jpg",
            categoryId = 6
        )
    )

    fun getSpecialtyById(id: Int): Specialty? = getSpecialties().find { it.id == id }

    fun getSpecialtiesByCategory(categoryId: Int): List<Specialty> =
        getSpecialties().filter { it.categoryId == categoryId }
}
