package com.example.demo.seeder;

import com.example.demo.Feedback.entity.Feedback;
import com.example.demo.Feedback.entity.FeedbackStatus;
import com.example.demo.Feedback.entity.FeedbackTypes;
import com.example.demo.Feedback.repository.FeedbackRepository;
import com.example.demo.Feedback.repository.FeedbackStatusRepository;
import com.example.demo.Feedback.repository.FeedbackTypesRepository;
import com.example.demo.MapSearch.service.GeocodingService;
import com.example.demo.admin.Admin;
import com.example.demo.admin.AdminPosition;
import com.example.demo.admin.AdminRepository;
import com.example.demo.shop.entity.OrderDetails;
import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.repository.ProductsRepository;
import com.example.demo.store.entity.Category;
import com.example.demo.store.entity.StoresInfo;
import com.example.demo.store.repository.CategoryRepository;
import com.example.demo.store.repository.StoreInfoRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("prod")
public class PostgreSQLSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final AdminRepository adminRepository;
    private final FeedbackStatusRepository feedbackStatusRepository;
    private final FeedbackTypesRepository feedbackTypesRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;
    private final ProductsRepository productsRepository;
    private final FeedbackRepository feedbackRepository;
    private final com.example.demo.shop.repository.OrdersRepository ordersRepository;
    private final com.example.demo.shop.repository.OrderDetailsRepository orderDetailsRepository;

    @Value("${app.seeder.enabled:false}")
    private boolean seederEnabled;

    public PostgreSQLSeeder(
            UserRepository userRepository, StoreInfoRepository storeInfoRepository,
            AdminRepository adminRepository, FeedbackStatusRepository feedbackStatusRepository,
            FeedbackTypesRepository feedbackTypesRepository, CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder, GeocodingService geocodingService,
            ProductsRepository productsRepository, FeedbackRepository feedbackRepository,
            com.example.demo.shop.repository.OrdersRepository ordersRepository,
            com.example.demo.shop.repository.OrderDetailsRepository orderDetailsRepository) {
        this.userRepository = userRepository;
        this.storeInfoRepository = storeInfoRepository;
        this.adminRepository = adminRepository;
        this.feedbackStatusRepository = feedbackStatusRepository;
        this.feedbackTypesRepository = feedbackTypesRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.geocodingService = geocodingService;
        this.productsRepository = productsRepository;
        this.feedbackRepository = feedbackRepository;
        this.ordersRepository = ordersRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seederEnabled) {
            System.out.println("Data Seeder is disabled.");
            return;
        }

        // 檢查是否已經有資料，避免重複寫入
        if (userRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping...");
            return;
        }

        System.out.println("Starting to seed PostgreSQL database...");

        seedFeedbackStatus();
        seedFeedbackTypes();
        seedCategories();
        seedAdmins();
        seedUsersAndStores();
        seedProducts();
        seedFeedbacks();
        seedOrders();

        System.out.println("PostgreSQL Database seeding completed successfully.");
    }

    private void seedFeedbackStatus() {
        List<String> statuses = Arrays.asList("待處理", "已處理", "待致電", "追蹤中");
        for (String statusName : statuses) {
            if (!feedbackStatusRepository.existsByStatusName(statusName)) {
                FeedbackStatus status = new FeedbackStatus();
                status.setStatusName(statusName);
                feedbackStatusRepository.save(status);
            }
        }
    }

    private void seedFeedbackTypes() {
        List<String> types = Arrays.asList("意見回饋", "商品上架", "餐廳訂位", "商品訂單", "問題通報", "表揚讚美", "其他");
        for (String typeName : types) {
            FeedbackTypes type = new FeedbackTypes();
            type.setTypeName(typeName);
            feedbackTypesRepository.save(type);
        }
    }

    private void seedCategories() {
        List<String> categories = Arrays.asList("親子友善", "寵物友善", "綠色宣言餐廳", "公平貿易咖啡豆", "蔬食", "無障礙設施", "有停車場", "提供插座");
        for (String catName : categories) {
            Category category = new Category();
            category.setCategoryName(catName);
            categoryRepository.save(category);
        }
    }

    private void seedAdmins() {
        String defaultPassword = passwordEncoder.encode("1qaz@WSX");
        String[][] adminData = {
                { "sa", "測試總管理員", "sa@admin.com", "SUPER_ADMIN" },
                { "hr", "測試人事", "hr@admin.com", "HUMAN_RESOURCE" },
                { "cs", "測試客服", "cs@admin.com", "CUSTOMER_SERVICE" },
                { "sh", "測試電商", "sh@admin.com", "SHOP_MANAGER" }
        };
        for (String[] data : adminData) {
            Admin admin = Admin.builder()
                    .account(data[0]).password(defaultPassword).name(data[1])
                    .position(AdminPosition.valueOf(data[3])).email(data[2]).enabled(true).build();
            adminRepository.save(admin);
        }
    }

    private void seedUsersAndStores() {
        Faker faker = new Faker(Locale.TAIWAN);
        String defaultPassword = passwordEncoder.encode("1qaz@WSX");
        String[][] userData = {
                { "Liam", "liam@test.com" }, { "Noah", "noah@test.com" }, { "Oliver", "oliver@test.com" },
                { "Elijah", "elijah@test.com" }, { "James", "james@test.com" }, { "William", "william@test.com" },
                { "Benjamin", "benjamin@test.com" }, { "Lucas", "lucas@test.com" }, { "Henry", "henry@test.com" },
                { "Alexander", "alexander@test.com" }, { "Mason", "mason@test.com" }, { "Michael", "michael@test.com" },
                { "Ethan", "ethan@test.com" }, { "Daniel", "daniel@test.com" }, { "Jacob", "jacob@test.com" },
                { "Logan", "logan@test.com" }, { "Jackson", "jackson@test.com" }, { "Levi", "levi@test.com" },
                { "Sebastian", "sebastian@test.com" }, { "Mateo", "mateo@test.com" }, { "Jack", "jack@test.com" },
                { "Owen", "owen@test.com" }, { "Theodore", "theodore@test.com" }, { "Aiden", "aiden@test.com" },
                { "Samuel", "samuel@test.com" }, { "Joseph", "joseph@test.com" }, { "John", "john@test.com" },
                { "David", "david@test.com" }, { "Wyatt", "wyatt@test.com" }, { "Matthew", "matthew@test.com" }
        };

        List<User> allSavedUsers = new ArrayList<>();
        for (int i = 0; i < userData.length; i++) {
            User user = User.builder()
                    .name(userData[i][0]).email(userData[i][1]).password(defaultPassword)
                    .isStore(i < 20).enabled(true).twoFactorEnabled(false).build();
            allSavedUsers.add(userRepository.save(user));
        }

        String[][] storeData = {
                { "日初壽司屋", "新北市中和區中山路三段122號", "02-2721-5508" },
                { "山海樓私房料理", "新北市三重區重新路二段21號", "04-2326-8801" },
                { "老王滷肉飯", "桃園市桃園區中正路47號", "02-2556-0931" },
                { "森林早午餐", "新北市淡水區中山路8號", "0978-351-206" },
                { "古早味排骨飯", "高雄市前金區中華三路21號", "04-2231-5094" },
                { "川味麻婆食堂", "南投縣草屯鎮太平路二段294號", "04-2228-3157" },
                { "港都海產店", "桃園市龜山區復興一路8號", "07-551-3372" },
                { "巷弄義麵屋", "台南市永康區中正南路358號", "0972-604-773" },
                { "藍海景觀餐廳", "屏東縣屏東市民生路295號", "03-978-6612" },
                { "金色三麥餐酒館", "南投縣埔里鎮中正路399號", "0937-115-624" },
                { "米香台菜餐廳", "台中市西屯區台灣大道四段1038號", "02-2395-8210" },
                { "香辣川味館", "台北市士林區中正路115號", "03-422-5631" },
                { "海港鮮味餐廳", "台北市信義區松壽路12號", "07-332-9184" },
                { "阿婆鹹酥雞", "雲林縣虎尾鎮林森路一段491號", "0916-875-320" },
                { "暖心鍋物", "新北市新莊區中正路425號", "0933-642-851" },
                { "富士山丼飯", "高雄市鳳山區中山路149號", "0914-992-385" },
                { "小時光甜點店", "金門縣金城鎮民生路45號", "0975-210-943" },
                { "滿福餃子館", "台中市北區三民路三段161號", "0955-481-903" },
                { "龍門客棧川菜", "花蓮縣花蓮市中山路295號", "02-2388-1723" },
                { "金月韓式料理", "台南市安平區華平路711號", "02-2735-6219" }
        };

        List<Category> allCats = categoryRepository.findAll();
        for (int i = 0; i < 20; i++) {
            StoresInfo store = new StoresInfo();
            store.setUser(allSavedUsers.get(i));
            store.setStoreName(storeData[i][0]);
            store.setAddress(storeData[i][1]);
            store.setStorePhone(storeData[i][2]);
            store.setDescription(faker.lorem().paragraph());
            store.setCoverImage("store" + (i % 6) + ".jpg");

            // 預設坐標防錯
            store.setLatitude(BigDecimal.valueOf(25.0330));
            store.setLongitude(BigDecimal.valueOf(121.5654));

            int num = faker.number().numberBetween(1, 3);
            Collections.shuffle(allCats);
            store.setCategories(allCats.subList(0, num));

            storeInfoRepository.save(store);
        }
    }

    private void saveProduct(String name, String cat, BigDecimal price, String desc, int qty, String img) {
        Products p = new Products();
        p.setProductName(name);
        p.setCategory(cat);
        p.setPrice(price);
        p.setProductDescription(desc);
        p.setImage(img);

        Stock s = new Stock();
        s.setAvailableQuantity(qty);
        s.setProduct(p);
        p.setStock(s);

        Products saved = productsRepository.save(p);
        saved.setProductCode("PRD-" + String.format("%04d", saved.getProductId()));
        productsRepository.save(saved);
    }

    private void seedProducts() {
        saveProduct("【小時光甜點店】盛宴．森林系莓果塔", "食品", new BigDecimal("1080"), "美味甜點...", 10, "/productPictures/cake.jpg");
        saveProduct("限量小農鮮蔬盒", "生鮮", new BigDecimal("499"), "新鮮蔬菜...", 2, "/productPictures/VeggieBox.jpg");
        // ... 其他產品比照辦理
    }

    private void seedFeedbacks() {
        FeedbackStatus status = feedbackStatusRepository.findAll().get(0);
        List<FeedbackTypes> types = feedbackTypesRepository.findAll();
        List<User> users = userRepository.findAll().subList(0, 5);

        for (int i = 0; i < users.size(); i++) {
            Feedback fb = new Feedback();
            fb.setCaseNumber("FB" + System.currentTimeMillis() + i);
            fb.setName(users.get(i).getName());
            fb.setEmail(users.get(i).getEmail());
            fb.setContents("這是一個自動產生的意見回饋內容。");
            fb.setFeedbackStatus(status);
            fb.setFeedbackTypes(types.get(0));
            fb.setUser(users.get(i));
            feedbackRepository.save(fb);
        }
    }

    private void seedOrders() {
        Faker faker = new Faker(Locale.TAIWAN);
        List<User> buyers = userRepository.findAll().stream().filter(u -> !u.getIsStore()).collect(Collectors.toList());
        List<Products> prods = productsRepository.findAll();

        for (int i = 0; i < 10; i++) {
            Orders o = new Orders();
            User b = buyers.get(i % buyers.size());
            o.setUser(b);
            o.setReceiverName(b.getName());
            o.setStatus("待付款");
            o.setTotalPrice(BigDecimal.ZERO);
            o.setMerchantTradeNo("ORD" + System.currentTimeMillis() + i);
            Orders savedO = ordersRepository.save(o);

            OrderDetails d = new OrderDetails();
            d.setOrder(savedO);
            d.setProduct(prods.get(0));
            d.setOrderQuantity(1);
            d.setSubtotal(prods.get(0).getPrice());
            orderDetailsRepository.save(d);

            savedO.setTotalPrice(prods.get(0).getPrice());
            ordersRepository.save(savedO);
        }
    }
}