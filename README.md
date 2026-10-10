# SVJ MART — Smart Shopping for Every Home 🛒✨
**B.Tech Artificial Intelligence and Data Science — Final Year Capstone Project**

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-blue.svg)](https://spring.io/projects/spring-security)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)](https://www.thymeleaf.org/)
[![Database](https://img.shields.io/badge/Database-H2%20File%20%7C%20MySQL-yellow.svg)](https://www.h2database.com/)
[![Build](https://img.shields.io/badge/Build-Maven%203.9+-red.svg)](https://maven.apache.org/)

---

## 1. Project Overview

**SVJ MART** is a production-grade, enterprise-ready supermarket and home-essentials e-commerce web platform engineered as a capstone project for **B.Tech Artificial Intelligence and Data Science**.

The platform is purpose-built for the fast-growing Indian grocery e-commerce landscape, providing consumers with a seamless online experience for purchasing daily groceries, staples, dairy, fresh produce, cleaning supplies, and personal care items. It bridges advanced data science with scalable enterprise engineering by featuring a native **Multi-Algorithm AI Layer**:
* **Content-Based Similarity Recommendation Engine** (attribute affinity scoring across brands, categories, and dietary properties).
* **Market-Basket Association Mining** ("Frequently Bought Together" affinity bundles based on co-occurrence in orders).
* **0/1 Greedy Knapsack Grocery Budget Optimizer** (maximizing household variety and nutritional diversity within strict budget ceilings).
* **Conversational AI Shopping Assistant** (recipe kit generation, entity recognition, and smart meal-prep basket synthesis).

---

## 2. Technology Stack

| Layer | Technologies & Frameworks |
|---|---|
| **Backend Framework** | Java 21 (LTS), Spring Boot 3.3.4 |
| **Security & Auth** | Spring Security 6, BCrypt Password Hashing, CSRF Cookie Protection, Role-Based Access Control (RBAC) |
| **Data Persistence** | Spring Data JPA, Hibernate 6.5, HikariCP Connection Pool |
| **Databases** | H2 Database (File-Persistent: `./data/svjmartdb`) for development/demo; Native MySQL 8.x configuration included |
| **Frontend UI/UX** | HTML5, CSS3 Custom Design System (CSS Variables, `clamp()` responsive typography), JavaScript (ES6+ Vanilla, AJAX, Debounced Search), Bootstrap 5.3 (Grid/Utility foundation), Bootstrap Icons |
| **Templating Engine** | Thymeleaf 3.1 with Spring Security Extras |
| **Build & Tooling** | Apache Maven 3.9+, Maven Wrapper (`mvnw`), JUnit 5, Mockito |

---

## 3. Brand Identity & Design System

The visual design system adheres strictly to a clean, high-contrast, modern supermarket aesthetic:

| Color Name | Hex Code | Purpose |
|---|---|---|
| **Midnight Navy** | `#0B1220` | Brand Header, Hero Sections, Deep Surfaces |
| **Deep Blue** | `#142B4A` | Dashboards, Auth Split-Screen Sidebar, Footers |
| **Electric Blue** | `#287BFF` | Primary Actions, Buttons, Highlights, Links |
| **Cyan Highlight** | `#22D3EE` | AI Badges, Glow Accents, Accent Badges |
| **Fresh Green** | `#16A34A` | Grocery Highlights, Veg Indicators, In-Stock Badges |
| **Warm Orange** | `#F59E0B` | Promotions, Rating Stars, Deal Countdown |
| **Soft Background** | `#F5F7FB` | Main Canvas, Body Background |
| **White Cards** | `#FFFFFF` | Product Cards, Modals, Forms |
| **Primary Text** | `#172033` | Headings, Product Names, Prices |
| **Secondary Text** | `#687386` | Subtitles, Specifications, Category Labels |
| **Sale Coral** | `#F04452` | Discount Pills, Non-Veg Badges, Out-of-Stock Badges |

---

## 4. Default Seeded Demonstration Credentials

SVJ MART includes a comprehensive database seeder (`DataSeeder.java`) with realistic Indian supermarket profiles pre-configured for live viva evaluations:

| Role | Email / Username | Password | Features & Privileges |
|---|---|---|---|
| **Administrator** | `admin@svjmart.com` | `Admin@1234` | Full system control: Seller approval/suspension, inventory monitoring, review moderation, coupon creation, revenue analytics. |
| **Seller** | `seller@svjmart.com` | `Seller@1234` | Merchant portal: Add/edit products, stock updates, order fulfillment pipeline, low-stock alerts, sales performance. |
| **Buyer (Customer)** | `buyer@svjmart.com` | `Buyer@1234` | Storefront: Cart, Wishlist, Multi-step checkout with simulated UPI/Cards/COD, Order timeline tracker, AI Assistant, Review submission. |

*Quick-Fill*: The login page (`/login`) includes 1-click test credential fill buttons for all three roles.

---

## 5. Comprehensive 16-Category Product Catalogue

The catalogue features **100+ authentic Indian supermarket products** with realistic Indian Rupee (₹) pricing, package weights, dietary tags, and high-resolution visuals across 16 categories:

1. **Rice, Grains and Staples** (Aashirvaad Sharbati Atta, India Gate Basmati Rice Classic, Ponni Boiled Rice, Toor Dal, Idli Rice, Moong Dal, Chana Dal, Brown Rice, Poha, Rava, Sugar, Tata Salt)
2. **Cooking Oils and Ingredients** (Fortune Sunflower Oil, Dhara Mustard Oil, Idhayam Sesame Oil, Amul Pure Cow Ghee, Figo Extra Virgin Olive Oil, Cornflour, Baking Powder)
3. **Spices and Masalas** (Everest Turmeric Powder, MDH Deggi Mirch, Catch Coriander Powder, Tata Sampann Biryani Masala, Everest Garam Masala, Whole Cumin, Cardamom, Cloves)
4. **Dairy Products** (Amul Taaza Homogenised Milk, Nandini Toned Milk, Mother Dairy Classic Dahi, Milky Mist Fresh Paneer, Amul Salted Butter, Britannia Cheese Slices, Chaach)
5. **Fresh Fruits and Vegetables** (Shimla Royal Apples, Robusta Bananas, Nagpur Oranges, Alphonso Mangoes, Farm Fresh Potatoes, Hybrid Tomatoes, Bangalore Red Onions, Fresh Spinach, Coriander)
6. **Snacks and Munchies** (Lay's Magic Masala, Haldiram's Bhujia Sev, Act II Butter Popcorn, Britannia Good Day Cookies, Parle-G, Cadbury Dairy Milk Silk)
7. **Breakfast and Bakery** (Modern 100% Whole Wheat Bread, Kellogg's Corn Flakes, Quaker Rolled Oats, Kissan Mixed Fruit Jam, Pintola All-Natural Peanut Butter, Britannia Rusk)
8. **Beverages** (Tata Tea Gold, Red Label Tea, Nescafe Classic Instant Coffee, Bru Gold Filter Coffee, Tropicana 100% Orange Juice, Real Mixed Fruit Juice, Raw Pressery Coconut Water)
9. **Packaged and Ready-to-Cook Foods** (Maggi 2-Minute Masala Noodles, Sunfeast YiPPee! Pasta Treat, iD Fresh Ready Idli & Dosa Batter, MTR Ready-to-Eat Paneer Butter Masala, Lijjat Papad)
10. **Cooking and Kitchen Essentials** (Freshwrap Aluminium Foil, Milton Antibacterial Containers, Food Grade Cling Wrap, Scotch-Brite Scrub Sponge, Stainless Steel Gas Lighter)
11. **Cleaning Products** (Surf Excel Matic Liquid Detergent, Ariel Complete Washing Powder, Vim Lemon Dishwash Gel, Lizol Disinfectant Floor Cleaner, Harpic Power Plus Toilet Cleaner, Colin Glass Cleaner)
12. **Personal Care** (Dettol Original Bathing Soap, Dove Deep Moisture Body Wash, Head & Shoulders Anti-Dandruff Shampoo, Colgate Total Active Fresh, Dettol Hand Sanitizer, Nivea Sun Protect)
13. **Baby Care** (Pampers All-Round Pants Diapers, Johnson's Baby No-Tears Shampoo, Sebamed Baby Gentle Cleansing Bar, Nestlé Cerelac Fortified Cereal, Himalaya Baby Wipes)
14. **Pet Supplies** (Pedigree Adult Complete Dog Food Chicken & Veg, Whiskas Adult Wet Cat Food, Purepet Biscuits, Odour-Lock Bentonite Cat Litter)
15. **Stationery and Home Office** (Classmate Pulse Long Spiral Notebook, Reynolds 045 Fine Ball Pens, Kangaroo Stapler, Camlin Exam Geometry Box, Post-it Notes)
16. **Pooja Needs** (Cycle Pure Agarbatti, Mangalam Bhimseni Pure Camphor, Pure Cotton Phool Batti Ghee Wicks, Pooja Sesame Oil, Brass Diya)

---

## 6. AI Subsystem & Data Science Modules

### A. Algorithmic Budget Knapsack Optimizer
* **Mathematical Formulation**: Bounded 0/1 Knapsack with multi-category constraints:
  $$\max \sum_{i=1}^n v_i x_i \quad \text{subject to} \quad \sum_{i=1}^n p_i x_i \le B$$
  where $v_i$ represents nutritional/staple utility, $p_i$ is price, and $B$ is the buyer's budget ceiling.
* **Category Diversity Guarantee**: Ensures no single aisle monopolizes the basket, giving the household balanced staples, dairy, oil, and hygiene.

### B. Market-Basket Association Mining ("Frequently Bought Together")
* Calculates item affinity co-occurrence scores $S(A, B) = \frac{\text{Support}(A \cup B)}{\text{Support}(A)}$ across completed transactions.
* Surfaces dynamic recipe bundles (e.g., Basmati Rice + Biryani Masala + Pure Cow Ghee).

### C. Content-Based Product Recommendation Engine
* Computes feature similarity vectors across product tags, brand affinity, category taxonomy, dietary classification (Vegetarian/Non-Vegetarian), and price tiers to recommend relevant alternatives.

### D. Conversational Natural Language Shopping Assistant
* Client-side drawer widget (`#aiAssistantDrawer`) powered by `/api/ai/chat`.
* Understands natural language queries such as:
  * *"I want to make biryani for dinner"* $\rightarrow$ Assembles Royal Biryani Kit.
  * *"South Indian breakfast ingredients"* $\rightarrow$ Curates Idli/Dosa batter, ghee, oats, bread.
  * *"Budget groceries under 500 rupees"* $\rightarrow$ Solves the knapsack and returns ₹488 optimized basket.
  * *"High protein healthy diet"* $\rightarrow$ Returns nutrient-dense pulses, paneer, and oats.

---

## 7. Installation and Execution Guide

### Prerequisites
* **Java**: JDK 21 (Eclipse Adoptium Temurin or Oracle JDK 21)
* **Build Tool**: Apache Maven 3.9+ (or included `./mvnw`)
* **Browser**: Any modern browser (Chrome, Edge, Firefox, Safari)

### Steps to Run

1. **Clone or Navigate to the Project Root**:
   ```powershell
   cd C:\Users\HP\.gemini\antigravity\scratch\svj-mart
   ```

2. **Build and Package the Application**:
   ```powershell
   mvn clean package -DskipTests
   ```

3. **Run the Spring Boot Application**:
   ```powershell
   java -jar target/svj-mart-1.0.0.jar
   ```

4. **Access the Web Application**:
   * **Storefront & Homepage**: [http://localhost:8080/](http://localhost:8080/)
   * **Product Catalogue**: [http://localhost:8080/catalog](http://localhost:8080/catalog)
   * **Authentication Portal**: [http://localhost:8080/login](http://localhost:8080/login)
   * **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     * JDBC URL: `jdbc:h2:file:./data/svjmartdb`
     * Username: `sa`
     * Password: *(empty)*

---

## 8. Switching to MySQL (Production Database)

SVJ MART is pre-configured for seamless MySQL transition. In `src/main/resources/application.properties`, uncomment the MySQL profile block:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/svjmartdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 9. Automated Testing & Verification

The test suite covers unit and integration tests using JUnit 5, Mockito, and Spring Boot Test:

```powershell
mvn test
```

### Test Coverage Highlights
* `SvjMartApplicationTests`: Spring context initialization and bean wiring.
* `CartServiceTest`: Add items, duplicate item quantity aggregation, recalculation, and removal.
* `CouponServiceTest`: Coupon validation, expiry checks, minimum order value logic, percentage vs flat discounts.
* `AiRecommendationServiceTest`: Co-occurrence mining and content-based similarity score verification.

---

## 10. Academic Project Authorship

* **Project**: SVJ MART — Smart Shopping for Every Home
* **Degree**: Bachelor of Technology (B.Tech)
* **Department**: Artificial Intelligence and Data Science
* **Academic Year**: 2026-2027
* **License**: Educational & Capstone Project Use
