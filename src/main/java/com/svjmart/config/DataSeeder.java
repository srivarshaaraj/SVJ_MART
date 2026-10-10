package com.svjmart.config;

import com.svjmart.entity.*;
import com.svjmart.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SellerProfileRepository sellerProfileRepository,
                      AddressRepository addressRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      CouponRepository couponRepository,
                      ReviewRepository reviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.addressRepository = addressRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0 && productRepository.count() >= 50) {
            return; // Already seeded
        }

        // 1. Seed Users
        User admin = userRepository.findByEmail("admin@svjmart.com").orElseGet(() -> {
            User u = new User("SVJ Mart Admin", "admin@svjmart.com", passwordEncoder.encode("Admin@1234"), "9876543210", Role.ROLE_ADMIN);
            return userRepository.save(u);
        });

        User seller = userRepository.findByEmail("seller@svjmart.com").orElseGet(() -> {
            User u = new User("SVJ Supermarket Direct", "seller@svjmart.com", passwordEncoder.encode("Seller@1234"), "9876543211", Role.ROLE_SELLER);
            User saved = userRepository.save(u);
            SellerProfile profile = new SellerProfile(saved, "SVJ Direct Supermarket", "Official SVJ Mart Fulfillment Center", "33AAAAA0000A1Z5", true);
            sellerProfileRepository.save(profile);
            saved.setSellerProfile(profile);
            return saved;
        });

        User freshFarmSeller = userRepository.findByEmail("freshfarm@svjmart.com").orElseGet(() -> {
            User u = new User("Farm Fresh Organics", "freshfarm@svjmart.com", passwordEncoder.encode("Farm@1234"), "9876543212", Role.ROLE_SELLER);
            User saved = userRepository.save(u);
            SellerProfile profile = new SellerProfile(saved, "Farm Fresh Organics", "Locally sourced fresh produce and daily dairy farm products", "33BBBBB1111B2Z6", true);
            sellerProfileRepository.save(profile);
            saved.setSellerProfile(profile);
            return saved;
        });

        User pendingSeller = userRepository.findByEmail("newvendor@svjmart.com").orElseGet(() -> {
            User u = new User("Daily Needs Mart", "newvendor@svjmart.com", passwordEncoder.encode("Vendor@1234"), "9876543213", Role.ROLE_SELLER);
            User saved = userRepository.save(u);
            SellerProfile profile = new SellerProfile(saved, "Daily Needs Mart", "Applicant vendor awaiting administrator review", "33CCCCC2222C3Z7", false);
            sellerProfileRepository.save(profile);
            saved.setSellerProfile(profile);
            return saved;
        });

        User buyer = userRepository.findByEmail("buyer@svjmart.com").orElseGet(() -> {
            User u = new User("Rajesh Kumar", "buyer@svjmart.com", passwordEncoder.encode("Buyer@1234"), "9876543214", Role.ROLE_BUYER);
            User saved = userRepository.save(u);

            Address addr = new Address(saved, "Rajesh Kumar", "9876543214", "Flat 402, Lotus Orchid Apartments", "100 Feet Road, Indiranagar", "Bengaluru", "Karnataka", "560038", "Near Metro Station", true);
            addressRepository.save(addr);
            saved.addAddress(addr);
            return saved;
        });

        // 2. Seed Categories (16 official Indian supermarket categories)
        Map<String, Category> catMap = new LinkedHashMap<>();
        String[][] categoriesData = {
            {"Rice, Grains and Staples", "rice-grains-staples", "Premium rice, atta, wholesome dals, pulses, and kitchen pantry staples.", "bi-box-seam", "1"},
            {"Cooking Oils and Ingredients", "cooking-oils-ingredients", "Refined sunflower oil, mustard oil, ghee, butter, and baking essentials.", "bi-droplet-half", "2"},
            {"Spices and Masalas", "spices-masalas", "Aromatic whole spices, pure spice powders, garam masala, and regional blends.", "bi-flower1", "3"},
            {"Dairy Products", "dairy-products", "Fresh pasteurized milk, creamy curd, paneer, butter, cheese, and buttermilk.", "bi-cup-straw", "4"},
            {"Fresh Fruits and Vegetables", "fresh-fruits-vegetables", "Farm-fresh handpicked seasonal fruits and crisp everyday cooking vegetables.", "bi-apple", "5"},
            {"Snacks and Munchies", "snacks-munchies", "Crispy chips, traditional namkeen, premium biscuits, chocolates, and sweets.", "bi-cookie", "6"},
            {"Breakfast and Bakery", "breakfast-bakery", "Artisan breads, rolled oats, breakfast cereals, jams, and breakfast spreads.", "bi-egg-fried", "7"},
            {"Beverages", "beverages", "Filter tea, roasted coffee beans, fruit juices, health drinks, and mineral water.", "bi-cup-hot", "8"},
            {"Packaged and Ready-to-Cook", "packaged-ready-to-cook", "Instant noodles, gourmet pasta, idli-dosa batter, ready soups, and papads.", "bi-fire", "9"},
            {"Cooking and Kitchen Essentials", "cooking-and-kitchen-essentials", "Food-grade aluminium foil, containers, sponges, cling wrap, and kitchenware.", "bi-basket", "10"},
            {"Cleaning Products", "cleaning-products", "Liquid detergents, disinfectant floor cleaners, dishwash bars, and mops.", "bi-stars", "11"},
            {"Personal Care", "personal-care", "Bathing soaps, gentle shampoos, herbal toothpastes, moisturizers, and hair oils.", "bi-heart-pulse", "12"},
            {"Baby Care", "baby-care", "Ultra-absorbent diapers, gentle baby wipes, baby shampoo, and gentle lotions.", "bi-emoji-smile", "13"},
            {"Home and Household Essentials", "home-household-essentials", "Paper napkins, tissue rolls, alkaline batteries, LED bulbs, and organizers.", "bi-house-door", "14"},
            {"Pet Essentials", "pet-essentials", "Nutritious dog food, cat food, grooming shampoos, and delicious pet treats.", "bi-tencent-qq", "15"},
            {"Pooja and Indian Household Essentials", "pooja-and-indian-household-essentials", "Fragrant incense agarbathi, camphor tablets, diya ghee wicks, and pooja oil.", "bi-brightness-high", "16"}
        };

        for (String[] c : categoriesData) {
            String name = c[0];
            String slug = c[1];
            String desc = c[2];
            String icon = c[3];
            int order = Integer.parseInt(c[4]);

            Category cat = categoryRepository.findBySlug(slug).orElseGet(() -> {
                Category newCat = new Category(name, slug, desc, icon, order, true);
                return categoryRepository.save(newCat);
            });
            catMap.put(slug, cat);
        }

        // 3. Seed Comprehensive Indian Supermarket Product Catalogue (100+ Products)
        if (productRepository.count() < 100) {
            seedFullSupermarketCatalogue(catMap, seller, freshFarmSeller);
        }

        // 4. Seed Promotional Coupons
        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("WELCOME50", 0, new BigDecimal("50.00"), new BigDecimal("299.00"), new BigDecimal("50.00"), true));
            couponRepository.save(new Coupon("SVJ100", 0, new BigDecimal("100.00"), new BigDecimal("699.00"), new BigDecimal("100.00"), true));
            couponRepository.save(new Coupon("SAVER15", 15, BigDecimal.ZERO, new BigDecimal("499.00"), new BigDecimal("150.00"), true));
            couponRepository.save(new Coupon("FREEDEL", 0, new BigDecimal("40.00"), new BigDecimal("199.00"), new BigDecimal("40.00"), true));
        }

        // 5. Seed Demonstration Reviews
        if (reviewRepository.count() == 0) {
            List<Product> sampleProducts = productRepository.findAll();
            if (!sampleProducts.isEmpty()) {
                Product p1 = sampleProducts.get(0);
                reviewRepository.save(new Review(p1, buyer, 5, "Excellent Quality & Fast Delivery!", "Consistently fresh, well-packaged, and arrived right on time for our weekly cooking.", true));
                if (sampleProducts.size() > 3) {
                    Product p2 = sampleProducts.get(3);
                    reviewRepository.save(new Review(p2, buyer, 5, "Unbeatable Freshness and Price", "Very good taste and texture. Genuine supermarket quality at great savings.", true));
                }
            }
        }
    }

    private void seedFullSupermarketCatalogue(Map<String, Category> cats, User seller, User farmSeller) {
        List<Product> products = new ArrayList<>();

        // Helper to add products
        // Category 1: Rice, Grains and Staples
        Category c1 = cats.get("rice-grains-staples");
        products.add(createP("Aashirvaad Superior Sharbati Atta", "Aashirvaad", c1, "Flours", "100% whole wheat atta made from MP Sharbati grains. Makes naturally soft and fluffy rotis.", "5 kg", "kg", "285.00", "325.00", 120, "/images/products/aashirvaad-atta.jpg", seller, false, true, true, true, "Whole Wheat", "Store in a cool dry airtight container", "Rich in dietary fiber and essential plant minerals"));
        products.add(createP("India Gate Basmati Rice Classic", "India Gate", c1, "Rice", "Aged long grain aromatic Basmati rice. Perfect for royal biryanis and aromatic pulao dishes.", "5 kg", "kg", "649.00", "799.00", 85, "/images/products/india-gate-basmati.jpg", seller, false, true, true, true, "100% Aged Basmati Rice", "Store in dry pantry away from moisture", "Carbohydrates: 78g, Protein: 8.2g per 100g"));
        products.add(createP("Ponni Boiled Rice Premium", "Double Horse", c1, "Rice", "Fine south Indian Ponni boiled rice, cleaned and sorted. Ideal for everyday lunch and sambar rice.", "10 kg", "kg", "580.00", "650.00", 60, "/images/products/ponni-rice.jpg", seller, false, true, false, true, "Ponni Boiled Rice", "Store in airtight drum", "Easily digestible staple"));
        products.add(createP("Tata Sampann Unpolished Toor Dal", "Tata Sampann", c1, "Dals & Pulses", "Rich in natural protein, unpolished toor dal without artificial colors or chemical polishing.", "1 kg", "kg", "175.00", "199.00", 150, "/images/products/tata-toor-dal.jpg", seller, false, true, true, true, "Pigeon Peas (Toor)", "Airtight jar", "High protein: 22g per 100g"));
        products.add(createP("Fortune Premium Idli Rice", "Fortune", c1, "Rice", "Short-grain parboiled rice specially milled for fluffy, soft idlis and crisp golden dosas.", "5 kg", "kg", "240.00", "280.00", 90, "/images/products/idli-rice.jpg", seller, false, true, false, true, "Parboiled Idli Rice", "Cool dry place", "Traditional idli rice texture"));
        products.add(createP("Tata Sampann Moong Dal Yellow", "Tata Sampann", c1, "Dals & Pulses", "Unpolished split yellow moong dal for delicious khichdi, dal tadka, and healthy soups.", "1 kg", "kg", "142.00", "165.00", 110, "/images/products/moong-dal.jpg", seller, false, true, false, true, "Yellow Split Moong", "Cool dry container", "Easily digestible, high fiber"));
        products.add(createP("Fortune Chakki Fresh Atta", "Fortune", c1, "Flours", "100% pure wheat flour grounded in traditional stone chakki to retain wholesome natural bran.", "10 kg", "kg", "459.00", "520.00", 75, "/images/products/fortune-atta.jpg", seller, false, true, false, true, "Whole Wheat", "Keep sealed", "Wholesome dietary fiber"));
        products.add(createP("Organic Brown Rice", "24 Mantra", c1, "Rice", "Nutritious whole grain brown rice packed with bran layer and natural minerals for healthy living.", "1 kg", "kg", "115.00", "135.00", 50, "/images/products/brown-rice.jpg", seller, false, true, false, true, "100% Certified Organic Brown Rice", "Store dry", "Zero cholesterol, high fiber"));
        products.add(createP("Tata Salt Vacuum Evaporated", "Tata Salt", c1, "Salt & Sugar", "Desh Ka Namak. Pure vacuum evaporated iodized salt ensuring mental and physical health.", "1 kg", "kg", "28.00", "30.00", 300, "/images/products/tata-salt.jpg", seller, false, true, true, true, "Iodized Edible Salt", "Dry shaker or jar", "Sodium: 38.7g per 100g"));
        products.add(createP("Madhur Pure & Hygienic Sugar", "Madhur", c1, "Salt & Sugar", "Sulphur-free sparkling refined white crystal sugar, untouched by hand.", "5 kg", "kg", "245.00", "275.00", 140, "/images/products/madhur-sugar.jpg", seller, false, true, false, true, "100% Pure Cane Sugar", "Dry jar", "Energy: 398 kcal per 100g"));
        products.add(createP("MTR Roasted Rava Sooji", "MTR", c1, "Flours", "Pre-roasted semolina for effortless preparation of quick upma, rava dosa, and kesari halwa.", "1 kg", "kg", "68.00", "78.00", 95, "/images/products/rava-sooji.jpg", seller, false, true, false, true, "Semolina (Wheat)", "Store cool", "Low fat, instant breakfast staple"));
        products.add(createP("Tata Sampann Kabuli Chana", "Tata Sampann", c1, "Dals & Pulses", "Large grain unpolished chickpeas for authentic Amritsari chole and Mediterranean hummus.", "1 kg", "kg", "165.00", "190.00", 80, "/images/products/kabuli-chana.jpg", seller, false, true, false, true, "Kabuli Chickpeas", "Sealed container", "Protein: 19g, Fiber: 12g"));

        // Category 2: Cooking Oils and Ingredients
        Category c2 = cats.get("cooking-oils-ingredients");
        products.add(createP("Fortune Sunlite Refined Sunflower Oil", "Fortune", c2, "Cooking Oils", "Light and clear refined sunflower oil enriched with Vitamin A & D. Low absorption formula.", "1 litre", "litre", "138.00", "160.00", 180, "/images/products/fortune-sunflower-oil.jpg", seller, false, true, true, true, "Refined Sunflower Oil, Vitamin A, Vitamin D", "Store in cool dark spot", "Zero cholesterol, rich in Omega 6"));
        products.add(createP("Amul Pure Cow Ghee Tin", "Amul", c2, "Ghee & Butter", "Traditional aromatic granular cow ghee made from fresh farm cream. Pure golden goodness.", "1 litre", "litre", "595.00", "650.00", 110, "/images/products/amul-cow-ghee.jpg", seller, false, true, true, true, "Milk Fat from Cow Milk", "Room temperature", "Rich in Vitamins A, D, E"));
        products.add(createP("Dhara Kachi Ghani Mustard Oil", "Dhara", c2, "Cooking Oils", "Cold pressed pungent kachi ghani mustard oil for authentic North Indian curries and pickles.", "1 litre", "litre", "162.00", "185.00", 90, "/images/products/mustard-oil.jpg", seller, false, true, false, true, "Pure Cold Pressed Mustard Oil", "Away from direct sunlight", "Natural antioxidants & MUFA"));
        products.add(createP("Idhayam Gingelly Sesame Oil", "Idhayam", c2, "Cooking Oils", "Authentic cold-pressed sesame oil. Enhances the taste of south Indian dishes, vatha kuzhambu, and dosas.", "1 litre", "litre", "345.00", "390.00", 75, "/images/products/sesame-oil.jpg", seller, false, true, false, true, "100% Pure Sesame Oil", "Cool shelf", "Traditional healthy oil"));
        products.add(createP("Parachute 100% Pure Coconut Cooking Oil", "Parachute", c2, "Cooking Oils", "Cold pressed from sun-dried coconuts. Ideal for coastal Indian cuisine and traditional recipes.", "1 litre", "litre", "265.00", "299.00", 85, "/images/products/coconut-oil.jpg", seller, false, true, false, true, "100% Pure Coconut Oil", "Store dry", "Natural MCTs"));
        products.add(createP("Saffola Gold Pro Healthy Lifestyle Oil", "Saffola", c2, "Cooking Oils", "Dual seed blend of Rice Bran and Sunflower oil with Oryzanol to keep cholesterol in check.", "5 litres", "litre", "780.00", "920.00", 65, "/images/products/saffola-gold.jpg", seller, false, true, true, true, "Rice Bran Oil, Sunflower Oil, Antioxidants", "Cool pantry", "LOSORB technology for 29% less oil absorption"));
        products.add(createP("Everest Cornflour Powder", "Everest", c2, "Baking & Cooking", "Finely milled white corn starch for thickening soups, gravies, crisp pakoras, and Indo-Chinese.", "500 g", "g", "45.00", "50.00", 100, "/images/products/cornflour.jpg", seller, false, true, false, true, "Maize Starch", "Dry container", "Gluten free thickener"));
        products.add(createP("Weikfield Baking Powder", "Weikfield", c2, "Baking & Cooking", "Double action baking powder for light, spongy cakes, fluffy puris, and naan.", "100 g", "g", "38.00", "42.00", 120, "/images/products/baking-powder.jpg", seller, false, true, false, true, "Sodium Bicarbonate, Sodium Aluminium Sulphate", "Keep sealed tightly", "Consistent baking rise"));

        // Category 3: Spices and Masalas
        Category c3 = cats.get("spices-masalas");
        products.add(createP("Everest Turmeric Powder Haldi", "Everest", c3, "Powdered Spices", "Rich in high curcumin content. Selected from finest Sangli & Salem turmeric roots.", "500 g", "g", "125.00", "145.00", 140, "/images/products/turmeric-powder.jpg", seller, false, true, true, true, "Pure Ground Turmeric", "Cool dry dark container", "Natural antibacterial curcumin"));
        products.add(createP("MDH Deggi Mirch Red Chilli Powder", "MDH", c3, "Powdered Spices", "Unique blend of Kashmiri and red chillies providing vibrant crimson color and moderate heat.", "500 g", "g", "195.00", "230.00", 130, "/images/products/deggi-mirch.jpg", seller, false, true, true, true, "Ground Sun-dried Chillies", "Dry airtight tin", "Rich natural capsicum color"));
        products.add(createP("Catch Coriander Powder Dhaniya", "Catch", c3, "Powdered Spices", "Aromatic coriander powder made with low temperature grinding technology to preserve essential oils.", "500 g", "g", "110.00", "130.00", 100, "/images/products/coriander-powder.jpg", seller, false, true, false, true, "Pure Ground Coriander Seeds", "Keep away from moisture", "Aromatic earthy flavour"));
        products.add(createP("Aachi Garam Masala Powder", "Aachi", c3, "Blended Masalas", "Traditional royal blend of 14 roasted spices for rich aromatic curries and gravies.", "200 g", "g", "82.00", "95.00", 115, "/images/products/garam-masala.jpg", seller, false, true, false, true, "Cardamom, Cinnamon, Cloves, Mace, Cumin, Pepper", "Airtight glass jar", "Rich pungent aroma"));
        products.add(createP("MTR Sambar Powder Authentic", "MTR", c3, "Blended Masalas", "South Indian secret recipe for heavenly homemade sambar with authentic taste and aroma.", "200 g", "g", "78.00", "90.00", 140, "/images/products/sambar-powder.jpg", seller, false, true, true, true, "Coriander, Red Chilli, Cumin, Fenugreek, Asafoetida", "Seal after opening", "Authentic Udupi flavour"));
        products.add(createP("Eastern Chicken Masala Powder", "Eastern", c3, "Blended Masalas", "Flavorful coastal blend of spices crafted to prepare tender, restaurant-style chicken curry.", "100 g", "g", "48.00", "55.00", 90, "/images/products/chicken-masala.jpg", seller, false, false, false, true, "Chilli, Coriander, Pepper, Turmeric, Clove, Nutmeg", "Dry place", "Authentic South Indian non-veg curry mix"));
        products.add(createP("Tata Sampann Biryani Masala", "Tata Sampann", c3, "Blended Masalas", "Crafted by Chef Sanjeev Kapoor with natural oils intact for delectable fragrant biryani.", "100 g", "g", "55.00", "65.00", 110, "/images/products/biryani-masala.jpg", seller, false, true, true, true, "Black Pepper, Mace, Star Anise, Cinnamon, Cardamom", "Cool dry place", "Chef formulated spice balance"));
        products.add(createP("24 Mantra Organic Cumin Seeds Jeera", "24 Mantra", c3, "Whole Spices", "Certified organic whole cumin seeds with warm earthy aroma and digestive benefits.", "200 g", "g", "95.00", "115.00", 85, "/images/products/cumin-seeds.jpg", seller, false, true, false, true, "100% Organic Cumin Seeds", "Airtight container", "Aromatic digestive spice"));

        // Category 4: Dairy Products
        Category c4 = cats.get("dairy-products");
        products.add(createP("Amul Taaza Homogenised Toned Milk", "Amul", c4, "Milk", "Nutritious toned milk with 3% fat, UHT treated to stay fresh for months without refrigeration till opened.", "1 litre", "litre", "74.00", "78.00", 200, "/images/products/amul-taaza-milk.jpg", farmSeller, true, true, true, true, "Toned Milk, Vitamin A, Vitamin D", "Refrigerate after opening. Shelf life 180 days unopened", "Protein: 3.2g, Calcium: 120mg"));
        products.add(createP("Milky Mist Fresh Paneer Block", "Milky Mist", c4, "Paneer & Curd", "Soft, melt-in-the-mouth paneer made from fresh cow milk. Ideal for paneer butter masala and tikka.", "200 g", "g", "98.00", "110.00", 80, "/images/products/milky-mist-paneer.jpg", farmSeller, true, true, true, true, "Pasteurized Cow Milk, Citric Acid", "Keep refrigerated at 4°C. Consume within 7 days", "High Protein: 18g per 100g"));
        products.add(createP("Nandini Thick Curd Tub", "Nandini", c4, "Paneer & Curd", "Homogenized creamy set curd with traditional dahi taste. Good source of gut-friendly probiotics.", "500 g", "g", "35.00", "38.00", 120, "/images/products/nandini-curd.jpg", farmSeller, true, true, false, true, "Pasteurized Milk, Active Lactic Cultures", "Keep refrigerated at 4°C", "Digestive probiotics & calcium"));
        products.add(createP("Amul Salted Butter Block", "Amul", c4, "Butter & Cheese", "The classic Taste of India. Golden salted butter made from fresh dairy cream.", "500 g", "g", "275.00", "290.00", 95, "/images/products/amul-butter.jpg", farmSeller, true, true, true, true, "Butter Fat (80%), Moisture, Salt", "Keep refrigerated below 10°C", "Energy: 722 kcal per 100g"));
        products.add(createP("Britannia Processed Cheese Slices", "Britannia", c4, "Butter & Cheese", "Individually wrapped creamy cheese slices. Perfect for grilled cheese sandwiches and burgers.", "200 g (10 slices)", "pack", "135.00", "150.00", 70, "/images/products/cheese-slices.jpg", farmSeller, true, true, false, true, "Cheese, Milk Solids, Emulsifier", "Refrigerate at 4°C", "Rich in dairy calcium & protein"));
        products.add(createP("Mother Dairy Spiced Buttermilk Chaach", "Mother Dairy", c4, "Milk Drinks", "Refreshing low-calorie spiced buttermilk tempered with green chilli, ginger, and curry leaves.", "500 ml", "ml", "20.00", "22.00", 150, "/images/products/chaach.jpg", farmSeller, true, true, false, true, "Curd, Water, Salt, Spices", "Refrigerate and serve chilled", "Refreshing hydration with gut flora"));

        // Category 5: Fresh Fruits and Vegetables
        Category c5 = cats.get("fresh-fruits-vegetables");
        products.add(createP("Shimla Royal Gala Apples", "Farm Fresh", c5, "Fruits", "Crisp, sweet, and juicy handpicked mountain apples packed with antioxidants.", "1 kg (4-5 pcs)", "kg", "179.00", "210.00", 65, "/images/products/shimla-apples.jpg", farmSeller, true, true, true, true, "Fresh Grade-A Apples", "Keep in refrigerator crisper drawer", "Rich in vitamin C and dietary pectin fiber"));
        products.add(createP("Fresh Robusta Bananas", "Farm Fresh", c5, "Fruits", "Naturally ripened sweet robusta bananas. Instant energy booster packed with potassium.", "1 kg (6-8 pcs)", "kg", "58.00", "65.00", 90, "/images/products/bananas.jpg", farmSeller, true, true, true, true, "Fresh Bananas", "Store at room temperature", "Potassium: 358mg per 100g"));
        products.add(createP("Farm Fresh Desi Tomatoes", "Farm Fresh", c5, "Vegetables", "Juicy, ripe red farm tomatoes, ideal for rich gravies, rasam, and fresh salads.", "1 kg", "kg", "38.00", "45.00", 110, "/images/products/tomatoes.jpg", farmSeller, true, true, true, true, "Fresh Farm Tomatoes", "Cool airy basket", "Rich in lycopene antioxidant"));
        products.add(createP("Nashik Medium Red Onions", "Farm Fresh", c5, "Vegetables", "Pungent and crisp red onions sourced directly from Nashik farms. Essential daily cooking base.", "2 kg", "kg", "72.00", "85.00", 130, "/images/products/onions.jpg", farmSeller, true, true, true, true, "Fresh Dry Onions", "Dry ventilated basket", "Essential everyday base"));
        products.add(createP("Agra Jyoti Cooking Potatoes", "Farm Fresh", c5, "Vegetables", "Firm and clean potatoes with uniform skin. Perfect for boiling, roasting, and curries.", "2 kg", "kg", "64.00", "75.00", 140, "/images/products/potatoes.jpg", farmSeller, true, true, false, true, "Fresh Potatoes", "Store away from light and dampness", "Good source of potassium & carbs"));
        products.add(createP("Tender Green Beans French", "Farm Fresh", c5, "Vegetables", "Crisp, stringless green French beans. Sourced fresh from local farms.", "500 g", "g", "42.00", "50.00", 60, "/images/products/french-beans.jpg", farmSeller, true, true, false, true, "Fresh Green Beans", "Refrigerate in mesh bag", "Low calorie, rich in vitamin K"));
        products.add(createP("Fresh Organic Palak Spinach", "Farm Fresh", c5, "Vegetables", "Cleaned, tender leafy spinach. Power-packed with iron and vitamins for healthy palak paneer.", "250 g bunch", "bunch", "25.00", "30.00", 50, "/images/products/palak-spinach.jpg", farmSeller, true, true, false, true, "Fresh Farm Spinach", "Refrigerate in vegetable crisper", "Rich in iron, folate, and carotenoids"));
        products.add(createP("Fresh Lemons Yellow Juicy", "Farm Fresh", c5, "Vegetables", "Tangy, juicy farm lemons. Great for lemonade, rasam, and dressing.", "250 g (5-6 pcs)", "pack", "32.00", "40.00", 80, "/images/products/lemons.jpg", farmSeller, true, true, false, true, "Fresh Farm Lemons", "Room temp or crisper", "High Vitamin C"));

        // Category 6: Snacks and Munchies
        Category c6 = cats.get("snacks-munchies");
        products.add(createP("Haldiram's Nagpur Aloo Bhujia", "Haldiram's", c6, "Namkeen", "Crunchy and spicy potato sev with secret mint-seasoning blend. The quintessential Indian tea snack.", "400 g", "g", "110.00", "125.00", 160, "/images/products/aloo-bhujia.jpg", seller, false, true, true, true, "Potatoes, Gram Flour, Spices, Edible Oil", "Store in cool airtight tin", "Crispy savory taste"));
        products.add(createP("Lay's India's Magic Masala Chips", "Lay's", c6, "Chips", "Crispy ridged potato chips seasoned with a signature blend of fiery Indian spices.", "115 g", "g", "48.00", "50.00", 180, "/images/products/lays-magic-masala.jpg", seller, false, true, true, true, "Potatoes, Edible Vegetable Oil, Masala Seasoning", "Keep sealed away from moisture", "Crunchy snack"));
        products.add(createP("Britannia Good Day Butter Cookies", "Britannia", c6, "Biscuits", "Rich buttery cookies sprinkled with crunchy cashew bits. Smiling moments with every bite.", "600 g mega pack", "g", "135.00", "150.00", 120, "/images/products/good-day-cookies.jpg", seller, false, true, true, true, "Wheat Flour, Butter, Cashew Bits, Milk Solids", "Airtight container", "Delicious melt-in-mouth sweetness"));
        products.add(createP("Parle-G Gold Glucose Biscuits", "Parle", c6, "Biscuits", "The nation's favourite tea biscuit packed with wholesome goodness of wheat and milk.", "1 kg family pack", "kg", "90.00", "100.00", 200, "/images/products/parle-g.jpg", seller, false, true, true, true, "Wheat Flour, Sugar, Invert Syrup, Milk Solids", "Keep in dry container", "Instant energy snack"));
        products.add(createP("Cadbury Dairy Milk Silk Chocolate Bar", "Cadbury", c6, "Chocolates", "Indulgent, smoother, creamier milk chocolate that melts luxuriously in the mouth.", "150 g", "g", "175.00", "190.00", 80, "/images/products/dairy-milk-silk.jpg", seller, false, true, true, true, "Sugar, Milk Solids, Cocoa Butter, Cocoa Solids", "Store in cool conditions between 15-20°C", "Premium melt chocolate"));
        products.add(createP("Bingo! Mad Angles Achaari Masti", "Bingo", c6, "Chips", "Triangle corn and rice chips seasoned with tangy spicy mango pickle spices.", "130 g", "g", "45.00", "50.00", 90, "/images/products/bingo-mad-angles.jpg", seller, false, true, false, true, "Rice Grits, Corn Grits, Mango Pickle Flavour", "Seal after opening", "Crunchy tangy texture"));
        products.add(createP("Oreo Original Vanilla Creme Sandwich", "Cadbury Oreo", c6, "Biscuits", "Twist, lick, and dunk! Rich chocolate cookies stuffed with smooth vanilla cream.", "300 g family pack", "g", "85.00", "95.00", 110, "/images/products/oreo-cookies.jpg", seller, false, true, false, true, "Refined Wheat Flour, Sugar, Cocoa Solids, Edible Oil", "Dry cool pantry", "Classic cookie sandwich"));
        products.add(createP("Kurkure Masala Munch Crunchy", "Kurkure", c6, "Namkeen", "Crunchy puffed snacks made from dal, rice, and corn tossed in fiery Indian spices.", "150 g", "g", "38.00", "40.00", 130, "/images/products/kurkure.jpg", seller, false, true, false, true, "Rice Meal, Corn Meal, Gram Meal, Spices", "Keep sealed", "100% vegetarian crispy puff"));

        // Category 7: Breakfast and Bakery
        Category c7 = cats.get("breakfast-bakery");
        products.add(createP("Modern 100% Whole Wheat Bread", "Modern", c7, "Breads", "Baked fresh with 100% whole wheat flour, zero maida, high dietary fiber for wholesome breakfasts.", "400 g", "g", "52.00", "55.00", 70, "/images/products/whole-wheat-bread.jpg", farmSeller, true, true, true, true, "Whole Wheat Flour, Yeast, Malt Extract, Salt", "Store in dry place, consume within 4 days", "High dietary fiber, zero cholesterol"));
        products.add(createP("Kellogg's Real Almond & Honey Corn Flakes", "Kellogg's", c7, "Cereals", "Crunchy golden corn flakes tossed with real California almonds and pure honey nectar.", "1 kg", "kg", "399.00", "475.00", 85, "/images/products/kelloggs-corn-flakes.jpg", seller, false, true, true, true, "Corn Grits, Sugar, Almonds (8%), Honey, Malt", "Airtight jar", "Enriched with 8 essential vitamins"));
        products.add(createP("Quaker Rolled Wholegrain White Oats", "Quaker", c7, "Oats", "100% natural wholegrain rolled oats. Rich in beta-glucan soluble fiber that helps reduce cholesterol.", "1 kg", "kg", "185.00", "220.00", 100, "/images/products/quaker-oats.jpg", seller, false, true, true, true, "100% Wholegrain Rolled Oats", "Dry sealed jar", "Heart-healthy complex carbohydrates"));
        products.add(createP("Kissan Mixed Fruit Jam Sweet & Tangy", "Kissan", c7, "Jams & Spreads", "Blend of 8 fresh fruits: banana, papaya, apple, pear, pineapple, mango, grape, and orange.", "500 g", "g", "155.00", "175.00", 90, "/images/products/kissan-jam.jpg", seller, false, true, false, true, "Mixed Fruit Pulp, Sugar, Pectin, Citric Acid", "Refrigerate after opening", "Natural fruit sweetness"));
        products.add(createP("Pintola All-Natural Crunchy Peanut Butter", "Pintola", c7, "Jams & Spreads", "Pure roasted peanuts with zero added sugar, zero salt, zero hydrogenated oils. 30g protein per 100g.", "1 kg", "kg", "380.00", "450.00", 65, "/images/products/peanut-butter.jpg", seller, false, true, true, true, "100% Roasted Peanuts", "Room temperature", "High plant protein: 30g per 100g"));
        products.add(createP("Britannia Daily Fresh White Sandwich Bread", "Britannia", c7, "Breads", "Soft, pillowy white sandwich bread baked daily from finest wheat flour.", "400 g", "g", "44.00", "48.00", 80, "/images/products/britannia-white-bread.jpg", farmSeller, true, true, false, true, "Wheat Flour, Sugar, Yeast, Edible Vegetable Oil", "Store dry, consume within 4 days", "Soft slice texture"));

        // Category 8: Beverages
        Category c8 = cats.get("beverages");
        products.add(createP("Tata Tea Gold Premium Black Tea", "Tata Tea", c8, "Tea", "Exquisite blend of strong Assam CTC tea leaves gently combined with gently rolled aromatic long leaves.", "1 kg", "kg", "525.00", "620.00", 110, "/images/products/tata-tea-gold.jpg", seller, false, true, true, true, "Assam CTC Black Tea with Long Leaves", "Store in clean tin container", "Rich aroma with brisk strength"));
        products.add(createP("Nescafé Classic Instant Coffee Powder", "Nescafé", c8, "Coffee", "100% pure roasted Robusta coffee beans. Signature bold aroma and rich morning kick.", "200 g glass jar", "g", "420.00", "475.00", 90, "/images/products/nescafe-classic.jpg", seller, false, true, true, true, "100% Pure Instant Soluble Coffee", "Keep glass jar airtight", "Zero sugar, rich caffeine aroma"));
        products.add(createP("Real Fruit Power Mixed Fruit Juice", "Real", c8, "Juices", "Made from goodness of 9 fruits with no added preservatives. Rich in Vitamin C.", "1 litre", "litre", "110.00", "130.00", 130, "/images/products/real-mixed-fruit.jpg", seller, false, true, true, true, "Mixed Fruit Concentrates, Water, Vitamin C", "Refrigerate after opening, consume in 5 days", "Good source of daily vitamin C"));
        products.add(createP("Bru Green Label Filter Coffee Blend", "Bru", c8, "Coffee", "Authentic 53% coffee and 47% chicory blend roasted and ground for rich South Indian decoction.", "500 g", "g", "215.00", "245.00", 75, "/images/products/bru-filter-coffee.jpg", seller, false, true, false, true, "Coffee (53%), Chicory (47%)", "Airtight metal container", "Traditional thick golden decoction"));
        products.add(createP("Bisleri Mineral Water Jumbo Dispenser", "Bisleri", c8, "Water", "Purified with 10-step quality process with essential minerals added. Food-grade packaging.", "5 litres", "litre", "70.00", "75.00", 150, "/images/products/bisleri-5l.jpg", seller, false, true, false, true, "Treated Mineral Drinking Water", "Keep in clean shaded spot", "Enriched with magnesium & potassium"));
        products.add(createP("Paper Boat Tender Coconut Water", "Paper Boat", c8, "Health Drinks", "100% natural tender coconut water packed with natural electrolytes for rapid hydration.", "250 ml pack", "ml", "48.00", "55.00", 100, "/images/products/tender-coconut.jpg", seller, false, true, false, true, "Natural Coconut Water, Bio-antioxidants", "Serve chilled", "Natural electrolytes potassium & sodium"));

        // Category 9: Packaged and Ready-to-Cook Foods
        Category c9 = cats.get("packaged-ready-to-cook");
        products.add(createP("Maggi 2-Minute Masala Instant Noodles", "Maggi", c9, "Noodles & Pasta", "India's beloved masala noodles crafted with 10 roasted spices. Quick, tasty comfort in 2 minutes.", "560 g (8 packs)", "pack", "112.00", "128.00", 250, "/images/products/maggi-noodles.jpg", seller, false, true, true, true, "Wheat Flour, Edible Oil, Spices, Salt", "Store dry", "Quick family comfort food"));
        products.add(createP("iD Fresh Idli and Dosa Batter Ready", "iD Fresh", c9, "Ready Mixes", "100% natural, stone ground fermented batter with zero soda, zero preservatives. Naturally airy.", "1 kg pouch", "kg", "85.00", "95.00", 80, "/images/products/id-batter.jpg", farmSeller, true, true, true, true, "Rice, Urad Dal, RO Water, Fenugreek, Salt", "Keep refrigerated at 0-4°C", "Authentic soft idlis & crispy dosas"));
        products.add(createP("Bambino Roasted Wheat Vermicelli Semi", "Bambino", c9, "Noodles & Pasta", "Non-sticky, pre-roasted durum wheat vermicelli for quick sweet kheer, semiya upma, and biryani.", "850 g", "g", "78.00", "90.00", 110, "/images/products/bambino-vermicelli.jpg", seller, false, true, false, true, "100% Hard Durum Wheat Semolina", "Keep airtight", "High energy, zero cholesterol"));
        products.add(createP("MTR Ready to Eat Paneer Butter Masala", "MTR", c9, "Ready Meals", "Tender cottage cheese cubes simmered in rich buttery tomato cashew gravy. Just heat and serve.", "300 g", "g", "135.00", "150.00", 65, "/images/products/mtr-paneer-masala.jpg", seller, false, true, false, true, "Paneer, Tomatoes, Cream, Butter, Cashews, Spices", "Cool dry spot, refrigerate after opening", "No cooking required, heat & eat"));
        products.add(createP("Lijjat Udad Dal Papad with Pepper", "Lijjat", c9, "Papads & Pickles", "Traditional spicy crisp papads made from urad flour, black pepper, and asafoetida.", "200 g", "g", "72.00", "80.00", 120, "/images/products/lijjat-papad.jpg", seller, false, true, false, true, "Udad Flour, Salt, Black Pepper, Asafoetida", "Keep completely dry", "Traditional meal accompaniment"));
        products.add(createP("Mother's Recipe Mango Pickle Authentic", "Mother's Recipe", c9, "Papads & Pickles", "Traditional raw mango pickle cured in mustard oil with whole fenugreek and red chillies.", "500 g jar", "g", "125.00", "145.00", 95, "/images/products/mango-pickle.jpg", seller, false, true, false, true, "Mango Pieces, Salt, Mustard Oil, Spices", "Use dry spoon only", "Classic spicy-tangy homemade flavour"));

        // Category 10: Cooking and Kitchen Essentials
        Category c10 = cats.get("cooking-and-kitchen-essentials");
        products.add(createP("Hindalco Freshwrap Aluminium Foil", "Freshwrap", c10, "Foils & Wraps", "Heavy duty 11 micron food-grade aluminium foil. Keeps rotis, rolls, and meals piping hot and fresh.", "18 meters roll", "pack", "165.00", "199.00", 120, "/images/products/freshwrap-foil.jpg", seller, false, true, true, true, "Food-grade Aluminium", "Dry storage", "Bacterial barrier, moisture lock"));
        products.add(createP("Scotch-Brite Scrub Sponge Combo", "Scotch-Brite", c10, "Cleaning Tools", "High density sponge with tough stain-removing green nylon scrubber. Cleans dishes without scratching.", "Pack of 3", "pack", "75.00", "90.00", 150, "/images/products/scotch-brite-sponge.jpg", seller, false, true, true, true, "Synthetic Scrubber & Cellulose Sponge", "Rinse and air dry after use", "Durable kitchen cleaning"));
        products.add(createP("Pramukh Airtight Grocery Storage Jars", "Pramukh", c10, "Containers", "BPA-free transparent modular kitchen storage containers with easy snap locks for dals and staples.", "Set of 6 (1000ml)", "pack", "349.00", "499.00", 60, "/images/products/storage-jars.jpg", seller, false, true, false, true, "Food-grade BPA Free Virgin Plastic", "Dishwasher safe", "Moisture-proof airtight locking"));
        products.add(createP("Oddy Uniwraps Food Wrapping Paper", "Oddy", c10, "Foils & Wraps", "Eco-friendly, microwave safe, greaseproof parchment paper for wrapping tiffins, rotis, and snacks.", "20 meters roll", "pack", "149.00", "180.00", 90, "/images/products/uniwraps.jpg", seller, false, true, false, true, "100% Organic Cellulose Paper", "Keep in dry drawer", "Non-stick, microwave safe, breathable"));

        // Category 11: Cleaning Products
        Category c11 = cats.get("cleaning-products");
        products.add(createP("Surf Excel Matic Front Load Detergent Liquid", "Surf Excel", c11, "Laundry", "Powerful liquid formula that dissolves rapidly and eliminates tough stains inside washing machines.", "2 litres", "litre", "430.00", "499.00", 140, "/images/products/surf-excel-liquid.jpg", seller, false, true, true, true, "Surfactants, Enzymes, Fabric Conditioners", "Keep out of reach of children", "Safe on fabrics, leaves fragrance"));
        products.add(createP("Vim Dishwash Gel Lemon Anti-Germ", "Vim", c11, "Dishwash", "Concentrated dishwash liquid powered with real lemon juice. Removes burnt grease in 1 spoon.", "750 ml bottle", "ml", "145.00", "170.00", 160, "/images/products/vim-gel.jpg", seller, false, true, true, true, "Lemon Extract, Sodium Laureth Sulfate", "Rinse thoroughly with water", "Sparkling utensils with fresh citrus"));
        products.add(createP("Lizol Disinfectant Surface Floor Cleaner Citrus", "Lizol", c11, "Surface Cleaners", "Triple action disinfectant killing 99.9% germs. Safe on marble, tiles, and wooden flooring.", "2 litres", "litre", "355.00", "420.00", 110, "/images/products/lizol-cleaner.jpg", seller, false, true, true, true, "Benzalkonium Chloride, Fragrance, Surfactant", "Dilute in mop bucket water", "Hospital grade germ kill"));
        products.add(createP("Harpic Power Plus Toilet Cleaner Original", "Harpic", c11, "Toilet Cleaners", "Thick acidic formula that clings to toilet bowl, removing yellowish limescale and 99.9% germs.", "1 litre", "litre", "185.00", "215.00", 130, "/images/products/harpic-toilet.jpg", seller, false, true, false, true, "Hydrochloric Acid 10.5%, Fragrance", "Wear gloves, keep away from eyes", "10x better stain removal"));
        products.add(createP("Colin Glass and Household Cleaner Spray", "Colin", c11, "Surface Cleaners", "Special shine booster formula for streak-free sparkling mirrors, glass tabletops, and TV screens.", "500 ml trigger", "ml", "98.00", "110.00", 95, "/images/products/colin-spray.jpg", seller, false, true, false, true, "IPA, Shine Boosters, Fragrance", "Spray and wipe with dry microfiber", "Streak-free transparent glass"));
        products.add(createP("Shalimar Oxo-Biodegradable Garbage Bags", "Shalimar", c11, "Waste Disposal", "Sturdy leak-proof garbage trash bags with convenient tie strings. Fits medium household dustbins.", "Pack of 60 (Medium)", "pack", "135.00", "175.00", 150, "/images/products/garbage-bags.jpg", seller, false, true, false, true, "Oxo-Biodegradable Polyethylene", "Dry storage", "Leak-proof hygiene disposal"));

        // Category 12: Personal Care
        Category c12 = cats.get("personal-care");
        products.add(createP("Dettol Original Germ Protection Bathing Soap", "Dettol", c12, "Soaps & Body Wash", "Trusted antibacterial protection protecting family against 100 illness causing germs.", "5 x 125 g saver pack", "pack", "245.00", "290.00", 160, "/images/products/dettol-soap.jpg", seller, false, true, true, true, "Chloroxylenol, Palm Oil base, Pine Fragrance", "Keep on dry soap tray", "Dermatologically tested"));
        products.add(createP("Head & Shoulders Anti-Dandruff Shampoo Smooth", "Head & Shoulders", c12, "Hair Care", "Clinically proven up to 100% dandruff reduction with silky smooth conditioning for frizzy hair.", "650 ml pump", "ml", "495.00", "599.00", 90, "/images/products/head-shoulders.jpg", seller, false, true, true, true, "Zinc Pyrithione, Dimethicone, Conditioning Base", "External hair application", "Visible flake elimination"));
        products.add(createP("Colgate Strong Teeth Calcium Anticavity Paste", "Colgate", c12, "Oral Care", "Amino Shakti formula that infuses natural calcium to strengthen tooth enamel and prevent cavities.", "500 g saver pack (2x250g)", "g", "190.00", "225.00", 140, "/images/products/colgate-strong-teeth.jpg", seller, false, true, true, true, "Calcium Carbonate, Sodium Monofluorophosphate", "Brush twice daily", "IDA accepted enamel defense"));
        products.add(createP("Parachute Advansed Aloe Vera Coconut Hair Oil", "Parachute", c12, "Hair Care", "Feather-light conditioning blend of nourishing coconut oil and moisturizing aloe vera extracts.", "350 ml", "ml", "165.00", "195.00", 85, "/images/products/parachute-aloe-oil.jpg", seller, false, true, false, true, "Coconut Oil, Aloe Vera Extract, Vitamin E", "Massage into scalp and hair", "Non-sticky soft hair nourishment"));
        products.add(createP("Dove Deep Moisture Nourishing Body Wash", "Dove", c12, "Soaps & Body Wash", "Infused with NutriumMoisture to gently cleanse and moisturize skin layers, leaving skin soft.", "800 ml pump", "ml", "390.00", "475.00", 75, "/images/products/dove-body-wash.jpg", seller, false, true, false, true, "Mild Cleansers, Glycerin, Stearic Acid", "Lather with bath loofah", "Sulfate-free 24hr moisture"));
        products.add(createP("Nivea Soft Light Moisturising Cream", "Nivea", c12, "Skin Care", "Lightweight non-greasy moisturizing cream with Vitamin E and Jojoba oil. Quick absorbing.", "200 ml jar", "ml", "240.00", "299.00", 95, "/images/products/nivea-soft.jpg", seller, false, true, false, true, "Jojoba Oil, Vitamin E, Aqua, Glycerin", "Apply gently to face, hands, body", "Refreshing quick absorption"));

        // Category 13: Baby Care
        Category c13 = cats.get("baby-care");
        products.add(createP("Pampers All-round Protection Diaper Pants L", "Pampers", c13, "Diapers", "Cottony soft pants with magic gel locking wetness for up to 12 hours. Enriched with anti-rash lotion.", "Pack of 64 pants (Large)", "pack", "899.00", "1199.00", 70, "/images/products/pampers-large.jpg", seller, false, true, true, true, "Super Absorbent Polymer, Aloe Lotion", "Store in clean dry nursery bag", "Pediatrician recommended diaper"));
        products.add(createP("Johnson's Baby Gentle Cleansing Wipes", "Johnson's Baby", c13, "Baby Hygiene", "Enriched with silk extracts and 3x moisturizer. Alcohol-free and soap-free for delicate infant skin.", "Pack of 144 wipes (2x72)", "pack", "240.00", "300.00", 90, "/images/products/johnsons-wipes.jpg", seller, false, true, true, true, "99% Pure Water, Silk Protein Extract", "Keep lid tightly closed to prevent drying", "Hypoallergenic, pH balanced"));
        products.add(createP("Himalaya Gentle Baby Shampoo Hibiscus", "Himalaya", c13, "Baby Bath", "No-tears gentle baby shampoo enriched with natural extracts of chickpea and hibiscus.", "400 ml", "ml", "225.00", "275.00", 80, "/images/products/himalaya-baby-shampoo.jpg", seller, false, true, false, true, "Chickpea Extract, Hibiscus, Khus Grass", "Gentle scalp wash", "Paraben & synthetic color free"));
        products.add(createP("Sebamed Baby Protective Facial Cream", "Sebamed", c13, "Baby Skin", "Formulated at pH 5.5 to support baby's natural acid mantle against dryness and irritation.", "50 ml", "ml", "680.00", "750.00", 45, "/images/products/sebamed-cream.jpg", seller, false, true, false, true, "Panthenol, Allantoin, Lipid Complex", "Apply gently to clean cheeks", "Dermatologist approved pH 5.5"));

        // Category 14: Home and Household Essentials
        Category c14 = cats.get("home-household-essentials");
        products.add(createP("Origami So Soft 3-Ply Toilet Paper Rolls", "Origami", c14, "Paper Goods", "Ultra-soft and absorbent embossed 3-ply virgin paper rolls. Flushable and skin-safe.", "Pack of 12 rolls", "pack", "299.00", "390.00", 110, "/images/products/origami-toilet-rolls.jpg", seller, false, true, true, true, "100% Virgin Wood Pulp", "Store in dry cabinet", "Chlorine-free gentle paper"));
        products.add(createP("Duracell Ultra Alkaline AA Batteries", "Duracell", c14, "Electricals", "Power check indicator with up to 100% longer lasting power for toys, remotes, and clocks.", "Pack of 8 AA cells", "pack", "280.00", "340.00", 130, "/images/products/duracell-aa.jpg", seller, false, true, true, true, "Alkaline Zinc-Manganese Dioxide", "Keep in cool dry drawer", "Leak proof Duralock technology"));
        products.add(createP("Philips 9W B22 LED Cool Daylight Bulb", "Philips", c14, "Electricals", "Energy saving LED bulb with EyeComfort technology. 825 lumens bright glare-free lighting.", "Pack of 2", "pack", "199.00", "260.00", 95, "/images/products/philips-led-bulb.jpg", seller, false, true, false, true, "Polycarbonate & Aluminium Heat Sink", "Standard B22 base", "85% energy saving, 15000h life"));
        products.add(createP("Selpak Premium 3-Ply Kitchen Towels", "Selpak", c14, "Paper Goods", "Heavy absorbency circular patterned paper towels. Dries spills and fried food oil efficiently.", "Pack of 4 rolls", "pack", "225.00", "290.00", 80, "/images/products/selpak-towels.jpg", seller, false, true, false, true, "100% Pure Embossed Cellulose", "Dry kitchen shelf", "Oil and water ultra absorbent"));
        products.add(createP("Heavy Duty Plastic Clothes Pegs Clips", "HomeStyle", c14, "Home Utility", "Rust-free stainless steel springs with strong grip silicone pads for breezy laundry days.", "Pack of 24 clips", "pack", "89.00", "120.00", 140, "/images/products/clothes-clips.jpg", seller, false, true, false, true, "Virgin Polypropylene, Stainless Spring", "Store in pegs basket", "UV-treated weather resistant"));

        // Category 15: Pet Essentials
        Category c15 = cats.get("pet-essentials");
        products.add(createP("Pedigree Adult Dry Dog Food Chicken & Veg", "Pedigree", c15, "Dog Food", "Balanced complete nutrition for adult dogs with 20% crude protein, Omega 6, and zinc for shiny coat.", "3 kg bag", "kg", "675.00", "750.00", 65, "/images/products/pedigree-dog-food.jpg", seller, false, false, true, true, "Cereals, Chicken & Meat By-products, Minerals", "Keep bag tightly sealed in cool pantry", "Complete nutrition for adult canines"));
        products.add(createP("Whiskas Adult Dry Cat Food Ocean Fish", "Whiskas", c15, "Cat Food", "Crunchy kibbles packed with real ocean fish, taurine for eyesight, and vitamin E.", "1.2 kg bag", "kg", "380.00", "425.00", 55, "/images/products/whiskas-cat-food.jpg", seller, false, false, false, true, "Wholegrain Cereals, Ocean Fish, Taurine, Zinc", "Seal bag after each feeding", "Enriched with essential taurine"));
        products.add(createP("Drools Real Chicken Dog Biscuits Jar", "Drools", c15, "Pet Treats", "Fortified dog training biscuits with real chicken and calcium for strong teeth and bones.", "500 g jar", "g", "165.00", "190.00", 75, "/images/products/drools-biscuits.jpg", seller, false, false, false, true, "Real Chicken, Whole Wheat, Calcium, Vitamins", "Keep jar closed", "Crunchy tartar control treats"));

        // Category 16: Pooja and Indian Household Essentials
        Category c16 = cats.get("pooja-and-indian-household-essentials");
        products.add(createP("Cycle Pure Agarbathies Heritage Woods", "Cycle Pure", c16, "Incense & Camphor", "Handcrafted premium incense sticks with traditional sandalwood and sacred natural herbs.", "Pack of 120 sticks", "pack", "95.00", "110.00", 180, "/images/products/cycle-agarbatti.jpg", seller, false, true, true, true, "Natural Charcoal, Herbs, Sandalwood Essential Oil", "Keep away from moisture and direct heat", "Long lasting serene temple fragrance"));
        products.add(createP("Mangaldeep Sandal Dhoop Cones", "Mangaldeep", c16, "Incense & Camphor", "Smokeless temple grade sandal dhoop cones for peaceful daily pooja and meditation.", "Pack of 30 cones", "pack", "55.00", "65.00", 120, "/images/products/mangaldeep-dhoop.jpg", seller, false, true, false, true, "Sandalwood Bark, Natural Gums, Fragrance", "Store dry", "Deep divine lingering aroma"));
        products.add(createP("Pure Bhimseni Camphor Tablets Karpoor", "PureDevotion", c16, "Incense & Camphor", "100% pure organic Bhimseni camphor. Burns completely with zero residue and purifying aroma.", "100 g jar", "g", "145.00", "180.00", 130, "/images/products/bhimseni-camphor.jpg", seller, false, true, true, true, "100% Pure Distilled Bhimseni Camphor", "Keep jar sealed tightly to prevent evaporation", "Leaves zero black soot, air purifying"));
        products.add(createP("Pure Cow Ghee Diya Cotton Wicks", "PoojaShree", c16, "Diya & Wicks", "Ready-to-light cotton wicks soaked in pure cow ghee and camphor. Burns serenely for 30 minutes.", "Pack of 50 wicks", "pack", "135.00", "160.00", 115, "/images/products/ghee-wicks.jpg", seller, false, true, true, true, "Pure Cow Ghee, Cotton Wick, Natural Camphor", "Store in cool dark spot", "Convenient ready-to-light brass diyas"));
        products.add(createP("Deepam Pancha Deepam Pooja Oil", "Deepam", c16, "Pooja Oils", "Divine blend of 5 auspicious oils: sesame, coconut, mahua, castor, and ghee with sugandh.", "1 litre", "litre", "195.00", "230.00", 95, "/images/products/deepam-oil.jpg", seller, false, true, false, true, "5-Oil Auspicious Blend, Temple Perfume", "For lighting lamps only", "Bright smoke-free divine flame"));

        productRepository.saveAll(products);
    }

    private Product createP(String name, String brand, Category cat, String subcat, String desc,
                            String size, String unit, String price, String origPrice, int stock,
                            String img, User seller, boolean perishable, boolean veg,
                            boolean featured, boolean bestseller, String ingredients,
                            String storage, String nutrition) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(cat);
        p.setSubcategory(subcat);
        p.setDescription(desc);
        p.setPackageSize(size);
        p.setUnit(unit);
        p.setPrice(new BigDecimal(price));
        p.setOriginalPrice(origPrice != null ? new BigDecimal(origPrice) : new BigDecimal(price));
        p.setStockQuantity(stock);
        p.setImageUrl(img);
        p.setSeller(seller);
        p.setAvailable(stock > 0);
        p.setPerishable(perishable);
        p.setVegetarian(veg);
        p.setFeatured(featured);
        p.setBestseller(bestseller);
        p.setIngredients(ingredients);
        p.setStorageInstructions(storage);
        p.setNutritionalInfo(nutrition);
        p.setAverageRating(4.5 + (new Random().nextInt(5) * 0.1));
        p.setReviewCount(12 + new Random().nextInt(80));
        p.setCreatedAt(LocalDateTime.now().minusDays(new Random().nextInt(30)));
        return p;
    }
}
